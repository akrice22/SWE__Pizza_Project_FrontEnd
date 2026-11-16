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
import javax.swing.JRadioButton;
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
 * Step 3 checkout and receipt screen handling payment method selection
 * (Cash, Check, Credit, Debit), gratuity calculation, signature authorization,
 * order persistence to {@code data/orders.txt}, and formatted thermal-style receipt display.
 */
public class CheckoutReceiptPanel extends JPanel {

    /** Local file persistence store. */
    private final DataStore dataStore;

    /** Parent frame coordinator. */
    private final PizzaAppFrame frame;

    /** Credit card payment method selector. */
    private final JRadioButton creditRadio;

    /** Debit card payment method selector. */
    private final JRadioButton debitRadio;

    /** Cash payment method selector. */
    private final JRadioButton cashRadio;

    /** Physical check payment method selector. */
    private final JRadioButton checkRadio;

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

    /** Custom or preset tip percentage/dollar controls. */
    private final JRadioButton tip0Radio;

    /** 15% preset tip option. */
    private final JRadioButton tip15Radio;

    /** 18% preset tip option. */
    private final JRadioButton tip18Radio;

    /** 20% preset tip option. */
    private final JRadioButton tip20Radio;

    /** Custom dollar tip option. */
    private final JRadioButton tipCustomRadio;

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

    /** Monospaced text area rendering the formatted receipt. */
    private final JTextArea receiptArea;

    /** Most recently completed order record shown in the receipt pane. */
    private OrderRecord latestCompletedOrder;

    /**
     * Constructs the {@code CheckoutReceiptPanel}.
     *
     * @param dataStore shared {@link DataStore} instance
     * @param frame     parent {@link PizzaAppFrame} coordinator
     */
    public CheckoutReceiptPanel(DataStore dataStore, PizzaAppFrame frame) {
        this.dataStore = dataStore;
        this.frame = frame;
        this.creditRadio = new JRadioButton("Credit", true);
        this.debitRadio = new JRadioButton("Debit", false);
        this.cashRadio = new JRadioButton("Cash", false);
        this.checkRadio = new JRadioButton("Check", false);
        this.tenderLayout = new CardLayout();
        this.tenderPanel = new JPanel(tenderLayout);

        this.cardNameField = Theme.createTextField(18);
        this.cardNumberField = Theme.createTextField(18);
        this.cardExpiryField = Theme.createTextField(8);
        this.cardCvvField = Theme.createTextField(6);
        this.cashTenderedField = Theme.createTextField(10);
        this.cashChangeLabel = new JLabel("Change Due: $0.00");
        this.checkNumberField = Theme.createTextField(10);
        this.checkAmountField = Theme.createTextField(10);

        this.tip0Radio = new JRadioButton("0%", false);
        this.tip15Radio = new JRadioButton("15%", true);
        this.tip18Radio = new JRadioButton("18%", false);
        this.tip20Radio = new JRadioButton("20%", false);
        this.tipCustomRadio = new JRadioButton("Custom $", false);
        this.customTipField = Theme.createTextField(7);

        this.breakdownLabel = new JLabel();
        this.typedSignatureField = Theme.createTextField(20);
        this.signatureCanvas = new SignatureCanvasPanel();
        this.authorizeCheck = new JCheckBox("I authorize this transaction and confirm my signature.");
        this.receiptArea = new JTextArea();

        buildLayout();
    }

    /**
     * Builds the split checkout form (left) and formatted paper receipt display (right).
     */
    private void buildLayout() {
        setLayout(new GridLayout(1, 2, 16, 0));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        add(buildPaymentFormCard());
        add(buildReceiptCard());
    }

    /**
     * Constructs the left-hand payment, tip, and signature authorization card.
     *
     * @return payment form {@link JPanel}
     */
    private JPanel buildPaymentFormCard() {
        JPanel card = Theme.createCardPanel("Step 3: Checkout & Payment Authorization");
        card.setLayout(new BorderLayout(0, 10));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // Payment Method Radios
        ButtonGroup payGroup = new ButtonGroup();
        payGroup.add(creditRadio);
        payGroup.add(debitRadio);
        payGroup.add(cashRadio);
        payGroup.add(checkRadio);
        styleRadio(creditRadio);
        styleRadio(debitRadio);
        styleRadio(cashRadio);
        styleRadio(checkRadio);

        JPanel payRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        payRow.setOpaque(false);
        payRow.add(Theme.createFieldLabel("Payment:"));
        payRow.add(creditRadio);
        payRow.add(debitRadio);
        payRow.add(cashRadio);
        payRow.add(checkRadio);

        creditRadio.addActionListener(e -> tenderLayout.show(tenderPanel, "CARD"));
        debitRadio.addActionListener(e -> tenderLayout.show(tenderPanel, "CARD"));
        cashRadio.addActionListener(e -> {
            tenderLayout.show(tenderPanel, "CASH");
            refreshFinancialSummary();
        });
        checkRadio.addActionListener(e -> {
            tenderLayout.show(tenderPanel, "CHECK");
            refreshFinancialSummary();
        });

        // Dynamic Tender Details
        tenderPanel.setOpaque(false);
        tenderPanel.add(buildCardSubPanel(), "CARD");
        tenderPanel.add(buildCashSubPanel(), "CASH");
        tenderPanel.add(buildCheckSubPanel(), "CHECK");
        tenderLayout.show(tenderPanel, "CARD");

        // Tip Selection
        ButtonGroup tipGroup = new ButtonGroup();
        tipGroup.add(tip0Radio);
        tipGroup.add(tip15Radio);
        tipGroup.add(tip18Radio);
        tipGroup.add(tip20Radio);
        tipGroup.add(tipCustomRadio);
        styleRadio(tip0Radio);
        styleRadio(tip15Radio);
        styleRadio(tip18Radio);
        styleRadio(tip20Radio);
        styleRadio(tipCustomRadio);

        JPanel tipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tipRow.setOpaque(false);
        tipRow.add(Theme.createFieldLabel("Tip:"));
        tipRow.add(tip0Radio);
        tipRow.add(tip15Radio);
        tipRow.add(tip18Radio);
        tipRow.add(tip20Radio);
        tipRow.add(tipCustomRadio);
        customTipField.setText("0.00");
        customTipField.setEnabled(false);
        tipRow.add(customTipField);

        tip0Radio.addActionListener(e -> {
            customTipField.setEnabled(false);
            refreshFinancialSummary();
        });
        tip15Radio.addActionListener(e -> {
            customTipField.setEnabled(false);
            refreshFinancialSummary();
        });
        tip18Radio.addActionListener(e -> {
            customTipField.setEnabled(false);
            refreshFinancialSummary();
        });
        tip20Radio.addActionListener(e -> {
            customTipField.setEnabled(false);
            refreshFinancialSummary();
        });
        tipCustomRadio.addActionListener(e -> {
            customTipField.setEnabled(true);
            refreshFinancialSummary();
        });

        JButton applyTipBtn = Theme.createSecondaryButton("Recalculate");
        applyTipBtn.addActionListener(e -> refreshFinancialSummary());
        tipRow.add(applyTipBtn);

        // Totals Summary Box
        JPanel totalsCard = Theme.createCardPanel("Order Summary");
        totalsCard.setLayout(new FlowLayout(FlowLayout.LEFT));
        breakdownLabel.setFont(Theme.FONT_MONO);
        breakdownLabel.setForeground(Theme.TEXT_PRIMARY);
        totalsCard.add(breakdownLabel);

        // Signature Block
        JPanel sigPanel = Theme.createCardPanel("Customer Signature Confirmation");
        sigPanel.setLayout(new BorderLayout(6, 6));

        JPanel typedRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        typedRow.setOpaque(false);
        typedRow.add(Theme.createFieldLabel("Typed Signature:"));
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
        body.add(Box.createVerticalStrut(6));
        body.add(tenderPanel);
        body.add(Box.createVerticalStrut(6));
        body.add(tipRow);
        body.add(Box.createVerticalStrut(8));
        body.add(totalsCard);
        body.add(Box.createVerticalStrut(8));
        body.add(sigPanel);

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        footer.setOpaque(false);
        JButton backBtn = Theme.createSecondaryButton("<- Back to Menu");
        backBtn.addActionListener(e -> frame.showCard(PizzaAppFrame.CARD_MENU));

        JButton completeBtn = Theme.createPrimaryButton("Authorize & Complete Order ->");
        completeBtn.addActionListener(e -> submitOrder());

        footer.add(backBtn, BorderLayout.WEST);
        footer.add(completeBtn, BorderLayout.EAST);

        card.add(new JScrollPane(body), BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Builds Credit/Debit card fields sub-panel.
     *
     * @return card input {@link JPanel}
     */
    private JPanel buildCardSubPanel() {
        JPanel p = Theme.createCardPanel("Card Details");
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        cardNumberField.setText("4242424242424242");
        cardExpiryField.setText("12/28");
        cardCvvField.setText("123");

        c.gridy = 0;
        c.gridx = 0;
        p.add(Theme.createFieldLabel("Cardholder Name:"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        p.add(cardNameField, c);

        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0.0;
        p.add(Theme.createFieldLabel("Card Number (16 digits):"), c);
        c.gridx = 1;
        c.weightx = 1.0;
        p.add(cardNumberField, c);

        JPanel expCvv = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        expCvv.setOpaque(false);
        expCvv.add(Theme.createFieldLabel("Exp (MM/YY):"));
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
        JPanel p = Theme.createCardPanel("Cash Payment");
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.add(Theme.createFieldLabel("Amount Tendered ($):"));
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
        JPanel p = Theme.createCardPanel("Check Payment Details");
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.add(Theme.createFieldLabel("Check #:"));
        checkNumberField.setText("1042");
        p.add(checkNumberField);
        p.add(Theme.createFieldLabel("Check Amount ($):"));
        checkAmountField.setText("50.00");
        p.add(checkAmountField);
        return p;
    }

    /**
     * Constructs the right-hand paper receipt viewer and save/print controls.
     *
     * @return receipt viewer {@link JPanel}
     */
    private JPanel buildReceiptCard() {
        JPanel card = Theme.createCardPanel("Itemized Order Receipt");
        card.setLayout(new BorderLayout(0, 10));

        receiptArea.setEditable(false);
        receiptArea.setBackground(Theme.RECEIPT_PAPER);
        receiptArea.setForeground(Theme.RECEIPT_INK);
        receiptArea.setFont(Theme.FONT_MONO);
        receiptArea.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        receiptArea.setText("Complete payment on the left to generate your official receipt...");

        JScrollPane scroll = new JScrollPane(receiptArea);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_STONE, 1));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton saveTxtBtn = Theme.createSecondaryButton("Save Receipt .txt");
        saveTxtBtn.addActionListener(e -> exportReceiptTextFile());

        JButton printBtn = Theme.createSecondaryButton("Print Receipt");
        printBtn.addActionListener(e -> printReceipt());

        JButton newOrderBtn = Theme.createPrimaryButton("Start New Order");
        newOrderBtn.addActionListener(e -> frame.startFreshCustomerOrder());

        actions.add(saveTxtBtn);
        actions.add(printBtn);
        actions.add(newOrderBtn);

        card.add(scroll, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Refreshes the checkout financial summary using current cart items and active customer details.
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
                        + "Tip: <b>$%.2f</b> &nbsp;|&nbsp; <font color='#FDE68A'>GRAND TOTAL: $%.2f</font></html>",
                totals[0], orderType, totals[1], totals[2], totals[3], totals[4]
        ));
        updateCashChangeLabel();
    }

    /**
     * Computes tip in USD based on the active tip preset or custom amount input.
     *
     * @param subtotal current order subtotal
     * @return tip dollar amount
     */
    private double computeSelectedTip(double subtotal) {
        if (tip0Radio.isSelected()) {
            return 0.0;
        }
        if (tip15Radio.isSelected()) {
            return MenuCatalog.round2(subtotal * 0.15);
        }
        if (tip18Radio.isSelected()) {
            return MenuCatalog.round2(subtotal * 0.18);
        }
        if (tip20Radio.isSelected()) {
            return MenuCatalog.round2(subtotal * 0.20);
        }
        try {
            return MenuCatalog.round2(Math.max(0.0, Double.parseDouble(customTipField.getText().trim())));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    /**
     * Updates the cash change due label based on entered tendered amount.
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
     * Validates payment method inputs and signature, saves the order to {@code data/orders.txt},
     * renders the thermal receipt, and clears the active cart.
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
        if (creditRadio.isSelected() || debitRadio.isSelected()) {
            method = creditRadio.isSelected() ? "Credit" : "Debit";
            String digits = cardNumberField.getText().replaceAll("\\D", "");
            if (cardNameField.getText().trim().isEmpty() || digits.length() < 12) {
                JOptionPane.showMessageDialog(this, "Enter cardholder name and a valid card number.", "Card Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            details = "Card ending in " + digits.substring(digits.length() - 4);
        } else if (cashRadio.isSelected()) {
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
            JOptionPane.showMessageDialog(this, "Please type your signature or draw on the signature pad.", "Signature Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!authorizeCheck.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please check the authorization confirmation box before completing.", "Consent Required", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Complete an order first to export a receipt.", "No Receipt", JOptionPane.INFORMATION_MESSAGE);
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
     * Sends the receipt text area directly to the operating system print dialog.
     */
    private void printReceipt() {
        try {
            receiptArea.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Styles radio buttons uniformly.
     *
     * @param rb radio button
     */
    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(Theme.TEXT_PRIMARY);
        rb.setFont(Theme.FONT_BODY);
    }
}
