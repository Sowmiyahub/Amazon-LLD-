import java.util.*;

// Flight: Stores flight details and available seats.
// Passenger: Represents the traveller.
// Booking: Stores booking details and total fare.
// AirlineService: Handles booking and cancellation.
// Simplified: In-memory storage, no payment or check-in.

class AirlineService {
    private Map<String, Flight> flights = new HashMap<>();
    private Map<String, Booking> bookings = new HashMap<>();

    public void addFlight(Flight flight) {
        flights.put(flight.id, flight);
    }

    public Booking bookSeats(String flightId, Passenger passenger,
                             List<String> seatIds) {
        Flight flight = flights.get(flightId);

        if (flight == null)
            throw new IllegalArgumentException("Flight not found");

        synchronized (flight) {
            Set<String> uniqueSeats = new HashSet<>(seatIds);

            if (uniqueSeats.size() != seatIds.size() || seatIds.isEmpty())
                throw new IllegalArgumentException("Invalid seat selection");

            long fare = 0;

            for (String id : seatIds) {
                Seat seat = flight.seats.get(id);

                if (seat == null || seat.booked)
                    throw new IllegalStateException("Seat unavailable: " + id);

                fare += seat.price;
            }

            for (String id : seatIds)
                flight.seats.get(id).booked = true;

            Booking booking = new Booking(
                UUID.randomUUID().toString(), passenger, flight, seatIds, fare
            );

            bookings.put(booking.id, booking);
            return booking;
        }
    }

    public void cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);

        if (booking == null || booking.cancelled)
            throw new IllegalArgumentException("Invalid booking");

        synchronized (booking.flight) {
            for (String id : booking.seatIds)
                booking.flight.seats.get(id).booked = false;

            booking.cancelled = true;
        }
    }
}

class Flight {
    String id, source, destination;
    Map<String, Seat> seats = new HashMap<>();

    Flight(String id, String source, String destination) {
        this.id = id;
        this.source = source;
        this.destination = destination;
    }

    void addSeat(String id, long price) {
        seats.put(id, new Seat(id, price));
    }
}

class Seat {
    String id;
    long price;
    boolean booked;

    Seat(String id, long price) {
        this.id = id;
        this.price = price;
    }
}

class Passenger {
    String id, name;

    Passenger(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Booking {
    String id;
    Passenger passenger;
    Flight flight;
    List<String> seatIds;
    long totalFare;
    boolean cancelled;

    Booking(String id, Passenger passenger, Flight flight,
            List<String> seatIds, long totalFare) {
        this.id = id;
        this.passenger = passenger;
        this.flight = flight;
        this.seatIds = new ArrayList<>(seatIds);
        this.totalFare = totalFare;
    }
}

public class Main {
    public static void main(String[] args) {
        AirlineService service = new AirlineService();

        Flight flight = new Flight("AI101", "Chennai", "Delhi");
        flight.addSeat("1A", 5000);
        flight.addSeat("1B", 5000);
        service.addFlight(flight);

        Passenger passenger = new Passenger("P1", "Alice");

        Booking booking = service.bookSeats(
            "AI101", passenger, Arrays.asList("1A", "1B")
        );

        System.out.println("Booking ID: " + booking.id);
        System.out.println("Total fare: ₹" + booking.totalFare);

        service.cancelBooking(booking.id);
        System.out.println("Booking cancelled");
    }
}