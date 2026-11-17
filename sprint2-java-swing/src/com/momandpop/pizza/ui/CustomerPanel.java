package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.OrderItem;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Step 1 screen matching Figures 6, 7A, and 7B: 10-digit customer phone lookup,
 * charge account pill selectors (Visa/MasterCard), stacked delivery inputs, and past orders quick reorder.
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

    /** Pill toggles selecting charge account type (Visa, MasterCard, Discover, Amex, None). */
    private final List<Theme.TogglePillButton> chargePills;

    /** Currently selected charge account type label. */
    private String selectedChargeAccount;

    /** Toggle pill selecting delivery fulfillment ($3.99 fee). */
    private final Theme.TogglePillButton deliveryPill;

    /** Toggle pill selecting counter pickup fulfillment ($0.00 fee). */
    private final Theme.TogglePillButton pickupPill;

    /** List model displaying past orders for the looked-up customer. */
    private final DefaultListModel<String> pastOrdersModel;

    /** Visual list of past orders. */
    private final JList<String> historyList;

    /** Cached past orders corresponding to entries in {@code pastOrdersModel}. */
    private transient List<OrderRecord> loadedPastOrders;

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
        this.nameField = Theme.createTextField(20);
        this.phoneField = Theme.createTextField(16);
        this.streetField = Theme.createTextField(26);
        this.subdivisionField = Theme.createTextField(18);
        this.intersectionField = Theme.createTextField(18);
        this.chargePills = new ArrayList<>();
        this.selectedChargeAccount = "Visa";
        this.deliveryPill = new Theme.TogglePillButton("DELIVERY ($3.99 Fee)", true);
        this.pickupPill = new Theme.TogglePillButton("PICKUP (Free)", false);
        this.pastOrdersModel = new DefaultListModel<>();
        this.historyList = new JList<>(pastOrdersModel);
        this.loadedPastOrders = Collections.emptyList();

        buildLayout();
    }

    /**
     * Assembles the lookup bar, stacked customer form card, and past orders history card.
     */
    private void buildLayout() {
        setLayout(new BorderLayout(16, 14));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        // Top Header & Quick Lookup Card
        Theme.RoundedCardPanel lookupCard = Theme.createTitledCard("Step 01", "⚡ Quick Lookup (Returning Customers)", true);
        JPanel lookupBody = lookupCard.getContentPane();
        lookupBody.setLayout(new BorderLayout(10, 8));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        searchRow.add(lookupPhoneField, BorderLayout.CENTER);

        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBtns.setOpaque(false);
        JButton findBtn = Theme.createPrimaryButton("Find My Info");
        findBtn.addActionListener(e -> performPhoneLookup(lookupPhoneField.getText()));
        JButton clearBtn = Theme.createSecondaryButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());
        searchBtns.add(findBtn);
        searchBtns.add(clearBtn);
        searchRow.add(searchBtns, BorderLayout.EAST);

        lookupStatusLabel.setFont(Theme.FONT_BODY);
        lookupStatusLabel.setForeground(Theme.TEXT_MUTED);

        lookupBody.add(searchRow, BorderLayout.CENTER);
        lookupBody.add(lookupStatusLabel, BorderLayout.SOUTH);

        // Center split: Left = Form, Right = Past Orders
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 18, 0));
        centerSplit.setOpaque(false);
        centerSplit.add(buildCustomerFormCard());
        centerSplit.add(buildPastOrdersCard());

        // Bottom actions
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JPanel leftFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFooter.setOpaque(false);
        JButton backBtn = Theme.createSecondaryButton("← Return Home");
        backBtn.addActionListener(e -> frame.showCard(PizzaAppFrame.CARD_LOGIN));
        JButton updateOnlyBtn = Theme.createSecondaryButton("Update & Save Profile Only");
        updateOnlyBtn.addActionListener(e -> saveProfileOnly());
        leftFooter.add(backBtn);
        leftFooter.add(updateOnlyBtn);

        JButton saveContinueBtn = Theme.createPrimaryButton("SAVE DETAILS & START CUSTOM ORDER →");
        saveContinueBtn.addActionListener(e -> saveAndProceed());

        footer.add(leftFooter, BorderLayout.WEST);
        footer.add(saveContinueBtn, BorderLayout.EAST);

        add(lookupCard, BorderLayout.NORTH);
        add(centerSplit, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Builds the stacked modern customer intake card matching Figure 6.
     *
     * @return constructed form {@link JPanel}
     */
    private JPanel buildCustomerFormCard() {
        Theme.RoundedCardPanel card = Theme.createTitledCard("Customer Master Record", "Delivery & Contact Info", false);
        JPanel content = card.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Fulfillment Pills
        ButtonGroup group = new ButtonGroup();
        group.add(deliveryPill);
        group.add(pickupPill);

        JPanel typePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        typePanel.setOpaque(false);
        typePanel.add(deliveryPill);
        typePanel.add(pickupPill);
        deliveryPill.addActionListener(e -> updateAddressEnabledState());
        pickupPill.addActionListener(e -> updateAddressEnabledState());
        JPanel typeStack = Theme.createStackedInput("ORDER FULFILLMENT", typePanel);
        typeStack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Name & Phone Row
        JPanel namePhoneRow = new JPanel(new GridLayout(1, 2, 12, 0));
        namePhoneRow.setOpaque(false);
        namePhoneRow.setAlignmentX(LEFT_ALIGNMENT);
        namePhoneRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        namePhoneRow.add(Theme.createStackedInput("FULL NAME *", nameField));
        namePhoneRow.add(Theme.createStackedInput("PHONE NUMBER (10 DIGITS) *", phoneField));

        // Charge Account Pills (Zero JComboBox)
        String[] chargeOptions = {"Visa", "MasterCard", "Discover", "Amex", "None"};
        JPanel chargeRow = new JPanel(new GridLayout(1, 5, 6, 0));
        chargeRow.setOpaque(false);
        ButtonGroup chargeGroup = new ButtonGroup();
        for (int i = 0; i < chargeOptions.length; i++) {
            String opt = chargeOptions[i];
            Theme.TogglePillButton pill = new Theme.TogglePillButton(opt, i == 0);
            pill.setActionCommand(opt);
            pill.addActionListener(e -> selectedChargeAccount = opt);
            chargeGroup.add(pill);
            chargePills.add(pill);
            chargeRow.add(pill);
        }
        JPanel chargeStack = Theme.createStackedInput("CHARGE ACCOUNT ON FILE", chargeRow);
        chargeStack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        // Street Address
        JPanel streetStack = Theme.createStackedInput("STREET ADDRESS", streetField);

        // Subdivision & Intersection Row
        JPanel subIntRow = new JPanel(new GridLayout(1, 2, 12, 0));
        subIntRow.setOpaque(false);
        subIntRow.setAlignmentX(LEFT_ALIGNMENT);
        subIntRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        subIntRow.add(Theme.createStackedInput("SUBDIVISION / COMPLEX", subdivisionField));
        subIntRow.add(Theme.createStackedInput("NEAREST MAJOR INTERSECTION", intersectionField));

        content.add(typeStack);
        content.add(Box.createVerticalStrut(10));
        content.add(namePhoneRow);
        content.add(Box.createVerticalStrut(10));
        content.add(chargeStack);
        content.add(Box.createVerticalStrut(10));
        content.add(streetStack);
        content.add(Box.createVerticalStrut(10));
        content.add(subIntRow);
        return card;
    }

    /**
     * Builds the returning customer past-orders card with quick reorder action.
     *
     * @return constructed history card {@link JPanel}
     */
    private JPanel buildPastOrdersCard() {
        Theme.RoundedCardPanel card = Theme.createTitledCard("Order History", "Past Favorites & Quick Re-Order", false);
        JPanel content = card.getContentPane();
        content.setLayout(new BorderLayout(0, 10));

        historyList.setBackground(Theme.BG_INPUT);
        historyList.setForeground(Theme.TEXT_PRIMARY);
        historyList.setFont(Theme.FONT_MONO);
        historyList.setCellRenderer(new PastOrderCellRenderer());
        JScrollPane scroll = new JScrollPane(historyList);
        Theme.styleScrollPane(scroll);

        JPanel south = new JPanel(new BorderLayout(0, 10));
        south.setOpaque(false);

        JButton reorderBtn = Theme.createSecondaryButton("+ Re-Order Medium Thin Pepperoni & Extra Cheese");
        reorderBtn.addActionListener(e -> reorderFavoritePizza());

        Theme.RoundedCardPanel tipBox = Theme.createCardPanel(null);
        tipBox.setLayout(new BoxLayout(tipBox, BoxLayout.Y_AXIS));
        JLabel tip1 = new JLabel("SAMPLE PROFILES IN data/customers.txt");
        tip1.setFont(Theme.FONT_LABEL);
        tip1.setForeground(Theme.ACCENT_AMBER);
        JLabel tip2 = new JLabel("<html><body style='width:440px'>• Jane Doe (Visa): 555-555-5555 &nbsp;&nbsp;• James Smith (MC): 124-183-7581<br/>• Juan Gonzalez: 555-123-4567</body></html>");
        tip2.setFont(Theme.FONT_BODY);
        tip2.setForeground(Theme.TEXT_MUTED);
        tipBox.add(tip1);
        tipBox.add(Box.createVerticalStrut(4));
        tipBox.add(tip2);

        south.add(reorderBtn, BorderLayout.NORTH);
        south.add(tipBox, BorderLayout.SOUTH);

        content.add(scroll, BorderLayout.CENTER);
        content.add(south, BorderLayout.SOUTH);
        return card;
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
            loadedPastOrders = Collections.emptyList();
            lookupStatusLabel.setForeground(Theme.ACCENT_AMBER);
            lookupStatusLabel.setText("No prior profile found for " + digits + ". Fill out your details below to register.");
            phoneField.setText(digits);
            return;
        }

        populateFields(found);
        frame.setActiveCustomer(found);
        loadedPastOrders = dataStore.findOrdersByPhone(digits);
        for (OrderRecord rec : loadedPastOrders) {
            pastOrdersModel.addElement(String.format("Order #%d • %s • $%.2f (%s) — %s",
                    rec.getOrderId(), rec.getTimestamp(), rec.getGrandTotal(),
                    rec.getPaymentMethod(), rec.getItemsSummary()));
        }
        if (loadedPastOrders.isEmpty()) {
            pastOrdersModel.addElement("No prior orders on file yet.");
        }

        lookupStatusLabel.setForeground(Theme.STATUS_SUCCESS);
        lookupStatusLabel.setText("✓ Welcome back, " + found.getName() + " (" + found.getChargeAccountType() + ")! Loaded " + loadedPastOrders.size() + " past order(s).");
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

        String acct = c.getChargeAccountType();
        if (acct != null && !acct.isEmpty()) {
            for (Theme.TogglePillButton pill : chargePills) {
                if (pill.getActionCommand().equalsIgnoreCase(acct)) {
                    pill.setSelected(true);
                    selectedChargeAccount = pill.getActionCommand();
                }
            }
        }
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
        if (!chargePills.isEmpty()) {
            chargePills.get(0).setSelected(true);
            selectedChargeAccount = chargePills.get(0).getActionCommand();
        }
        deliveryPill.setSelected(true);
        pastOrdersModel.clear();
        loadedPastOrders = Collections.emptyList();
        lookupStatusLabel.setForeground(Theme.TEXT_MUTED);
        lookupStatusLabel.setText("Enter a 10-digit phone number (e.g., 5555555555) to autofill saved details.");
        updateAddressEnabledState();
    }

    /**
     * Validates and saves the customer profile without leaving Step 1.
     */
    private void saveProfileOnly() {
        Customer saved = validateAndSaveCustomer();
        if (saved != null) {
            lookupStatusLabel.setForeground(Theme.STATUS_SUCCESS);
            lookupStatusLabel.setText("✓ Customer profile saved/updated in data/customers.txt for " + saved.getName() + ".");
        }
    }

    /**
     * Adds a quick favorite pizza matching the Sprint spec ("Medium, Thin &amp; Crispy, Pepperoni + Extra Cheese")
     * after ensuring customer info is saved, and jumps straight to the Menu &amp; Cart view.
     */
    private void reorderFavoritePizza() {
        Customer saved = validateAndSaveCustomer();
        if (saved == null) {
            return;
        }
        OrderItem fav = OrderItem.createPizza(
                "Medium (12\")",
                "Thin & Crispy",
                "Normal Sauce",
                java.util.Arrays.asList("Artisan Pepperoni", "Extra Mozzarella Cheese"),
                17.24
        );
        frame.getMenuOrderPanel().addItemDirectlyToCart(fav);
        frame.showCard(PizzaAppFrame.CARD_MENU);
    }

    /**
     * Validates customer inputs, persists to {@code customers.txt}, and returns the saved record.
     *
     * @return saved {@link Customer}, or {@code null} if validation failed
     */
    private Customer validateAndSaveCustomer() {
        String name = nameField.getText().trim();
        String digits = Customer.normalizeDigits(phoneField.getText());
        String orderType = deliveryPill.isSelected() ? "delivery" : "pickup";
        String street = streetField.getText().trim();
        String subdivision = subdivisionField.getText().trim();
        String intersection = intersectionField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the customer's full name.", "Missing Name", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (digits.length() != 10) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 10-digit phone number.", "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if ("delivery".equals(orderType) && street.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Street address is required for delivery orders.", "Missing Street Address", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Customer customer = new Customer(0, name, digits, orderType, street, subdivision, intersection, selectedChargeAccount);
        Customer saved = dataStore.saveOrUpdateCustomer(customer);
        frame.setActiveCustomer(saved);
        return saved;
    }

    /**
     * Validates customer inputs, persists to {@code customers.txt},
     * updates the active session customer, and opens the pizza builder.
     */
    private void saveAndProceed() {
        Customer saved = validateAndSaveCustomer();
        if (saved != null) {
            frame.showCard(PizzaAppFrame.CARD_MENU);
        }
    }

    /**
     * Wraps returning-customer order history lines cleanly inside styled dark cards.
     */
    private static class PastOrderCellRenderer extends DefaultListCellRenderer {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs the {@code PastOrderCellRenderer}.
         */
        PastOrderCellRenderer() {
        }

        /**
         * Renders each past-order line with word-wrapped HTML text and warm card padding.
         *
         * @param list         parent list
         * @param value        order summary string
         * @param index        cell index
         * @param isSelected   selection flag
         * @param cellHasFocus focus flag
         * @return styled cell label
         */
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setText("<html><body style='width:390px'>" + String.valueOf(value) + "</body></html>");
            lbl.setFont(Theme.FONT_BODY);
            lbl.setForeground(isSelected ? Theme.ACCENT_AMBER : Theme.TEXT_PRIMARY);
            lbl.setBackground(isSelected ? Theme.BG_CARD : Theme.BG_INPUT);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            return lbl;
        }
    }
}
