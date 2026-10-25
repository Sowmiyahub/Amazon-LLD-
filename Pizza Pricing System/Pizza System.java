
import java.util.*;

// 1. Main entity
class Pizza {
    private final PizzaSize size;
    private final List<Topping> toppings;

    public Pizza(PizzaSize size, List<Topping> toppings) {
        this.size = Objects.requireNonNull(size);
        this.toppings = new ArrayList<>(toppings);
    }

    public PizzaSize getSize() {
        return size;
    }

    public List<Topping> getToppings() {
        return Collections.unmodifiableList(toppings);
    }

    public double calculatePrice(PricingStrategy strategy) {
        return strategy.calculatePrice(this);
    }
}

// 2. Pricing contract
interface PricingStrategy {
    double calculatePrice(Pizza pizza);
}

// 3. Standard pricing implementation
class StandardPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(Pizza pizza) {
        double price = pizza.getSize().getBasePrice();

        for (Topping topping : pizza.getToppings()) {
            price += topping.getPrice();
        }

        return price;
    }
}

// 4. Example alternative: premium pricing
class PremiumPricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(Pizza pizza) {
        double price = pizza.getSize().getBasePrice();

        for (Topping topping : pizza.getToppings()) {
            price += topping.getPrice() * 1.5;
        }

        return price;
    }
}

// 5. Pizza sizes and base prices
enum PizzaSize {
    SMALL(150.0),
    MEDIUM(250.0),
    LARGE(350.0);

    private final double basePrice;

    PizzaSize(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }
}

// 6. Topping entity
class Topping {
    private final String name;
    private final double price;

    public Topping(String name, double price) {
        if (name == null || name.trim().isEmpty() || price < 0) {
            throw new IllegalArgumentException("Invalid topping");
        }

        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

// 7. Example usage
public class Main {
    public static void main(String[] args) {
        List<Topping> toppings = Arrays.asList(
            new Topping("Cheese", 40.0),
            new Topping("Olives", 30.0)
        );

        Pizza pizza = new Pizza(PizzaSize.MEDIUM, toppings);

        PricingStrategy standard = new StandardPricingStrategy();
        System.out.println("Standard price: "
                + pizza.calculatePrice(standard));

        PricingStrategy premium = new PremiumPricingStrategy();
        System.out.println("Premium price: "
                + pizza.calculatePrice(premium));
    }
}
