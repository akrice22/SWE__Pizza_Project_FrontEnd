package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.model.StaffUser;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Password-protected Staff and Owner Management Dashboard reading from
 * {@code data/orders.txt}, {@code data/customers.txt}, and {@code data/staff.txt},
 * including the Daily Business Summary Report, Customer Directory, and Cook Order Slips.
 */
public class AdminPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Card identifier for the Transactions tab. */
    private static final String TAB_ORDERS = "ORDERS";

    /** Card identifier for the Customer Directory tab. */
    private static final String TAB_CUSTOMERS = "CUSTOMERS";

    /** Card identifier for the Staff &amp; Workers tab. */
    private static final String TAB_STAFF = "STAFF";

    /** Card identifier for the Daily Business Summary Report tab. */
    private static final String TAB_SUMMARY = "SUMMARY";

    /** Shared file-based persistence store. */
    private final transient DataStore dataStore;

    /** Parent frame coordinator. */
    private final PizzaAppFrame frame;

    /** CardLayout switching between Orders, Customers, Staff, and Daily Summary views. */
    private final CardLayout tabCardLayout;

    /** Panel container holding tab views. */
    private final JPanel tabContentPanel;

    /** Table model backing the Orders &amp; Transactions tab. */
    private final DefaultTableModel ordersModel;

    /** Table displaying past orders. */
    private final JTable ordersTable;

    /** Table model backing the Customer Directory tab. */
    private final DefaultTableModel customersModel;

    /** Table displaying registered customers. */
    private final JTable customersTable;

    /** Table model backing the Staff Directory tab. */
    private final DefaultTableModel staffModel;

    /** Table displaying staff accounts. */
    private final JTable staffTable;

    /** Monospaced report viewer displaying the Daily Business Summary Report. */
    private final JTextArea dailySummaryArea;

    /** Summary header displaying gross revenue, total tax, and total tips. */
    private final JLabel kpiSummaryLabel;

    /** Input field for enrolling a new staff username. */
    private final JTextField newStaffUserField;

    /** Input field for enrolling a new staff password. */
    private final JTextField newStaffPassField;

    /** Input field for enrolling a new staff full name. */
    private final JTextField newStaffNameField;

    /** Selected role when enrolling a new staff account. */
    private String selectedNewStaffRole;

    /** Controls whether passwords in the staff table are masked or visible. */
    private boolean showStaffPasswords;

    /**
     * Constructs the {@code AdminPanel} and loads initial records from disk.
     *
     * @param dataStore shared {@link DataStore} instance
     * @param frame     parent {@link PizzaAppFrame} coordinator
     */
    public AdminPanel(DataStore dataStore, PizzaAppFrame frame) {
        this.dataStore = dataStore;
        this.frame = frame;
        this.tabCardLayout = new CardLayout();
        this.tabContentPanel = new JPanel(tabCardLayout);
        this.selectedNewStaffRole = "worker";
        this.showStaffPasswords = false;

        this.ordersModel = new DefaultTableModel(
                new String[]{"Order #", "Timestamp", "Customer", "Phone", "Type", "Payment", "Subtotal", "Tax", "Tip", "Total", "Items"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        this.ordersTable = new JTable(ordersModel);

        this.customersModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Phone", "Charge Acct", "Order Type", "Street Address", "Subdivision", "Intersection"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        this.customersTable = new JTable(customersModel);

        this.staffModel = new DefaultTableModel(
                new String[]{"ID", "Username", "Password", "Full Name", "Role", "Phone", "Email", "Date Hired", "Status"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        this.staffTable = new JTable(staffModel);

        this.dailySummaryArea = new JTextArea();
        this.kpiSummaryLabel = new JLabel("Gross Revenue: $0.00  •  Tips: $0.00  •  Tax: $0.00");
        this.newStaffUserField = Theme.createTextField(10);
        this.newStaffPassField = Theme.createTextField(10);
        this.newStaffNameField = Theme.createTextField(14);

        buildLayout();
        refreshAllTables();
    }

    /**
     * Assembles the Management Dashboard header, pill navigation tabs, and report tables.
     */
    private void buildLayout() {
        setLayout(new BorderLayout(12, 14));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // Top Header Card
        Theme.RoundedCardPanel header = Theme.createAmberCardPanel(null);
        header.setLayout(new BorderLayout(12, 10));

        JPanel titleStack = new JPanel(new BorderLayout(0, 2));
        titleStack.setOpaque(false);
        JLabel badge = new JLabel("● STAFF & OWNER TERMINAL (PASSWORD PROTECTED)");
        badge.setFont(Theme.FONT_LABEL);
        badge.setForeground(Theme.STATUS_SUCCESS);
        JLabel title = new JLabel("MOM & POP MANAGEMENT DASHBOARD");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        titleStack.add(badge, BorderLayout.NORTH);
        titleStack.add(title, BorderLayout.SOUTH);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);
        kpiSummaryLabel.setFont(Theme.FONT_MONO_BOLD);
        kpiSummaryLabel.setForeground(Theme.ACCENT_AMBER);
        rightControls.add(kpiSummaryLabel);

        JButton refreshBtn = Theme.createSecondaryButton("Reload .txt Files");
        refreshBtn.addActionListener(e -> refreshAllTables());
        rightControls.add(refreshBtn);

        JButton lockBtn = Theme.createDangerButton("🔒 Lock Terminal");
        lockBtn.addActionListener(e -> frame.logoutStaff());
        rightControls.add(lockBtn);

        header.add(titleStack, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);

        // Pill Tabs Row
        JPanel tabsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tabsRow.setOpaque(false);
        Theme.TogglePillButton ordersTabPill = new Theme.TogglePillButton("TRANSACTIONS & RECEIPTS", true);
        Theme.TogglePillButton customersTabPill = new Theme.TogglePillButton("CUSTOMER DIRECTORY", false);
        Theme.TogglePillButton staffTabPill = new Theme.TogglePillButton("STAFF & WORKERS", false);
        Theme.TogglePillButton summaryTabPill = new Theme.TogglePillButton("DAILY BUSINESS SUMMARY", false);

        ButtonGroup tabGroup = new ButtonGroup();
        tabGroup.add(ordersTabPill);
        tabGroup.add(customersTabPill);
        tabGroup.add(staffTabPill);
        tabGroup.add(summaryTabPill);

        ordersTabPill.addActionListener(e -> tabCardLayout.show(tabContentPanel, TAB_ORDERS));
        customersTabPill.addActionListener(e -> tabCardLayout.show(tabContentPanel, TAB_CUSTOMERS));
        staffTabPill.addActionListener(e -> tabCardLayout.show(tabContentPanel, TAB_STAFF));
        summaryTabPill.addActionListener(e -> tabCardLayout.show(tabContentPanel, TAB_SUMMARY));

        tabsRow.add(ordersTabPill);
        tabsRow.add(customersTabPill);
        tabsRow.add(staffTabPill);
        tabsRow.add(summaryTabPill);

        tabContentPanel.setOpaque(false);
        tabContentPanel.add(buildOrdersTab(), TAB_ORDERS);
        tabContentPanel.add(buildCustomersTab(), TAB_CUSTOMERS);
        tabContentPanel.add(buildStaffTab(), TAB_STAFF);
        tabContentPanel.add(buildSummaryTab(), TAB_SUMMARY);
        tabCardLayout.show(tabContentPanel, TAB_ORDERS);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);
        centerContainer.add(tabsRow, BorderLayout.NORTH);
        centerContainer.add(tabContentPanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * Builds the Orders &amp; Transactions tab with Customer Receipt and Cook Slip viewers.
     *
     * @return constructed orders tab {@link JPanel}
     */
    private JPanel buildOrdersTab() {
        Theme.RoundedCardPanel p = Theme.createTitledCard("Transaction Ledger", "All Store Orders (data/orders.txt)", false);
        JPanel content = p.getContentPane();
        content.setLayout(new BorderLayout(0, 10));
        Theme.styleTable(ordersTable);
        configureColumnWidths(ordersTable, new int[]{58, 145, 105, 100, 78, 72, 72, 62, 62, 72, 290});
        JScrollPane scroll = new JScrollPane(ordersTable);
        Theme.styleScrollPane(scroll);
        content.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        JButton viewSlipBtn = Theme.createSecondaryButton("👨‍🍳 View Cook Order Slip");
        viewSlipBtn.addActionListener(e -> viewSelectedOrderDocument(true));

        JButton viewReceiptBtn = Theme.createPrimaryButton("📄 View / Print Customer Receipt");
        viewReceiptBtn.addActionListener(e -> viewSelectedOrderDocument(false));

        bottom.add(viewSlipBtn);
        bottom.add(viewReceiptBtn);
        content.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    /**
     * Builds the read-only Customer Directory tab backed by {@code customers.txt}.
     *
     * @return constructed customers tab {@link JPanel}
     */
    private JPanel buildCustomersTab() {
        Theme.RoundedCardPanel p = Theme.createTitledCard("Customer Master File", "Registered Customers (data/customers.txt)", false);
        JPanel content = p.getContentPane();
        content.setLayout(new BorderLayout());
        Theme.styleTable(customersTable);
        configureColumnWidths(customersTable, new int[]{52, 120, 115, 95, 85, 220, 175, 175});
        JScrollPane scroll = new JScrollPane(customersTable);
        Theme.styleScrollPane(scroll);
        content.add(scroll, BorderLayout.CENTER);
        return p;
    }

    /**
     * Builds the Staff Directory tab including password mask toggle and new employee enrollment.
     *
     * @return constructed staff tab {@link JPanel}
     */
    private JPanel buildStaffTab() {
        Theme.RoundedCardPanel p = Theme.createTitledCard("Team Access", "Staff Accounts & Enrollment (data/staff.txt)", false);
        JPanel content = p.getContentPane();
        content.setLayout(new BorderLayout(0, 10));
        Theme.styleTable(staffTable);
        configureColumnWidths(staffTable, new int[]{52, 95, 100, 130, 85, 110, 210, 100, 85});
        JScrollPane scroll = new JScrollPane(staffTable);
        Theme.styleScrollPane(scroll);
        content.add(scroll, BorderLayout.CENTER);

        Theme.RoundedCardPanel enrollBar = Theme.createTitledCard(null, "Enroll New Staff Member", false);
        JPanel barContent = enrollBar.getContentPane();
        barContent.setLayout(new BorderLayout(10, 8));

        JPanel inputsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        inputsRow.setOpaque(false);
        inputsRow.add(Theme.createStackedInput("USERNAME", newStaffUserField));
        inputsRow.add(Theme.createStackedInput("PASSWORD", newStaffPassField));
        inputsRow.add(Theme.createStackedInput("FULL NAME", newStaffNameField));

        JPanel rolePills = new JPanel(new GridLayout(1, 3, 4, 0));
        rolePills.setOpaque(false);
        ButtonGroup roleGroup = new ButtonGroup();
        for (String r : new String[]{"worker", "manager", "owner"}) {
            Theme.TogglePillButton pill = new Theme.TogglePillButton(r.toUpperCase(), "worker".equals(r));
            pill.addActionListener(e -> selectedNewStaffRole = r);
            roleGroup.add(pill);
            rolePills.add(pill);
        }
        inputsRow.add(Theme.createStackedInput("ROLE", rolePills));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton togglePassBtn = Theme.createSecondaryButton("👁 Show / Hide Passwords");
        togglePassBtn.addActionListener(e -> {
            showStaffPasswords = !showStaffPasswords;
            refreshAllTables();
        });
        JButton addStaffBtn = Theme.createPrimaryButton("+ Enroll Staff");
        addStaffBtn.addActionListener(e -> enrollStaffMember());
        btnRow.add(togglePassBtn);
        btnRow.add(addStaffBtn);

        barContent.add(inputsRow, BorderLayout.CENTER);
        barContent.add(btnRow, BorderLayout.SOUTH);

        content.add(enrollBar, BorderLayout.SOUTH);
        return p;
    }

    /**
     * Builds the Daily Business Summary Report tab displaying totals by payment type and order volume.
     *
     * @return constructed summary report tab {@link JPanel}
     */
    private JPanel buildSummaryTab() {
        Theme.RoundedCardPanel p = Theme.createTitledCard("Management Reporting", "Daily Business Summary Report", false);
        JPanel content = p.getContentPane();
        content.setLayout(new BorderLayout(0, 10));

        dailySummaryArea.setEditable(false);
        dailySummaryArea.setBackground(Theme.RECEIPT_PAPER);
        dailySummaryArea.setForeground(Theme.RECEIPT_INK);
        dailySummaryArea.setFont(Theme.FONT_MONO);
        dailySummaryArea.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JScrollPane scroll = new JScrollPane(dailySummaryArea);
        Theme.styleScrollPane(scroll);
        content.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton saveBtn = Theme.createSecondaryButton("💾 Save Summary .txt");
        saveBtn.addActionListener(e -> saveDailySummaryFile());

        JButton printBtn = Theme.createPrimaryButton("🖨️ Print Daily Summary");
        printBtn.addActionListener(e -> printDailySummary());

        actions.add(saveBtn);
        actions.add(printBtn);
        content.add(actions, BorderLayout.SOUTH);
        return p;
    }

    /**
     * Reloads all tables and regenerates the Daily Business Summary Report from disk.
     */
    public final void refreshAllTables() {
        ordersModel.setRowCount(0);
        double gross = 0.0;
        double taxTotal = 0.0;
        double tipTotal = 0.0;

        List<OrderRecord> orders = dataStore.loadOrders();
        for (int i = orders.size() - 1; i >= 0; i--) {
            OrderRecord o = orders.get(i);
            gross += o.getGrandTotal();
            taxTotal += o.getTax();
            tipTotal += o.getTip();
            ordersModel.addRow(new Object[]{
                    "#" + o.getOrderId(),
                    o.getTimestamp(),
                    o.getCustomerName(),
                    o.getCustomerPhone(),
                    o.getOrderType().toUpperCase(),
                    o.getPaymentMethod().toUpperCase(),
                    Theme.formatCurrency(o.getSubtotal()),
                    Theme.formatCurrency(o.getTax()),
                    Theme.formatCurrency(o.getTip()),
                    Theme.formatCurrency(o.getGrandTotal()),
                    o.getItemsSummary()
            });
        }

        kpiSummaryLabel.setText(String.format("GROSS: $%.2f  •  TIPS: $%.2f  •  TAX: $%.2f",
                gross, tipTotal, taxTotal));

        customersModel.setRowCount(0);
        for (Customer c : dataStore.loadCustomers()) {
            customersModel.addRow(new Object[]{
                    "#" + c.getId(),
                    c.getName(),
                    c.getFormattedPhone(),
                    c.getChargeAccountType(),
                    c.getOrderType().toUpperCase(),
                    c.getStreetAddress(),
                    c.getSubdivision(),
                    c.getNearestIntersection()
            });
        }

        staffModel.setRowCount(0);
        for (StaffUser u : dataStore.loadStaffUsers()) {
            staffModel.addRow(new Object[]{
                    "#" + u.getId(),
                    u.getUsername(),
                    showStaffPasswords ? u.getPassword() : "••••••••",
                    u.getFullName(),
                    u.getRole().toUpperCase(),
                    u.getPhone(),
                    u.getEmail(),
                    u.getDateHired(),
                    u.isActive() ? "ACTIVE" : "ARCHIVED"
            });
        }

        dailySummaryArea.setText(dataStore.generateDailySummaryReport());
        dailySummaryArea.setCaretPosition(0);
    }

    /**
     * Opens a popup displaying either the Customer Receipt or Cook Order Slip for the selected order.
     *
     * @param cookSlip {@code true} to show the kitchen cook slip; {@code false} for customer receipt
     */
    private void viewSelectedOrderDocument(boolean cookSlip) {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order row first.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String idCell = String.valueOf(ordersModel.getValueAt(row, 0)).replace("#", "").trim();
        int targetId = Integer.parseInt(idCell);
        for (OrderRecord rec : dataStore.loadOrders()) {
            if (rec.getOrderId() == targetId) {
                String content = cookSlip ? rec.formatCookOrderSlipText() : rec.formatReceiptText();
                String title = cookSlip ? "Order #" + targetId + " Cook Order Slip" : "Order #" + targetId + " Customer Receipt";
                JTextArea area = new JTextArea(content);
                area.setEditable(false);
                area.setFont(Theme.FONT_MONO);
                area.setBackground(Theme.RECEIPT_PAPER);
                area.setForeground(Theme.RECEIPT_INK);
                area.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
                JScrollPane sp = new JScrollPane(area);
                sp.setPreferredSize(new Dimension(490, 520));
                JOptionPane.showMessageDialog(this, sp, title, JOptionPane.PLAIN_MESSAGE);
                return;
            }
        }
    }

    /**
     * Exports the Daily Business Summary Report to {@code data/daily_summary.txt}.
     */
    private void saveDailySummaryFile() {
        Path out = dataStore.getDataDir().resolve("daily_summary.txt");
        try {
            Files.write(out, dailySummaryArea.getText().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Saved Daily Summary to: " + out.toAbsolutePath(), "Report Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Prints the Daily Business Summary Report.
     */
    private void printDailySummary() {
        try {
            dailySummaryArea.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates and writes a new staff record into {@code data/staff.txt}.
     */
    private void enrollStaffMember() {
        String user = newStaffUserField.getText().trim().toLowerCase();
        String pass = newStaffPassField.getText().trim();
        String name = newStaffNameField.getText().trim();

        if (user.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username, password, and full name.", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            StaffUser newUser = new StaffUser(0, user, pass, name, selectedNewStaffRole, "555-000-0000",
                    user + "@momandpoppizza.com", LocalDate.now().toString(), true);
            dataStore.saveStaffUser(newUser);
            newStaffUserField.setText("");
            newStaffPassField.setText("");
            newStaffNameField.setText("");
            refreshAllTables();
            JOptionPane.showMessageDialog(this, "Enrolled staff account '" + user + "' into data/staff.txt.", "Staff Enrolled", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Duplicate Username", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Applies balanced preferred widths to a {@link JTable} column model so timestamps and descriptions remain readable.
     *
     * @param table  target {@link JTable}
     * @param widths array of preferred widths matching column indices
     */
    private static void configureColumnWidths(JTable table, int[] widths) {
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }
}
