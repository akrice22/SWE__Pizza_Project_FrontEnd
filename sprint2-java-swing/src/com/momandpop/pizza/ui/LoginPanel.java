package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.StaffUser;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Polished Home &amp; Login screen matching Figures 5A and 5B: atmospheric hero banner,
 * 3-column craftsmanship strip, 5-column menu showcase cards, returning customer quick launcher,
 * and password-protected Staff Portal sign-in.
 */
public class LoginPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Reference to local file-based data store. */
    private final transient DataStore dataStore;

    /** Parent frame coordinator for screen navigation. */
    private final PizzaAppFrame frame;

    /** Staff login username input field. */
    private final JTextField usernameField;

    /** Staff login password input field. */
    private final JPasswordField passwordField;

    /** Status banner displaying login feedback or validation errors. */
    private final JLabel statusLabel;

    /** Quick customer phone input field for returning customer orders. */
    private final JTextField customerPhoneField;

    /**
     * Constructs the {@code LoginPanel}.
     *
     * @param dataStore shared {@link DataStore} instance
     * @param frame     parent {@link PizzaAppFrame} coordinator
     */
    public LoginPanel(DataStore dataStore, PizzaAppFrame frame) {
        this.dataStore = dataStore;
        this.frame = frame;
        this.usernameField = Theme.createTextField(18);
        this.passwordField = Theme.createPasswordField(18);
        this.statusLabel = new JLabel(" ");
        this.customerPhoneField = Theme.createTextField(16);

        buildLayout();
    }

    /**
     * Initializes the scrollable landing showcase and dual action portals.
     */
    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_APP);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Theme.BG_APP);
        container.setBorder(BorderFactory.createEmptyBorder(20, 44, 28, 44));

        container.add(buildHeroBanner());
        container.add(Box.createVerticalStrut(14));
        container.add(buildFeatureStrip());
        container.add(Box.createVerticalStrut(14));
        container.add(buildMenuShowcaseStrip());
        container.add(Box.createVerticalStrut(16));

        JPanel twoCols = new JPanel(new GridLayout(1, 2, 18, 0));
        twoCols.setOpaque(false);
        twoCols.setMaximumSize(new Dimension(2400, 300));
        twoCols.add(buildCustomerCard());
        twoCols.add(buildStaffCard());
        container.add(twoCols);

        JScrollPane scroll = new JScrollPane(container);
        Theme.styleScrollPane(scroll);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Builds the top hero showcase banner with warm hearth glow matching Figure 5A.
     *
     * @return constructed hero banner {@link JPanel}
     */
    private JPanel buildHeroBanner() {
        Theme.RoundedCardPanel hero = Theme.createHeroCardPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        hero.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JLabel eyebrow = new JLabel("STONE OVEN HEARTH & KITCHEN");
        eyebrow.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 11));
        eyebrow.setForeground(Theme.ACCENT_AMBER);
        eyebrow.setAlignmentX(CENTER_ALIGNMENT);

        JLabel headline = new JLabel("<html><center>STONE-FIRED<br/><i><font color='#FDE68A'>with</font></i> SOUL</center></html>", SwingConstants.CENTER);
        headline.setFont(Theme.FONT_HERO);
        headline.setForeground(Theme.TEXT_PRIMARY);
        headline.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Slow-fermented 48-hour dough, crushed San Marzano tomatoes, and whole milk mozzarella.", SwingConstants.CENTER);
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JButton cta = Theme.createPrimaryButton("START CUSTOM ORDER →");
        cta.setAlignmentX(CENTER_ALIGNMENT);
        cta.addActionListener(e -> frame.startFreshCustomerOrder());

        hero.add(eyebrow);
        hero.add(Box.createVerticalStrut(8));
        hero.add(headline);
        hero.add(Box.createVerticalStrut(8));
        hero.add(sub);
        hero.add(Box.createVerticalStrut(16));
        hero.add(cta);
        return hero;
    }

    /**
     * Builds the 3-column craftsmanship highlight strip directly beneath the hero banner.
     *
     * @return constructed strip {@link JPanel}
     */
    private JPanel buildFeatureStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 14, 0));
        strip.setOpaque(false);
        strip.add(buildHighlightCard("01. 48-HOUR COLD FERMENT", "Our naturally leavened dough rests cold for two full days to build airy, blistered crusts."));
        strip.add(buildHighlightCard("02. HAND-STRETCHED & FIRED", "Each pie is shaped by hand on marble slabs and baked at 800°F on stone hearths."));
        strip.add(buildHighlightCard("03. FAST LOCAL DELIVERY", "From oven to box to your doorstep for a flat $3.99 fee, or free counter pickup."));
        return strip;
    }

    /**
     * Builds one highlight card inside the craftsmanship strip.
     *
     * @param title   highlight heading
     * @param details highlight copy
     * @return styled {@link JPanel}
     */
    private JPanel buildHighlightCard(String title, String details) {
        Theme.RoundedCardPanel card = Theme.createCardPanel(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_LABEL);
        t.setForeground(Theme.ACCENT_AMBER);
        JLabel d = new JLabel("<html><body style='width:260px'>" + details + "</body></html>");
        d.setFont(Theme.FONT_BODY);
        d.setForeground(Theme.TEXT_MUTED);
        card.add(t);
        card.add(Box.createVerticalStrut(5));
        card.add(d);
        return card;
    }

    /**
     * Builds the 5-card Provisions &amp; Craft Menu Overview strip matching Figure 5B.
     *
     * @return constructed menu overview {@link JPanel}
     */
    private JPanel buildMenuShowcaseStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 5, 10, 0));
        strip.setOpaque(false);
        strip.add(buildMenuMiniCard("[01]", "SIZES & BASE", "$10.99 – $19.99", "Small 10\" to X-Large 16\""));
        strip.add(buildMenuMiniCard("[02]", "ARTISAN CRUSTS", "4 Styles", "Hand-Tossed, Thin, Thick, Garlic"));
        strip.add(buildMenuMiniCard("[03]", "VEGGIES & CHEESE", "+$1.00 – $1.50", "Extra Cheese, Mushrooms, Olives"));
        strip.add(buildMenuMiniCard("[04]", "SAVORY MEATS", "+$1.75 – $2.50", "Pepperoni, Sausage, Prosciutto"));
        strip.add(buildMenuMiniCard("[05]", "CRAFT DRINKS", "3 Drink Sizes", "Mexican Coke, San Pellegrino"));
        return strip;
    }

    /**
     * Builds one compact menu showcase card for the landing preview.
     *
     * @param tag     numeric tag (e.g. "[01]")
     * @param title   category title
     * @param badge   pricing badge text
     * @param summary short description
     * @return styled mini card {@link JPanel}
     */
    private JPanel buildMenuMiniCard(String tag, String title, String badge, String summary) {
        Theme.RoundedCardPanel card = Theme.createCardPanel(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel tagLbl = new JLabel(tag);
        tagLbl.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 10));
        tagLbl.setForeground(Theme.ACCENT_AMBER);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new java.awt.Font("Georgia", java.awt.Font.BOLD, 13));
        titleLbl.setForeground(Theme.TEXT_PRIMARY);
        JLabel badgeLbl = new JLabel(badge);
        badgeLbl.setFont(Theme.FONT_MONO_BOLD);
        badgeLbl.setForeground(Theme.ACCENT_AMBER);
        JLabel sumLbl = new JLabel("<html><body style='width:140px'>" + summary + "</body></html>");
        sumLbl.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
        sumLbl.setForeground(Theme.TEXT_MUTED);

        card.add(tagLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(badgeLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(sumLbl);
        return card;
    }

    /**
     * Builds the left-hand Customer Ordering &amp; Quick Phone Sign-In card.
     *
     * @return constructed customer entry card {@link JPanel}
     */
    private JPanel buildCustomerCard() {
        Theme.RoundedCardPanel card = Theme.createTitledCard("Customer Portal", "Quick Lookup & Start Order", false);
        JPanel content = card.getContentPane();
        content.setLayout(new BorderLayout(0, 12));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel desc = new JLabel("<html><body style='width:380px'>Enter a returning customer's 10-digit phone (e.g. <b>5555555555</b> for Jane Doe) to auto-fill address &amp; past orders, or start fresh.</body></html>");
        desc.setFont(Theme.FONT_BODY);
        desc.setForeground(Theme.TEXT_MUTED);
        desc.setAlignmentX(LEFT_ALIGNMENT);

        customerPhoneField.setText("5555555555");
        JPanel phoneStack = Theme.createStackedInput("RETURNING CUSTOMER PHONE (10 DIGITS)", customerPhoneField);
        phoneStack.setAlignmentX(LEFT_ALIGNMENT);

        body.add(desc);
        body.add(Box.createVerticalStrut(12));
        body.add(phoneStack);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        JButton lookupBtn = Theme.createPrimaryButton("Lookup & Start Order →");
        lookupBtn.addActionListener(e -> frame.startCustomerOrderWithPhone(customerPhoneField.getText()));

        JButton startFreshBtn = Theme.createSecondaryButton("New Guest Order");
        startFreshBtn.addActionListener(e -> frame.startFreshCustomerOrder());

        actions.add(lookupBtn);
        actions.add(startFreshBtn);

        content.add(body, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Builds the right-hand Staff &amp; Admin Portal password-protected sign-in card.
     *
     * @return constructed staff login card {@link JPanel}
     */
    private JPanel buildStaffCard() {
        Theme.RoundedCardPanel card = Theme.createTitledCard("🔒 Staff Access", "Owner & Manager Terminal Login", true);
        JPanel content = card.getContentPane();
        content.setLayout(new BorderLayout(0, 10));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel inputsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        inputsRow.setOpaque(false);
        inputsRow.setAlignmentX(LEFT_ALIGNMENT);
        inputsRow.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 64));
        inputsRow.add(Theme.createStackedInput("STAFF USERNAME", usernameField));
        inputsRow.add(Theme.createStackedInput("SECURITY PASSWORD", passwordField));

        statusLabel.setFont(Theme.FONT_BODY);
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusLabel.setText("Verify credentials from data/staff.txt (e.g., owner / pizza123).");
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);

        body.add(inputsRow);
        body.add(Box.createVerticalStrut(10));
        body.add(statusLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        JButton loginBtn = Theme.createPrimaryButton("Unlock Staff Dashboard →");
        loginBtn.addActionListener(e -> handleStaffLogin());
        btnPanel.add(loginBtn);

        content.add(body, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Clears typed staff login credentials when locking the terminal.
     */
    public void clearPasswordInputs() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setForeground(Theme.TEXT_MUTED);
        statusLabel.setText("Terminal locked. Sign in again to access Management Dashboard.");
    }

    /**
     * Validates staff credentials against {@code data/staff.txt} and unlocks the Staff/Admin view.
     */
    private void handleStaffLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            statusLabel.setForeground(Theme.STATUS_ERROR);
            statusLabel.setText("Enter both username and password.");
            return;
        }

        StaffUser matched = dataStore.authenticateStaff(user, pass);
        if (matched == null) {
            statusLabel.setForeground(Theme.STATUS_ERROR);
            statusLabel.setText("Invalid username or password. Access denied.");
            passwordField.setText("");
            return;
        }

        statusLabel.setForeground(Theme.STATUS_SUCCESS);
        statusLabel.setText("Authenticated as " + matched.getFullName() + ".");
        passwordField.setText("");
        frame.onStaffAuthenticated(matched);
    }
}
