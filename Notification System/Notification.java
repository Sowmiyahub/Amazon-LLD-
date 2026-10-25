
/*
========================================================
DESIGN: NOTIFICATION SYSTEM
========================================================

1. NotificationService
   - Main service/controller.
   - Sends notifications through the selected channel.
   - Uses the appropriate notification strategy.

2. Notification
   - Stores recipient, message, and notification type.

3. NotificationChannel (Interface)
   - Defines the common send() operation.
   - Allows different notification delivery methods.

4. EmailChannel
   - Sends email notifications.

5. SMSChannel
   - Sends SMS notifications.

6. PushChannel
   - Sends push notifications.

7. NotificationType (Enum)
   - EMAIL, SMS, PUSH.

========================================================
CLASS RELATIONSHIPS
========================================================

NotificationService
  └── selects a NotificationChannel

NotificationChannel
  ├── EmailChannel
  ├── SMSChannel
  └── PushChannel

Notification
  └── contains recipient, message, and type

========================================================
DESIGN PATTERN
========================================================

Strategy Pattern
- Each channel implements NotificationChannel.
- NotificationService selects the appropriate strategy.
- New channels can be added without changing the
  notification delivery logic.

MAIN FLOW
1. Create a notification.
2. Select the notification type.
3. Find the matching channel.
4. Send the notification.

========================================================
*/

// 1. Main service/controller
import java.util.*;

class NotificationService {
    private final Map<NotificationType, NotificationChannel> channels =
        new HashMap<>();

    public NotificationService() {
        channels.put(NotificationType.EMAIL, new EmailChannel());
        channels.put(NotificationType.SMS, new SMSChannel());
        channels.put(NotificationType.PUSH, new PushChannel());
    }

    public void sendNotification(Notification notification) {
        NotificationChannel channel =
            channels.get(notification.getType());

        if (channel == null) {
            throw new IllegalArgumentException(
                "Unsupported notification type"
            );
        }

        channel.send(
            notification.getRecipient(),
            notification.getMessage()
        );
    }
}

// 2. Notification entity
class Notification {
    private final String recipient;
    private final String message;
    private final NotificationType type;

    public Notification(
            String recipient, String message, NotificationType type) {
        if (recipient == null || recipient.trim().isEmpty()
                || message == null || message.trim().isEmpty()
                || type == null) {
            throw new IllegalArgumentException(
                "Invalid notification details"
            );
        }

        this.recipient = recipient;
        this.message = message;
        this.type = type;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }
}

// 3. Notification channel interface
interface NotificationChannel {
    void send(String recipient, String message);
}

// 4. Email notification strategy
class EmailChannel implements NotificationChannel {
    @Override
    public void send(String recipient, String message) {
        System.out.println(
            "Sending EMAIL to " + recipient + ": " + message
        );
    }
}

// 5. SMS notification strategy
class SMSChannel implements NotificationChannel {
    @Override
    public void send(String recipient, String message) {
        System.out.println(
            "Sending SMS to " + recipient + ": " + message
        );
    }
}

// 6. Push notification strategy
class PushChannel implements NotificationChannel {
    @Override
    public void send(String recipient, String message) {
        System.out.println(
            "Sending PUSH notification to " + recipient
                + ": " + message
        );
    }
}

// 7. Notification type
enum NotificationType {
    EMAIL,
    SMS,
    PUSH
}

// 8. Example usage
public class Main {
    public static void main(String[] args) {
        NotificationService service = new NotificationService();

        Notification email = new Notification(
            "user@example.com",
            "Welcome to our application!",
            NotificationType.EMAIL
        );

        Notification sms = new Notification(
            "+919876543210",
            "Your OTP is 123456",
            NotificationType.SMS
        );

        Notification push = new Notification(
            "user123",
            "You have a new message",
            NotificationType.PUSH
        );

        service.sendNotification(email);
        service.sendNotification(sms);
        service.sendNotification(push);
    }
}
