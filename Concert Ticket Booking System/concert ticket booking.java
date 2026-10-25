import java.time.LocalDateTime;
import java.util.*;

// ConcertTicketBookingService: Manages concerts, seat availability,
// bookings, cancellations, and booking lookup.
//
// Concert: Represents a concert at a venue and maintains its seats.
// Seat: Represents an individual seat with category, price, and status.
// Booking: Connects a customer to selected seats and stores total price.
// Customer: Represents a user booking tickets.
//
// Simplifications: In-memory storage, no payment gateway,
// no temporary seat locks, no refunds, and no notification service.

class ConcertTicketBookingService {
    private Map<String, Concert> concerts = new HashMap<>();
    private Map<String, Booking> bookings = new HashMap<>();

    public void addConcert(Concert concert) {
        if (concerts.containsKey(concert.getConcertId())) {
            throw new IllegalArgumentException("Concert already exists");
        }
        concerts.put(concert.getConcertId(), concert);
    }

    public List<Concert> searchConcerts(String keyword) {
        List<Concert> result = new ArrayList<>();
        String query = keyword.toLowerCase();

        for (Concert concert : concerts.values()) {
            if (concert.getName().toLowerCase().contains(query)
                    || concert.getVenue().toLowerCase().contains(query)) {
                result.add(concert);
            }
        }
        return result;
    }

    public List<Seat> getAvailableSeats(String concertId) {
        return getConcert(concertId).getAvailableSeats();
    }

    public Booking bookTickets(
            String concertId, Customer customer, List<String> seatIds) {

        Concert concert = getConcert(concertId);

        synchronized (concert) {
            List<Seat> selectedSeats = new ArrayList<>();
            Set<String> uniqueSeatIds = new HashSet<>();

            for (String seatId : seatIds) {
                if (!uniqueSeatIds.add(seatId)) {
                    throw new IllegalArgumentException("Duplicate seat selected");
                }

                Seat seat = concert.getSeat(seatId);

                if (seat == null) {
                    throw new IllegalArgumentException(
                        "Seat not found: " + seatId
                    );
                }

                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new IllegalStateException(
                        "Seat is unavailable: " + seatId
                    );
                }

                selectedSeats.add(seat);
            }

            if (selectedSeats.isEmpty()) {
                throw new IllegalArgumentException("Select at least one seat");
            }

            long totalPrice = 0;
            for (Seat seat : selectedSeats) {
                totalPrice += seat.getPrice();
            }

            String bookingId = UUID.randomUUID().toString();
            Booking booking = new Booking(
                bookingId, customer, concert, selectedSeats, totalPrice
            );

            // Mark seats only after validating every selected seat.
            for (Seat seat : selectedSeats) {
                seat.setStatus(SeatStatus.BOOKED);
            }

            bookings.put(bookingId, booking);
            return booking;
        }
    }

    public void cancelBooking(String bookingId) {
        Booking booking = getBooking(bookingId);
        Concert concert = booking.getConcert();

        synchronized (concert) {
            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new IllegalStateException(
                    "Booking cannot be cancelled"
                );
            }

            for (Seat seat : booking.getSeats()) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }

            booking.setStatus(BookingStatus.CANCELLED);
        }
    }

    public Booking getBookingDetails(String bookingId) {
        return getBooking(bookingId);
    }

    private Concert getConcert(String concertId) {
        Concert concert = concerts.get(concertId);
        if (concert == null) {
            throw new IllegalArgumentException("Concert not found");
        }
        return concert;
    }

    private Booking getBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }
        return booking;
    }
}

class Concert {
    private final String concertId;
    private final String name;
    private final String venue;
    private final LocalDateTime dateTime;
    private final Map<String, Seat> seats = new HashMap<>();

    public Concert(String concertId, String name,
                   String venue, LocalDateTime dateTime) {
        this.concertId = concertId;
        this.name = name;
        this.venue = venue;
        this.dateTime = dateTime;
    }

    public void addSeat(Seat seat) {
        if (seats.containsKey(seat.getSeatId())) {
            throw new IllegalArgumentException("Seat already exists");
        }
        seats.put(seat.getSeatId(), seat);
    }

    public Seat getSeat(String seatId) {
        return seats.get(seatId);
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> available = new ArrayList<>();

        for (Seat seat : seats.values()) {
            if (seat.getStatus() == SeatStatus.AVAILABLE) {
                available.add(seat);
            }
        }

        return available;
    }

    public String getConcertId() {
        return concertId;
    }

    public String getName() {
        return name;
    }

    public String getVenue() {
        return venue;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return name + " at " + venue + " on " + dateTime;
    }
}

class Seat {
    private final String seatId;
    private final TicketCategory category;
    private final long price;
    private SeatStatus status;

    public Seat(String seatId, TicketCategory category, long price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        this.seatId = seatId;
        this.category = category;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getSeatId() {
        return seatId;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public long getPrice() {
        return price;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return seatId + " [" + category + "] - ₹" + price
            + " (" + status + ")";
    }
}

class Customer {
    private final String customerId;
    private final String name;
    private final String email;

    public Customer(String customerId, String name, String email) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

class Booking {
    private final String bookingId;
    private final Customer customer;
    private final Concert concert;
    private final List<Seat> seats;
    private final long totalPrice;
    private final LocalDateTime bookingTime;
    private BookingStatus status;

    public Booking(String bookingId, Customer customer, Concert concert,
                   List<Seat> seats, long totalPrice) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.concert = concert;
        this.seats = new ArrayList<>(seats);
        this.totalPrice = totalPrice;
        this.bookingTime = LocalDateTime.now();
        this.status = BookingStatus.CONFIRMED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Concert getConcert() {
        return concert;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        List<String> seatIds = new ArrayList<>();
        for (Seat seat : seats) {
            seatIds.add(seat.getSeatId());
        }

        return "Booking ID: " + bookingId
            + "\nCustomer: " + customer.getName()
            + "\nConcert: " + concert.getName()
            + "\nSeats: " + seatIds
            + "\nTotal: ₹" + totalPrice
            + "\nStatus: " + status;
    }
}

enum TicketCategory {
    GENERAL,
    SILVER,
    GOLD,
    VIP
}

enum SeatStatus {
    AVAILABLE,
    BOOKED
}

enum BookingStatus {
    CONFIRMED,
    CANCELLED
}

public class Main {
    public static void main(String[] args) {
        ConcertTicketBookingService service =
            new ConcertTicketBookingService();

        Concert concert = new Concert(
            "C1",
            "Live Music Night",
            "Chennai Trade Centre",
            LocalDateTime.now().plusDays(7)
        );

        concert.addSeat(new Seat("A1", TicketCategory.VIP, 5000));
        concert.addSeat(new Seat("A2", TicketCategory.VIP, 5000));
        concert.addSeat(new Seat("B1", TicketCategory.GOLD, 3000));
        concert.addSeat(new Seat("B2", TicketCategory.GOLD, 3000));
        concert.addSeat(new Seat("C1", TicketCategory.GENERAL, 1000));

        service.addConcert(concert);

        Customer customer = new Customer(
            "U1", "Alice", "alice@example.com"
        );

        System.out.println("Available concerts:");
        for (Concert result : service.searchConcerts("music")) {
            System.out.println(result);
        }

        System.out.println("\nAvailable seats:");
        for (Seat seat : service.getAvailableSeats("C1")) {
            System.out.println(seat);
        }

        Booking booking = service.bookTickets(
            "C1", customer, Arrays.asList("A1", "A2")
        );

        System.out.println("\nBooking confirmed:");
        System.out.println(booking);

        System.out.println("\nSeats after booking:");
        for (Seat seat : service.getAvailableSeats("C1")) {
            System.out.println(seat);
        }

        service.cancelBooking(booking.getBookingId());

        System.out.println("\nBooking cancelled.");
        System.out.println(service.getBookingDetails(booking.getBookingId()));

        System.out.println("\nAvailable seats after cancellation:");
        for (Seat seat : service.getAvailableSeats("C1")) {
            System.out.println(seat);
        }
    }
}