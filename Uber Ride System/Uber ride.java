
import java.util.*;

// 1. Main system: manages ride requests and lifecycle
class RideService {
    private final List<Driver> drivers = new ArrayList<>();
    private final Map<String, Ride> rides = new HashMap<>();

    public void addDriver(Driver driver) {
        drivers.add(driver);
    }

    public Ride requestRide(Rider rider, Location destination) {
        Driver driver = findNearestDriver(rider.getLocation());

        if (driver == null) {
            throw new IllegalStateException("No drivers available");
        }

        Ride ride = new Ride(rider, driver,
                rider.getLocation(), destination);

        driver.setAvailable(false);
        rides.put(ride.getId(), ride);

        return ride;
    }

    private Driver findNearestDriver(Location pickup) {
        Driver nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver driver : drivers) {
            if (!driver.isAvailable()) continue;

            double distance = driver.getLocation().distanceTo(pickup);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = driver;
            }
        }

        return nearest;
    }

    public void startRide(String rideId) {
        Ride ride = getRide(rideId);

        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new IllegalStateException("Ride cannot be started");
        }

        ride.setStatus(RideStatus.IN_PROGRESS);
    }

    public double completeRide(String rideId) {
        Ride ride = getRide(rideId);

        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalStateException("Ride is not in progress");
        }

        ride.setStatus(RideStatus.COMPLETED);
        ride.getDriver().setAvailable(true);

        return ride.calculateFare();
    }

    private Ride getRide(String rideId) {
        Ride ride = rides.get(rideId);

        if (ride == null) {
            throw new IllegalArgumentException("Invalid ride ID");
        }

        return ride;
    }
}

// 2. Ride entity
class Ride {
    private final String id;
    private final Rider rider;
    private final Driver driver;
    private final Location pickup;
    private final Location destination;
    private RideStatus status;

    public Ride(Rider rider, Driver driver,
                Location pickup, Location destination) {
        this.id = UUID.randomUUID().toString();
        this.rider = rider;
        this.driver = driver;
        this.pickup = pickup;
        this.destination = destination;
        this.status = RideStatus.ASSIGNED;
    }

    public double calculateFare() {
        double distance = pickup.distanceTo(destination);
        double baseFare = 50.0;
        double perKmRate = 15.0;

        return baseFare + distance * perKmRate;
    }

    public String getId() {
        return id;
    }

    public Driver getDriver() {
        return driver;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }
}

// 3. Driver
class Driver {
    private final String id;
    private final String name;
    private Location location;
    private boolean available;

    public Driver(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.available = true;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}

// 4. Rider
class Rider {
    private final String id;
    private final String name;
    private Location location;

    public Rider(String id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}

// 5. Location with simple distance calculation
class Location {
    private final double latitude;
    private final double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double distanceTo(Location other) {
        double dx = latitude - other.latitude;
        double dy = longitude - other.longitude;

        // Simplified distance for the interview model
        return Math.sqrt(dx * dx + dy * dy) * 111;
    }
}

// 6. Ride lifecycle
enum RideStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
