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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Styled Home &amp; Login screen featuring the Mom &amp; Pop Pizzeria hero banner,
 * returning customer quick-order launcher, and password-protected Staff Portal sign-in.
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
     * Initializes the centered hero showcase, feature highlights, and dual action cards.
     */
    private void buildLayout() {
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 32, 24, 32));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(980, 680));

        container.add(buildHeroBanner());
        container.add(Box.createVerticalStrut(14));
        container.add(buildFeatureStrip());
        container.add(Box.createVerticalStrut(16));

        JPanel twoCols = new JPanel(new GridLayout(1, 2, 18, 0));
        twoCols.setOpaque(false);
        twoCols.add(buildCustomerCard());
        twoCols.add(buildStaffCard());
        container.add(twoCols);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(container, gbc);
    }

    /**
     * Builds the top hero showcase banner matching the landing page aesthetic.
     *
     * @return constructed hero banner {@link JPanel}
     */
    private JPanel buildHeroBanner() {
        Theme.RoundedCardPanel hero = Theme.createAmberCardPanel(null);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("STONE OVEN HEARTH & KITCHEN");
        eyebrow.setFont(Theme.FONT_LABEL);
        eyebrow.setForeground(Theme.ACCENT_AMBER);
        eyebrow.setAlignmentX(CENTER_ALIGNMENT);

        JLabel headline = new JLabel("STONE-FIRED with SOUL", SwingConstants.CENTER);
        headline.setFont(Theme.FONT_HERO);
        headline.setForeground(Theme.TEXT_PRIMARY);
        headline.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Slow-fermented 48-hour sourdough, crushed San Marzano tomatoes, and craft beverages.", SwingConstants.CENTER);
        sub.setFont(Theme.FONT_BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JButton cta = Theme.createPrimaryButton("START CUSTOM ORDER →");
        cta.setAlignmentX(CENTER_ALIGNMENT);
        cta.addActionListener(e -> frame.startFreshCustomerOrder());

        hero.add(eyebrow);
        hero.add(Box.createVerticalStrut(6));
        hero.add(headline);
        hero.add(Box.createVerticalStrut(6));
        hero.add(sub);
        hero.add(Box.createVerticalStrut(14));
        hero.add(cta);
        return hero;
    }

    /**
     * Builds the 3-column artisan craftsmanship highlight strip.
     *
     * @return constructed strip {@link JPanel}
     */
    private JPanel buildFeatureStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 3, 14, 0));
        strip.setOpaque(false);
        strip.add(buildHighlightCard("01. 48-HOUR COLD FERMENT", "Naturally leavened dough rested cold for airy, blistered crusts."));
        strip.add(buildHighlightCard("02. HAND-STRETCHED & FIRED", "Choose from 4 sizes, 4 crusts, and up to 4 gourmet toppings."));
        strip.add(buildHighlightCard("03. FAST LOCAL DELIVERY", "Flat $3.99 neighborhood delivery or free counter pickup."));
        return strip;
    }

    /**
     * Builds one small highlight block inside the feature strip.
     *
     * @param title   highlight title
     * @param details highlight description
     * @return constructed highlight {@link JPanel}
     */
    private JPanel buildHighlightCard(String title, String details) {
        Theme.RoundedCardPanel card = Theme.createCardPanel(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_LABEL);
        t.setForeground(Theme.ACCENT_AMBER);
        JLabel d = new JLabel("<html><body style='width:220px'>" + details + "</body></html>");
        d.setFont(Theme.FONT_BODY);
        d.setForeground(Theme.TEXT_MUTED);
        card.add(t);
        card.add(Box.createVerticalStrut(4));
        card.add(d);
        return card;
    }

    /**
     * Builds the left-hand Customer Ordering &amp; Quick Phone Sign-In card.
     *
     * @return constructed customer entry card {@link JPanel}
     */
    private JPanel buildCustomerCard() {
        Theme.RoundedCardPanel card = Theme.createCardPanel("Customer Ordering Portal");
        card.setLayout(new BorderLayout(0, 12));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel title = new JLabel("Returning Customer Quick Lookup");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel desc = new JLabel("<html><body style='width:320px'>Look up saved delivery info &amp; past orders using your 10-digit phone (e.g. <b>5555555555</b>), or start a new guest order.</body></html>");
        desc.setFont(Theme.FONT_BODY);
        desc.setForeground(Theme.TEXT_MUTED);

        JPanel phoneInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        phoneInputRow.setOpaque(false);
        phoneInputRow.add(Theme.createFieldLabel("PHONE NUMBER (10 DIGITS)"));

        customerPhoneField.setText("5555555555");

        body.add(title);
        body.add(Box.createVerticalStrut(4));
        body.add(desc);
        body.add(Box.createVerticalStrut(10));
        body.add(phoneInputRow);
        body.add(customerPhoneField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        JButton lookupBtn = Theme.createPrimaryButton("Lookup Customer →");
        lookupBtn.addActionListener(e -> frame.startCustomerOrderWithPhone(customerPhoneField.getText()));

        JButton startFreshBtn = Theme.createSecondaryButton("New Guest Order");
        startFreshBtn.addActionListener(e -> frame.startFreshCustomerOrder());

        actions.add(lookupBtn);
        actions.add(startFreshBtn);

        card.add(body, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Builds the right-hand Staff &amp; Admin Portal password-protected sign-in card.
     *
     * @return constructed staff login card {@link JPanel}
     */
    private JPanel buildStaffCard() {
        Theme.RoundedCardPanel card = Theme.createCardPanel("🔒 Staff & Owner Terminal Login");
        card.setLayout(new BorderLayout(0, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel hint = new JLabel("<html>Admin access is password-protected (verified against <code>data/staff.txt</code>). Default: <b>owner</b> / <b>pizza123</b></html>");
        hint.setFont(Theme.FONT_BODY);
        hint.setForeground(Theme.TEXT_MUTED);
        form.add(hint, c);

        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0.0;
        form.add(Theme.createFieldLabel("USERNAME"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        form.add(usernameField, c);

        c.gridy = 2;
        c.gridx = 0;
        c.weightx = 0.0;
        form.add(Theme.createFieldLabel("PASSWORD"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        form.add(passwordField, c);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 2;
        statusLabel.setFont(Theme.FONT_BODY);
        statusLabel.setForeground(Theme.STATUS_ERROR);
        form.add(statusLabel, c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton loginBtn = Theme.createPrimaryButton("Unlock Staff Dashboard →");
        loginBtn.addActionListener(e -> handleStaffLogin());
        btnPanel.add(loginBtn);

        card.add(form, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Clears typed staff login credentials when locking the terminal.
     */
    public void clearPasswordInputs() {
        usernameField.setText("");
        passwordField.setText("");
        statusLabel.setText(" ");
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
