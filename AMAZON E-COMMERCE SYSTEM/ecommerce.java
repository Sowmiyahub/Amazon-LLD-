/*
========================================================
DESIGN: AMAZON E-COMMERCE SYSTEM
========================================================

1. AmazonService
   - Main service/controller.
   - Manages products and customers.
   - Handles add-to-cart, checkout, and order retrieval.

2. Product
   - Stores product ID, name, price, and available stock.
   - Reduces stock when an order is confirmed.

3. Customer
   - Stores customer details.
   - Owns a shopping cart.

4. Cart
   - Stores cart items.
   - Adds products, calculates total price, and clears cart.

5. CartItem
   - Represents a product and its quantity in the cart.
   - Calculates the subtotal.

6. Order
   - Stores order ID, customer, items, total amount, and status.
   - Tracks the order lifecycle.

7. Payment
   - Handles payment processing.
   - Integrates with a payment gateway (simulated here).

8. PaymentMethod (Enum)
   - CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING.

9. OrderStatus (Enum)
   - CREATED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED.

========================================================
CLASS RELATIONSHIPS
========================================================

AmazonService
  ├── manages Products
  ├── manages Customers
  └── manages Orders

Customer
  └── has one Cart

Cart
  └── contains multiple CartItems

CartItem
  └── references one Product

Order
  ├── belongs to one Customer
  └── contains multiple CartItems

AmazonService
  └── uses Payment during checkout

========================================================
MAIN FLOW
========================================================

1. Add products to the catalog.
2. Register a customer.
3. Customer adds products to the cart.
4. Customer checks out.
5. Create an order and calculate the total.
6. Process payment.
7. Confirm the order and clear the cart.

========================================================
*/

// Implementation starts below

import java.util.*;

// 1. Main service: handles product browsing, cart, orders and checkout
class AmazonService {
    private final Map<String, Product> products = new HashMap<>();
    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, Order> orders = new HashMap<>();

    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    public Product getProduct(String productId) {
        Product product = products.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        return product;
    }

    public void registerCustomer(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    public void addToCart(String customerId, String productId, int quantity) {
        Customer customer = getCustomer(customerId);
        Product product = getProduct(productId);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        customer.getCart().addItem(product, quantity);
    }

    public Order checkout(String customerId, PaymentMethod paymentMethod) {
        Customer customer = getCustomer(customerId);
        Cart cart = customer.getCart();

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order(customer, cart.getItems());

        Payment payment = new Payment(
            UUID.randomUUID().toString(),
            order.getTotalAmount(),
            paymentMethod
        );

        if (!payment.processPayment()) {
            throw new IllegalStateException("Payment failed");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orders.put(order.getId(), order);
        cart.clear();

        return order;
    }

    public Order getOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        return order;
    }

    private Customer getCustomer(String customerId) {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        return customer;
    }
}

// 2. Product entity
class Product {
    private final String id;
    private final String name;
    private final double price;
    private int availableQuantity;

    public Product(String id, String name, double price, int quantity) {
        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid price or quantity");
        }

        this.id = id;
        this.name = name;
        this.price = price;
        this.availableQuantity = quantity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getAvailableQuantity() { return availableQuantity; }

    public void reduceStock(int quantity) {
        if (quantity <= 0 || availableQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        availableQuantity -= quantity;
    }
}

// 3. Customer entity
class Customer {
    private final String id;
    private final String name;
    private final Cart cart = new Cart();

    public Customer(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Cart getCart() { return cart; }
}

// 4. Cart item: product and quantity
class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public void increaseQuantity(int quantity) {
        this.quantity += quantity;
    }

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }
}

// 5. Shopping cart
class Cart {
    private final Map<String, CartItem> items = new HashMap<>();

    public void addItem(Product product, int quantity) {
        if (product.getAvailableQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }

        CartItem item = items.get(product.getId());

        if (item == null) {
            items.put(product.getId(), new CartItem(product, quantity));
        } else {
            if (product.getAvailableQuantity() < item.getQuantity() + quantity) {
                throw new IllegalStateException("Insufficient stock");
            }
            item.increaseQuantity(quantity);
        }
    }

    public Map<String, CartItem> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : items.values()) {
            total += item.getSubtotal();
        }
        return total;
    }

    public void clear() {
        items.clear();
    }
}

// 6. Order entity
class Order {
    private final String id;
    private final Customer customer;
    private final List<CartItem> items;
    private final double totalAmount;
    private OrderStatus status;

    public Order(Customer customer, Map<String, CartItem> cartItems) {
        this.id = UUID.randomUUID().toString();
        this.customer = customer;

        this.items = new ArrayList<>();
        double total = 0;

        for (CartItem item : cartItems.values()) {
            this.items.add(
                new CartItem(item.getProduct(), item.getQuantity())
            );
            total += item.getSubtotal();
        }

        this.totalAmount = total;
        this.status = OrderStatus.CREATED;
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    public double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

// 7. Payment entity
class Payment {
    private final String id;
    private final double amount;
    private final PaymentMethod method;

    public Payment(String id, double amount, PaymentMethod method) {
        this.id = id;
        this.amount = amount;
        this.method = method;
    }

    public boolean processPayment() {
        // Simulated payment gateway
        System.out.println(
            "Processing " + method + " payment of Rs. " + amount
        );
        return true;
    }
}

// 8. Enums
enum PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING
}

enum OrderStatus {
    CREATED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
}

// 9. Example usage
public class Main {
    public static void main(String[] args) {
        AmazonService amazon = new AmazonService();

        Product laptop = new Product("P101", "Laptop", 50000, 10);
        Product mouse = new Product("P102", "Mouse", 800, 50);

        amazon.addProduct(laptop);
        amazon.addProduct(mouse);

        Customer customer = new Customer("C101", "Arun");
        amazon.registerCustomer(customer);

        amazon.addToCart("C101", "P101", 1);
        amazon.addToCart("C101", "P102", 2);

        Order order = amazon.checkout("C101", PaymentMethod.UPI);

        System.out.println("Order ID: " + order.getId());
        System.out.println("Total: Rs. " + order.getTotalAmount());
        System.out.println("Status: " + order.getStatus());
    }
}