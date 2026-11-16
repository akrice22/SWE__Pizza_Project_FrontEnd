package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Step 1 screen handling 10-digit customer phone lookups from {@code data/customers.txt},
 * past order viewing, fulfillment type selection (Delivery vs. Pickup), and new customer entry.
 */
public class CustomerPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Shared data persistence manager. */
    private final transient DataStore dataStore;

    /** Parent frame coordinator. */
    private final PizzaAppFrame frame;

    /** Search bar input for returning customer phone number. */
    private final JTextField lookupPhoneField;

    /** Feedback banner displaying lookup results. */
    private final JLabel lookupStatusLabel;

    /** Customer full name input field. */
    private final JTextField nameField;

    /** Customer 10-digit phone number input field. */
    private final JTextField phoneField;

    /** Delivery street address input field. */
    private final JTextField streetField;

    /** Subdivision or complex input field. */
    private final JTextField subdivisionField;

    /** Nearest major intersection input field. */
    private final JTextField intersectionField;

    /** Toggle pill selecting delivery fulfillment ($3.99 fee). */
    private final Theme.TogglePillButton deliveryPill;

    /** Toggle pill selecting counter pickup fulfillment ($0.00 fee). */
    private final Theme.TogglePillButton pickupPill;

    /** List model displaying past orders for the looked-up customer. */
    private final DefaultListModel<String> pastOrdersModel;

    /**
     * Constructs the {@code CustomerPanel}.
     *
     * @param dataStore shared {@link DataStore} instance
     * @param frame     parent {@link PizzaAppFrame} coordinator
     */
    public CustomerPanel(DataStore dataStore, PizzaAppFrame frame) {
        this.dataStore = dataStore;
        this.frame = frame;
        this.lookupPhoneField = Theme.createTextField(18);
        this.lookupStatusLabel = new JLabel("Enter a 10-digit phone number (e.g., 5555555555) to autofill saved details.");
        this.nameField = Theme.createTextField(22);
        this.phoneField = Theme.createTextField(16);
        this.streetField = Theme.createTextField(26);
        this.subdivisionField = Theme.createTextField(18);
        this.intersectionField = Theme.createTextField(18);
        this.deliveryPill = new Theme.TogglePillButton("DELIVERY ($3.99 Fee)", true);
        this.pickupPill = new Theme.TogglePillButton("PICKUP (Free)", false);
        this.pastOrdersModel = new DefaultListModel<>();

        buildLayout();
    }

    /**
     * Assembles the lookup bar, customer form inputs, and past orders history list.
     */
    private void buildLayout() {
        setLayout(new BorderLayout(16, 14));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        // Top Header & Quick Lookup Card
        Theme.RoundedCardPanel lookupCard = Theme.createAmberCardPanel("Step 01 • Quick Lookup (Returning Customers)");
        lookupCard.setLayout(new BorderLayout(10, 8));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchRow.setOpaque(false);
        searchRow.add(Theme.createFieldLabel("PHONE NUMBER"));
        searchRow.add(lookupPhoneField);

        JButton findBtn = Theme.createPrimaryButton("Find My Info");
        findBtn.addActionListener(e -> performPhoneLookup(lookupPhoneField.getText()));
        searchRow.add(findBtn);

        JButton clearBtn = Theme.createSecondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());
        searchRow.add(clearBtn);

        lookupStatusLabel.setFont(Theme.FONT_BODY);
        lookupStatusLabel.setForeground(Theme.TEXT_MUTED);

        lookupCard.add(searchRow, BorderLayout.CENTER);
        lookupCard.add(lookupStatusLabel, BorderLayout.SOUTH);

        // Center split: Left = Form, Right = Past Orders
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 18, 0));
        centerSplit.setOpaque(false);
        centerSplit.add(buildCustomerFormCard());
        centerSplit.add(buildPastOrdersCard());

        // Bottom actions
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        JButton backBtn = Theme.createSecondaryButton("← Return to Home / Login");
        backBtn.addActionListener(e -> frame.showCard(PizzaAppFrame.CARD_LOGIN));

        JButton saveContinueBtn = Theme.createPrimaryButton("SAVE DETAILS & BUILD CUSTOM PIZZA →");
        saveContinueBtn.addActionListener(e -> saveAndProceed());

        footer.add(backBtn, BorderLayout.WEST);
        footer.add(saveContinueBtn, BorderLayout.EAST);

        add(lookupCard, BorderLayout.NORTH);
        add(centerSplit, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Builds the main customer contact and delivery address card.
     *
     * @return constructed form {@link JPanel}
     */
    private JPanel buildCustomerFormCard() {
        Theme.RoundedCardPanel card = Theme.createCardPanel("Delivery & Customer Info");
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        ButtonGroup group = new ButtonGroup();
        group.add(deliveryPill);
        group.add(pickupPill);

        JPanel typePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        typePanel.setOpaque(false);
        typePanel.add(deliveryPill);
        typePanel.add(pickupPill);

        deliveryPill.addActionListener(e -> updateAddressEnabledState());
        pickupPill.addActionListener(e -> updateAddressEnabledState());

        int row = 0;
        addFormRow(card, c, row++, "FULFILLMENT", typePanel);
        addFormRow(card, c, row++, "FULL NAME *", nameField);
        addFormRow(card, c, row++, "PHONE NUMBER *", phoneField);
        addFormRow(card, c, row++, "STREET ADDRESS", streetField);
        addFormRow(card, c, row++, "SUBDIVISION / APT", subdivisionField);
        addFormRow(card, c, row, "NEAREST INTERSECTION", intersectionField);

        return card;
    }

    /**
     * Builds the returning customer past-orders card.
     *
     * @return constructed history card {@link JPanel}
     */
    private JPanel buildPastOrdersCard() {
        Theme.RoundedCardPanel card = Theme.createCardPanel("Past Favorites (Order History)");
        card.setLayout(new BorderLayout(0, 10));

        JList<String> historyList = new JList<>(pastOrdersModel);
        historyList.setBackground(Theme.BG_INPUT);
        historyList.setForeground(Theme.TEXT_PRIMARY);
        historyList.setFont(Theme.FONT_MONO);
        historyList.setFixedCellHeight(30);
        JScrollPane scroll = new JScrollPane(historyList);
        Theme.styleScrollPane(scroll);

        JPanel tipBox = new JPanel();
        tipBox.setLayout(new BoxLayout(tipBox, BoxLayout.Y_AXIS));
        tipBox.setOpaque(false);
        JLabel tip1 = new JLabel("Sample Profiles in data/customers.txt:");
        tip1.setFont(Theme.FONT_LABEL);
        tip1.setForeground(Theme.ACCENT_AMBER);
        JLabel tip2 = new JLabel("• Jane Doe: 555-555-5555   • James Smith: 124-183-7581   • Juan Ramos: 555-123-4567");
        tip2.setFont(Theme.FONT_BODY);
        tip2.setForeground(Theme.TEXT_MUTED);
        tipBox.add(tip1);
        tipBox.add(Box.createVerticalStrut(3));
        tipBox.add(tip2);

        card.add(scroll, BorderLayout.CENTER);
        card.add(tipBox, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Helper to add a labeled input row inside a {@link GridBagLayout}.
     *
     * @param parent    target panel
     * @param c         constraints object
     * @param row       grid row index
     * @param labelText field caption
     * @param comp      input component
     */
    private void addFormRow(JPanel parent, GridBagConstraints c, int row, String labelText, Component comp) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.0;
        parent.add(Theme.createFieldLabel(labelText), c);

        c.gridx = 1;
        c.weightx = 1.0;
        parent.add(comp, c);
    }

    /**
     * Enables or disables delivery address inputs based on Delivery vs. Pickup mode.
     */
    private void updateAddressEnabledState() {
        boolean isDelivery = deliveryPill.isSelected();
        streetField.setEnabled(isDelivery);
        subdivisionField.setEnabled(isDelivery);
        intersectionField.setEnabled(isDelivery);
    }

    /**
     * Looks up a customer by 10-digit phone number in {@code data/customers.txt} and fills fields.
     *
     * @param rawPhone raw or formatted phone string
     */
    public void performPhoneLookup(String rawPhone) {
        String digits = Customer.normalizeDigits(rawPhone);
        lookupPhoneField.setText(digits);
        if (digits.length() != 10) {
            lookupStatusLabel.setForeground(Theme.STATUS_ERROR);
            lookupStatusLabel.setText("Invalid phone number. Please enter exactly 10 digits (e.g., 5555555555).");
            return;
        }

        Customer found = dataStore.findCustomerByPhone(digits);
        pastOrdersModel.clear();
        if (found == null) {
            lookupStatusLabel.setForeground(Theme.ACCENT_AMBER);
            lookupStatusLabel.setText("No prior profile found for " + digits + ". Fill out your details below to register.");
            phoneField.setText(digits);
            return;
        }

        populateFields(found);
        List<OrderRecord> history = dataStore.findOrdersByPhone(digits);
        for (OrderRecord rec : history) {
            pastOrdersModel.addElement(String.format("Order #%d • %s • $%.2f (%s) — %s",
                    rec.getOrderId(), rec.getTimestamp(), rec.getGrandTotal(),
                    rec.getPaymentMethod(), rec.getItemsSummary()));
        }
        if (history.isEmpty()) {
            pastOrdersModel.addElement("No prior orders on file yet.");
        }

        lookupStatusLabel.setForeground(Theme.STATUS_SUCCESS);
        lookupStatusLabel.setText("Welcome back, " + found.getName() + "! Loaded profile and " + history.size() + " past order(s).");
    }

    /**
     * Populates all customer input fields from a {@link Customer} instance.
     *
     * @param c customer profile
     */
    public void populateFields(Customer c) {
        if (c == null) {
            return;
        }
        nameField.setText(c.getName());
        phoneField.setText(c.getPhone());
        if ("pickup".equalsIgnoreCase(c.getOrderType())) {
            pickupPill.setSelected(true);
        } else {
            deliveryPill.setSelected(true);
        }
        streetField.setText(c.getStreetAddress());
        subdivisionField.setText(c.getSubdivision());
        intersectionField.setText(c.getNearestIntersection());
        updateAddressEnabledState();
    }

    /**
     * Clears all customer fields for a brand-new guest order.
     */
    public void clearForm() {
        lookupPhoneField.setText("");
        nameField.setText("");
        phoneField.setText("");
        streetField.setText("");
        subdivisionField.setText("");
        intersectionField.setText("");
        deliveryPill.setSelected(true);
        pastOrdersModel.clear();
        lookupStatusLabel.setForeground(Theme.TEXT_MUTED);
        lookupStatusLabel.setText("Enter a 10-digit phone number (e.g., 5555555555) to autofill saved details.");
        updateAddressEnabledState();
    }

    /**
     * Validates customer inputs, persists to {@code customers.txt},
     * updates the active session customer, and opens the pizza builder.
     */
    private void saveAndProceed() {
        String name = nameField.getText().trim();
        String digits = Customer.normalizeDigits(phoneField.getText());
        String orderType = deliveryPill.isSelected() ? "delivery" : "pickup";
        String street = streetField.getText().trim();
        String subdivision = subdivisionField.getText().trim();
        String intersection = intersectionField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the customer's full name.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (digits.length() != 10) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit phone number.", "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("delivery".equals(orderType) && street.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Street address is required for delivery orders.", "Missing Street Address", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer customer = new Customer(0, name, digits, orderType, street, subdivision, intersection);
        Customer saved = dataStore.saveOrUpdateCustomer(customer);
        frame.setActiveCustomer(saved);
        frame.showCard(PizzaAppFrame.CARD_MENU);
    }
}
