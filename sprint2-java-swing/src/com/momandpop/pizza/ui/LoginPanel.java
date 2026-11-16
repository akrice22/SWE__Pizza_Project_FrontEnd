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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Initial login and entry screen providing staff authentication against {@code data/staff.txt}
 * as well as quick navigation to start a customer order or customer lookup.
 */
public class LoginPanel extends JPanel {

    /** Reference to local file-based data store. */
    private final DataStore dataStore;

    /** Parent frame coordinator for navigating between screens. */
    private final PizzaAppFrame frame;

    /** Staff login username input field. */
    private final JTextField usernameField;

    /** Staff login password input field. */
    private final JPasswordField passwordField;

    /** Status banner displaying login feedback or validation errors. */
    private final JLabel statusLabel;

    /** Quick customer phone input field for direct customer login. */
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
     * Initializes the centered two-column layout for Customer Ordering and Staff Login.
     */
    private void buildLayout() {
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setMaximumSize(new Dimension(920, 620));

        JPanel heroCard = Theme.createCardPanel(null);
        heroCard.setLayout(new BoxLayout(heroCard, BoxLayout.Y_AXIS));
        JLabel headline = new JLabel("MOM & POP PIZZERIA — STONE-FIRED WITH SOUL");
        headline.setFont(Theme.FONT_TITLE);
        headline.setForeground(Theme.ACCENT_AMBER);
        JLabel subtitle = new JLabel("Desktop POS & Custom Pizza Ordering System (Sprint 2 Standalone Edition)");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_MUTED);
        heroCard.add(headline);
        heroCard.add(Box.createVerticalStrut(6));
        heroCard.add(subtitle);

        JPanel twoCols = new JPanel(new GridLayout(1, 2, 20, 0));
        twoCols.setOpaque(false);
        twoCols.add(buildCustomerCard());
        twoCols.add(buildStaffCard());

        container.add(heroCard);
        container.add(Box.createVerticalStrut(20));
        container.add(twoCols);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(container, gbc);
    }

    /**
     * Builds the left-hand Customer Ordering &amp; Quick Phone Sign-In card.
     *
     * @return constructed customer entry card {@link JPanel}
     */
    private JPanel buildCustomerCard() {
        JPanel card = Theme.createCardPanel("Customer Ordering");
        card.setLayout(new BorderLayout(0, 14));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel title = new JLabel("Start a Custom Pizza Order");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel desc = new JLabel("<html><body style='width:280px'>Build up to 4-topping custom stone-fired pizzas, add cold craft beverages, or look up returning customer profiles by phone number.</body></html>");
        desc.setFont(Theme.FONT_BODY);
        desc.setForeground(Theme.TEXT_MUTED);

        JPanel phoneRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        phoneRow.setOpaque(false);
        JLabel phoneHint = Theme.createFieldLabel("Returning Customer Phone (Optional, e.g. 555-555-5555):");
        customerPhoneField.setText("5555555555");
        phoneRow.add(phoneHint);

        info.add(title);
        info.add(Box.createVerticalStrut(8));
        info.add(desc);
        info.add(Box.createVerticalStrut(14));
        info.add(phoneRow);
        info.add(customerPhoneField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton lookupBtn = Theme.createSecondaryButton("Lookup & Start Order");
        lookupBtn.addActionListener(e -> {
            frame.startCustomerOrderWithPhone(customerPhoneField.getText());
        });

        JButton startFreshBtn = Theme.createPrimaryButton("Start New Order ->");
        startFreshBtn.addActionListener(e -> frame.startFreshCustomerOrder());

        actions.add(lookupBtn);
        actions.add(startFreshBtn);

        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Builds the right-hand Staff &amp; Admin Portal login card.
     *
     * @return constructed staff login card {@link JPanel}
     */
    private JPanel buildStaffCard() {
        JPanel card = Theme.createCardPanel("Staff & Management Portal");
        card.setLayout(new BorderLayout(0, 12));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel hint = new JLabel("<html>Default credentials in <b>data/staff.txt</b>: username <code>owner</code> / password <code>pizza123</code></html>");
        hint.setFont(Theme.FONT_BODY);
        hint.setForeground(Theme.TEXT_MUTED);
        form.add(hint, c);

        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0.0;
        form.add(Theme.createFieldLabel("Username:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        usernameField.setText("owner");
        form.add(usernameField, c);

        c.gridy = 2;
        c.gridx = 0;
        c.weightx = 0.0;
        form.add(Theme.createFieldLabel("Password:"), c);

        c.gridx = 1;
        c.weightx = 1.0;
        passwordField.setText("pizza123");
        form.add(passwordField, c);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 2;
        statusLabel.setFont(Theme.FONT_BODY);
        statusLabel.setForeground(Theme.STATUS_ERROR);
        form.add(statusLabel, c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton loginBtn = Theme.createPrimaryButton("Sign In to Staff Portal ->");
        loginBtn.addActionListener(e -> handleStaffLogin());
        btnPanel.add(loginBtn);

        card.add(form, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Validates staff credentials against {@code data/staff.txt} and opens the Staff/Admin view.
     */
    private void handleStaffLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        StaffUser matched = dataStore.authenticateStaff(user, pass);
        if (matched == null) {
            statusLabel.setForeground(Theme.STATUS_ERROR);
            statusLabel.setText("Invalid username or password. Check data/staff.txt.");
            return;
        }
        statusLabel.setForeground(Theme.STATUS_SUCCESS);
        statusLabel.setText("Welcome, " + matched.getFullName() + " (" + matched.getRole() + ")");
        frame.onStaffAuthenticated(matched);
    }
}
