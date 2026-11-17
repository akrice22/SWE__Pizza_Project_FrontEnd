package com.momandpop.pizza.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Central catalog holding all menu options, pricing constants, tax rate, and delivery fees
 * migrated from {@code src/menuData.js} and {@code src/App.jsx}.
 */
public final class MenuCatalog {

    /** Sales tax rate applied to food and beverage subtotals (8%). */
    public static final double TAX_RATE = 0.08;

    /** Flat delivery fee applied when fulfillment is set to delivery ($3.99). */
    public static final double DELIVERY_FEE = 3.99;

    /** Maximum number of toppings permitted per custom pizza. */
    public static final int MAX_TOPPINGS_PER_PIZZA = 4;

    /** Available pizza sizes (4 distinct sizes). */
    public static final List<MenuOption> PIZZA_SIZES = Collections.unmodifiableList(Arrays.asList(
            new MenuOption("small", "Small (10\")", 10.99, "Personal 6-slice pie", "size"),
            new MenuOption("medium", "Medium (12\")", 13.99, "Great for 2 people (8 slices)", "size"),
            new MenuOption("large", "Large (14\")", 16.99, "Family favorite (8 large slices)", "size"),
            new MenuOption("xlarge", "X-Large (16\")", 19.99, "Party size (12 slices)", "size")
    ));

    /** Available pizza crust styles (4 distinct crusts). */
    public static final List<MenuOption> CRUST_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            new MenuOption("hand-tossed", "Classic Hand-Tossed", 0.00, "Traditional", "crust"),
            new MenuOption("thin-crispy", "Thin & Crispy", 0.00, "Extra Crunch", "crust"),
            new MenuOption("stone-thick", "Stone-Baked Thick", 1.50, "Chewy & Airy", "crust"),
            new MenuOption("garlic-parm", "Garlic Butter Herb", 1.00, "House Special", "crust")
    ));

    /** Available sauce intensity levels (3 levels). */
    public static final List<String> SAUCE_LEVELS = Collections.unmodifiableList(Arrays.asList(
            "Light Sauce",
            "Normal Sauce",
            "Extra Sauce"
    ));

    /** Garden vegetable &amp; cheese toppings (7 distinct toppings). */
    public static final List<MenuOption> VEGGIE_TOPPINGS = Collections.unmodifiableList(Arrays.asList(
            new MenuOption("extra-cheese", "Extra Mozzarella Cheese", 1.50, "Artisan Cheese", "veggie"),
            new MenuOption("mushrooms", "Fresh Mushrooms", 1.25, "Garden Veggie", "veggie"),
            new MenuOption("red-onions", "Caramelized Red Onion", 1.00, "Garden Veggie", "veggie"),
            new MenuOption("bell-peppers", "Green Bell Peppers", 1.00, "Garden Veggie", "veggie"),
            new MenuOption("black-olives", "Kalamata Black Olives", 1.25, "Garden Veggie", "veggie"),
            new MenuOption("spinach", "Baby Spinach", 1.25, "Garden Veggie", "veggie"),
            new MenuOption("roma-tomatoes", "Sliced Roma Tomatoes", 1.25, "Garden Veggie", "veggie")
    ));

    /** Savory meat toppings (6 distinct toppings). */
    public static final List<MenuOption> MEAT_TOPPINGS = Collections.unmodifiableList(Arrays.asList(
            new MenuOption("pepperoni", "Artisan Pepperoni", 1.75, "Savory Meat", "meat"),
            new MenuOption("italian-sausage", "Spicy Italian Sausage", 1.75, "Savory Meat", "meat"),
            new MenuOption("bacon", "Smoked Crispy Bacon", 2.00, "Savory Meat", "meat"),
            new MenuOption("grilled-chicken", "Herbed Grilled Chicken", 2.00, "Savory Meat", "meat"),
            new MenuOption("prosciutto", "Dry-Cured Prosciutto", 2.50, "Savory Meat", "meat"),
            new MenuOption("meatballs", "Handcrafted Beef Meatballs", 2.00, "Savory Meat", "meat")
    ));

    /** Craft beverage options (6 distinct beverages). */
    public static final List<MenuOption> BEVERAGES = Collections.unmodifiableList(Arrays.asList(
            new MenuOption("mexican-coke", "Mexican Cane Sugar Coca-Cola", 2.50, "Craft Soda", "beverage"),
            new MenuOption("san-pellegrino-lemon", "San Pellegrino Limonata", 2.25, "Sparkling Citrus", "beverage"),
            new MenuOption("san-pellegrino-blood-orange", "San Pellegrino Aranciata Rossa", 2.25, "Blood Orange Sparkling", "beverage"),
            new MenuOption("craft-root-beer", "Hearth-Draft Root Beer", 2.25, "Pure Cane & Vanilla", "beverage"),
            new MenuOption("iced-tea", "Unsweetened Black Peach Iced Tea", 2.00, "Cold-brewed daily", "beverage"),
            new MenuOption("sparkling-water", "Mountain Spring Sparkling Water", 1.75, "Crisp Mineral Water", "beverage")
    ));

    /** Beverage size multipliers/upcharges (3 distinct sizes). */
    public static final List<MenuOption> BEVERAGE_SIZES = Collections.unmodifiableList(Arrays.asList(
            new MenuOption("bev-small", "Small (12 oz)", 0.00, "Standard bottle/cup", "bev_size"),
            new MenuOption("bev-medium", "Medium (20 oz)", 0.75, "Regular fountain/bottle", "bev_size"),
            new MenuOption("bev-large", "Large (32 oz)", 1.50, "Large fountain/pitcher", "bev_size")
    ));

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MenuCatalog() {
    }

    /**
     * Rounds a monetary amount to two decimal places using standard half-up rounding.
     *
     * @param value raw currency amount
     * @return value rounded to two decimal places
     */
    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Looks up a topping option by its display name across both vegetable and meat lists.
     *
     * @param name display name of the topping
     * @return matching {@code MenuOption}, or {@code null} if not found
     */
    public static MenuOption findToppingByName(String name) {
        for (MenuOption opt : VEGGIE_TOPPINGS) {
            if (opt.getName().equalsIgnoreCase(name)) {
                return opt;
            }
        }
        for (MenuOption opt : MEAT_TOPPINGS) {
            if (opt.getName().equalsIgnoreCase(name)) {
                return opt;
            }
        }
        return null;
    }

    /**
     * Represents a single selectable option on the restaurant menu.
     */
    public static class MenuOption {

        /** Unique identifier key for the option. */
        private final String id;

        /** Human-readable display label. */
        private final String name;

        /** Unit price or price surcharge in USD. */
        private final double price;

        /** Short description or category note. */
        private final String description;

        /** Grouping category identifier. */
        private final String category;

        /**
         * Constructs a {@code MenuOption}.
         *
         * @param id          unique identifier
         * @param name        display name
         * @param price       price or surcharge in USD
         * @param description descriptive tag
         * @param category    category identifier
         */
        public MenuOption(String id, String name, double price, String description, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.description = description;
            this.category = category;
        }

        /**
         * Returns the unique identifier.
         *
         * @return option ID
         */
        public String getId() {
            return id;
        }

        /**
         * Returns the display name.
         *
         * @return option name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the price or upcharge in USD.
         *
         * @return price in dollars
         */
        public double getPrice() {
            return price;
        }

        /**
         * Returns the short description.
         *
         * @return description text
         */
        public String getDescription() {
            return description;
        }

        /**
         * Returns the grouping category.
         *
         * @return category identifier
         */
        public String getCategory() {
            return category;
        }

        /**
         * Returns a formatted label including price for UI dropdowns and lists.
         *
         * @return formatted label string
         */
        @Override
        public String toString() {
            if (price <= 0.0) {
                return name + " (Included)";
            }
            return String.format("%s ($%.2f)", name, price);
        }
    }
}
