import java.time.LocalDateTime;
import java.util.*;

class MeetingRoomService {
    private Map<String, MeetingRoom> rooms = new HashMap<>();
    private Map<String, Booking> bookings = new HashMap<>();

    public void addRoom(MeetingRoom room) {
        rooms.put(room.id, room);
    }

    public Booking bookRoom(String roomId, String title,
                            LocalDateTime start, LocalDateTime end) {
        MeetingRoom room = rooms.get(roomId);

        if (room == null)
            throw new IllegalArgumentException("Room not found");

        if (!start.isBefore(end))
            throw new IllegalArgumentException("Invalid time range");

        synchronized (room) {
            for (Booking b : room.bookings) {
                if (!b.cancelled && start.isBefore(b.end)
                        && end.isAfter(b.start)) {
                    throw new IllegalStateException("Room already booked");
                }
            }

            Booking booking = new Booking(
                UUID.randomUUID().toString(), title, room, start, end
            );

            room.bookings.add(booking);
            bookings.put(booking.id, booking);
            return booking;
        }
    }

    public void cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);

        if (booking == null || booking.cancelled)
            throw new IllegalArgumentException("Invalid booking");

        synchronized (booking.room) {
            booking.cancelled = true;
        }
    }
}

class MeetingRoom {
    String id;
    String name;
    List<Booking> bookings = new ArrayList<>();

    MeetingRoom(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Booking {
    String id;
    String title;
    MeetingRoom room;
    LocalDateTime start, end;
    boolean cancelled;

    Booking(String id, String title, MeetingRoom room,
            LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.title = title;
        this.room = room;
        this.start = start;
        this.end = end;
    }
}

public class Main {
    public static void main(String[] args) {
        MeetingRoomService service = new MeetingRoomService();

        service.addRoom(new MeetingRoom("R1", "Conference Room A"));

        LocalDateTime start = LocalDateTime.of(2026, 10, 26, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 10, 26, 11, 0);

        Booking booking = service.bookRoom(
            "R1", "Sprint Planning", start, end
        );

        System.out.println("Booking ID: " + booking.id);

        service.cancelBooking(booking.id);
        System.out.println("Meeting cancelled");
    }
}