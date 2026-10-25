//The flow is: create notification → choose channel 
// → send notification → return delivery status.

import java.util.*;

// 1. Main system: handles notification delivery
class NotificationService {
    private final Map<ChannelType, NotificationChannel> channels;

    public NotificationService(List<NotificationChannel> channelList) {
        channels = new HashMap<>();

        for (NotificationChannel channel : channelList) {
            channels.put(channel.getType(), channel);
        }
    }

    public boolean sendNotification(Notification notification) {
        NotificationChannel channel =
                channels.get(notification.getChannelType());

        if (channel == null) {
            throw new IllegalArgumentException("Unsupported channel");
        }

        return channel.send(notification);
    }
}

// 2. Notification entity
class Notification {
    private final String recipient;
    private final String message;
    private final ChannelType channelType;

    public Notification(String recipient, String message,
                        ChannelType channelType) {
        if (recipient == null || recipient.trim().isEmpty()
                || message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid notification");
        }

        this.recipient = recipient;
        this.message = message;
        this.channelType = Objects.requireNonNull(channelType);
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public ChannelType getChannelType() {
        return channelType;
    }
}

// 3. Common contract for delivery channels
interface NotificationChannel {
    ChannelType getType();

    boolean send(Notification notification);
}

// 4. Email implementation
class EmailChannel implements NotificationChannel {

    @Override
    public ChannelType getType() {
        return ChannelType.EMAIL;
    }

    @Override
    public boolean send(Notification notification) {
        System.out.println("Sending EMAIL to "
                + notification.getRecipient()
                + ": " + notification.getMessage());

        return true; // Simulated delivery
    }
}

// 5. SMS implementation
class SMSChannel implements NotificationChannel {

    @Override
    public ChannelType getType() {
        return ChannelType.SMS;
    }

    @Override
    public boolean send(Notification notification) {
        System.out.println("Sending SMS to "
                + notification.getRecipient()
                + ": " + notification.getMessage());

        return true; // Simulated delivery
    }
}

// 6. Push notification implementation
class PushChannel implements NotificationChannel {

    @Override
    public ChannelType getType() {
        return ChannelType.PUSH;
    }

    @Override
    public boolean send(Notification notification) {
        System.out.println("Sending PUSH to "
                + notification.getRecipient()
                + ": " + notification.getMessage());

        return true; // Simulated delivery
    }
}

// 7. Supported channels
enum ChannelType {
    EMAIL,
    SMS,
    PUSH
}

// 8. Example usage
public class Main {
    public static void main(String[] args) {
        NotificationService service = new NotificationService(
                Arrays.asList(
                    new EmailChannel(),
                    new SMSChannel(),
                    new PushChannel()
                )
        );

        Notification notification = new Notification(
                "user@example.com",
                "Your order has been shipped!",
                ChannelType.EMAIL
        );

        boolean success = service.sendNotification(notification);
        System.out.println("Sent successfully: " + success);
    }
}
