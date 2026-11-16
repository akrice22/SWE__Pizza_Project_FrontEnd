package com.momandpop.pizza;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.MenuCatalog;
import com.momandpop.pizza.model.OrderItem;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.model.StaffUser;
import com.momandpop.pizza.storage.DataStore;
import com.momandpop.pizza.ui.PizzaAppFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Main entry point for the Mom &amp; Pop Pizzeria standalone Java Swing desktop application.
 * Supports both interactive GUI launch and automated {@code --smoke-test} headless verification.
 */
public final class MomAndPopPizzaApp {

    /**
     * Private constructor to prevent instantiation of the entry-point class.
     */
    private MomAndPopPizzaApp() {
    }

    /**
     * Application main entry point. Pass {@code --smoke-test} to verify persistence,
     * pricing math, and constraint rules in headless mode; otherwise opens the Swing GUI.
     *
     * @param args optional command-line arguments
     */
    public static void main(String[] args) {
        DataStore dataStore = new DataStore();

        if (args != null && args.length > 0 && "--smoke-test".equalsIgnoreCase(args[0])) {
            runSmokeTest(dataStore);
            return;
        }

        configureLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            PizzaAppFrame frame = new PizzaAppFrame(dataStore);
            frame.setVisible(true);
        });
    }

    /**
     * Applies cross-platform look-and-feel so dark theme colors render consistently on macOS and Windows.
     */
    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default Swing L&F if unavailable
        }
    }

    /**
     * Executes headless automated verification checks across data initialization,
     * staff authentication, customer phone lookup, menu catalog rules, pricing math,
     * and order persistence.
     *
     * @param dataStore initialized {@link DataStore} instance
     */
    private static void runSmokeTest(DataStore dataStore) {
        if (!Files.exists(dataStore.getDataDir().resolve("staff.txt"))
                || !Files.exists(dataStore.getDataDir().resolve("customers.txt"))
                || !Files.exists(dataStore.getDataDir().resolve("orders.txt"))) {
            throw new IllegalStateException("Expected data/*.txt files were not created.");
        }

        StaffUser owner = dataStore.authenticateStaff("owner", "pizza123");
        if (owner == null || !"owner".equalsIgnoreCase(owner.getRole())) {
            throw new IllegalStateException("Default owner authentication failed.");
        }

        Customer jane = dataStore.findCustomerByPhone("555-555-5555");
        if (jane == null || !"Jane Doe".equals(jane.getName())) {
            throw new IllegalStateException("Customer lookup for 555-555-5555 failed.");
        }

        if (MenuCatalog.PIZZA_SIZES.size() < 4
                || MenuCatalog.CRUST_OPTIONS.size() < 3
                || (MenuCatalog.VEGGIE_TOPPINGS.size() + MenuCatalog.MEAT_TOPPINGS.size()) < 8
                || MenuCatalog.BEVERAGES.size() < 5
                || MenuCatalog.BEVERAGE_SIZES.size() < 3
                || MenuCatalog.MAX_TOPPINGS_PER_PIZZA != 4) {
            throw new IllegalStateException("Menu catalog failed Sprint 2 requirement counts.");
        }

        double[] totals = DataStore.calculateTotals(20.00, "delivery", 3.00);
        if (Math.abs(totals[1] - 3.99) > 0.001 || Math.abs(totals[2] - 1.60) > 0.001 || Math.abs(totals[4] - 28.59) > 0.001) {
            throw new IllegalStateException("Tax/Delivery/Total calculation mismatch.");
        }

        List<OrderItem> sampleItems = Arrays.asList(
                OrderItem.createPizza("Large (14\")", "Thin & Crispy", "Normal Sauce",
                        Arrays.asList("Artisan Pepperoni", "Fresh Mushrooms"), 19.99),
                OrderItem.createBeverage("Mexican Cane Sugar Coca-Cola", "Large (32 oz)", 4.00, 1)
        );
        OrderRecord preview = new OrderRecord(99, "2026-11-16 10:00:00", jane.getName(), jane.getPhone(),
                "delivery", jane.getFullAddressSummary(), "Check", "Check #2045 ($30.00)",
                totals[0], totals[1], totals[2], totals[3], totals[4], "Jane Doe", "Smoke test summary", sampleItems);
        String receipt = preview.formatReceiptText();
        if (!receipt.contains("GRAND TOTAL:") || !receipt.contains("Check #2045")) {
            throw new IllegalStateException("Receipt formatting verification failed.");
        }

        System.out.println("SMOKE TEST PASSED: dataDir=" + dataStore.getDataDir().toAbsolutePath()
                + ", staffCount=" + dataStore.loadStaffUsers().size()
                + ", customerCount=" + dataStore.loadCustomers().size()
                + ", orderCount=" + dataStore.loadOrders().size());
    }
}
