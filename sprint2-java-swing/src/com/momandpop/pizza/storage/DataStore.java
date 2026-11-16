package com.momandpop.pizza.storage;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.MenuCatalog;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.model.StaffUser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Local plain-text persistence manager handling reading and writing of
 * {@code staff.txt}, {@code customers.txt}, and {@code orders.txt} inside
 * the local {@code data/} directory with zero database or network dependencies.
 */
public class DataStore {

    /** Directory containing local plain-text persistence files. */
    private final Path dataDir;

    /** File path for staff user accounts ({@code staff.txt}). */
    private final Path staffFile;

    /** File path for customer profiles ({@code customers.txt}). */
    private final Path customersFile;

    /** File path for completed order records ({@code orders.txt}). */
    private final Path ordersFile;

    /**
     * Constructs a {@code DataStore} using the default {@code data/} directory
     * relative to the current working directory (or {@code sprint2-java-swing/data}).
     */
    public DataStore() {
        this(resolveDefaultDataDir());
    }

    /**
     * Constructs a {@code DataStore} backed by a specific directory path.
     *
     * @param dataDir directory path where {@code .txt} files will be stored
     */
    public DataStore(Path dataDir) {
        this.dataDir = dataDir;
        this.staffFile = dataDir.resolve("staff.txt");
        this.customersFile = dataDir.resolve("customers.txt");
        this.ordersFile = dataDir.resolve("orders.txt");
        ensureInitialized();
    }

    /**
     * Resolves the {@code data/} folder depending on whether the application is launched
     * from inside {@code sprint2-java-swing/} or from the parent repository root.
     *
     * @return resolved {@link Path} to the data directory
     */
    private static Path resolveDefaultDataDir() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path subProjectData = cwd.resolve("sprint2-java-swing").resolve("data");
        if (Files.exists(cwd.resolve("sprint2-java-swing"))) {
            return subProjectData;
        }
        return cwd.resolve("data");
    }

    /**
     * Ensures the {@code data/} folder and default sample data files exist on disk.
     * Missing files are automatically created with initial sample records.
     */
    public final void ensureInitialized() {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            if (!Files.exists(staffFile)) {
                writeDefaultStaffFile();
            }
            if (!Files.exists(customersFile)) {
                writeDefaultCustomersFile();
            }
            if (!Files.exists(ordersFile)) {
                writeDefaultOrdersFile();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to initialize local data directory: " + dataDir, ex);
        }
    }

    /**
     * Returns the resolved data directory path.
     *
     * @return data directory {@link Path}
     */
    public Path getDataDir() {
        return dataDir;
    }

    /**
     * Writes default staff credentials (including {@code owner / pizza123}) to {@code staff.txt}.
     *
     * @throws IOException if file creation fails
     */
    private void writeDefaultStaffFile() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# id|username|password|fullName|role|phone|email|dateHired|active");
        lines.add(new StaffUser(1, "owner", "pizza123", "Store Owner", "owner",
                "555-111-2222", "owner@momandpoppizza.com", "2020-01-15", true).toFileLine());
        lines.add(new StaffUser(2, "manager1", "pizza123", "Amy Rice", "manager",
                "555-222-3333", "amy@momandpoppizza.com", "2021-06-01", true).toFileLine());
        lines.add(new StaffUser(3, "worker1", "pizza123", "John Dough", "worker",
                "555-333-4444", "john@momandpoppizza.com", "2023-09-10", true).toFileLine());
        Files.write(staffFile, lines, StandardCharsets.UTF_8);
    }

    /**
     * Writes default sample customers to {@code customers.txt}.
     *
     * @throws IOException if file creation fails
     */
    private void writeDefaultCustomersFile() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name|phone|orderType|streetAddress|subdivision|nearestIntersection");
        lines.add(new Customer(1, "Jane Doe", "5555555555", "delivery",
                "123 Oak Street", "Amber Ridge Apt 4B", "Main St & 4th Ave").toFileLine());
        lines.add(new Customer(2, "James Smith", "1241837581", "delivery",
                "555 Freemont Circle", "Bethesda", "Oak Rd & Pine St").toFileLine());
        lines.add(new Customer(3, "Juan Ramos", "5551234567", "delivery",
                "105 Ardenlee Dr", "Oakridge", "Maple & 5th").toFileLine());
        Files.write(customersFile, lines, StandardCharsets.UTF_8);
    }

    /**
     * Writes default sample historical orders to {@code orders.txt}.
     *
     * @throws IOException if file creation fails
     */
    private void writeDefaultOrdersFile() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# orderId|timestamp|customerName|customerPhone|orderType|deliveryAddress|paymentMethod|paymentDetails|subtotal|deliveryFee|tax|tip|grandTotal|signature|itemsSummary");
        lines.add(new OrderRecord(1, "2026-11-08 20:36:12", "James Smith", "1241837581",
                "delivery", "555 Freemont Circle, Bethesda", "Credit", "Card ending in 3456",
                26.69, 3.99, 2.14, 4.00, 36.82, "James Smith",
                "Pizza [Medium (12\"), Classic Hand-Tossed, Normal Sauce, Toppings: Artisan Pepperoni, Green Bell Peppers, Fresh Mushrooms] ($17.99) ;; Drink [1x San Pellegrino Limonata - Medium (20 oz)] ($3.00)",
                null).toFileLine());
        lines.add(new OrderRecord(2, "2026-11-08 21:12:30", "Jane Doe", "5555555555",
                "delivery", "123 Oak Street, Amber Ridge Apt 4B", "Cash", "Tendered $35.00",
                19.94, 3.99, 1.60, 2.99, 28.52, "Jane Doe",
                "Pizza [Medium (12\"), Classic Hand-Tossed, Normal Sauce, Toppings: Artisan Pepperoni, Kalamata Black Olives] ($16.99) ;; Drink [1x San Pellegrino Limonata - Medium (20 oz)] ($2.95)",
                null).toFileLine());
        Files.write(ordersFile, lines, StandardCharsets.UTF_8);
    }

    /**
     * Authenticates a staff member against {@code staff.txt}.
     *
     * @param username entered username (case-insensitive)
     * @param password entered password
     * @return matching active {@link StaffUser}, or {@code null} if invalid
     */
    public StaffUser authenticateStaff(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        String targetUser = username.trim().toLowerCase();
        for (StaffUser user : loadStaffUsers()) {
            if (user.isActive()
                    && user.getUsername().equalsIgnoreCase(targetUser)
                    && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Reads all staff users from {@code staff.txt}.
     *
     * @return list of {@link StaffUser} records
     */
    public List<StaffUser> loadStaffUsers() {
        List<StaffUser> users = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(staffFile, StandardCharsets.UTF_8)) {
                StaffUser user = StaffUser.fromFileLine(line);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Error reading staff.txt", ex);
        }
        return users;
    }

    /**
     * Appends or saves a new staff user to {@code staff.txt}, assigning a new sequential ID if needed.
     *
     * @param user staff user to persist
     * @return persisted {@link StaffUser} with assigned ID
     */
    public StaffUser saveStaffUser(StaffUser user) {
        List<StaffUser> users = loadStaffUsers();
        int maxId = 0;
        for (StaffUser u : users) {
            if (u.getId() > maxId) {
                maxId = u.getId();
            }
            if (u.getUsername().equalsIgnoreCase(user.getUsername())) {
                throw new IllegalArgumentException("Username '" + user.getUsername() + "' already exists.");
            }
        }
        if (user.getId() <= 0) {
            user.setId(maxId + 1);
        }
        users.add(user);
        writeStaffUsers(users);
        return user;
    }

    /**
     * Writes the complete staff list to {@code staff.txt}.
     *
     * @param users list of staff users
     */
    private void writeStaffUsers(List<StaffUser> users) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|username|password|fullName|role|phone|email|dateHired|active");
        for (StaffUser u : users) {
            lines.add(u.toFileLine());
        }
        try {
            Files.write(staffFile, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Error writing staff.txt", ex);
        }
    }

    /**
     * Reads all customer profiles from {@code customers.txt}.
     *
     * @return list of {@link Customer} records
     */
    public List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(customersFile, StandardCharsets.UTF_8)) {
                Customer customer = Customer.fromFileLine(line);
                if (customer != null) {
                    customers.add(customer);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Error reading customers.txt", ex);
        }
        return customers;
    }

    /**
     * Finds a customer record matching a 10-digit phone number.
     *
     * @param rawPhone raw or formatted phone input
     * @return matching {@link Customer}, or {@code null} if not found
     */
    public Customer findCustomerByPhone(String rawPhone) {
        String digits = Customer.normalizeDigits(rawPhone);
        if (digits.isEmpty()) {
            return null;
        }
        for (Customer c : loadCustomers()) {
            if (c.getPhone().equals(digits)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Inserts a new customer or updates an existing customer matching the phone number in {@code customers.txt}.
     *
     * @param customer customer record to save
     * @return persisted {@link Customer} with assigned ID
     */
    public Customer saveOrUpdateCustomer(Customer customer) {
        List<Customer> customers = loadCustomers();
        String digits = Customer.normalizeDigits(customer.getPhone());
        customer.setPhone(digits);

        int maxId = 0;
        int matchIndex = -1;
        for (int i = 0; i < customers.size(); i++) {
            Customer existing = customers.get(i);
            if (existing.getId() > maxId) {
                maxId = existing.getId();
            }
            if (existing.getPhone().equals(digits)) {
                matchIndex = i;
            }
        }

        if (matchIndex >= 0) {
            customer.setId(customers.get(matchIndex).getId());
            customers.set(matchIndex, customer);
        } else {
            if (customer.getId() <= 0) {
                customer.setId(maxId + 1);
            }
            customers.add(customer);
        }

        List<String> lines = new ArrayList<>();
        lines.add("# id|name|phone|orderType|streetAddress|subdivision|nearestIntersection");
        for (Customer c : customers) {
            lines.add(c.toFileLine());
        }
        try {
            Files.write(customersFile, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Error writing customers.txt", ex);
        }
        return customer;
    }

    /**
     * Reads all completed orders from {@code orders.txt}.
     *
     * @return list of {@link OrderRecord} objects
     */
    public List<OrderRecord> loadOrders() {
        List<OrderRecord> orders = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(ordersFile, StandardCharsets.UTF_8)) {
                OrderRecord record = OrderRecord.fromFileLine(line);
                if (record != null) {
                    orders.add(record);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Error reading orders.txt", ex);
        }
        return orders;
    }

    /**
     * Finds all historical orders matching a customer's 10-digit phone number.
     *
     * @param rawPhone raw or formatted phone number
     * @return list of matching {@link OrderRecord} objects
     */
    public List<OrderRecord> findOrdersByPhone(String rawPhone) {
        String digits = Customer.normalizeDigits(rawPhone);
        List<OrderRecord> matching = new ArrayList<>();
        if (digits.isEmpty()) {
            return matching;
        }
        for (OrderRecord order : loadOrders()) {
            if (Customer.normalizeDigits(order.getCustomerPhone()).equals(digits)) {
                matching.add(order);
            }
        }
        return matching;
    }

    /**
     * Assigns a sequential order ID and saves a completed order to {@code orders.txt}.
     *
     * @param order completed order record
     * @return persisted {@link OrderRecord} with assigned order ID
     */
    public OrderRecord saveOrder(OrderRecord order) {
        List<OrderRecord> orders = loadOrders();
        int maxId = 0;
        for (OrderRecord existing : orders) {
            if (existing.getOrderId() > maxId) {
                maxId = existing.getOrderId();
            }
        }
        if (order.getOrderId() <= 0) {
            order.setOrderId(maxId + 1);
        }
        orders.add(order);

        List<String> lines = new ArrayList<>();
        lines.add("# orderId|timestamp|customerName|customerPhone|orderType|deliveryAddress|paymentMethod|paymentDetails|subtotal|deliveryFee|tax|tip|grandTotal|signature|itemsSummary");
        for (OrderRecord r : orders) {
            lines.add(r.toFileLine());
        }
        try {
            Files.write(ordersFile, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Error writing orders.txt", ex);
        }
        return order;
    }

    /**
     * Computes subtotal, delivery fee ($3.99 for delivery), 8% sales tax, tip, and grand total.
     *
     * @param subtotal  order subtotal in USD
     * @param orderType "delivery" or "pickup"
     * @param tipAmount tip amount in USD
     * @return array of 5 doubles: {@code [subtotal, deliveryFee, tax, tip, grandTotal]}
     */
    public static double[] calculateTotals(double subtotal, String orderType, double tipAmount) {
        double cleanSub = MenuCatalog.round2(Math.max(0.0, subtotal));
        double deliveryFee = "delivery".equalsIgnoreCase(orderType) ? MenuCatalog.DELIVERY_FEE : 0.0;
        double tax = MenuCatalog.round2(cleanSub * MenuCatalog.TAX_RATE);
        double cleanTip = MenuCatalog.round2(Math.max(0.0, tipAmount));
        double grandTotal = MenuCatalog.round2(cleanSub + deliveryFee + tax + cleanTip);
        return new double[]{cleanSub, deliveryFee, tax, cleanTip, grandTotal};
    }
}
