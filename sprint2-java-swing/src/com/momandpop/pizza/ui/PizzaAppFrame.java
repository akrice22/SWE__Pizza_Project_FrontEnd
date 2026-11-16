package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.StaffUser;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Main desktop application window ({@link JFrame}) hosting the top navigation bar
 * and a {@link CardLayout} container coordinating all Sprint 2 screens.
 */
public class PizzaAppFrame extends JFrame {

    /** Card identifier for the Login screen. */
    public static final String CARD_LOGIN = "LOGIN";

    /** Card identifier for the Customer Lookup &amp; Entry screen. */
    public static final String CARD_CUSTOMER = "CUSTOMER";

    /** Card identifier for the Pizza &amp; Beverage Ordering screen. */
    public static final String CARD_MENU = "MENU";

    /** Card identifier for the Checkout &amp; Receipt screen. */
    public static final String CARD_CHECKOUT = "CHECKOUT";

    /** Card identifier for the Staff &amp; Management screen. */
    public static final String CARD_ADMIN = "ADMIN";

    /** Local plain-text storage manager. */
    private final DataStore dataStore;

    /** Card layout managing screen transitions. */
    private final CardLayout cardLayout;

    /** Central container holding all screen panels. */
    private final JPanel cardContainer;

    /** Login screen panel. */
    private final LoginPanel loginPanel;

    /** Customer info and lookup panel. */
    private final CustomerPanel customerPanel;

    /** Pizza builder, beverage selector, and cart panel. */
    private final MenuOrderPanel menuOrderPanel;

    /** Checkout payment and receipt panel. */
    private final CheckoutReceiptPanel checkoutReceiptPanel;

    /** Staff and management tables panel. */
    private final AdminPanel adminPanel;

    /** Navigation bar badge displaying current cart count and subtotal. */
    private final JButton cartNavBtn;

    /** Status indicator displaying logged-in staff user or guest mode. */
    private final JLabel sessionStatusLabel;

    /** Currently active customer for the in-progress order. */
    private Customer activeCustomer;

    /** Currently signed-in staff user, or {@code null} when in guest mode. */
    private StaffUser loggedInStaff;

    /**
     * Constructs and initializes the {@code PizzaAppFrame}.
     *
     * @param dataStore local {@link DataStore} instance
     */
    public PizzaAppFrame(DataStore dataStore) {
        super("Mom & Pop Pizzeria — Desktop POS & Ordering (Java Swing)");
        this.dataStore = dataStore;
        this.cardLayout = new CardLayout();
        this.cardContainer = new JPanel(cardLayout);
        this.cartNavBtn = Theme.createPrimaryButton("Cart (0) - $0.00");
        this.sessionStatusLabel = new JLabel("Mode: Guest / Walk-in");

        this.loginPanel = new LoginPanel(dataStore, this);
        this.customerPanel = new CustomerPanel(dataStore, this);
        this.menuOrderPanel = new MenuOrderPanel(this);
        this.checkoutReceiptPanel = new CheckoutReceiptPanel(dataStore, this);
        this.adminPanel = new AdminPanel(dataStore, this);

        initFrame();
    }

    /**
     * Configures frame dimensions, top navigation header, and card registration.
     */
    private void initFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 820);
        setMinimumSize(new Dimension(1020, 700));
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_APP);

        cardContainer.setBackground(Theme.BG_APP);
        cardContainer.add(loginPanel, CARD_LOGIN);
        cardContainer.add(customerPanel, CARD_CUSTOMER);
        cardContainer.add(menuOrderPanel, CARD_MENU);
        cardContainer.add(checkoutReceiptPanel, CARD_CHECKOUT);
        cardContainer.add(adminPanel, CARD_ADMIN);

        add(buildTopNavBar(), BorderLayout.NORTH);
        add(cardContainer, BorderLayout.CENTER);
        showCard(CARD_LOGIN);
    }

    /**
     * Builds the persistent top navigation bar across all screens.
     *
     * @return constructed navigation {@link JPanel}
     */
    private JPanel buildTopNavBar() {
        JPanel nav = new JPanel(new BorderLayout(12, 0));
        nav.setBackground(new ColorWithHeader());
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_STONE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandBox.setOpaque(false);
        JLabel brand = new JLabel("MOM & POP PIZZERIA");
        brand.setFont(Theme.FONT_TITLE);
        brand.setForeground(Theme.ACCENT_AMBER);
        sessionStatusLabel.setFont(Theme.FONT_LABEL);
        sessionStatusLabel.setForeground(Theme.STATUS_SUCCESS);
        brandBox.add(brand);
        brandBox.add(sessionStatusLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton loginNav = Theme.createSecondaryButton("1. Login");
        loginNav.addActionListener(e -> showCard(CARD_LOGIN));

        JButton custNav = Theme.createSecondaryButton("2. Customer Info");
        custNav.addActionListener(e -> showCard(CARD_CUSTOMER));

        JButton menuNav = Theme.createSecondaryButton("3. Menu & Pizza Builder");
        menuNav.addActionListener(e -> {
            menuOrderPanel.refreshCartSummary();
            showCard(CARD_MENU);
        });

        JButton checkoutNav = Theme.createSecondaryButton("4. Checkout & Receipt");
        checkoutNav.addActionListener(e -> openCheckoutScreen());

        JButton adminNav = Theme.createSecondaryButton("5. Staff / Admin View");
        adminNav.addActionListener(e -> {
            adminPanel.refreshAllTables();
            showCard(CARD_ADMIN);
        });

        cartNavBtn.addActionListener(e -> showCard(CARD_MENU));

        buttons.add(loginNav);
        buttons.add(custNav);
        buttons.add(menuNav);
        buttons.add(checkoutNav);
        buttons.add(adminNav);
        buttons.add(cartNavBtn);

        nav.add(brandBox, BorderLayout.WEST);
        nav.add(buttons, BorderLayout.EAST);
        return nav;
    }

    /**
     * Switches the visible screen inside the central {@link CardLayout}.
     *
     * @param cardName target card constant (e.g., {@link #CARD_MENU})
     */
    public void showCard(String cardName) {
        if (CARD_MENU.equals(cardName)) {
            menuOrderPanel.refreshCartSummary();
        }
        cardLayout.show(cardContainer, cardName);
    }

    /**
     * Starts a fresh customer order by clearing customer details and cart, then opening Step 1.
     */
    public void startFreshCustomerOrder() {
        this.activeCustomer = null;
        customerPanel.clearForm();
        menuOrderPanel.clearCart();
        showCard(CARD_CUSTOMER);
    }

    /**
     * Opens the Customer Panel and runs an automatic phone lookup for the supplied number.
     *
     * @param rawPhone phone number typed on the login screen
     */
    public void startCustomerOrderWithPhone(String rawPhone) {
        showCard(CARD_CUSTOMER);
        customerPanel.performPhoneLookup(rawPhone);
    }

    /**
     * Updates navigation status when a staff member authenticates and opens the Admin view.
     *
     * @param user authenticated {@link StaffUser}
     */
    public void onStaffAuthenticated(StaffUser user) {
        this.loggedInStaff = user;
        sessionStatusLabel.setText("Staff: " + user.getFullName() + " [" + user.getRole().toUpperCase() + "]");
        adminPanel.refreshAllTables();
        showCard(CARD_ADMIN);
    }

    /**
     * Opens the Checkout &amp; Receipt panel after refreshing financial totals.
     */
    public void openCheckoutScreen() {
        checkoutReceiptPanel.refreshFinancialSummary();
        showCard(CARD_CHECKOUT);
    }

    /**
     * Updates the top navigation cart counter button.
     *
     * @param count    number of items in cart
     * @param subtotal current cart subtotal in USD
     */
    public void updateCartBadge(int count, double subtotal) {
        cartNavBtn.setText(String.format("Cart (%d) - $%.2f", count, subtotal));
    }

    /**
     * Returns the currently active {@link Customer}.
     *
     * @return active customer or {@code null}
     */
    public Customer getActiveCustomer() {
        return activeCustomer;
    }

    /**
     * Sets the active {@link Customer} and refreshes cart delivery fee display.
     *
     * @param activeCustomer customer record
     */
    public void setActiveCustomer(Customer activeCustomer) {
        this.activeCustomer = activeCustomer;
        menuOrderPanel.refreshCartSummary();
    }

    /**
     * Returns the currently signed-in {@link StaffUser}.
     *
     * @return signed-in staff user or {@code null}
     */
    public StaffUser getLoggedInStaff() {
        return loggedInStaff;
    }

    /**
     * Returns the {@link MenuOrderPanel} reference.
     *
     * @return menu order panel
     */
    public MenuOrderPanel getMenuOrderPanel() {
        return menuOrderPanel;
    }

    /**
     * Returns the {@link AdminPanel} reference.
     *
     * @return admin panel
     */
    public AdminPanel getAdminPanel() {
        return adminPanel;
    }

    /**
     * Returns the {@link DataStore} backing this application.
     *
     * @return data store instance
     */
    public DataStore getDataStore() {
        return dataStore;
    }

    /**
     * Internal dark header background color helper.
     */
    private static class ColorWithHeader extends java.awt.Color {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Creates the header espresso color (#191512).
         */
        ColorWithHeader() {
            super(25, 21, 18);
        }
    }
}
