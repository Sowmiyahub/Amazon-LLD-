import java.util.*;

// 1. Main service: handles inventory operations
class InventoryService {
    private final Map<String, Product> products = new HashMap<>();
    private final Map<String, Integer> inventory = new HashMap<>();

    // Add a new product to the catalog
    public void addProduct(Product product) {
        products.put(product.getId(), product);
        inventory.putIfAbsent(product.getId(), 0);
    }

    // Add stock to an existing product
    public void addStock(String productId, int quantity) {
        validateProduct(productId);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        inventory.put(
            productId,
            inventory.get(productId) + quantity
        );
    }

    // Remove stock when a product is sold or dispatched
    public void removeStock(String productId, int quantity) {
        validateProduct(productId);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        int currentStock = inventory.get(productId);

        if (currentStock < quantity) {
            throw new IllegalStateException("Insufficient stock");
        }

        inventory.put(productId, currentStock - quantity);
    }

    // Check available quantity
    public int getStock(String productId) {
        validateProduct(productId);
        return inventory.get(productId);
    }

    // Get product details
    public Product getProduct(String productId) {
        validateProduct(productId);
        return products.get(productId);
    }

    // List all products with stock details
    public void displayInventory() {
        for (Product product : products.values()) {
            System.out.println(
                product.getName() +
                " | Stock: " + inventory.get(product.getId()) +
                " | Price: " + product.getPrice()
            );
        }
    }

    private void validateProduct(String productId) {
        if (!products.containsKey(productId)) {
            throw new IllegalArgumentException("Product not found");
        }
    }
}

// 2. Product entity
class Product {
    private final String id;
    private final String name;
    private final double price;

    public Product(String id, String name, double price) {
        if (id == null || id.trim().isEmpty()
                || name == null || name.trim().isEmpty()
                || price < 0) {
            throw new IllegalArgumentException("Invalid product details");
        }

        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

// 3. Example usage
public class Main {
    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();

        Product laptop = new Product("P101", "Laptop", 50000);
        Product mouse = new Product("P102", "Mouse", 800);

        inventory.addProduct(laptop);
        inventory.addProduct(mouse);

        inventory.addStock("P101", 10);
        inventory.addStock("P102", 50);

        inventory.removeStock("P101", 2);
        inventory.removeStock("P102", 5);

        System.out.println("Laptop stock: " +
            inventory.getStock("P101"));

        inventory.displayInventory();
    }
}