
import java.time.LocalDateTime;
import java.util.*;

// 1. Main system: manages parking and exit
class ParkingLot {
    private final List<ParkingFloor> floors;
    private final Map<String, Ticket> activeTickets;

    public ParkingLot(List<ParkingFloor> floors) {
        this.floors = floors;
        this.activeTickets = new HashMap<>();
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicle.getType());

            if (spot != null) {
                spot.parkVehicle(vehicle);

                Ticket ticket = new Ticket(vehicle, spot);
                activeTickets.put(ticket.getId(), ticket);
                return ticket;
            }
        }

        return null; // No available spot
    }

    public double unparkVehicle(String ticketId) {
        Ticket ticket = activeTickets.remove(ticketId);

        if (ticket == null) {
            throw new IllegalArgumentException("Invalid ticket");
        }

        ticket.getSpot().removeVehicle();
        ticket.setExitTime(LocalDateTime.now());

        return ticket.calculateFee();
    }
}

// 2. A floor contains multiple parking spots
class ParkingFloor {
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>(spots);
    }

    public ParkingSpot findAvailableSpot(ParkingSpotType type) {
        for (ParkingSpot spot : spots) {
            if (spot.isAvailable() && spot.getType() == type) {
                return spot;
            }
        }
        return null;
    }

    public int getFloorNumber() {
        return floorNumber;
    }
}

// 3. Individual parking spot
class ParkingSpot {
    private final String spotId;
    private final ParkingSpotType type;
    private Vehicle vehicle;

    public ParkingSpot(String spotId, ParkingSpotType type) {
        this.spotId = spotId;
        this.type = type;
    }

    public boolean isAvailable() {
        return vehicle == null;
    }

    public boolean parkVehicle(Vehicle vehicle) {
        if (!isAvailable() || vehicle.getType() != type) {
            return false;
        }

        this.vehicle = vehicle;
        return true;
    }

    public void removeVehicle() {
        if (vehicle == null) {
            throw new IllegalStateException("Spot is already empty");
        }
        vehicle = null;
    }

    public ParkingSpotType getType() {
        return type;
    }

    public String getSpotId() {
        return spotId;
    }
}

// 4. Vehicle
class Vehicle {
    private final String licensePlate;
    private final ParkingSpotType type;

    public Vehicle(String licensePlate, ParkingSpotType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public ParkingSpotType getType() {
        return type;
    }
}

// 5. Parking ticket
class Ticket {
    private final String id;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;

    public Ticket(Vehicle vehicle, ParkingSpot spot) {
        this.id = UUID.randomUUID().toString();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
    }

    public double calculateFee() {
        LocalDateTime end = (exitTime == null)
                ? LocalDateTime.now() : exitTime;

        long hours = java.time.Duration.between(entryTime, end).toHours();
        hours = Math.max(1, hours + 1);

        double rate = (vehicle.getType() == ParkingSpotType.CAR)
                ? 30.0 : 15.0;

        return hours * rate;
    }

    public String getId() {
        return id;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
}

// 6. Supported parking spot types
enum ParkingSpotType {
    BIKE,
    CAR,
    TRUCK
}
