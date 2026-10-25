import java.util.*;

/*
 * Online Stock Brokerage System
 *
 * 1. BrokerageService - places buy/sell orders.
 * 2. Account - manages cash balance and stock holdings.
 * 3. Stock - stores stock symbol and current price.
 * 4. Order - represents a buy/sell order.
 * 5. User - represents an investor.
 * 6. Enums - OrderType and OrderStatus.
 * 7. Main - demonstrates the workflow.
 *
 * Flow:
 * Place order -> Validate funds/shares -> Update account -> Record order.
 *
 * Simplification:
 * Market orders execute immediately at the current price.
 */

class BrokerageService {
    private Map<String, Stock> stocks = new HashMap<>();
    private Map<String, Account> accounts = new HashMap<>();
    private List<Order> orders = new ArrayList<>();
    private int nextOrderId = 1;

    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);
    }

    public void addAccount(Account account) {
        accounts.put(account.getId(), account);
    }

    public Order placeOrder(String accountId, String symbol,
                            OrderType type, int quantity) {
        Account account = accounts.get(accountId);
        Stock stock = stocks.get(symbol);

        if (account == null || stock == null) {
            throw new IllegalArgumentException("Account or stock not found");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        double amount = stock.getPrice() * quantity;

        if (type == OrderType.BUY) {
            account.buy(symbol, quantity, amount);
        } else {
            account.sell(symbol, quantity, amount);
        }

        Order order = new Order(
            "O" + nextOrderId++, symbol, type, quantity,
            stock.getPrice(), OrderStatus.EXECUTED
        );

        orders.add(order);
        return order;
    }
}

class Account {
    private String id;
    private User owner;
    private double cashBalance;
    private Map<String, Integer> holdings = new HashMap<>();

    public Account(String id, User owner, double cashBalance) {
        this.id = id;
        this.owner = owner;
        this.cashBalance = cashBalance;
    }

    public void buy(String symbol, int quantity, double amount) {
        if (cashBalance < amount) {
            throw new IllegalStateException("Insufficient funds");
        }

        cashBalance -= amount;
        holdings.merge(symbol, quantity, Integer::sum);
    }

    public void sell(String symbol, int quantity, double amount) {
        int owned = holdings.getOrDefault(symbol, 0);

        if (owned < quantity) {
            throw new IllegalStateException("Insufficient shares");
        }

        holdings.put(symbol, owned - quantity);
        cashBalance += amount;
    }

    public int getShares(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public String getId() {
        return id;
    }
}

class Stock {
    private String symbol;
    private double price;

    public Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }
}

class Order {
    private String id;
    private String symbol;
    private OrderType type;
    private int quantity;
    private double executedPrice;
    private OrderStatus status;

    public Order(String id, String symbol, OrderType type,
                 int quantity, double executedPrice, OrderStatus status) {
        this.id = id;
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.executedPrice = executedPrice;
        this.status = status;
    }
}

class User {
    private String id;
    private String name;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

enum OrderType {
    BUY, SELL
}

enum OrderStatus {
    PENDING, EXECUTED, CANCELLED, REJECTED
}

public class Main {
    public static void main(String[] args) {
        BrokerageService service = new BrokerageService();

        User user = new User("U1", "Arun");
        Account account = new Account("A1", user, 10000);

        service.addAccount(account);
        service.addStock(new Stock("AAPL", 200));

        service.placeOrder("A1", "AAPL", OrderType.BUY, 10);
        service.placeOrder("A1", "AAPL", OrderType.SELL, 3);

        System.out.println("Cash balance: " + account.getCashBalance());
        System.out.println("AAPL shares: " + account.getShares("AAPL"));
    }
}