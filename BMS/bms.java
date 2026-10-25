/*
Class Design: BookMyShow

1. BookMyShowService
   - Main service for searching movies, viewing shows, and booking seats.
   - Coordinates show selection and booking.

2. Movie
   - Represents a movie with title and duration.

3. Theatre
   - Represents a theatre containing screens.

4. Screen
   - Contains seats available for shows.

5. Seat
   - Represents a seat with an ID and category.

6. Show
   - Connects a movie, screen, show time, and seat availability.
   - Tracks booked seats and prevents duplicate bookings.

7. Booking
   - Stores booking details, selected seats, and booking status.

8. Main Flow
   - Search movies.
   - View shows for a movie.
   - Select seats and book them.
   - Confirm booking.

Design:
   - Service coordinates the workflow.
   - Show manages seat availability.
   - Booking represents the reservation.

Note:
   - Payment gateway and database are external and omitted.
   - In production, seat reservation must be atomic and concurrency-safe.
*/

import java.time.LocalDateTime;
import java.util.*;

class BookMyShowService {
    private final List<Movie> movies = new ArrayList<>();
    private final List<Theatre> theatres = new ArrayList<>();
    private final List<Show> shows = new ArrayList<>();
    private final Map<String, Booking> bookings = new HashMap<>();

    public void addMovie(Movie movie) {
        movies.add(movie);
    }

    public void addTheatre(Theatre theatre) {
        theatres.add(theatre);
    }

    public void addShow(Show show) {
        shows.add(show);
    }

    public List<Movie> getMovies() {
        return Collections.unmodifiableList(movies);
    }

    public List<Show> getShows(String movieId) {
        List<Show> result = new ArrayList<>();

        for (Show show : shows) {
            if (show.getMovie().getId().equals(movieId)) {
                result.add(show);
            }
        }

        return result;
    }

    public Booking bookSeats(String userId, String showId,
                             List<String> seatIds) {
        Show selectedShow = null;

        for (Show show : shows) {
            if (show.getId().equals(showId)) {
                selectedShow = show;
                break;
            }
        }

        if (selectedShow == null) {
            throw new IllegalArgumentException("Show not found");
        }

        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Select at least one seat");
        }

        selectedShow.bookSeats(seatIds);

        double amount = selectedShow.calculatePrice(seatIds);
        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                userId,
                selectedShow,
                seatIds,
                amount
        );

        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }
}

class Movie {
    private final String id;
    private final String title;
    private final int durationMinutes;

    public Movie(String id, String title, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.durationMinutes = durationMinutes;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}

class Theatre {
    private final String id;
    private final String name;
    private final String city;
    private final List<Screen> screens = new ArrayList<>();

    public Theatre(String id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }

    public void addScreen(Screen screen) {
        screens.add(screen);
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public List<Screen> getScreens() {
        return Collections.unmodifiableList(screens);
    }
}

class Screen {
    private final String id;
    private final String name;
    private final List<Seat> seats = new ArrayList<>();

    public Screen(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }
}

class Seat {
    private final String id;
    private final String category;
    private final double price;

    public Seat(String id, String category, double price) {
        this.id = id;
        this.category = category;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }
}

class Show {
    private final String id;
    private final Movie movie;
    private final Theatre theatre;
    private final Screen screen;
    private final LocalDateTime startTime;
    private final Map<String, Seat> seats = new HashMap<>();
    private final Set<String> bookedSeatIds = new HashSet<>();

    public Show(String id, Movie movie, Theatre theatre,
                Screen screen, LocalDateTime startTime) {
        this.id = id;
        this.movie = movie;
        this.theatre = theatre;
        this.screen = screen;
        this.startTime = startTime;

        for (Seat seat : screen.getSeats()) {
            seats.put(seat.getId(), seat);
        }
    }

    public synchronized void bookSeats(List<String> seatIds) {
        Set<String> requestedSeats = new HashSet<>(seatIds);

        if (requestedSeats.size() != seatIds.size()) {
            throw new IllegalArgumentException("Duplicate seat selected");
        }

        for (String seatId : requestedSeats) {
            if (!seats.containsKey(seatId)) {
                throw new IllegalArgumentException(
                        "Invalid seat: " + seatId
                );
            }

            if (bookedSeatIds.contains(seatId)) {
                throw new IllegalStateException(
                        "Seat already booked: " + seatId
                );
            }
        }

        bookedSeatIds.addAll(requestedSeats);
    }

    public double calculatePrice(List<String> seatIds) {
        double total = 0;

        for (String seatId : seatIds) {
            total += seats.get(seatId).getPrice();
        }

        return total;
    }

    public List<Seat> getAvailableSeats() {
        List<Seat> available = new ArrayList<>();

        for (Seat seat : seats.values()) {
            if (!bookedSeatIds.contains(seat.getId())) {
                available.add(seat);
            }
        }

        return available;
    }

    public String getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public Screen getScreen() {
        return screen;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}

class Booking {
    private final String id;
    private final String userId;
    private final Show show;
    private final List<String> seatIds;
    private final double amount;
    private final LocalDateTime bookedAt;

    public Booking(String id, String userId, Show show,
                   List<String> seatIds, double amount) {
        this.id = id;
        this.userId = userId;
        this.show = show;
        this.seatIds = new ArrayList<>(seatIds);
        this.amount = amount;
        this.bookedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Show getShow() {
        return show;
    }

    public List<String> getSeatIds() {
        return Collections.unmodifiableList(seatIds);
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }
}

public class Main {
    public static void main(String[] args) {
        BookMyShowService service = new BookMyShowService();

        Movie movie = new Movie("M1", "Interstellar", 169);
        Theatre theatre = new Theatre("T1", "PVR Cinemas", "Chennai");

        Screen screen = new Screen("SC1", "Screen 1");
        screen.addSeat(new Seat("A1", "GOLD", 200));
        screen.addSeat(new Seat("A2", "GOLD", 200));
        screen.addSeat(new Seat("B1", "PREMIUM", 300));

        theatre.addScreen(screen);

        Show show = new Show(
                "SH1",
                movie,
                theatre,
                screen,
                LocalDateTime.of(2026, 10, 26, 18, 30)
        );

        service.addMovie(movie);
        service.addTheatre(theatre);
        service.addShow(show);

        System.out.println("Movies:");
        for (Movie m : service.getMovies()) {
            System.out.println(m.getTitle());
        }

        System.out.println("\nAvailable shows:");
        for (Show s : service.getShows("M1")) {
            System.out.println(
                    s.getTheatre().getName() + " | " +
                    s.getStartTime()
            );
        }

        System.out.println("\nAvailable seats:");
        for (Seat seat : show.getAvailableSeats()) {
            System.out.println(
                    seat.getId() + " - " + seat.getCategory() +
                    " - Rs." + seat.getPrice()
            );
        }

        Booking booking = service.bookSeats(
                "U1", "SH1", Arrays.asList("A1", "A2")
        );

        System.out.println("\nBooking successful!");
        System.out.println("Booking ID: " + booking.getId());
        System.out.println("Seats: " + booking.getSeatIds());
        System.out.println("Amount: Rs." + booking.getAmount());
    }
}