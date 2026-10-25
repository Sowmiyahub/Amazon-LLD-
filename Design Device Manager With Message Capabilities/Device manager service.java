import java.util.*;

class DeviceManager {
    private Map<String, Device> devices = new HashMap<>();

    public void registerDevice(Device device) {
        devices.put(device.id, device);
    }

    public void sendMessage(String senderId, String receiverId, String content) {
        Device sender = devices.get(senderId);
        Device receiver = devices.get(receiverId);

        if (sender == null || receiver == null)
            throw new IllegalArgumentException("Device not registered");

        Message message = new Message(senderId, receiverId, content);
        receiver.receiveMessage(message);
    }
}

class Device {
    String id;
    String name;
    List<Message> inbox = new ArrayList<>();

    Device(String id, String name) {
        this.id = id;
        this.name = name;
    }

    void receiveMessage(Message message) {
        inbox.add(message);
    }

    public void showMessages() {
        for (Message message : inbox)
            System.out.println(message.senderId + ": " + message.content);
    }
}

class Message {
    String senderId;
    String receiverId;
    String content;

    Message(String senderId, String receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }
}

public class Main {
    public static void main(String[] args) {
        DeviceManager manager = new DeviceManager();

        Device phone = new Device("D1", "Phone");
        Device laptop = new Device("D2", "Laptop");

        manager.registerDevice(phone);
        manager.registerDevice(laptop);

        manager.sendMessage("D1", "D2", "Hello from phone!");
        manager.sendMessage("D2", "D1", "Hello from laptop!");

        System.out.println("Laptop inbox:");
        laptop.showMessages();

        System.out.println("Phone inbox:");
        phone.showMessages();
    }
}