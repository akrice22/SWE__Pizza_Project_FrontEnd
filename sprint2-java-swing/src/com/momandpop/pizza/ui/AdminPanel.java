package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.model.StaffUser;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import java.time.LocalDate;
import java.util.List;

/**
 * Password-protected Staff and Owner Management Dashboard reading from
 * {@code data/orders.txt}, {@code data/customers.txt}, and {@code data/staff.txt}.
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

    /** Shared file-based persistence store. */
    private final transient DataStore dataStore;

    /** Parent frame coordinator. */
    private final PizzaAppFrame frame;

    /** CardLayout switching between Orders, Customers, and Staff views. */
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

    /** Summary header displaying gross revenue, total tax, and total tips. */
    private final JLabel kpiSummaryLabel;

    /** Input field for enrolling a new staff username. */
    private final JTextField newStaffUserField;

    /** Input field for enrolling a new staff password. */
    private final JTextField newStaffPassField;

    /** Input field for enrolling a new staff full name. */
    private final JTextField newStaffNameField;

    /** Role selector for enrolling a new staff member. */
    private final JComboBox<String> newStaffRoleCombo;

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
                new String[]{"ID", "Name", "Phone", "Order Type", "Street Address", "Subdivision", "Intersection"}, 0) {
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

        this.kpiSummaryLabel = new JLabel("Gross Revenue: $0.00  •  Tips: $0.00  •  Tax: $0.00");
        this.newStaffUserField = Theme.createTextField(10);
        this.newStaffPassField = Theme.createTextField(10);
        this.newStaffNameField = Theme.createTextField(14);
        this.newStaffRoleCombo = new JComboBox<>(new String[]{"worker", "manager", "owner"});

        buildLayout();
        refreshAllTables();
    }

    /**
     * Assembles the Management Dashboard header, pill navigation tabs, and data tables.
     */
    private void buildLayout() {
        setLayout(new BorderLayout(12, 14));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

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

        ButtonGroup tabGroup = new ButtonGroup();
        tabGroup.add(ordersTabPill);
        tabGroup.add(customersTabPill);
        tabGroup.add(staffTabPill);

        ordersTabPill.addActionListener(e -> tabCardLayout.show(tabContentPanel, TAB_ORDERS));
        customersTabPill.addActionListener(e -> tabCardLayout.show(tabContentPanel, TAB_CUSTOMERS));
        staffTabPill.addActionListener(e -> tabCardLayout.show(tabContentPanel, TAB_STAFF));

        tabsRow.add(ordersTabPill);
        tabsRow.add(customersTabPill);
        tabsRow.add(staffTabPill);

        tabContentPanel.setOpaque(false);
        tabContentPanel.add(buildOrdersTab(), TAB_ORDERS);
        tabContentPanel.add(buildCustomersTab(), TAB_CUSTOMERS);
        tabContentPanel.add(buildStaffTab(), TAB_STAFF);
        tabCardLayout.show(tabContentPanel, TAB_ORDERS);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);
        centerContainer.add(tabsRow, BorderLayout.NORTH);
        centerContainer.add(tabContentPanel, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * Builds the Orders &amp; Transactions tab with a receipt inspection button.
     *
     * @return constructed orders tab {@link JPanel}
     */
    private JPanel buildOrdersTab() {
        Theme.RoundedCardPanel p = Theme.createCardPanel("All Store Transactions (data/orders.txt)");
        p.setLayout(new BorderLayout(0, 10));
        Theme.styleTable(ordersTable);
        JScrollPane scroll = new JScrollPane(ordersTable);
        Theme.styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton viewReceiptBtn = Theme.createPrimaryButton("Print / View Selected Order Receipt");
        viewReceiptBtn.addActionListener(e -> viewSelectedOrderReceipt());
        bottom.add(viewReceiptBtn);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    /**
     * Builds the read-only Customer Directory tab backed by {@code customers.txt}.
     *
     * @return constructed customers tab {@link JPanel}
     */
    private JPanel buildCustomersTab() {
        Theme.RoundedCardPanel p = Theme.createCardPanel("Registered Customers (data/customers.txt)");
        p.setLayout(new BorderLayout());
        Theme.styleTable(customersTable);
        JScrollPane scroll = new JScrollPane(customersTable);
        Theme.styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    /**
     * Builds the Staff Directory tab including password mask toggle and new employee enrollment.
     *
     * @return constructed staff tab {@link JPanel}
     */
    private JPanel buildStaffTab() {
        Theme.RoundedCardPanel p = Theme.createCardPanel("Staff Accounts & Enrollment (data/staff.txt)");
        p.setLayout(new BorderLayout(0, 10));
        Theme.styleTable(staffTable);
        JScrollPane scroll = new JScrollPane(staffTable);
        Theme.styleScrollPane(scroll);
        p.add(scroll, BorderLayout.CENTER);

        Theme.RoundedCardPanel enrollBar = Theme.createCardPanel("Enroll New Staff Account");
        enrollBar.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        Theme.styleComboBox(newStaffRoleCombo);

        enrollBar.add(Theme.createFieldLabel("USER:"));
        enrollBar.add(newStaffUserField);
        enrollBar.add(Theme.createFieldLabel("PASS:"));
        enrollBar.add(newStaffPassField);
        enrollBar.add(Theme.createFieldLabel("NAME:"));
        enrollBar.add(newStaffNameField);
        enrollBar.add(Theme.createFieldLabel("ROLE:"));
        enrollBar.add(newStaffRoleCombo);

        JButton addStaffBtn = Theme.createPrimaryButton("+ Enroll Staff");
        addStaffBtn.addActionListener(e -> enrollStaffMember());
        enrollBar.add(addStaffBtn);

        JButton togglePassBtn = Theme.createSecondaryButton("👁 Show / Hide Passwords");
        togglePassBtn.addActionListener(e -> {
            showStaffPasswords = !showStaffPasswords;
            refreshAllTables();
        });
        enrollBar.add(togglePassBtn);

        p.add(enrollBar, BorderLayout.SOUTH);
        return p;
    }

    /**
     * Reloads all three tables and recalculates revenue KPI metrics from disk.
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
    }

    /**
     * Opens a styled popup displaying the full thermal-style receipt for the selected order row.
     */
    private void viewSelectedOrderReceipt() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order row first.", "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String idCell = String.valueOf(ordersModel.getValueAt(row, 0)).replace("#", "").trim();
        int targetId = Integer.parseInt(idCell);
        for (OrderRecord rec : dataStore.loadOrders()) {
            if (rec.getOrderId() == targetId) {
                JTextArea area = new JTextArea(rec.formatReceiptText());
                area.setEditable(false);
                area.setFont(Theme.FONT_MONO);
                area.setBackground(Theme.RECEIPT_PAPER);
                area.setForeground(Theme.RECEIPT_INK);
                area.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
                JScrollPane sp = new JScrollPane(area);
                sp.setPreferredSize(new Dimension(480, 500));
                JOptionPane.showMessageDialog(this, sp, "Order #" + targetId + " Receipt", JOptionPane.PLAIN_MESSAGE);
                return;
            }
        }
    }

    /**
     * Validates and writes a new staff record into {@code data/staff.txt}.
     */
    private void enrollStaffMember() {
        String user = newStaffUserField.getText().trim().toLowerCase();
        String pass = newStaffPassField.getText().trim();
        String name = newStaffNameField.getText().trim();
        String role = String.valueOf(newStaffRoleCombo.getSelectedItem());

        if (user.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username, password, and full name.", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            StaffUser newUser = new StaffUser(0, user, pass, name, role, "555-000-0000",
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
}
