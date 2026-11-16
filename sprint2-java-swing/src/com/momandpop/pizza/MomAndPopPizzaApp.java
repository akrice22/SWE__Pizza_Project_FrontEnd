package com.momandpop.pizza;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.MenuCatalog;
import com.momandpop.pizza.model.OrderItem;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.model.StaffUser;
import com.momandpop.pizza.storage.DataStore;
import com.momandpop.pizza.ui.PizzaAppFrame;
import com.momandpop.pizza.ui.Theme;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if (args != null && args.length >= 2 && "--render-screenshots".equalsIgnoreCase(args[0])) {
            renderScreenshots(dataStore, Paths.get(args[1]));
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
        if (jane == null || !"Jane Doe".equals(jane.getName()) || !"Visa".equalsIgnoreCase(jane.getChargeAccountType())) {
            throw new IllegalStateException("Customer lookup or charge account verification for 555-555-5555 failed.");
        }

        if (MenuCatalog.findToppingByName("Extra Mozzarella Cheese") == null
                || MenuCatalog.findToppingByName("Artisan Pepperoni") == null) {
            throw new IllegalStateException("Required sample toppings (Extra Mozzarella Cheese / Artisan Pepperoni) missing.");
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
                OrderItem.createPizza("Medium (12\")", "Thin & Crispy", "Normal Sauce",
                        Arrays.asList("Artisan Pepperoni", "Extra Mozzarella Cheese"), 17.24),
                OrderItem.createBeverage("Mexican Cane Sugar Coca-Cola", "Large (32 oz)", 4.00, 1)
        );
        OrderRecord preview = new OrderRecord(99, "2026-11-16 10:00:00", jane.getName(), jane.getPhone(),
                "delivery", jane.getFullAddressSummary(), "Check", "Check #2045 ($30.00)",
                totals[0], totals[1], totals[2], totals[3], totals[4], "Jane Doe", "Smoke test summary", sampleItems);
        String receipt = preview.formatReceiptText();
        String cookSlip = preview.formatCookOrderSlipText();
        String dailyReport = dataStore.generateDailySummaryReport();
        if (!receipt.contains("GRAND TOTAL:") || !receipt.contains("Check #2045")
                || !cookSlip.contains("KITCHEN COOK SLIP") || !cookSlip.contains("EXTRA MOZZARELLA CHEESE")
                || !dailyReport.contains("DAILY BUSINESS SUMMARY")) {
            throw new IllegalStateException("Report formatting verification failed.");
        }

        System.out.println("SMOKE TEST PASSED: dataDir=" + dataStore.getDataDir().toAbsolutePath()
                + ", staffCount=" + dataStore.loadStaffUsers().size()
                + ", customerCount=" + dataStore.loadCustomers().size()
                + ", orderCount=" + dataStore.loadOrders().size());
    }

    /**
     * Renders all 5 application screens offscreen to PNG images for visual quality inspection.
     *
     * @param dataStore initialized {@link DataStore} instance
     * @param outDir    output directory for generated PNG images
     */
    private static void renderScreenshots(DataStore dataStore, Path outDir) {
        configureLookAndFeel();
        try {
            Files.createDirectories(outDir);
            SwingUtilities.invokeAndWait(() -> {
                PizzaAppFrame frame = new PizzaAppFrame(dataStore);
                frame.setSize(1280, 860);
                frame.addNotify();
                Container rootContent = frame.getContentPane();
                rootContent.setBounds(0, 0, 1280, 860);
                layoutComponentTree(rootContent);

                // Prepare populated state across customer, menu, checkout, and admin views
                Customer jane = dataStore.findCustomerByPhone("5555555555");
                frame.getCustomerPanel().performPhoneLookup("5555555555");
                frame.getMenuOrderPanel().addItemDirectlyToCart(OrderItem.createPizza(
                        "Medium (12\")", "Thin & Crispy", "Normal Sauce",
                        Arrays.asList("Artisan Pepperoni", "Extra Mozzarella Cheese"), 17.24));
                frame.getMenuOrderPanel().addItemDirectlyToCart(OrderItem.createBeverage(
                        "San Pellegrino Limonata", "Medium (20 oz)", 3.00, 1));
                List<OrderRecord> orders = dataStore.loadOrders();
                if (!orders.isEmpty()) {
                    frame.getCheckoutReceiptPanel().previewOrderRecord(orders.get(orders.size() - 1));
                }

                String[] cards = {
                        PizzaAppFrame.CARD_LOGIN,
                        PizzaAppFrame.CARD_CUSTOMER,
                        PizzaAppFrame.CARD_MENU,
                        PizzaAppFrame.CARD_CHECKOUT,
                        PizzaAppFrame.CARD_ADMIN
                };
                for (int i = 0; i < cards.length; i++) {
                    if (PizzaAppFrame.CARD_ADMIN.equals(cards[i])) {
                        StaffUser owner = dataStore.authenticateStaff("owner", "pizza123");
                        if (owner != null) {
                            frame.onStaffAuthenticated(owner);
                        }
                    } else {
                        frame.showCard(cards[i]);
                    }
                    rootContent.setBounds(0, 0, 1280, 860);
                    layoutComponentTree(rootContent);

                    BufferedImage img = new BufferedImage(1280, 860, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = img.createGraphics();
                    g2.setColor(Theme.BG_APP);
                    g2.fillRect(0, 0, 1280, 860);
                    rootContent.printAll(g2);
                    g2.dispose();

                    try {
                        ImageIO.write(img, "png", outDir.resolve((i + 1) + "_" + cards[i].toLowerCase() + ".png").toFile());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
                frame.dispose();
                if (jane != null) {
                    System.out.println("RENDERED 5 SCREENSHOTS TO " + outDir.toAbsolutePath());
                }
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Screenshot rendering failed", ex);
        }
    }

    /**
     * Recursively validates and lays out a Swing container tree for offscreen rendering.
     *
     * @param container root container to lay out recursively
     */
    private static void layoutComponentTree(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layoutComponentTree((Container) child);
            }
        }
    }
}
