package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.MenuCatalog;
import com.momandpop.pizza.model.MenuCatalog.MenuOption;
import com.momandpop.pizza.model.OrderItem;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Step 2 ordering screen featuring the Pizza Builder (4 sizes, 4 crusts, 3 sauces,
 * 12 toppings with strict max-4 limit enforcement), Beverage Selector (6 beverages
 * across 3 sizes), real-time pricing, and an interactive Order Cart.
 */
public class MenuOrderPanel extends JPanel {

    /** Parent frame coordinator. */
    private final PizzaAppFrame frame;

    /** Active cart list stored in memory for the current session. */
    private final List<OrderItem> cartItems;

    /** Swing list model backing the visual cart component. */
    private final DefaultListModel<OrderItem> cartListModel;

    /** Visual cart list component. */
    private final JList<OrderItem> cartList;

    /** Dropdown selector for pizza size (4 sizes). */
    private final JComboBox<MenuOption> sizeCombo;

    /** Dropdown selector for crust option (4 crusts). */
    private final JComboBox<MenuOption> crustCombo;

    /** Dropdown selector for sauce intensity (3 options). */
    private final JComboBox<String> sauceCombo;

    /** Checkboxes representing all 12 toppings (6 veggies + 6 meats). */
    private final List<JCheckBox> toppingCheckboxes;

    /** Warning label displayed when user hits or exceeds the 4-topping limit. */
    private final JLabel toppingCounterLabel;

    /** Live price label for the currently configured pizza. */
    private final JLabel livePizzaPriceLabel;

    /** Button to add or update the configured pizza in the cart. */
    private final JButton addPizzaBtn;

    /** Index of the pizza currently being edited in the cart, or {@code -1} if adding new. */
    private int editingCartIndex;

    /** Dropdown selector for beverages (6 beverages). */
    private final JComboBox<MenuOption> beverageCombo;

    /** Dropdown selector for beverage sizes (3 sizes). */
    private final JComboBox<MenuOption> beverageSizeCombo;

    /** Quantity spinner for adding beverages. */
    private final JSpinner beverageQtySpinner;

    /** Live price label for the currently selected beverage and quantity. */
    private final JLabel liveBeveragePriceLabel;

    /** Cart subtotal display label. */
    private final JLabel subtotalLabel;

    /** Cart delivery fee display label. */
    private final JLabel deliveryFeeLabel;

    /** Cart estimated tax display label. */
    private final JLabel taxLabel;

    /** Cart estimated total display label. */
    private final JLabel estimatedTotalLabel;

    /**
     * Constructs the {@code MenuOrderPanel}.
     *
     * @param frame parent {@link PizzaAppFrame} coordinator
     */
    public MenuOrderPanel(PizzaAppFrame frame) {
        this.frame = frame;
        this.cartItems = new ArrayList<>();
        this.cartListModel = new DefaultListModel<>();
        this.cartList = new JList<>(cartListModel);
        this.sizeCombo = new JComboBox<>(MenuCatalog.PIZZA_SIZES.toArray(new MenuOption[0]));
        this.crustCombo = new JComboBox<>(MenuCatalog.CRUST_OPTIONS.toArray(new MenuOption[0]));
        this.sauceCombo = new JComboBox<>(MenuCatalog.SAUCE_LEVELS.toArray(new String[0]));
        this.toppingCheckboxes = new ArrayList<>();
        this.toppingCounterLabel = new JLabel("Selected Toppings: 0 / 4 (Maximum 4 toppings per pizza)");
        this.livePizzaPriceLabel = new JLabel("Pizza Price: $13.99");
        this.addPizzaBtn = Theme.createPrimaryButton("+ Add Pizza to Cart");
        this.editingCartIndex = -1;

        this.beverageCombo = new JComboBox<>(MenuCatalog.BEVERAGES.toArray(new MenuOption[0]));
        this.beverageSizeCombo = new JComboBox<>(MenuCatalog.BEVERAGE_SIZES.toArray(new MenuOption[0]));
        this.beverageQtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        this.liveBeveragePriceLabel = new JLabel("Beverage Total: $2.50");

        this.subtotalLabel = new JLabel("Subtotal: $0.00");
        this.deliveryFeeLabel = new JLabel("Delivery Fee: $3.99");
        this.taxLabel = new JLabel("Est. Tax (8%): $0.00");
        this.estimatedTotalLabel = new JLabel("Estimated Total: $0.00");

        buildLayout();
        resetPizzaBuilder();
        updateLiveBeveragePrice();
        refreshCartSummary();
    }

    /**
     * Assembles the left customization column (Pizza + Beverages) and right Order Cart column.
     */
    private void buildLayout() {
        setLayout(new BorderLayout(16, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);
        leftColumn.add(buildPizzaBuilderCard());
        leftColumn.add(Box.createVerticalStrut(12));
        leftColumn.add(buildBeverageCard());

        JPanel rightColumn = buildCartCard();

        JPanel mainSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        mainSplit.setOpaque(false);
        mainSplit.add(leftColumn);
        mainSplit.add(rightColumn);

        add(mainSplit, BorderLayout.CENTER);
    }

    /**
     * Builds the interactive Pizza Builder card enforcing size, crust, sauce, and max 4 toppings.
     *
     * @return constructed pizza builder {@link JPanel}
     */
    private JPanel buildPizzaBuilderCard() {
        JPanel card = Theme.createCardPanel("Step 2A: Custom Pizza Builder (Max 4 Toppings)");
        card.setLayout(new BorderLayout(0, 10));

        JPanel baseSelectors = new JPanel(new GridLayout(3, 2, 8, 6));
        baseSelectors.setOpaque(false);

        sizeCombo.setSelectedIndex(1); // Default Medium (12")
        sauceCombo.setSelectedIndex(1); // Default Normal Sauce
        styleCombo(sizeCombo);
        styleCombo(crustCombo);
        styleCombo(sauceCombo);

        sizeCombo.addActionListener(e -> updateLivePizzaPrice());
        crustCombo.addActionListener(e -> updateLivePizzaPrice());
        sauceCombo.addActionListener(e -> updateLivePizzaPrice());

        baseSelectors.add(Theme.createFieldLabel("1. Pizza Size (4 options):"));
        baseSelectors.add(sizeCombo);
        baseSelectors.add(Theme.createFieldLabel("2. Crust Style (4 options):"));
        baseSelectors.add(crustCombo);
        baseSelectors.add(Theme.createFieldLabel("3. Sauce Level:"));
        baseSelectors.add(sauceCombo);

        // Toppings Grid (6 Veggies + 6 Meats = 12 Toppings)
        JPanel toppingsWrapper = new JPanel(new BorderLayout(0, 6));
        toppingsWrapper.setOpaque(false);

        toppingCounterLabel.setFont(Theme.FONT_LABEL);
        toppingCounterLabel.setForeground(Theme.ACCENT_AMBER);
        toppingsWrapper.add(toppingCounterLabel, BorderLayout.NORTH);

        JPanel toppingsGrid = new JPanel(new GridLayout(6, 2, 6, 4));
        toppingsGrid.setOpaque(false);

        List<MenuOption> allToppings = new ArrayList<>();
        allToppings.addAll(MenuCatalog.VEGGIE_TOPPINGS);
        allToppings.addAll(MenuCatalog.MEAT_TOPPINGS);

        for (MenuOption top : allToppings) {
            String label = String.format("%s (+$%.2f)", top.getName(), top.getPrice());
            JCheckBox cb = new JCheckBox(label);
            cb.setActionCommand(top.getName());
            cb.setOpaque(false);
            cb.setForeground(Theme.TEXT_PRIMARY);
            cb.setFont(Theme.FONT_BODY);
            cb.addActionListener(e -> handleToppingToggle(cb));
            toppingCheckboxes.add(cb);
            toppingsGrid.add(cb);
        }
        toppingsWrapper.add(toppingsGrid, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(8, 0));
        bottomBar.setOpaque(false);
        livePizzaPriceLabel.setFont(Theme.FONT_MONO_BOLD);
        livePizzaPriceLabel.setForeground(Theme.ACCENT_AMBER);

        addPizzaBtn.addActionListener(e -> saveCurrentPizzaToCart());
        bottomBar.add(livePizzaPriceLabel, BorderLayout.WEST);
        bottomBar.add(addPizzaBtn, BorderLayout.EAST);

        card.add(baseSelectors, BorderLayout.NORTH);
        card.add(toppingsWrapper, BorderLayout.CENTER);
        card.add(bottomBar, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Builds the Craft Beverages selector card supporting 6 drinks across 3 drink sizes.
     *
     * @return constructed beverage {@link JPanel}
     */
    private JPanel buildBeverageCard() {
        JPanel card = Theme.createCardPanel("Step 2B: Craft Beverages (3 Sizes)");
        card.setLayout(new BorderLayout(0, 10));

        JPanel grid = new JPanel(new GridLayout(3, 2, 8, 6));
        grid.setOpaque(false);

        styleCombo(beverageCombo);
        styleCombo(beverageSizeCombo);
        beverageCombo.addActionListener(e -> updateLiveBeveragePrice());
        beverageSizeCombo.addActionListener(e -> updateLiveBeveragePrice());
        beverageQtySpinner.addChangeListener(e -> updateLiveBeveragePrice());

        grid.add(Theme.createFieldLabel("Beverage:"));
        grid.add(beverageCombo);
        grid.add(Theme.createFieldLabel("Drink Size (S / M / L):"));
        grid.add(beverageSizeCombo);
        grid.add(Theme.createFieldLabel("Quantity:"));
        grid.add(beverageQtySpinner);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        liveBeveragePriceLabel.setFont(Theme.FONT_MONO_BOLD);
        liveBeveragePriceLabel.setForeground(Theme.ACCENT_AMBER);

        JButton addBevBtn = Theme.createSecondaryButton("+ Add Beverage");
        addBevBtn.addActionListener(e -> addCurrentBeverageToCart());

        bottom.add(liveBeveragePriceLabel, BorderLayout.WEST);
        bottom.add(addBevBtn, BorderLayout.EAST);

        card.add(grid, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Builds the right-hand Order Cart card showing active items, edit/remove actions, and financial summary.
     *
     * @return constructed cart {@link JPanel}
     */
    private JPanel buildCartCard() {
        JPanel card = Theme.createCardPanel("Current Order Cart");
        card.setLayout(new BorderLayout(0, 10));

        cartList.setBackground(Theme.BG_INPUT);
        cartList.setForeground(Theme.TEXT_PRIMARY);
        cartList.setFont(Theme.FONT_MONO);
        cartList.setFixedCellHeight(28);

        JScrollPane scroll = new JScrollPane(cartList);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_STONE, 1));

        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cartActions.setOpaque(false);

        JButton editBtn = Theme.createSecondaryButton("Edit Selected Pizza");
        editBtn.addActionListener(e -> editSelectedItem());

        JButton removeBtn = Theme.createSecondaryButton("Remove Selected");
        removeBtn.addActionListener(e -> removeSelectedItem());

        JButton clearBtn = Theme.createSecondaryButton("Clear Cart");
        clearBtn.addActionListener(e -> clearCart());

        cartActions.add(editBtn);
        cartActions.add(removeBtn);
        cartActions.add(clearBtn);

        JPanel summaryPanel = new JPanel(new GridLayout(4, 1, 0, 4));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        styleSummaryLabel(subtotalLabel, false);
        styleSummaryLabel(deliveryFeeLabel, false);
        styleSummaryLabel(taxLabel, false);
        styleSummaryLabel(estimatedTotalLabel, true);
        summaryPanel.add(subtotalLabel);
        summaryPanel.add(deliveryFeeLabel);
        summaryPanel.add(taxLabel);
        summaryPanel.add(estimatedTotalLabel);

        JPanel navActions = new JPanel(new BorderLayout(8, 0));
        navActions.setOpaque(false);

        JButton backCustomerBtn = Theme.createSecondaryButton("<- Edit Customer Info");
        backCustomerBtn.addActionListener(e -> frame.showCard(PizzaAppFrame.CARD_CUSTOMER));

        JButton checkoutBtn = Theme.createPrimaryButton("Proceed to Checkout & Receipt ->");
        checkoutBtn.addActionListener(e -> proceedToCheckout());

        navActions.add(backCustomerBtn, BorderLayout.WEST);
        navActions.add(checkoutBtn, BorderLayout.EAST);

        JPanel southContainer = new JPanel();
        southContainer.setLayout(new BoxLayout(southContainer, BoxLayout.Y_AXIS));
        southContainer.setOpaque(false);
        southContainer.add(cartActions);
        southContainer.add(summaryPanel);
        southContainer.add(navActions);

        card.add(scroll, BorderLayout.CENTER);
        card.add(southContainer, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Enforces the strict maximum of 4 toppings per pizza whenever a topping checkbox is toggled.
     *
     * @param source the {@link JCheckBox} clicked by the user
     */
    private void handleToppingToggle(JCheckBox source) {
        int count = countSelectedToppings();
        if (count > MenuCatalog.MAX_TOPPINGS_PER_PIZZA) {
            source.setSelected(false);
            count = countSelectedToppings();
            toppingCounterLabel.setForeground(Theme.STATUS_ERROR);
            toppingCounterLabel.setText("Limit Reached: Maximum " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA + " toppings per pizza!");
            JOptionPane.showMessageDialog(
                    this,
                    "A maximum of " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA + " toppings is allowed per pizza.\n"
                            + "Uncheck an existing topping first before adding another.",
                    "Maximum 4 Toppings Enforced",
                    JOptionPane.WARNING_MESSAGE
            );
        } else {
            toppingCounterLabel.setForeground(count == MenuCatalog.MAX_TOPPINGS_PER_PIZZA ? Theme.STATUS_SUCCESS : Theme.ACCENT_AMBER);
            toppingCounterLabel.setText("Selected Toppings: " + count + " / " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA
                    + " (Maximum " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA + " toppings per pizza)");
        }
        updateLivePizzaPrice();
    }

    /**
     * Counts how many topping checkboxes are currently selected.
     *
     * @return selected topping count
     */
    private int countSelectedToppings() {
        int count = 0;
        for (JCheckBox cb : toppingCheckboxes) {
            if (cb.isSelected()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Computes the live price of the pizza currently configured in the builder.
     *
     * @return computed pizza price in USD
     */
    public double computeCurrentPizzaPrice() {
        MenuOption size = (MenuOption) sizeCombo.getSelectedItem();
        MenuOption crust = (MenuOption) crustCombo.getSelectedItem();
        double price = (size != null ? size.getPrice() : 0.0) + (crust != null ? crust.getPrice() : 0.0);

        for (JCheckBox cb : toppingCheckboxes) {
            if (cb.isSelected()) {
                MenuOption top = MenuCatalog.findToppingByName(cb.getActionCommand());
                if (top != null) {
                    price += top.getPrice();
                }
            }
        }
        return MenuCatalog.round2(price);
    }

    /**
     * Computes the unit price of the currently selected beverage and drink size.
     *
     * @return computed beverage unit price in USD
     */
    public double computeCurrentBeverageUnitPrice() {
        MenuOption bev = (MenuOption) beverageCombo.getSelectedItem();
        MenuOption size = (MenuOption) beverageSizeCombo.getSelectedItem();
        double unit = (bev != null ? bev.getPrice() : 0.0) + (size != null ? size.getPrice() : 0.0);
        return MenuCatalog.round2(unit);
    }

    /**
     * Updates the live pizza price label.
     */
    private void updateLivePizzaPrice() {
        livePizzaPriceLabel.setText("Pizza Price: " + Theme.formatCurrency(computeCurrentPizzaPrice()));
    }

    /**
     * Updates the live beverage price label based on drink, size, and quantity.
     */
    private void updateLiveBeveragePrice() {
        double unit = computeCurrentBeverageUnitPrice();
        int qty = ((Number) beverageQtySpinner.getValue()).intValue();
        double total = MenuCatalog.round2(unit * qty);
        liveBeveragePriceLabel.setText(String.format("Beverage Total: $%.2f ($%.2f ea)", total, unit));
    }

    /**
     * Saves the configured pizza into the cart (or updates an existing edited item).
     */
    private void saveCurrentPizzaToCart() {
        MenuOption size = (MenuOption) sizeCombo.getSelectedItem();
        MenuOption crust = (MenuOption) crustCombo.getSelectedItem();
        String sauce = (String) sauceCombo.getSelectedItem();

        List<String> selectedToppings = new ArrayList<>();
        for (JCheckBox cb : toppingCheckboxes) {
            if (cb.isSelected()) {
                selectedToppings.add(cb.getActionCommand());
            }
        }
        if (selectedToppings.size() > MenuCatalog.MAX_TOPPINGS_PER_PIZZA) {
            JOptionPane.showMessageDialog(this, "Maximum 4 toppings allowed.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double price = computeCurrentPizzaPrice();
        OrderItem pizzaItem = OrderItem.createPizza(
                size != null ? size.getName() : "Medium (12\")",
                crust != null ? crust.getName() : "Classic Hand-Tossed",
                sauce != null ? sauce : "Normal Sauce",
                selectedToppings,
                price
        );

        if (editingCartIndex >= 0 && editingCartIndex < cartItems.size()) {
            cartItems.set(editingCartIndex, pizzaItem);
            cartListModel.set(editingCartIndex, pizzaItem);
            editingCartIndex = -1;
            addPizzaBtn.setText("+ Add Pizza to Cart");
        } else {
            cartItems.add(pizzaItem);
            cartListModel.addElement(pizzaItem);
        }
        resetPizzaBuilder();
        refreshCartSummary();
    }

    /**
     * Adds the configured beverage and quantity into the cart.
     */
    private void addCurrentBeverageToCart() {
        MenuOption bev = (MenuOption) beverageCombo.getSelectedItem();
        MenuOption size = (MenuOption) beverageSizeCombo.getSelectedItem();
        int qty = ((Number) beverageQtySpinner.getValue()).intValue();
        double unit = computeCurrentBeverageUnitPrice();

        OrderItem bevItem = OrderItem.createBeverage(
                bev != null ? bev.getName() : "Beverage",
                size != null ? size.getName() : "Small (12 oz)",
                unit,
                qty
        );
        cartItems.add(bevItem);
        cartListModel.addElement(bevItem);
        beverageQtySpinner.setValue(1);
        refreshCartSummary();
    }

    /**
     * Loads a selected pizza from the cart back into the builder for editing.
     */
    private void editSelectedItem() {
        int idx = cartList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select an item in the cart first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        OrderItem item = cartItems.get(idx);
        if (!item.isPizza()) {
            JOptionPane.showMessageDialog(this, "Only custom pizzas can be loaded back into the builder. Use Remove to adjust beverages.", "Beverage Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        editingCartIndex = idx;
        addPizzaBtn.setText("Update Pizza in Cart");

        for (int i = 0; i < sizeCombo.getItemCount(); i++) {
            if (sizeCombo.getItemAt(i).getName().equals(item.getSizeLabel())) {
                sizeCombo.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < crustCombo.getItemCount(); i++) {
            if (crustCombo.getItemAt(i).getName().equals(item.getCrustLabel())) {
                crustCombo.setSelectedIndex(i);
                break;
            }
        }
        sauceCombo.setSelectedItem(item.getSauceLabel());

        for (JCheckBox cb : toppingCheckboxes) {
            cb.setSelected(item.getToppings().contains(cb.getActionCommand()));
        }
        int count = countSelectedToppings();
        toppingCounterLabel.setForeground(Theme.ACCENT_AMBER);
        toppingCounterLabel.setText("Selected Toppings: " + count + " / 4 (Editing cart item #" + (idx + 1) + ")");
        updateLivePizzaPrice();
    }

    /**
     * Removes the currently selected line item from the order cart.
     */
    private void removeSelectedItem() {
        int idx = cartList.getSelectedIndex();
        if (idx >= 0) {
            cartItems.remove(idx);
            cartListModel.remove(idx);
            if (editingCartIndex == idx) {
                editingCartIndex = -1;
                addPizzaBtn.setText("+ Add Pizza to Cart");
            }
            refreshCartSummary();
        }
    }

    /**
     * Clears all items from the active order cart.
     */
    public void clearCart() {
        cartItems.clear();
        cartListModel.clear();
        editingCartIndex = -1;
        addPizzaBtn.setText("+ Add Pizza to Cart");
        refreshCartSummary();
    }

    /**
     * Resets the pizza builder controls to default selections.
     */
    private void resetPizzaBuilder() {
        sizeCombo.setSelectedIndex(1);
        crustCombo.setSelectedIndex(0);
        sauceCombo.setSelectedIndex(1);
        for (JCheckBox cb : toppingCheckboxes) {
            cb.setSelected(false);
        }
        toppingCounterLabel.setForeground(Theme.ACCENT_AMBER);
        toppingCounterLabel.setText("Selected Toppings: 0 / 4 (Maximum 4 toppings per pizza)");
        updateLivePizzaPrice();
    }

    /**
     * Recalculates and updates cart subtotal, delivery fee, tax, and top navigation badge.
     */
    public void refreshCartSummary() {
        double sub = getCartSubtotal();
        Customer activeCustomer = frame.getActiveCustomer();
        String orderType = activeCustomer != null ? activeCustomer.getOrderType() : "delivery";
        double[] totals = DataStore.calculateTotals(sub, orderType, 0.0);

        subtotalLabel.setText("Subtotal: " + Theme.formatCurrency(totals[0]));
        deliveryFeeLabel.setText("Delivery Fee (" + orderType + "): " + Theme.formatCurrency(totals[1]));
        taxLabel.setText("Est. Tax (8%): " + Theme.formatCurrency(totals[2]));
        estimatedTotalLabel.setText("Estimated Total (before tip): " + Theme.formatCurrency(totals[4]));
        frame.updateCartBadge(cartItems.size(), totals[0]);
    }

    /**
     * Validates that the cart is non-empty and transitions to checkout.
     */
    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Add at least one pizza or beverage before checking out.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        frame.openCheckoutScreen();
    }

    /**
     * Returns the cumulative subtotal of all items currently in the cart.
     *
     * @return subtotal in USD
     */
    public double getCartSubtotal() {
        double sum = 0.0;
        for (OrderItem item : cartItems) {
            sum += item.getTotalPrice();
        }
        return MenuCatalog.round2(sum);
    }

    /**
     * Returns a copy of all items currently in the cart.
     *
     * @return list of {@link OrderItem} entries
     */
    public List<OrderItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    /**
     * Styles a combo box to match the dark stone theme.
     *
     * @param combo target {@link JComboBox}
     */
    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(Theme.FONT_BODY);
        combo.setBackground(Theme.BG_INPUT);
        combo.setForeground(Theme.TEXT_PRIMARY);
    }

    /**
     * Styles a cart financial summary label.
     *
     * @param lbl    target {@link JLabel}
     * @param isBold whether to highlight as final total
     */
    private void styleSummaryLabel(JLabel lbl, boolean isBold) {
        lbl.setFont(isBold ? Theme.FONT_MONO_BOLD : Theme.FONT_MONO);
        lbl.setForeground(isBold ? Theme.ACCENT_AMBER : Theme.TEXT_PRIMARY);
    }
}
