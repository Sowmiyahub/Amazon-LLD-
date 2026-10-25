import java.util.*;

// DigitalWalletService: Creates wallets, adds money, transfers funds,
// checks balances, and retrieves transaction history.
//
// Wallet: Stores the owner's balance and transaction history.
// Transaction: Records each money movement.
// TransactionType: Identifies CREDIT, DEBIT, and TRANSFER.
// Simplifications: In-memory storage, no payment gateway or database.
// All wallet balance changes are synchronized for basic thread safety.

class DigitalWalletService {
    private Map<String, Wallet> wallets = new HashMap<>();
    private List<Transaction> transactions = new ArrayList<>();

    public void createWallet(String userId) {
        if (wallets.containsKey(userId)) {
            throw new IllegalArgumentException("Wallet already exists");
        }
        wallets.put(userId, new Wallet(userId));
    }

    public void addMoney(String userId, long amount) {
        validateAmount(amount);
        Wallet wallet = getWallet(userId);

        synchronized (wallet) {
            wallet.credit(amount);
            transactions.add(new Transaction(
                userId, null, amount, TransactionType.CREDIT
            ));
        }
    }

    public void transfer(String fromUserId, String toUserId, long amount) {
        validateAmount(amount);

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        Wallet sender = getWallet(fromUserId);
        Wallet receiver = getWallet(toUserId);

        // Lock wallets in a consistent order to reduce deadlock risk.
        Wallet first = fromUserId.compareTo(toUserId) < 0 ? sender : receiver;
        Wallet second = first == sender ? receiver : sender;

        synchronized (first) {
            synchronized (second) {
                if (sender.getBalance() < amount) {
                    throw new IllegalArgumentException("Insufficient balance");
                }

                sender.debit(amount);
                receiver.credit(amount);

                transactions.add(new Transaction(
                    fromUserId, toUserId, amount, TransactionType.TRANSFER
                ));
            }
        }
    }

    public long getBalance(String userId) {
        Wallet wallet = getWallet(userId);
        synchronized (wallet) {
            return wallet.getBalance();
        }
    }

    public List<Transaction> getTransactionHistory(String userId) {
        getWallet(userId); // Validate that the wallet exists.

        List<Transaction> result = new ArrayList<>();
        synchronized (transactions) {
            for (Transaction transaction : transactions) {
                if (transaction.getFromUserId().equals(userId)
                        || userId.equals(transaction.getToUserId())) {
                    result.add(transaction);
                }
            }
        }
        return result;
    }

    private Wallet getWallet(String userId) {
        Wallet wallet = wallets.get(userId);
        if (wallet == null) {
            throw new IllegalArgumentException("Wallet not found: " + userId);
        }
        return wallet;
    }

    private void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
}

class Wallet {
    private final String userId;
    private long balanceInPaise;

    public Wallet(String userId) {
        this.userId = userId;
        this.balanceInPaise = 0;
    }

    public String getUserId() {
        return userId;
    }

    public long getBalance() {
        return balanceInPaise;
    }

    public void credit(long amount) {
        balanceInPaise += amount;
    }

    public void debit(long amount) {
        balanceInPaise -= amount;
    }
}

class Transaction {
    private final String fromUserId;
    private final String toUserId;
    private final long amountInPaise;
    private final TransactionType type;
    private final Date timestamp;

    public Transaction(String fromUserId, String toUserId,
                       long amountInPaise, TransactionType type) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amountInPaise = amountInPaise;
        this.type = type;
        this.timestamp = new Date();
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public long getAmountInPaise() {
        return amountInPaise;
    }

    public TransactionType getType() {
        return type;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    @Override
    public String toString() {
        return type + " | Amount: " + amountInPaise
            + " paise | From: " + fromUserId
            + " | To: " + toUserId
            + " | Time: " + timestamp;
    }
}

enum TransactionType {
    CREDIT,
    DEBIT,
    TRANSFER
}

public class Main {
    public static void main(String[] args) {
        DigitalWalletService service = new DigitalWalletService();

        service.createWallet("Alice");
        service.createWallet("Bob");

        service.addMoney("Alice", 100_000); // ₹1,000
        service.transfer("Alice", "Bob", 25_000); // ₹250

        System.out.println("Alice balance: ₹"
            + service.getBalance("Alice") / 100.0);
        System.out.println("Bob balance: ₹"
            + service.getBalance("Bob") / 100.0);

        System.out.println("\nAlice's transaction history:");
        for (Transaction transaction :
                service.getTransactionHistory("Alice")) {
            System.out.println(transaction);
        }
    }
}