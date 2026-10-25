/*
Class Design: Vending Machine (State Design Pattern)

1. VendingMachine
   - Main class that manages inventory, inserted money, and current state.
   - Delegates user actions to the current state.

2. VendingMachineState
   - Interface defining actions: selectProduct, insertMoney, dispense, cancel.

3. IdleState
   - Waits for the user to select a product or insert money.

4. HasMoneyState
   - Handles product selection, additional money, dispensing, and cancellation.

5. Product
   - Stores product ID, name, price, and available quantity.

6. Main Flow
   - Select product -> Insert money -> Dispense product.
   - If payment is insufficient, request more money.
   - If cancelled, refund the inserted money.

Design Pattern: State
Data Structure: HashMap for inventory

Note:
   - Cash handling and physical dispensing are simulated.
   - Production vending machines need hardware integration and concurrency handling.
*/

import java.util.*;

interface VendingMachineState {
    void selectProduct(VendingMachine machine, String productId);
    void insertMoney(VendingMachine machine, double amount);
    void dispense(VendingMachine machine);
    void cancel(VendingMachine machine);
}

class VendingMachine {
    private final Map<String, Product> inventory = new HashMap<>();
    private VendingMachineState currentState;
    private Product selectedProduct;
    private double insertedMoney;

    public VendingMachine() {
        currentState = new IdleState();
    }

    public void addProduct(Product product) {
        inventory.put(product.getId(), product);
    }

    public Product getProduct(String productId) {
        return inventory.get(productId);
    }

    public void setState(VendingMachineState state) {
        currentState = state;
    }

    public Product getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Product product) {
        selectedProduct = product;
    }

    public double getInsertedMoney() {
        return insertedMoney;
    }

    public void addMoney(double amount) {
        insertedMoney += amount;
    }

    public void resetMoney() {
        insertedMoney = 0;
    }

    public void selectProduct(String productId) {
        currentState.selectProduct(this, productId);
    }

    public void insertMoney(double amount) {
        currentState.insertMoney(this, amount);
    }

    public void dispense() {
        currentState.dispense(this);
    }

    public void cancel() {
        currentState.cancel(this);
    }
}

class IdleState implements VendingMachineState {
    @Override
    public void selectProduct(VendingMachine machine, String productId) {
        Product product = machine.getProduct(productId);

        if (product == null) {
            System.out.println("Invalid product.");
            return;
        }

        if (product.getQuantity() <= 0) {
            System.out.println("Product out of stock.");
            return;
        }

        machine.setSelectedProduct(product);
        machine.setState(new HasMoneyState());
        System.out.println("Selected: " + product.getName()
                + ". Price: Rs." + product.getPrice());
    }

    @Override
    public void insertMoney(VendingMachine machine, double amount) {
        System.out.println("Please select a product first.");
    }

    @Override
    public void dispense(VendingMachine machine) {
        System.out.println("Please select a product first.");
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("Nothing to cancel.");
    }
}

class HasMoneyState implements VendingMachineState {
    @Override
    public void selectProduct(VendingMachine machine, String productId) {
        System.out.println("Please complete or cancel the current transaction.");
    }

    @Override
    public void insertMoney(VendingMachine machine, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        machine.addMoney(amount);
        System.out.println("Inserted: Rs." + amount);
        System.out.println("Total inserted: Rs." + machine.getInsertedMoney());
    }

    @Override
    public void dispense(VendingMachine machine) {
        Product product = machine.getSelectedProduct();

        if (product == null) {
            machine.setState(new IdleState());
            return;
        }

        if (product.getQuantity() <= 0) {
            System.out.println("Product is out of stock.");
            cancel(machine);
            return;
        }

        double inserted = machine.getInsertedMoney();

        if (inserted < product.getPrice()) {
            System.out.println("Insufficient money. Please insert Rs."
                    + (product.getPrice() - inserted) + " more.");
            return;
        }

        product.reduceQuantity();

        double change = inserted - product.getPrice();
        System.out.println("Dispensing: " + product.getName());

        if (change > 0) {
            System.out.println("Please collect change: Rs." + change);
        }

        machine.resetMoney();
        machine.setSelectedProduct(null);
        machine.setState(new IdleState());
    }

    @Override
    public void cancel(VendingMachine machine) {
        System.out.println("Transaction cancelled.");

        if (machine.getInsertedMoney() > 0) {
            System.out.println("Refunding: Rs." + machine.getInsertedMoney());
        }

        machine.resetMoney();
        machine.setSelectedProduct(null);
        machine.setState(new IdleState());
    }
}

class Product {
    private final String id;
    private final String name;
    private final double price;
    private int quantity;

    public Product(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
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

    public int getQuantity() {
        return quantity;
    }

    public void reduceQuantity() {
        if (quantity <= 0) {
            throw new IllegalStateException("Product is out of stock");
        }
        quantity--;
    }
}

public class Main {
    public static void main(String[] args) {
        VendingMachine machine = new VendingMachine();

        machine.addProduct(new Product("P1", "Coke", 40, 5));
        machine.addProduct(new Product("P2", "Chips", 25, 3));

        machine.selectProduct("P1");
        machine.insertMoney(20);
        machine.dispense();       // Insufficient money

        machine.insertMoney(30);
        machine.dispense();       // Dispenses Coke, returns Rs.10 change

        machine.selectProduct("P2");
        machine.insertMoney(50);
        machine.cancel();         // Refunds Rs.50
    }
}