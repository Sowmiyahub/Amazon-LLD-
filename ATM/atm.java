/*
Class Design: ATM (State Design Pattern)

1. ATM
   - Maintains the current state.
   - Stores the ATM's cash balance.
   - Delegates user actions to the current state.

2. ATMState (Interface)
   - Defines operations allowed in each state.

3. IdleState
   - Waits for a card to be inserted.

4. HasCardState
   - Accepts PIN input and validates the card.

5. AuthenticatedState
   - Allows balance inquiry, withdrawal, deposit, and card ejection.

6. Card
   - Represents the bank card and its PIN/account details.

7. BankAccount
   - Stores account balance and supports deposits/withdrawals.

8. Main Flow
   - Idle -> Card Inserted -> HasCard
   - Valid PIN -> Authenticated
   - Transaction -> Authenticated
   - Eject Card -> Idle

Design Pattern: State Pattern
Note:
   - Bank operations are simulated in memory.
   - Real ATM hardware, bank network, and cash dispenser are external.
*/

import java.util.*;

interface ATMState {
    void insertCard(ATM atm, Card card);
    void enterPin(ATM atm, String pin);
    void checkBalance(ATM atm);
    void withdraw(ATM atm, double amount);
    void deposit(ATM atm, double amount);
    void ejectCard(ATM atm);
}

class ATM {
    private ATMState currentState;
    private double cashAvailable;

    public ATM(double cashAvailable) {
        this.cashAvailable = cashAvailable;
        this.currentState = new IdleState();
    }

    public void setState(ATMState state) {
        this.currentState = state;
    }

    public double getCashAvailable() {
        return cashAvailable;
    }

    public void dispenseCash(double amount) {
        cashAvailable -= amount;
    }

    public void insertCard(Card card) {
        currentState.insertCard(this, card);
    }

    public void enterPin(String pin) {
        currentState.enterPin(this, pin);
    }

    public void checkBalance() {
        currentState.checkBalance(this);
    }

    public void withdraw(double amount) {
        currentState.withdraw(this, amount);
    }

    public void deposit(double amount) {
        currentState.deposit(this, amount);
    }

    public void ejectCard() {
        currentState.ejectCard(this);
    }
}

class IdleState implements ATMState {
    @Override
    public void insertCard(ATM atm, Card card) {
        if (card == null) {
            System.out.println("Invalid card");
            return;
        }

        card.resetAuthentication();
        card.setCurrentAccount(card.getAccount());
        atm.setState(new HasCardState(card));
        System.out.println("Card inserted. Please enter PIN.");
    }

    @Override
    public void enterPin(ATM atm, String pin) {
        System.out.println("Please insert a card first.");
    }

    @Override
    public void checkBalance(ATM atm) {
        System.out.println("Please insert a card first.");
    }

    @Override
    public void withdraw(ATM atm, double amount) {
        System.out.println("Please insert a card first.");
    }

    @Override
    public void deposit(ATM atm, double amount) {
        System.out.println("Please insert a card first.");
    }

    @Override
    public void ejectCard(ATM atm) {
        System.out.println("No card inserted.");
    }
}

class HasCardState implements ATMState {
    private final Card card;

    public HasCardState(Card card) {
        this.card = card;
    }

    @Override
    public void insertCard(ATM atm, Card newCard) {
        System.out.println("A card is already inserted.");
    }

    @Override
    public void enterPin(ATM atm, String pin) {
        if (card.validatePin(pin)) {
            atm.setState(new AuthenticatedState(card));
            System.out.println("PIN verified. Authentication successful.");
        } else {
            System.out.println("Incorrect PIN.");
        }
    }

    @Override
    public void checkBalance(ATM atm) {
        System.out.println("Please authenticate first.");
    }

    @Override
    public void withdraw(ATM atm, double amount) {
        System.out.println("Please authenticate first.");
    }

    @Override
    public void deposit(ATM atm, double amount) {
        System.out.println("Please authenticate first.");
    }

    @Override
    public void ejectCard(ATM atm) {
        card.resetAuthentication();
        atm.setState(new IdleState());
        System.out.println("Card ejected.");
    }
}

class AuthenticatedState implements ATMState {
    private final Card card;

    public AuthenticatedState(Card card) {
        this.card = card;
    }

    @Override
    public void insertCard(ATM atm, Card newCard) {
        System.out.println("A card is already inserted.");
    }

    @Override
    public void enterPin(ATM atm, String pin) {
        System.out.println("Already authenticated.");
    }

    @Override
    public void checkBalance(ATM atm) {
        System.out.println("Balance: Rs." + card.getCurrentAccount().getBalance());
    }

    @Override
    public void withdraw(ATM atm, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid withdrawal amount.");
            return;
        }

        BankAccount account = card.getCurrentAccount();

        if (amount > account.getBalance()) {
            System.out.println("Insufficient account balance.");
            return;
        }

        if (amount > atm.getCashAvailable()) {
            System.out.println("ATM has insufficient cash.");
            return;
        }

        account.withdraw(amount);
        atm.dispenseCash(amount);
        System.out.println("Please collect your cash: Rs." + amount);
    }

    @Override
    public void deposit(ATM atm, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid deposit amount.");
            return;
        }

        card.getCurrentAccount().deposit(amount);
        System.out.println("Deposit successful: Rs." + amount);
    }

    @Override
    public void ejectCard(ATM atm) {
        card.resetAuthentication();
        atm.setState(new IdleState());
        System.out.println("Card ejected. Thank you.");
    }
}

class Card {
    private final String cardNumber;
    private final String pin;
    private final BankAccount account;
    private BankAccount currentAccount;

    public Card(String cardNumber, String pin, BankAccount account) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.account = account;
    }

    public boolean validatePin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    public BankAccount getAccount() {
        return account;
    }

    public BankAccount getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(BankAccount account) {
        this.currentAccount = account;
    }

    public void resetAuthentication() {
        currentAccount = null;
    }
}

class BankAccount {
    private final String accountNumber;
    private double balance;

    public BankAccount(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public void withdraw(double amount) {
        balance -= amount;
    }

    public void deposit(double amount) {
        balance += amount;
    }
}

public class Main {
    public static void main(String[] args) {
        BankAccount account = new BankAccount("ACC101", 10000);
        Card card = new Card("CARD101", "1234", account);
        ATM atm = new ATM(5000);

        atm.checkBalance();

        atm.insertCard(card);
        atm.withdraw(1000);

        atm.enterPin("1234");
        atm.checkBalance();

        atm.withdraw(2000);
        atm.deposit(500);
        atm.checkBalance();

        atm.ejectCard();
        atm.checkBalance();
    }
}