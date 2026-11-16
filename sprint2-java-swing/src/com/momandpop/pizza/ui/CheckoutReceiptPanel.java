package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.MenuCatalog;
import com.momandpop.pizza.model.OrderItem;
import com.momandpop.pizza.model.OrderRecord;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.print.PrinterException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Step 3 checkout and receipt screen featuring pill selectors for Payment Methods
 * (Credit, Debit, Cash, Check), Tips, interactive Signature Canvas, order persistence
 * to {@code data/orders.txt}, and an authentic paper-styled receipt panel.
 */
public class CheckoutReceiptPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Local file persistence store. */
    private final transient DataStore dataStore;

    /** Parent frame coordinator. */
    private final PizzaAppFrame frame;

    /** Credit card payment pill button. */
    private final Theme.TogglePillButton creditPill;

    /** Debit card payment pill button. */
    private final Theme.TogglePillButton debitPill;

    /** Cash payment pill button. */
    private final Theme.TogglePillButton cashPill;

    /** Check payment pill button. */
    private final Theme.TogglePillButton checkPill;

    /** CardLayout switching sub-forms based on selected tender. */
    private final CardLayout tenderLayout;

    /** Panel holding dynamic payment tender inputs. */
    private final JPanel tenderPanel;

    /** Cardholder name input for card tenders. */
    private final JTextField cardNameField;

    /** Card number input for card tenders. */
    private final JTextField cardNumberField;

    /** Card expiry input (MM/YY). */
    private final JTextField cardExpiryField;

    /** Card CVV security code input. */
    private final JTextField cardCvvField;

    /** Cash tendered input field. */
    private final JTextField cashTenderedField;

    /** Label displaying computed cash change due. */
    private final JLabel cashChangeLabel;

    /** Physical check number input field. */
    private final JTextField checkNumberField;

    /** Check amount tendered input field. */
    private final JTextField checkAmountField;

    /** 0% tip pill button. */
    private final Theme.TogglePillButton tip0Pill;

    /** 15% tip pill button. */
    private final Theme.TogglePillButton tip15Pill;

    /** 18% tip pill button. */
    private final Theme.TogglePillButton tip18Pill;

    /** 20% tip pill button. */
    private final Theme.TogglePillButton tip20Pill;

    /** Custom dollar tip pill button. */
    private final Theme.TogglePillButton tipCustomPill;

    /** Custom dollar tip input field. */
    private final JTextField customTipField;

    /** Label showing subtotal, delivery fee, tax, tip, and final total. */
    private final JLabel breakdownLabel;

    /** Customer typed signature confirmation input field. */
    private final JTextField typedSignatureField;

    /** Interactive mouse-drawable signature canvas. */
    private final SignatureCanvasPanel signatureCanvas;

    /** Explicit authorization consent checkbox. */
    private final JCheckBox authorizeCheck;

    /** Status badge above the receipt paper indicating pending vs. confirmed payment. */
    private final JLabel receiptStatusLabel;

    /** Monospaced text area rendering the formatted receipt. */
    private final JTextArea receiptArea;

    /** Most recently completed order record shown in the receipt pane. */
    private transient OrderRecord latestCompletedOrder;

    /**
     * Constructs the {@code CheckoutReceiptPanel}.
     *
     * @param dataStore shared {@link DataStore} instance
     * @param frame     parent {@link PizzaAppFrame} coordinator
     */
    public CheckoutReceiptPanel(DataStore dataStore, PizzaAppFrame frame) {
        this.dataStore = dataStore;
        this.frame = frame;
        this.creditPill = new Theme.TogglePillButton("💳 Credit", true);
        this.debitPill = new Theme.TogglePillButton("🏧 Debit", false);
        this.cashPill = new Theme.TogglePillButton("💵 Cash", false);
        this.checkPill = new Theme.TogglePillButton("🧾 Check", false);
        this.tenderLayout = new CardLayout();
        this.tenderPanel = new JPanel(tenderLayout);

        this.cardNameField = Theme.createTextField(18);
        this.cardNumberField = Theme.createTextField(18);
        this.cardExpiryField = Theme.createTextField(7);
        this.cardCvvField = Theme.createTextField(5);
        this.cashTenderedField = Theme.createTextField(10);
        this.cashChangeLabel = new JLabel("Change Due: $0.00");
        this.checkNumberField = Theme.createTextField(10);
        this.checkAmountField = Theme.createTextField(10);

        this.tip0Pill = new Theme.TogglePillButton("0%", false);
        this.tip15Pill = new Theme.TogglePillButton("15%", true);
        this.tip18Pill = new Theme.TogglePillButton("18%", false);
        this.tip20Pill = new Theme.TogglePillButton("20%", false);
        this.tipCustomPill = new Theme.TogglePillButton("Custom", false);
        this.customTipField = Theme.createTextField(6);

        this.breakdownLabel = new JLabel();
        this.typedSignatureField = Theme.createTextField(18);
        this.signatureCanvas = new SignatureCanvasPanel();
        this.authorizeCheck = new JCheckBox("I authorize this transaction and accept this as my signature.");
        this.receiptStatusLabel = new JLabel("● Ready to Confirm Payment");
        this.receiptArea = new JTextArea();

        buildLayout();
    }

    /**
     * Builds the split checkout form (left) and formatted paper receipt display (right).
     */
    private void buildLayout() {
        setLayout(new GridLayout(1, 2, 18, 0));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        add(buildPaymentFormCard());
        add(buildReceiptCard());
    }

    /**
     * Constructs the left-hand payment, tip, and signature authorization card.
     *
     * @return payment form {@link JPanel}
     */
    private JPanel buildPaymentFormCard() {
        Theme.RoundedCardPanel card = Theme.createCardPanel("Step 03 • Review & Pay");
        card.setLayout(new BorderLayout(0, 10));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Payment Method Pills
        ButtonGroup payGroup = new ButtonGroup();
        payGroup.add(creditPill);
        payGroup.add(debitPill);
        payGroup.add(cashPill);
        payGroup.add(checkPill);

        JPanel payRow = new JPanel(new GridLayout(1, 4, 8, 0));
        payRow.setOpaque(false);
        payRow.add(creditPill);
        payRow.add(debitPill);
        payRow.add(cashPill);
        payRow.add(checkPill);

        creditPill.addActionListener(e -> tenderLayout.show(tenderPanel, "CARD"));
        debitPill.addActionListener(e -> tenderLayout.show(tenderPanel, "CARD"));
        cashPill.addActionListener(e -> {
            tenderLayout.show(tenderPanel, "CASH");
            refreshFinancialSummary();
        });
        checkPill.addActionListener(e -> {
            tenderLayout.show(tenderPanel, "CHECK");
            refreshFinancialSummary();
        });

        // Dynamic Tender Details
        tenderPanel.setOpaque(false);
        tenderPanel.add(buildCardSubPanel(), "CARD");
        tenderPanel.add(buildCashSubPanel(), "CASH");
        tenderPanel.add(buildCheckSubPanel(), "CHECK");
        tenderLayout.show(tenderPanel, "CARD");

        // Tip Selection Pills
        ButtonGroup tipGroup = new ButtonGroup();
        tipGroup.add(tip0Pill);
        tipGroup.add(tip15Pill);
        tipGroup.add(tip18Pill);
        tipGroup.add(tip20Pill);
        tipGroup.add(tipCustomPill);

        JPanel tipPillsRow = new JPanel(new GridLayout(1, 5, 6, 0));
        tipPillsRow.setOpaque(false);
        tipPillsRow.add(tip0Pill);
        tipPillsRow.add(tip15Pill);
        tipPillsRow.add(tip18Pill);
        tipPillsRow.add(tip20Pill);
        tipPillsRow.add(tipCustomPill);

        JPanel customTipBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        customTipBar.setOpaque(false);
        customTipBar.add(Theme.createFieldLabel("CUSTOM TIP ($):"));
        customTipField.setText("0.00");
        customTipField.setEnabled(false);
        customTipBar.add(customTipField);

        JButton applyTipBtn = Theme.createSecondaryButton("Recalculate Totals");
        applyTipBtn.addActionListener(e -> refreshFinancialSummary());
        customTipBar.add(applyTipBtn);

        tip0Pill.addActionListener(e -> {
            customTipField.setEnabled(false);
            refreshFinancialSummary();
        });
        tip15Pill.addActionListener(e -> {
            customTipField.setEnabled(false);
            refreshFinancialSummary();
        });
        tip18Pill.addActionListener(e -> {
            customTipField.setEnabled(false);
            refreshFinancialSummary();
        });
        tip20Pill.addActionListener(e -> {
            customTipField.setEnabled(false);
            refreshFinancialSummary();
        });
        tipCustomPill.addActionListener(e -> {
            customTipField.setEnabled(true);
            refreshFinancialSummary();
        });

        // Totals Summary Box
        Theme.RoundedCardPanel totalsCard = Theme.createAmberCardPanel("Financial Breakdown");
        totalsCard.setLayout(new FlowLayout(FlowLayout.LEFT));
        breakdownLabel.setFont(Theme.FONT_MONO);
        breakdownLabel.setForeground(Theme.TEXT_PRIMARY);
        totalsCard.add(breakdownLabel);

        // Signature Block
        Theme.RoundedCardPanel sigPanel = Theme.createCardPanel("Customer Digital Signature");
        sigPanel.setLayout(new BorderLayout(6, 6));

        JPanel typedRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        typedRow.setOpaque(false);
        typedRow.add(Theme.createFieldLabel("TYPED NAME:"));
        typedRow.add(typedSignatureField);
        JButton clearSigBtn = Theme.createSecondaryButton("Clear Canvas");
        clearSigBtn.addActionListener(e -> signatureCanvas.clearSignature());
        typedRow.add(clearSigBtn);

        authorizeCheck.setOpaque(false);
        authorizeCheck.setForeground(Theme.TEXT_PRIMARY);
        authorizeCheck.setFont(Theme.FONT_BODY);

        sigPanel.add(typedRow, BorderLayout.NORTH);
        sigPanel.add(signatureCanvas, BorderLayout.CENTER);
        sigPanel.add(authorizeCheck, BorderLayout.SOUTH);

        body.add(payRow);
        body.add(Box.createVerticalStrut(8));
        body.add(tenderPanel);
        body.add(Box.createVerticalStrut(8));
        body.add(tipPillsRow);
        body.add(Box.createVerticalStrut(4));
        body.add(customTipBar);
        body.add(Box.createVerticalStrut(6));
        body.add(totalsCard);
        body.add(Box.createVerticalStrut(8));
        body.add(sigPanel);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.getViewport().setBackground(Theme.BG_CARD);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        JButton backBtn = Theme.createSecondaryButton("← Back to Menu");
        backBtn.addActionListener(e -> frame.showCard(PizzaAppFrame.CARD_MENU));

        JButton completeBtn = Theme.createPrimaryButton("AUTHORIZE & COMPLETE ORDER →");
        completeBtn.addActionListener(e -> submitOrder());

        footer.add(backBtn, BorderLayout.WEST);
        footer.add(completeBtn, BorderLayout.EAST);

        card.add(bodyScroll, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Builds Credit/Debit card details sub-panel.
     *
     * @return card input {@link JPanel}
     */
    private JPanel buildCardSubPanel() {
        Theme.RoundedCardPanel p = Theme.createCardPanel("Card Details");
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        cardNumberField.setText("4242 4242 4242 4242");
        cardExpiryField.setText("12/28");
        cardCvvField.setText("123");

        c.gridy = 0;
        c.gridx = 0;
        p.add(Theme.createFieldLabel("CARDHOLDER"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        p.add(cardNameField, c);

        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0.0;
        p.add(Theme.createFieldLabel("CARD NUMBER"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        p.add(cardNumberField, c);

        JPanel expCvv = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        expCvv.setOpaque(false);
        expCvv.add(Theme.createFieldLabel("EXP:"));
        expCvv.add(cardExpiryField);
        expCvv.add(Theme.createFieldLabel("CVV:"));
        expCvv.add(cardCvvField);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 2;
        p.add(expCvv, c);
        return p;
    }

    /**
     * Builds Cash tendered input sub-panel with live change calculation.
     *
     * @return cash input {@link JPanel}
     */
    private JPanel buildCashSubPanel() {
        Theme.RoundedCardPanel p = Theme.createCardPanel("Cash Tender Details");
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.add(Theme.createFieldLabel("AMOUNT TENDERED ($):"));
        cashTenderedField.setText("50.00");
        p.add(cashTenderedField);

        JButton calcBtn = Theme.createSecondaryButton("Calc Change");
        calcBtn.addActionListener(e -> updateCashChangeLabel());
        p.add(calcBtn);

        cashChangeLabel.setFont(Theme.FONT_MONO_BOLD);
        cashChangeLabel.setForeground(Theme.STATUS_SUCCESS);
        p.add(cashChangeLabel);
        return p;
    }

    /**
     * Builds Physical Check payment input sub-panel.
     *
     * @return check input {@link JPanel}
     */
    private JPanel buildCheckSubPanel() {
        Theme.RoundedCardPanel p = Theme.createCardPanel("Physical Check Details");
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.add(Theme.createFieldLabel("CHECK #:"));
        checkNumberField.setText("1042");
        p.add(checkNumberField);
        p.add(Theme.createFieldLabel("CHECK AMOUNT ($):"));
        checkAmountField.setText("50.00");
        p.add(checkAmountField);
        return p;
    }

    /**
     * Constructs the right-hand paper receipt viewer matching the printable register modal.
     *
     * @return receipt viewer {@link JPanel}
     */
    private JPanel buildReceiptCard() {
        Theme.RoundedCardPanel card = Theme.createCardPanel("Printable Order Receipt");
        card.setLayout(new BorderLayout(0, 10));

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setOpaque(false);
        receiptStatusLabel.setFont(Theme.FONT_LABEL);
        receiptStatusLabel.setForeground(Theme.ACCENT_AMBER);
        statusBar.add(receiptStatusLabel, BorderLayout.WEST);

        receiptArea.setEditable(false);
        receiptArea.setBackground(Theme.RECEIPT_PAPER);
        receiptArea.setForeground(Theme.RECEIPT_INK);
        receiptArea.setFont(Theme.FONT_MONO);
        receiptArea.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        receiptArea.setText("Authorize payment on the left to generate your official receipt...");

        JScrollPane scroll = new JScrollPane(receiptArea);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_STONE, 1));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton saveTxtBtn = Theme.createSecondaryButton("💾 Save Receipt .txt");
        saveTxtBtn.addActionListener(e -> exportReceiptTextFile());

        JButton printBtn = Theme.createSecondaryButton("🖨️ Print Receipt");
        printBtn.addActionListener(e -> printReceipt());

        JButton newOrderBtn = Theme.createPrimaryButton("Start New Order →");
        newOrderBtn.addActionListener(e -> frame.startFreshCustomerOrder());

        actions.add(saveTxtBtn);
        actions.add(printBtn);
        actions.add(newOrderBtn);

        card.add(statusBar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Refreshes checkout financial totals from the active cart and customer details.
     */
    public void refreshFinancialSummary() {
        Customer cust = frame.getActiveCustomer();
        String customerName = cust != null ? cust.getName() : "Walk-In Guest";
        if (cardNameField.getText().trim().isEmpty()) {
            cardNameField.setText(customerName);
        }
        if (typedSignatureField.getText().trim().isEmpty()) {
            typedSignatureField.setText(customerName);
        }

        double subtotal = frame.getMenuOrderPanel().getCartSubtotal();
        String orderType = cust != null ? cust.getOrderType() : "delivery";
        double tipAmount = computeSelectedTip(subtotal);
        double[] totals = DataStore.calculateTotals(subtotal, orderType, tipAmount);

        breakdownLabel.setText(String.format(
                "<html>Subtotal: <b>$%.2f</b> &nbsp;|&nbsp; Delivery (%s): <b>$%.2f</b> &nbsp;|&nbsp; Tax (8%%): <b>$%.2f</b><br/>"
                        + "Tip: <b>$%.2f</b> &nbsp;|&nbsp; <font color='#FDE68A' size='+1'>GRAND TOTAL: $%.2f</font></html>",
                totals[0], orderType, totals[1], totals[2], totals[3], totals[4]
        ));
        updateCashChangeLabel();
    }

    /**
     * Computes tip in USD from selected preset pill or custom amount field.
     *
     * @param subtotal current order subtotal
     * @return tip dollar amount
     */
    private double computeSelectedTip(double subtotal) {
        if (tip0Pill.isSelected()) {
            return 0.0;
        }
        if (tip15Pill.isSelected()) {
            return MenuCatalog.round2(subtotal * 0.15);
        }
        if (tip18Pill.isSelected()) {
            return MenuCatalog.round2(subtotal * 0.18);
        }
        if (tip20Pill.isSelected()) {
            return MenuCatalog.round2(subtotal * 0.20);
        }
        try {
            return MenuCatalog.round2(Math.max(0.0, Double.parseDouble(customTipField.getText().trim())));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    /**
     * Updates the cash change due label based on entered cash tendered.
     */
    private void updateCashChangeLabel() {
        double subtotal = frame.getMenuOrderPanel().getCartSubtotal();
        Customer cust = frame.getActiveCustomer();
        String orderType = cust != null ? cust.getOrderType() : "delivery";
        double[] totals = DataStore.calculateTotals(subtotal, orderType, computeSelectedTip(subtotal));
        try {
            double tendered = Double.parseDouble(cashTenderedField.getText().trim());
            double change = Math.max(0.0, MenuCatalog.round2(tendered - totals[4]));
            cashChangeLabel.setText("Change Due: " + Theme.formatCurrency(change));
        } catch (NumberFormatException ex) {
            cashChangeLabel.setText("Change Due: $0.00");
        }
    }

    /**
     * Validates payment method inputs and signature, persists the order to {@code data/orders.txt},
     * displays the confirmed paper receipt, and clears the cart.
     */
    private void submitOrder() {
        List<OrderItem> items = frame.getMenuOrderPanel().getCartItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.", "Cannot Submit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer cust = frame.getActiveCustomer();
        String name = cust != null ? cust.getName() : "Walk-In Guest";
        String phone = cust != null ? cust.getPhone() : "0000000000";
        String orderType = cust != null ? cust.getOrderType() : "pickup";
        String address = cust != null ? cust.getFullAddressSummary() : "Pickup at Counter";

        double subtotal = frame.getMenuOrderPanel().getCartSubtotal();
        double tip = computeSelectedTip(subtotal);
        double[] totals = DataStore.calculateTotals(subtotal, orderType, tip);
        double grandTotal = totals[4];

        String method;
        String details;
        if (creditPill.isSelected() || debitPill.isSelected()) {
            method = creditPill.isSelected() ? "Credit" : "Debit";
            String digits = cardNumberField.getText().replaceAll("\\D", "");
            if (cardNameField.getText().trim().isEmpty() || digits.length() < 12) {
                JOptionPane.showMessageDialog(this, "Enter cardholder name and a valid card number.", "Card Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            details = "Card ending in " + digits.substring(digits.length() - 4);
        } else if (cashPill.isSelected()) {
            method = "Cash";
            double tendered;
            try {
                tendered = Double.parseDouble(cashTenderedField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid cash amount tendered.", "Cash Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (MenuCatalog.round2(tendered) < grandTotal) {
                JOptionPane.showMessageDialog(this, "Cash tendered must cover total due ($" + String.format("%.2f", grandTotal) + ").", "Insufficient Cash", JOptionPane.ERROR_MESSAGE);
                return;
            }
            details = String.format("Tendered $%.2f (Change $%.2f)", tendered, MenuCatalog.round2(tendered - grandTotal));
        } else {
            method = "Check";
            String chkNum = checkNumberField.getText().trim();
            if (chkNum.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter the check number.", "Check Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double chkAmt;
            try {
                chkAmt = Double.parseDouble(checkAmountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid check amount.", "Check Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (MenuCatalog.round2(chkAmt) < grandTotal) {
                JOptionPane.showMessageDialog(this, "Check amount must cover total due ($" + String.format("%.2f", grandTotal) + ").", "Insufficient Check", JOptionPane.ERROR_MESSAGE);
                return;
            }
            details = String.format("Check #%s ($%.2f)", chkNum, chkAmt);
        }

        String typedSig = typedSignatureField.getText().trim();
        if (typedSig.isEmpty() && !signatureCanvas.hasSignature()) {
            JOptionPane.showMessageDialog(this, "Please type your signature or draw on the signature canvas.", "Signature Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!authorizeCheck.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please check the authorization confirmation box.", "Consent Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String signatureRecord = typedSig.isEmpty() ? "[Drawn Signature] " + name : typedSig;
        StringBuilder summarySb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                summarySb.append(" ;; ");
            }
            summarySb.append(items.get(i).toCompactSummary());
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        OrderRecord order = new OrderRecord(
                0,
                now,
                name,
                phone,
                orderType,
                address,
                method,
                details,
                totals[0],
                totals[1],
                totals[2],
                totals[3],
                totals[4],
                signatureRecord,
                summarySb.toString(),
                items
        );

        latestCompletedOrder = dataStore.saveOrder(order);
        receiptStatusLabel.setForeground(Theme.STATUS_SUCCESS);
        receiptStatusLabel.setText("● Payment Confirmed — Order #" + latestCompletedOrder.getOrderId());
        receiptArea.setText(latestCompletedOrder.formatReceiptText());
        receiptArea.setCaretPosition(0);

        frame.getMenuOrderPanel().clearCart();
        frame.getAdminPanel().refreshAllTables();
        JOptionPane.showMessageDialog(this, "Order #" + latestCompletedOrder.getOrderId() + " confirmed and saved to data/orders.txt!", "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exports the displayed receipt text into {@code data/receipt_<orderId>.txt}.
     */
    private void exportReceiptTextFile() {
        if (latestCompletedOrder == null) {
            JOptionPane.showMessageDialog(this, "Complete an order first to save a receipt.", "No Receipt", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Path out = dataStore.getDataDir().resolve("receipt_" + latestCompletedOrder.getOrderId() + ".txt");
        try {
            Files.write(out, receiptArea.getText().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Saved receipt to: " + out.toAbsolutePath(), "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Sends the receipt text area to the operating system print dialog.
     */
    private void printReceipt() {
        try {
            receiptArea.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
