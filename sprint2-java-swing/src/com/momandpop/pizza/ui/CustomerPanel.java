package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
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

    /** Shared data persistence manager. */
    private final DataStore dataStore;

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

    /** Radio button selecting delivery fulfillment ($3.99 fee). */
    private final JRadioButton deliveryRadio;

    /** Radio button selecting counter pickup fulfillment ($0.00 fee). */
    private final JRadioButton pickupRadio;

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
        this.lookupPhoneField = Theme.createTextField(16);
        this.lookupStatusLabel = new JLabel("Enter a 10-digit phone number (e.g. 5555555555) to autofill returning details.");
        this.nameField = Theme.createTextField(24);
        this.phoneField = Theme.createTextField(18);
        this.streetField = Theme.createTextField(28);
        this.subdivisionField = Theme.createTextField(20);
        this.intersectionField = Theme.createTextField(20);
        this.deliveryRadio = new JRadioButton("Delivery ($3.99 Fee)", true);
        this.pickupRadio = new JRadioButton("Pickup (No Delivery Fee)", false);
        this.pastOrdersModel = new DefaultListModel<>();

        buildLayout();
    }

    /**
     * Assembles the lookup bar, customer form inputs, and past orders history list.
     */
    private void buildLayout() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        // Top Lookup Bar
        JPanel lookupCard = Theme.createCardPanel("Step 1A: Quick Lookup (Returning Customers)");
        lookupCard.setLayout(new BorderLayout(10, 8));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchRow.setOpaque(false);
        searchRow.add(Theme.createFieldLabel("Phone Number:"));
        searchRow.add(lookupPhoneField);

        JButton findBtn = Theme.createPrimaryButton("Find My Info");
        findBtn.addActionListener(e -> performPhoneLookup(lookupPhoneField.getText()));
        searchRow.add(findBtn);

        JButton clearBtn = Theme.createSecondaryButton("Clear / New Customer");
        clearBtn.addActionListener(e -> clearForm());
        searchRow.add(clearBtn);

        lookupStatusLabel.setFont(Theme.FONT_BODY);
        lookupStatusLabel.setForeground(Theme.TEXT_MUTED);

        lookupCard.add(searchRow, BorderLayout.NORTH);
        lookupCard.add(lookupStatusLabel, BorderLayout.SOUTH);

        // Center split: Left = Form, Right = Past Orders
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 18, 0));
        centerSplit.setOpaque(false);
        centerSplit.add(buildCustomerFormCard());
        centerSplit.add(buildPastOrdersCard());

        // Bottom actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        JButton backBtn = Theme.createSecondaryButton("<- Back to Login");
        backBtn.addActionListener(e -> frame.showCard(PizzaAppFrame.CARD_LOGIN));

        JButton saveContinueBtn = Theme.createPrimaryButton("Save Customer Details & Build Pizza ->");
        saveContinueBtn.addActionListener(e -> saveAndProceed());

        footer.add(backBtn);
        footer.add(saveContinueBtn);

        add(lookupCard, BorderLayout.NORTH);
        add(centerSplit, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Builds the main customer contact and address form card.
     *
     * @return constructed form {@link JPanel}
     */
    private JPanel buildCustomerFormCard() {
        JPanel card = Theme.createCardPanel("Step 1B: Customer & Delivery Details");
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        ButtonGroup group = new ButtonGroup();
        group.add(deliveryRadio);
        group.add(pickupRadio);
        styleRadio(deliveryRadio);
        styleRadio(pickupRadio);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        typePanel.setOpaque(false);
        typePanel.add(deliveryRadio);
        typePanel.add(pickupRadio);

        deliveryRadio.addActionListener(e -> updateAddressEnabledState());
        pickupRadio.addActionListener(e -> updateAddressEnabledState());

        int row = 0;
        addFormRow(card, c, row++, "Order Type:", typePanel);
        addFormRow(card, c, row++, "Full Name *:", nameField);
        addFormRow(card, c, row++, "10-Digit Phone *:", phoneField);
        addFormRow(card, c, row++, "Street Address:", streetField);
        addFormRow(card, c, row++, "Subdivision / Complex:", subdivisionField);
        addFormRow(card, c, row, "Nearest Intersection:", intersectionField);

        return card;
    }

    /**
     * Builds the returning customer past-orders history card.
     *
     * @return constructed history card {@link JPanel}
     */
    private JPanel buildPastOrdersCard() {
        JPanel card = Theme.createCardPanel("Past Orders (customers.txt / orders.txt)");
        card.setLayout(new BorderLayout(0, 10));

        JList<String> historyList = new JList<>(pastOrdersModel);
        historyList.setBackground(Theme.BG_INPUT);
        historyList.setForeground(Theme.TEXT_PRIMARY);
        historyList.setFont(Theme.FONT_MONO);
        JScrollPane scroll = new JScrollPane(historyList);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_STONE, 1));

        JLabel tip = new JLabel("Tip: Sample returning customers are 555-555-5555 (Jane Doe) & 124-183-7581 (James Smith).");
        tip.setFont(Theme.FONT_BODY);
        tip.setForeground(Theme.TEXT_MUTED);

        card.add(scroll, BorderLayout.CENTER);
        card.add(tip, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Helper to add a labeled input component row into a {@link GridBagLayout}.
     *
     * @param parent    target panel
     * @param c         constraints object
     * @param row       grid row index
     * @param labelText field caption
     * @param comp      input component
     */
    private void addFormRow(JPanel parent, GridBagConstraints c, int row, String labelText, java.awt.Component comp) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0.0;
        parent.add(Theme.createFieldLabel(labelText), c);

        c.gridx = 1;
        c.weightx = 1.0;
        parent.add(comp, c);
    }

    /**
     * Styles a radio button with dark stone aesthetics.
     *
     * @param rb radio button to style
     */
    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(Theme.TEXT_PRIMARY);
        rb.setFont(Theme.FONT_BODY);
    }

    /**
     * Enables or disables street address fields depending on Delivery vs. Pickup selection.
     */
    private void updateAddressEnabledState() {
        boolean isDelivery = deliveryRadio.isSelected();
        streetField.setEnabled(isDelivery);
        subdivisionField.setEnabled(isDelivery);
        intersectionField.setEnabled(isDelivery);
    }

    /**
     * Looks up a customer by 10-digit phone number in {@code data/customers.txt} and populates form fields.
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
            lookupStatusLabel.setText("No profile found for " + digits + ". Fill out your details below to register.");
            phoneField.setText(digits);
            return;
        }

        populateFields(found);
        List<OrderRecord> history = dataStore.findOrdersByPhone(digits);
        for (OrderRecord rec : history) {
            pastOrdersModel.addElement(String.format("Order #%d | %s | $%.2f (%s) - %s",
                    rec.getOrderId(), rec.getTimestamp(), rec.getGrandTotal(),
                    rec.getPaymentMethod(), rec.getItemsSummary()));
        }
        if (history.isEmpty()) {
            pastOrdersModel.addElement("No past orders on file yet.");
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
            pickupRadio.setSelected(true);
        } else {
            deliveryRadio.setSelected(true);
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
        deliveryRadio.setSelected(true);
        pastOrdersModel.clear();
        lookupStatusLabel.setForeground(Theme.TEXT_MUTED);
        lookupStatusLabel.setText("Enter a 10-digit phone number (e.g. 5555555555) to autofill returning details.");
        updateAddressEnabledState();
    }

    /**
     * Validates customer inputs, persists the record to {@code customers.txt},
     * updates the active session customer, and switches to the menu builder.
     */
    private void saveAndProceed() {
        String name = nameField.getText().trim();
        String digits = Customer.normalizeDigits(phoneField.getText());
        String orderType = deliveryRadio.isSelected() ? "delivery" : "pickup";
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
