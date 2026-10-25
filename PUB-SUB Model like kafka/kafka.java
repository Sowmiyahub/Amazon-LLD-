/*
Class Design: Pub-Sub Messaging System (Kafka-like)

1. MessageBroker
   - Main entry point.
   - Creates topics, publishes messages, and manages subscriptions.

2. Topic
   - Stores messages in order.
   - Maintains consumers subscribed to the topic.
   - Assigns offsets to messages.

3. Message
   - Contains message ID, payload, and offset.

4. Producer
   - Publishes messages to a topic through the broker.

5. Consumer
   - Subscribes to a topic and consumes messages.
   - Tracks the next offset to read.

Main Flow:
   - Broker creates a topic.
   - Producer publishes a message.
   - Topic stores the message with an offset.
   - Consumer reads messages from its next offset.

Design:
   - Topic-based publish-subscribe.
   - Each consumer maintains its own offset.
   - Multiple consumers can independently read the same topic.

Note:
   - Simplified in-memory implementation.
   - No partitions, replication, persistence, or consumer groups.
*/

import java.util.*;

class MessageBroker {
    private final Map<String, Topic> topics = new HashMap<>();

    public void createTopic(String topicName) {
        topics.putIfAbsent(topicName, new Topic(topicName));
    }

    public void publish(String topicName, String payload) {
        Topic topic = topics.get(topicName);

        if (topic == null) {
            throw new IllegalArgumentException("Topic not found");
        }

        topic.publish(payload);
    }

    public void subscribe(String topicName, Consumer consumer) {
        Topic topic = topics.get(topicName);

        if (topic == null) {
            throw new IllegalArgumentException("Topic not found");
        }

        topic.addConsumer(consumer);
        consumer.subscribe(topicName);
    }

    public List<Message> consume(String topicName, Consumer consumer) {
        Topic topic = topics.get(topicName);

        if (topic == null) {
            throw new IllegalArgumentException("Topic not found");
        }

        return topic.consume(consumer);
    }
}

class Topic {
    private final String name;
    private final List<Message> messages = new ArrayList<>();
    private final Set<Consumer> consumers = new HashSet<>();

    public Topic(String name) {
        this.name = name;
    }

    public synchronized void publish(String payload) {
        long offset = messages.size();
        messages.add(new Message(UUID.randomUUID().toString(), payload, offset));
    }

    public synchronized void addConsumer(Consumer consumer) {
        consumers.add(consumer);
    }

    public synchronized List<Message> consume(Consumer consumer) {
        if (!consumers.contains(consumer)) {
            throw new IllegalStateException("Consumer is not subscribed");
        }

        long nextOffset = consumer.getNextOffset(name);
        List<Message> result = new ArrayList<>();

        while (nextOffset < messages.size()) {
            Message message = messages.get((int) nextOffset);
            result.add(message);
            nextOffset++;
        }

        consumer.updateOffset(name, nextOffset);
        return result;
    }

    public String getName() {
        return name;
    }
}

class Message {
    private final String id;
    private final String payload;
    private final long offset;

    public Message(String id, String payload, long offset) {
        this.id = id;
        this.payload = payload;
        this.offset = offset;
    }

    public String getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }

    public long getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "Message{offset=" + offset + ", payload='" + payload + "'}";
    }
}

class Producer {
    private final MessageBroker broker;

    public Producer(MessageBroker broker) {
        this.broker = broker;
    }

    public void send(String topicName, String payload) {
        broker.publish(topicName, payload);
    }
}

class Consumer {
    private final String id;
    private final Map<String, Long> offsets = new HashMap<>();

    public Consumer(String id) {
        this.id = id;
    }

    public void subscribe(String topicName) {
        offsets.putIfAbsent(topicName, 0L);
    }

    public long getNextOffset(String topicName) {
        return offsets.getOrDefault(topicName, 0L);
    }

    public void updateOffset(String topicName, long nextOffset) {
        offsets.put(topicName, nextOffset);
    }

    public String getId() {
        return id;
    }
}

public class Main {
    public static void main(String[] args) {
        MessageBroker broker = new MessageBroker();

        broker.createTopic("orders");

        Producer producer = new Producer(broker);

        Consumer consumer1 = new Consumer("C1");
        Consumer consumer2 = new Consumer("C2");

        broker.subscribe("orders", consumer1);
        broker.subscribe("orders", consumer2);

        producer.send("orders", "Order-101 created");
        producer.send("orders", "Order-102 created");
        producer.send("orders", "Order-103 created");

        System.out.println("Consumer 1:");
        for (Message message : broker.consume("orders", consumer1)) {
            System.out.println(message);
        }

        System.out.println("\nConsumer 2:");
        for (Message message : broker.consume("orders", consumer2)) {
            System.out.println(message);
        }

        producer.send("orders", "Order-104 created");

        System.out.println("\nConsumer 1 - New messages:");
        for (Message message : broker.consume("orders", consumer1)) {
            System.out.println(message);
        }
    }
}