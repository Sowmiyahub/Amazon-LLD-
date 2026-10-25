import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

// HotelManagementService: Manages rooms, searches availability,
// creates reservations, checks guests in/out, and calculates bills.
//
// Room: Represents a hotel room with a type and nightly price.
// Guest: Represents a customer.
// Booking: Stores room, guest, dates, and booking status.
// RoomType: Defines room categories.
// BookingStatus: Tracks reservation lifecycle.
//
// Simplifications: In-memory storage, one room per booking,
// no payment gateway, housekeeping, or cancellation fees.

class HotelManagementService {
    private Map<Integer, Room> rooms = new HashMap<>();
    private Map<String, Booking> bookings = new HashMap<>();

    public void addRoom(Room room) {
        rooms.put(room.getRoomNumber(), room);
    }

    public List<Room> searchAvailableRooms(
            RoomType type, LocalDate checkIn, LocalDate checkOut) {

        validateDates(checkIn, checkOut);
        List<Room> available = new ArrayList<>();

        for (Room room : rooms.values()) {
            if (room.getType() == type
                    && isAvailable(room, checkIn, checkOut)) {
                available.add(room);
            }
        }

        return available;
    }

    public Booking bookRoom(
            Guest guest, int roomNumber,
            LocalDate checkIn, LocalDate checkOut) {

        validateDates(checkIn, checkOut);

        Room room = rooms.get(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }

        synchronized (room) {
            if (!isAvailable(room, checkIn, checkOut)) {
                throw new IllegalStateException("Room is not available");
            }

            String bookingId = UUID.randomUUID().toString();

            Booking booking = new Booking(
                bookingId, guest, room, checkIn, checkOut
            );

            bookings.put(bookingId, booking);
            return booking;
        }
    }

    public void checkIn(String bookingId) {
        Booking booking = getBooking(bookingId);

        synchronized (booking) {
            if (booking.getStatus() != BookingStatus.BOOKED) {
                throw new IllegalStateException("Booking is not valid for check-in");
            }

            if (LocalDate.now().isBefore(booking.getCheckIn())) {
                throw new IllegalStateException("Check-in date has not arrived");
            }

            if (LocalDate.now().isAfter(booking.getCheckOut())) {
                throw new IllegalStateException("Booking period has ended");
            }

            booking.setStatus(BookingStatus.CHECKED_IN);
        }
    }

    public long checkOut(String bookingId) {
        Booking booking = getBooking(bookingId);

        synchronized (booking) {
            if (booking.getStatus() != BookingStatus.CHECKED_IN) {
                throw new IllegalStateException("Guest is not checked in");
            }

            long bill = booking.calculateBill();
            booking.setStatus(BookingStatus.CHECKED_OUT);
            return bill;
        }
    }

    public Booking getBookingDetails(String bookingId) {
        return getBooking(bookingId);
    }

    private boolean isAvailable(
            Room room, LocalDate checkIn, LocalDate checkOut) {

        for (Booking booking : bookings.values()) {
            if (booking.getRoom().getRoomNumber() == room.getRoomNumber()
                    && booking.getStatus() != BookingStatus.CANCELLED
                    && booking.getStatus() != BookingStatus.CHECKED_OUT) {

                // Two bookings overlap when each starts before the other ends.
                boolean overlaps = checkIn.isBefore(booking.getCheckOut())
                    && booking.getCheckIn().isBefore(checkOut);

                if (overlaps) {
                    return false;
                }
            }
        }

        return true;
    }

    private Booking getBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }
        return booking;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null
                || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Invalid booking dates");
        }
    }
}

class Room {
    private final int roomNumber;
    private final RoomType type;
    private final long pricePerNight;

    public Room(int roomNumber, RoomType type, long pricePerNight) {
        if (pricePerNight < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public long getPricePerNight() {
        return pricePerNight;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type
            + "), ₹" + pricePerNight + "/night";
    }
}

class Guest {
    private final String guestId;
    private final String name;
    private final String phone;

    public Guest(String guestId, String name, String phone) {
        this.guestId = guestId;
        this.name = name;
        this.phone = phone;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}

class Booking {
    private final String bookingId;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private BookingStatus status;

    public Booking(String bookingId, Guest guest, Room room,
                   LocalDate checkIn, LocalDate checkOut) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = BookingStatus.BOOKED;
    }

    public long calculateBill() {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return nights * room.getPricePerNight();
    }

    public String getBookingId() {
        return bookingId;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Booking ID: " + bookingId
            + ", Guest: " + guest.getName()
            + ", Room: " + room.getRoomNumber()
            + ", Dates: " + checkIn + " to " + checkOut
            + ", Status: " + status;
    }
}

enum RoomType {
    SINGLE,
    DOUBLE,
    DELUXE,
    SUITE
}

enum BookingStatus {
    BOOKED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED
}

public class Main {
    public static void main(String[] args) {
        HotelManagementService service = new HotelManagementService();

        service.addRoom(new Room(101, RoomType.SINGLE, 2000));
        service.addRoom(new Room(201, RoomType.DELUXE, 4500));
        service.addRoom(new Room(301, RoomType.SUITE, 8000));

        Guest guest = new Guest("G1", "Alice", "9876543210");

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(3);

        System.out.println("Available rooms:");
        for (Room room :
                service.searchAvailableRooms(RoomType.SINGLE, checkIn, checkOut)) {
            System.out.println(room);
        }

        Booking booking = service.bookRoom(
            guest, 101, checkIn, checkOut
        );

        System.out.println("\nBooking created:");
        System.out.println(booking);

        service.checkIn(booking.getBookingId());
        System.out.println("\nGuest checked in.");

        long bill = service.checkOut(booking.getBookingId());
        System.out.println("Final bill: ₹" + bill);
    }
}