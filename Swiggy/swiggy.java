import java.util.*;

/*
 * Food Delivery System - 25-minute LLD
 *
 * 1. FoodDeliveryService - manages restaurants, orders, and delivery.
 * 2. Restaurant - stores menu and accepts orders.
 * 3. MenuItem - represents food and price.
 * 4. Customer - places orders.
 * 5. Order - stores items, total, and status.
 * 6. DeliveryPartner - delivers the order.
 *
 * Flow:
 * Browse menu -> Place order -> Assign delivery partner -> Update status.
 *
 * Simplification:
 * In-memory storage, one restaurant per order, no payment gateway.
 */

public class Main {
    public static void main(String[] args) {
        FoodDeliveryService service = new FoodDeliveryService();

        Restaurant restaurant = new Restaurant("R1", "Chennai Bites");
        restaurant.addMenuItem(new MenuItem("M1", "Dosa", 80));
        restaurant.addMenuItem(new MenuItem("M2", "Meals", 150));
        service.addRestaurant(restaurant);

        Customer customer = new Customer("C1", "Arun");
        DeliveryPartner partner = new DeliveryPartner("D1", "Kumar");
        service.addDeliveryPartner(partner);

        Order order = service.placeOrder(
            customer, restaurant,
            Map.of("M1", 2, "M2", 1)
        );

        service.assignDeliveryPartner(order, partner);
        service.updateOrderStatus(order, OrderStatus.PREPARING);
        service.updateOrderStatus(order, OrderStatus.OUT_FOR_DELIVERY);
        service.updateOrderStatus(order, OrderStatus.DELIVERED);

        System.out.println("Order total: " + order.getTotalAmount());
        System.out.println("Order status: " + order.getStatus());
    }
}

class FoodDeliveryService {
    private Map<String, Restaurant> restaurants = new HashMap<>();
    private Map<String, Order> orders = new HashMap<>();
    private Map<String, DeliveryPartner> partners = new HashMap<>();
    private int nextOrderId = 1;

    public void addRestaurant(Restaurant restaurant) {
        restaurants.put(restaurant.getId(), restaurant);
    }

    public void addDeliveryPartner(DeliveryPartner partner) {
        partners.put(partner.getId(), partner);
    }

    public Order placeOrder(Customer customer, Restaurant restaurant,
                            Map<String, Integer> itemQuantities) {
        if (!restaurants.containsKey(restaurant.getId())) {
            throw new IllegalArgumentException("Restaurant not found");
        }

        List<OrderItem> items = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            MenuItem menuItem = restaurant.getMenuItem(entry.getKey());

            if (menuItem == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Invalid menu item or quantity");
            }

            items.add(new OrderItem(menuItem, entry.getValue()));
        }

        Order order = new Order(
            "O" + nextOrderId++, customer, restaurant, items
        );

        orders.put(order.getId(), order);
        return order;
    }

    public void assignDeliveryPartner(Order order, DeliveryPartner partner) {
        if (!partners.containsKey(partner.getId())) {
            throw new IllegalArgumentException("Partner not found");
        }

        order.assignPartner(partner);
    }

    public void updateOrderStatus(Order order, OrderStatus status) {
        order.setStatus(status);
    }
}

class Restaurant {
    private String id;
    private String name;
    private Map<String, MenuItem> menu = new HashMap<>();

    public Restaurant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addMenuItem(MenuItem item) {
        menu.put(item.getId(), item);
    }

    public MenuItem getMenuItem(String id) {
        return menu.get(id);
    }

    public String getId() {
        return id;
    }
}

class MenuItem {
    private String id;
    private String name;
    private double price;

    public MenuItem(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public double getPrice() {
        return price;
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

class DeliveryPartner {
    private String id;
    private String name;

    public DeliveryPartner(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }
}

class Order {
    private String id;
    private Customer customer;
    private Restaurant restaurant;
    private List<OrderItem> items;
    private DeliveryPartner partner;
    private OrderStatus status = OrderStatus.PLACED;

    public Order(String id, Customer customer, Restaurant restaurant,
                 List<OrderItem> items) {
        this.id = id;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = items;
    }

    public double getTotalAmount() {
        return items.stream().mapToDouble(OrderItem::getSubtotal).sum();
    }

    public void assignPartner(DeliveryPartner partner) {
        this.partner = partner;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }
}

class OrderItem {
    private MenuItem menuItem;
    private int quantity;

    public OrderItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return menuItem.getPrice() * quantity;
    }
}

enum OrderStatus {
    PLACED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}