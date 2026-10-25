
import java.util.*;

// Common device functionality
interface Device {
    String getId();
    String getName();
    boolean isPluggedIn();
    int getChargingPercentage();
}

// Capability to display a message
interface Display {
    void showMessage(String message);
}

// Capability to speak a message
interface Speaker {
    void speakMessage(String message);
}

// A tablet supports all three capabilities
class Tablet implements Device, Display, Speaker {
    private String id;
    private String name;
    private boolean pluggedIn;
    private int chargingPercentage;

    public Tablet(String id, String name,
                  boolean pluggedIn, int chargingPercentage) {
        this.id = id;
        this.name = name;
        this.pluggedIn = pluggedIn;
        this.chargingPercentage = chargingPercentage;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPluggedIn() {
        return pluggedIn;
    }

    public int getChargingPercentage() {
        return chargingPercentage;
    }

    public void showMessage(String message) {
        System.out.println(name + " displays: " + message);
    }

    public void speakMessage(String message) {
        System.out.println(name + " speaks: " + message);
    }
}

// Manages registered devices
class DeviceManager {
    private Map<String, Device> devices = new HashMap<>();

    public void registerDevice(Device device) {
        devices.put(device.getId(), device);
    }

    public Device getDevice(String id) {
        Device device = devices.get(id);

        if (device == null) {
            throw new IllegalArgumentException("Device not registered");
        }

        return device;
    }

    public void showMessage(String id, String message) {
        Device device = getDevice(id);

        if (device instanceof Display) {
            ((Display) device).showMessage(message);
        } else {
            System.out.println("Device does not support display");
        }
    }

    public void speakMessage(String id, String message) {
        Device device = getDevice(id);

        if (device instanceof Speaker) {
            ((Speaker) device).speakMessage(message);
        } else {
            System.out.println("Device does not support speaker");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        DeviceManager manager = new DeviceManager();

        Tablet tablet = new Tablet("D1", "My Tablet", true, 85);
        manager.registerDevice(tablet);

        System.out.println("Plugged in: " + tablet.isPluggedIn());
        System.out.println("Battery: " + tablet.getChargingPercentage() + "%");

        manager.showMessage("D1", "Hello!");
        manager.speakMessage("D1", "Your battery is at 85 percent");
    }
}
