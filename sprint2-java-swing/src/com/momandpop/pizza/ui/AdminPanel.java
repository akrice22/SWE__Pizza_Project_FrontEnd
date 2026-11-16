package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.model.StaffUser;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

/**
 * Staff and Owner administrative view reading live records from
 * {@code data/orders.txt}, {@code data/customers.txt}, and {@code data/staff.txt}.
 */
public class AdminPanel extends JPanel {

    /** Shared file-based persistence store. */
    private final DataStore dataStore;

    /** Parent frame coordinator. */
    private final PizzaAppFrame frame;

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

    /**
     * Constructs the {@code AdminPanel} and loads initial records from disk.
     *
     * @param dataStore shared {@link DataStore} instance
     * @param frame     parent {@link PizzaAppFrame} coordinator
     */
    public AdminPanel(DataStore dataStore, PizzaAppFrame frame) {
        this.dataStore = dataStore;
        this.frame = frame;
        this.ordersModel = new DefaultTableModel(
                new String[]{"Order #", "Timestamp", "Customer", "Phone", "Type", "Payment", "Subtotal", "Tax", "Tip", "Total", "Items"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        this.ordersTable = new JTable(ordersModel);

        this.customersModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Phone", "Order Type", "Street Address", "Subdivision", "Intersection"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        this.customersTable = new JTable(customersModel);

        this.staffModel = new DefaultTableModel(
                new String[]{"ID", "Username", "Full Name", "Role", "Phone", "Email", "Date Hired", "Active"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        this.staffTable = new JTable(staffModel);

        this.kpiSummaryLabel = new JLabel("Gross Revenue: $0.00 | Tax: $0.00 | Tips: $0.00");
        this.newStaffUserField = Theme.createTextField(10);
        this.newStaffPassField = Theme.createTextField(10);
        this.newStaffNameField = Theme.createTextField(14);
        this.newStaffRoleCombo = new JComboBox<>(new String[]{"worker", "manager", "owner"});

        buildLayout();
        refreshAllTables();
    }

    /**
     * Assembles the tabbed administrative workspace.
     */
    private void buildLayout() {
        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel header = Theme.createCardPanel(null);
        header.setLayout(new BorderLayout());

        JLabel title = new JLabel("STAFF & MANAGEMENT PORTAL (Local File Store)");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.ACCENT_AMBER);

        kpiSummaryLabel.setFont(Theme.FONT_MONO_BOLD);
        kpiSummaryLabel.setForeground(Theme.STATUS_SUCCESS);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);
        rightControls.add(kpiSummaryLabel);

        JButton refreshBtn = Theme.createSecondaryButton("Reload Files");
        refreshBtn.addActionListener(e -> refreshAllTables());
        rightControls.add(refreshBtn);

        JButton backBtn = Theme.createPrimaryButton("Start Customer Order ->");
        backBtn.addActionListener(e -> frame.showCard(PizzaAppFrame.CARD_CUSTOMER));
        rightControls.add(backBtn);

        header.add(title, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_LABEL);
        tabs.addTab("Transactions & Orders (orders.txt)", buildOrdersTab());
        tabs.addTab("Customer Directory (customers.txt)", buildCustomersTab());
        tabs.addTab("Staff & Employees (staff.txt)", buildStaffTab());

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Builds the Orders &amp; Transactions tab with a receipt inspection button.
     *
     * @return constructed orders tab {@link JPanel}
     */
    private JPanel buildOrdersTab() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG_CARD);
        styleTable(ordersTable);
        p.add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton viewReceiptBtn = Theme.createPrimaryButton("View Selected Order Receipt");
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
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_CARD);
        styleTable(customersTable);
        p.add(new JScrollPane(customersTable), BorderLayout.CENTER);
        return p;
    }

    /**
     * Builds the Staff Directory tab including quick staff enrollment into {@code staff.txt}.
     *
     * @return constructed staff tab {@link JPanel}
     */
    private JPanel buildStaffTab() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.BG_CARD);
        styleTable(staffTable);
        p.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        JPanel enrollBar = Theme.createCardPanel("Enroll New Staff Account into staff.txt");
        enrollBar.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        enrollBar.add(Theme.createFieldLabel("Username:"));
        enrollBar.add(newStaffUserField);
        enrollBar.add(Theme.createFieldLabel("Password:"));
        enrollBar.add(newStaffPassField);
        enrollBar.add(Theme.createFieldLabel("Full Name:"));
        enrollBar.add(newStaffNameField);
        enrollBar.add(Theme.createFieldLabel("Role:"));
        enrollBar.add(newStaffRoleCombo);

        JButton addStaffBtn = Theme.createPrimaryButton("+ Enroll Staff");
        addStaffBtn.addActionListener(e -> enrollStaffMember());
        enrollBar.add(addStaffBtn);

        p.add(enrollBar, BorderLayout.SOUTH);
        return p;
    }

    /**
     * Reloads all three tables and updates revenue summary metrics from disk.
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
                    o.getOrderType(),
                    o.getPaymentMethod(),
                    Theme.formatCurrency(o.getSubtotal()),
                    Theme.formatCurrency(o.getTax()),
                    Theme.formatCurrency(o.getTip()),
                    Theme.formatCurrency(o.getGrandTotal()),
                    o.getItemsSummary()
            });
        }

        kpiSummaryLabel.setText(String.format("Gross Revenue: $%.2f  |  Tax: $%.2f  |  Tips: $%.2f",
                gross, taxTotal, tipTotal));

        customersModel.setRowCount(0);
        for (Customer c : dataStore.loadCustomers()) {
            customersModel.addRow(new Object[]{
                    "#" + c.getId(),
                    c.getName(),
                    c.getFormattedPhone(),
                    c.getOrderType(),
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
                    u.getFullName(),
                    u.getRole().toUpperCase(),
                    u.getPhone(),
                    u.getEmail(),
                    u.getDateHired(),
                    u.isActive() ? "Active" : "Archived"
            });
        }
    }

    /**
     * Opens a popup dialog displaying the formatted receipt for the selected order row.
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
                JScrollPane sp = new JScrollPane(area);
                sp.setPreferredSize(new java.awt.Dimension(460, 460));
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

    /**
     * Applies consistent dark stone styling and readable row height to a {@link JTable}.
     *
     * @param table table component to style
     */
    private void styleTable(JTable table) {
        table.setBackground(Theme.BG_INPUT);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(24);
        table.setGridColor(Theme.BORDER_STONE);
        table.getTableHeader().setFont(Theme.FONT_LABEL);
    }
}
