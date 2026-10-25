import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/*
 * Class Design:
 * 1. CarRentalService - searches, books, and returns cars.
 * 2. Car - stores car details and availability.
 * 3. Customer - represents the customer.
 * 4. Rental - stores booking details and rental duration.
 * 5. Main - demonstrates the workflow.
 *
 * Flow:
 * Search available car -> Book car -> Return car.
 */

public class Main {
    public static void main(String[] args) {
        CarRentalService service = new CarRentalService();

        Car car = new Car("C1", "Honda City", 2000);
        service.addCar(car);

        Customer customer = new Customer("U1", "Arun");

        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 4);

        List<Car> available = service.searchCars(start, end);
        System.out.println("Available cars: " + available.size());

        Rental rental = service.bookCar("C1", customer, start, end);
        System.out.println("Rental cost: " + rental.getTotalCost());

        service.returnCar(rental.getId());
        System.out.println("Car returned successfully");
    }
}

class CarRentalService {
    private Map<String, Car> cars = new HashMap<>();
    private Map<String, Rental> rentals = new HashMap<>();
    private int nextRentalId = 1;

    public void addCar(Car car) {
        cars.put(car.getId(), car);
    }

    public List<Car> searchCars(LocalDate start, LocalDate end) {
        List<Car> available = new ArrayList<>();

        for (Car car : cars.values()) {
            if (car.isAvailable(start, end, rentals.values())) {
                available.add(car);
            }
        }
        return available;
    }

    public Rental bookCar(String carId, Customer customer,
                          LocalDate start, LocalDate end) {
        Car car = cars.get(carId);

        if (car == null || !car.isAvailable(start, end, rentals.values())) {
            throw new IllegalStateException("Car not available");
        }

        Rental rental = new Rental(
            "R" + nextRentalId++, car, customer, start, end
        );

        rentals.put(rental.getId(), rental);
        return rental;
    }

    public void returnCar(String rentalId) {
        Rental rental = rentals.get(rentalId);

        if (rental == null) {
            throw new IllegalArgumentException("Invalid rental");
        }

        rental.returnCar();
    }
}

class Car {
    private String id;
    private String model;
    private double pricePerDay;

    public Car(String id, String model, double pricePerDay) {
        this.id = id;
        this.model = model;
        this.pricePerDay = pricePerDay;
    }

    public String getId() {
        return id;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public boolean isAvailable(LocalDate start, LocalDate end,
                               Collection<Rental> rentals) {
        for (Rental rental : rentals) {
            if (rental.getCar().getId().equals(id) &&
                rental.isActive() &&
                start.isBefore(rental.getEndDate()) &&
                rental.getStartDate().isBefore(end)) {
                return false;
            }
        }
        return true;
    }
}

class Customer {
    private String id;
    private String name;

    public Customer(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Rental {
    private String id;
    private Car car;
    private Customer customer;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active = true;

    public Rental(String id, Car car, Customer customer,
                  LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Invalid rental dates");
        }

        this.id = id;
        this.car = car;
        this.customer = customer;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public double getTotalCost() {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return days * car.getPricePerDay();
    }

    public void returnCar() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public String getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}