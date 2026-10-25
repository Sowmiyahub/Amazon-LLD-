import java.util.*;

class CategoryService {
    private Map<String, Category> categories = new HashMap<>();

    public Category createCategory(String id, String name, String parentId) {
        if (categories.containsKey(id))
            throw new IllegalArgumentException("Category already exists");

        Category parent = null;

        if (parentId != null) {
            parent = categories.get(parentId);
            if (parent == null)
                throw new IllegalArgumentException("Parent category not found");
        }

        Category category = new Category(id, name, parent);
        categories.put(id, category);

        if (parent != null)
            parent.subcategories.add(category);

        return category;
    }

    public void addProduct(String categoryId, String product) {
        Category category = categories.get(categoryId);

        if (category == null)
            throw new IllegalArgumentException("Category not found");

        category.products.add(product);
    }

    public void printHierarchy(String categoryId, int depth) {
        Category category = categories.get(categoryId);
        if (category == null) return;

        System.out.println("  ".repeat(depth) + category.name);

        for (String product : category.products)
            System.out.println("  ".repeat(depth + 1) + "- " + product);

        for (Category child : category.subcategories)
            printHierarchy(child.id, depth + 1);
    }
}

class Category {
    String id;
    String name;
    Category parent;
    List<Category> subcategories = new ArrayList<>();
    List<String> products = new ArrayList<>();

    Category(String id, String name, Category parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }
}

public class Main {
    public static void main(String[] args) {
        CategoryService service = new CategoryService();

        service.createCategory("1", "Electronics", null);
        service.createCategory("2", "Mobiles", "1");
        service.createCategory("3", "Laptops", "1");
        service.createCategory("4", "Android Phones", "2");

        service.addProduct("4", "Samsung Galaxy");
        service.addProduct("3", "MacBook");

        service.printHierarchy("1", 0);
    }
}