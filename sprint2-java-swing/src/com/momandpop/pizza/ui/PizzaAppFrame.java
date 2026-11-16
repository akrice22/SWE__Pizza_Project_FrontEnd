package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.StaffUser;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Main desktop application window ({@link JFrame}) hosting the styled navigation bar,
 * password-gated Staff Portal, and {@link CardLayout} coordinating all Sprint 2 screens.
 */
public class PizzaAppFrame extends JFrame {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Card identifier for the Login / Home screen. */
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
    private final transient DataStore dataStore;

    /** Card layout managing screen transitions. */
    private final CardLayout cardLayout;

    /** Central container holding all screen panels. */
    private final JPanel cardContainer;

    /** Login and home landing panel. */
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

    /** Navigation bar Staff Portal button reflecting lock/unlock state. */
    private final JButton adminNavBtn;

    /** Status indicator displaying logged-in staff user or guest mode. */
    private final JLabel sessionStatusLabel;

    /** Currently active customer for the in-progress order. */
    private transient Customer activeCustomer;

    /** Currently signed-in staff user, or {@code null} when locked. */
    private transient StaffUser loggedInStaff;

    /**
     * Constructs and initializes the {@code PizzaAppFrame}.
     *
     * @param dataStore local {@link DataStore} instance
     */
    public PizzaAppFrame(DataStore dataStore) {
        super("Mom & Pop Pizzeria — Desktop POS & Ordering");
        this.dataStore = dataStore;
        this.cardLayout = new CardLayout();
        this.cardContainer = new JPanel(cardLayout);
        this.cartNavBtn = Theme.createPrimaryButton("Cart (0) • $0.00");
        this.adminNavBtn = Theme.createSecondaryButton("🔒 Staff Portal");
        this.sessionStatusLabel = new JLabel("• Guest Ordering Mode");

        this.loginPanel = new LoginPanel(dataStore, this);
        this.customerPanel = new CustomerPanel(dataStore, this);
        this.menuOrderPanel = new MenuOrderPanel(this);
        this.checkoutReceiptPanel = new CheckoutReceiptPanel(dataStore, this);
        this.adminPanel = new AdminPanel(dataStore, this);

        initFrame();
    }

    /**
     * Configures frame dimensions, dark background, top navigation bar, and cards.
     */
    private void initFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 880);
        setMinimumSize(new Dimension(1100, 760));
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
     * Builds the persistent top navigation bar matching Figure 5A.
     *
     * @return constructed navigation {@link JPanel}
     */
    private JPanel buildTopNavBar() {
        JPanel nav = new JPanel(new BorderLayout(12, 0));
        nav.setBackground(Theme.BG_HEADER);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        brandBox.setOpaque(false);

        JPanel titles = new JPanel(new BorderLayout(0, 1));
        titles.setOpaque(false);
        JLabel brand = new JLabel("MOM & POP");
        brand.setFont(Theme.FONT_BRAND);
        brand.setForeground(Theme.TEXT_PRIMARY);
        JLabel subBrand = new JLabel("PIZZERIA & HEARTH");
        subBrand.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 10));
        subBrand.setForeground(Theme.ACCENT_AMBER);
        titles.add(brand, BorderLayout.NORTH);
        titles.add(subBrand, BorderLayout.SOUTH);

        sessionStatusLabel.setFont(Theme.FONT_LABEL);
        sessionStatusLabel.setForeground(Theme.STATUS_SUCCESS);

        brandBox.add(titles);
        brandBox.add(sessionStatusLabel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton loginNav = Theme.createSecondaryButton("Menu & Home");
        loginNav.addActionListener(e -> showCard(CARD_LOGIN));

        JButton custNav = Theme.createSecondaryButton("1. Customer Info");
        custNav.addActionListener(e -> showCard(CARD_CUSTOMER));

        JButton menuNav = Theme.createSecondaryButton("2. Build Pizza & Drinks");
        menuNav.addActionListener(e -> {
            menuOrderPanel.refreshCartSummary();
            showCard(CARD_MENU);
        });

        JButton checkoutNav = Theme.createSecondaryButton("3. Review & Pay");
        checkoutNav.addActionListener(e -> openCheckoutScreen());

        adminNavBtn.addActionListener(e -> openAdminPortal());

        cartNavBtn.addActionListener(e -> showCard(CARD_MENU));

        buttons.add(loginNav);
        buttons.add(custNav);
        buttons.add(menuNav);
        buttons.add(checkoutNav);
        buttons.add(adminNavBtn);
        buttons.add(cartNavBtn);

        nav.add(brandBox, BorderLayout.WEST);
        nav.add(buttons, BorderLayout.EAST);
        return nav;
    }

    /**
     * Switches the visible screen inside the central {@link CardLayout}.
     * Attempts to enter {@link #CARD_ADMIN} are gated through {@link #openAdminPortal()}.
     *
     * @param cardName target card constant
     */
    public void showCard(String cardName) {
        if (CARD_ADMIN.equals(cardName) && loggedInStaff == null) {
            openAdminPortal();
            return;
        }
        if (CARD_MENU.equals(cardName)) {
            menuOrderPanel.refreshCartSummary();
        } else if (CARD_CHECKOUT.equals(cardName)) {
            checkoutReceiptPanel.refreshFinancialSummary();
        }
        cardLayout.show(cardContainer, cardName);
    }

    /**
     * Gates access to the Staff &amp; Management Dashboard behind password authentication.
     * If no staff account is currently authenticated, displays a modal credential prompt.
     */
    public void openAdminPortal() {
        if (loggedInStaff != null) {
            adminPanel.refreshAllTables();
            cardLayout.show(cardContainer, CARD_ADMIN);
            return;
        }
        promptStaffLoginModal();
    }

    /**
     * Displays a styled dark-theme modal authentication dialog requiring valid credentials
     * from {@code data/staff.txt} before unlocking the Admin Portal.
     */
    private void promptStaffLoginModal() {
        JDialog dialog = new JDialog(this, "Staff Portal Authentication", true);
        dialog.setSize(450, 340);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG_APP);
        dialog.getContentPane().setLayout(new BorderLayout());

        Theme.RoundedCardPanel card = Theme.createTitledCard("Restricted Access", "Staff & Owner Authentication", true);
        JPanel content = card.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JTextField userField = Theme.createTextField(18);
        JPasswordField passField = Theme.createPasswordField(18);
        JLabel errLabel = new JLabel("Enter valid staff credentials (default: owner / pizza123).");
        errLabel.setFont(Theme.FONT_BODY);
        errLabel.setForeground(Theme.TEXT_MUTED);
        errLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel userStack = Theme.createStackedInput("WORKER / ADMIN USERNAME", userField);
        userStack.setAlignmentX(LEFT_ALIGNMENT);
        JPanel passStack = Theme.createStackedInput("SECURITY PASSWORD", passField);
        passStack.setAlignmentX(LEFT_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton unlockBtn = Theme.createPrimaryButton("Unlock Staff Dashboard →");
        unlockBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword());
            StaffUser matched = dataStore.authenticateStaff(u, p);
            if (matched == null) {
                errLabel.setForeground(Theme.STATUS_ERROR);
                errLabel.setText("Invalid username or password. Access denied.");
                passField.setText("");
                return;
            }
            dialog.dispose();
            onStaffAuthenticated(matched);
        });

        dialog.getRootPane().setDefaultButton(unlockBtn);
        btnRow.add(cancelBtn);
        btnRow.add(unlockBtn);

        content.add(errLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(userStack);
        content.add(Box.createVerticalStrut(10));
        content.add(passStack);
        content.add(Box.createVerticalStrut(16));
        content.add(btnRow);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG_APP);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        wrapper.add(card, BorderLayout.CENTER);
        dialog.getContentPane().add(wrapper, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    /**
     * Starts a fresh customer order by clearing active customer details and cart, then opening Step 1.
     */
    public void startFreshCustomerOrder() {
        this.activeCustomer = null;
        customerPanel.clearForm();
        menuOrderPanel.clearCart();
        showCard(CARD_CUSTOMER);
    }

    /**
     * Opens the Customer Panel and triggers an automatic 10-digit phone lookup.
     *
     * @param rawPhone phone number typed on the home screen
     */
    public void startCustomerOrderWithPhone(String rawPhone) {
        showCard(CARD_CUSTOMER);
        customerPanel.performPhoneLookup(rawPhone);
    }

    /**
     * Updates session state when staff authentication succeeds and opens the Admin Dashboard.
     *
     * @param user authenticated {@link StaffUser}
     */
    public void onStaffAuthenticated(StaffUser user) {
        this.loggedInStaff = user;
        adminNavBtn.setText("🔓 Staff Portal");
        sessionStatusLabel.setForeground(Theme.STATUS_SUCCESS);
        sessionStatusLabel.setText("• Staff: " + user.getFullName() + " [" + user.getRole().toUpperCase() + "]");
        adminPanel.refreshAllTables();
        cardLayout.show(cardContainer, CARD_ADMIN);
    }

    /**
     * Logs out the current staff user, re-locks the Admin Portal behind password protection,
     * and returns to the Home / Login screen.
     */
    public void logoutStaff() {
        this.loggedInStaff = null;
        adminNavBtn.setText("🔒 Staff Portal");
        sessionStatusLabel.setForeground(Theme.STATUS_SUCCESS);
        sessionStatusLabel.setText("• Guest Ordering Mode");
        loginPanel.clearPasswordInputs();
        cardLayout.show(cardContainer, CARD_LOGIN);
    }

    /**
     * Refreshes financial totals and opens the Checkout &amp; Receipt panel.
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
        cartNavBtn.setText(String.format("Cart (%d) • $%.2f", count, subtotal));
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
     * Sets the active {@link Customer} and refreshes cart delivery fee totals.
     *
     * @param activeCustomer customer record
     */
    public void setActiveCustomer(Customer activeCustomer) {
        this.activeCustomer = activeCustomer;
        menuOrderPanel.refreshCartSummary();
    }

    /**
     * Returns the currently authenticated {@link StaffUser}, or {@code null} if locked.
     *
     * @return authenticated staff user or {@code null}
     */
    public StaffUser getLoggedInStaff() {
        return loggedInStaff;
    }

    /**
     * Returns the {@link CheckoutReceiptPanel} reference.
     *
     * @return checkout receipt panel
     */
    public CheckoutReceiptPanel getCheckoutReceiptPanel() {
        return checkoutReceiptPanel;
    }

    /**
     * Returns the {@link CustomerPanel} reference.
     *
     * @return customer panel
     */
    public CustomerPanel getCustomerPanel() {
        return customerPanel;
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
}
