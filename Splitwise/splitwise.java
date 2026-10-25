import java.util.*;

/*
 * Splitwise - 25-minute LLD
 *
 * 1. SplitwiseService - creates groups, adds expenses, shows balances.
 * 2. User - represents a user.
 * 3. Group - manages group members and expenses.
 * 4. Expense - stores payer, amount, and splits.
 * 5. Split - stores each user's share.
 *
 * Flow:
 * Create group -> Add members -> Add expense -> Calculate balances.
 *
 * Balance:
 * Positive = user should receive money.
 * Negative = user owes money.
 *
 * This version supports equal splits only.
 */

public class Main {
    public static void main(String[] args) {
        SplitwiseService service = new SplitwiseService();

        User alice = new User("U1", "Alice");
        User bob = new User("U2", "Bob");
        User charlie = new User("U3", "Charlie");

        Group trip = service.createGroup("G1", "Trip");
        trip.addMember(alice);
        trip.addMember(bob);
        trip.addMember(charlie);

        service.addEqualExpense(trip, "E1", alice, 900,
                Arrays.asList(alice, bob, charlie));

        service.showBalances();
    }
}

class SplitwiseService {
    private Map<String, Group> groups = new HashMap<>();
    private Map<String, Double> balances = new HashMap<>();
    private Map<String, User> users = new HashMap<>();

    public Group createGroup(String id, String name) {
        Group group = new Group(id, name);
        groups.put(id, group);
        return group;
    }

    public void addEqualExpense(Group group, String expenseId,
                                User payer, double amount,
                                List<User> participants) {
        if (amount <= 0 || participants.isEmpty()) {
            throw new IllegalArgumentException("Invalid expense");
        }

        double share = amount / participants.size();
        List<Split> splits = new ArrayList<>();

        for (User user : participants) {
            splits.add(new Split(user, share));
            users.put(user.getId(), user);
        }

        Expense expense = new Expense(expenseId, payer, amount, splits);
        group.addExpense(expense);

        // Payer receives credit for the full amount paid.
        balances.merge(payer.getId(), amount, Double::sum);

        // Every participant owes their share.
        for (Split split : splits) {
            balances.merge(split.getUser().getId(),
                    -split.getAmount(), Double::sum);
        }
    }

    public void showBalances() {
        boolean found = false;

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            double balance = entry.getValue();

            if (Math.abs(balance) < 0.001) {
                continue;
            }

            User user = users.get(entry.getKey());
            System.out.printf("%s: %s %.2f%n",
                    user.getName(),
                    balance > 0 ? "gets back" : "owes",
                    Math.abs(balance));

            found = true;
        }

        if (!found) {
            System.out.println("All balances are settled");
        }
    }
}

class User {
    private String id;
    private String name;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class Group {
    private String id;
    private String name;
    private List<User> members = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addMember(User user) {
        members.add(user);
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }
}

class Expense {
    private String id;
    private User payer;
    private double amount;
    private List<Split> splits;

    public Expense(String id, User payer, double amount,
                   List<Split> splits) {
        this.id = id;
        this.payer = payer;
        this.amount = amount;
        this.splits = splits;
    }
}

class Split {
    private User user;
    private double amount;

    public Split(User user, double amount) {
        this.user = user;
        this.amount = amount;
    }

    public User getUser() {
        return user;
    }

    public double getAmount() {
        return amount;
    }
}