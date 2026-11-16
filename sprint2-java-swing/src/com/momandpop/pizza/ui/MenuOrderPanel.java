package com.momandpop.pizza.ui;

import com.momandpop.pizza.model.Customer;
import com.momandpop.pizza.model.MenuCatalog;
import com.momandpop.pizza.model.MenuCatalog.MenuOption;
import com.momandpop.pizza.model.OrderItem;
import com.momandpop.pizza.storage.DataStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Step 2 ordering screen featuring interactive pill selectors for Pizza Sizes (4),
 * Crusts (4), Sauces (3), Toppings (12 with strict max-4 cap), Beverages (6 across 3 sizes),
 * real-time price updates, and a custom-rendered Order Cart.
 */
public class MenuOrderPanel extends JPanel {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Parent frame coordinator. */
    private final PizzaAppFrame frame;

    /** Active cart items list. */
    private final List<OrderItem> cartItems;

    /** Swing list model backing the cart display. */
    private final DefaultListModel<OrderItem> cartListModel;

    /** Visual cart list component. */
    private final JList<OrderItem> cartList;

    /** Toggle pills for the 4 pizza sizes. */
    private final List<Theme.TogglePillButton> sizePills;

    /** Toggle pills for the 4 crust options. */
    private final List<Theme.TogglePillButton> crustPills;

    /** Toggle pills for the 3 sauce levels. */
    private final List<Theme.TogglePillButton> saucePills;

    /** Currently selected pizza size option. */
    private transient MenuOption selectedSize;

    /** Currently selected crust option. */
    private transient MenuOption selectedCrust;

    /** Currently selected sauce label. */
    private String selectedSauce;

    /** Toggle pills representing the 12 available toppings. */
    private final List<Theme.TogglePillButton> toppingPills;

    /** Warning label displayed when user hits or exceeds the 4-topping limit. */
    private final JLabel toppingCounterLabel;

    /** Live price label for the currently configured pizza. */
    private final JLabel livePizzaPriceLabel;

    /** Button to add or update the configured pizza in the cart. */
    private final JButton addPizzaBtn;

    /** Index of the pizza currently being edited, or {@code -1} when adding new. */
    private int editingCartIndex;

    /** Dropdown selector for the 6 craft beverages. */
    private final JComboBox<MenuOption> beverageCombo;

    /** Toggle pills for the 3 beverage sizes. */
    private final List<Theme.TogglePillButton> beverageSizePills;

    /** Currently selected beverage size option. */
    private transient MenuOption selectedBeverageSize;

    /** Quantity spinner for adding beverages. */
    private final JSpinner beverageQtySpinner;

    /** Live price label for the currently selected beverage. */
    private final JLabel liveBeveragePriceLabel;

    /** Cart subtotal label. */
    private final JLabel subtotalLabel;

    /** Cart delivery fee label. */
    private final JLabel deliveryFeeLabel;

    /** Cart estimated tax label. */
    private final JLabel taxLabel;

    /** Cart estimated total label. */
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
        this.sizePills = new ArrayList<>();
        this.crustPills = new ArrayList<>();
        this.saucePills = new ArrayList<>();
        this.toppingPills = new ArrayList<>();
        this.beverageSizePills = new ArrayList<>();

        this.selectedSize = MenuCatalog.PIZZA_SIZES.get(1);
        this.selectedCrust = MenuCatalog.CRUST_OPTIONS.get(0);
        this.selectedSauce = MenuCatalog.SAUCE_LEVELS.get(1);
        this.selectedBeverageSize = MenuCatalog.BEVERAGE_SIZES.get(0);

        this.toppingCounterLabel = new JLabel("Selected Toppings: 0 / 4 (Maximum 4 toppings per pizza)");
        this.livePizzaPriceLabel = new JLabel("Item Price: $13.99");
        this.addPizzaBtn = Theme.createPrimaryButton("+ Add Pizza to Order");
        this.editingCartIndex = -1;

        this.beverageCombo = new JComboBox<>(MenuCatalog.BEVERAGES.toArray(new MenuOption[0]));
        this.beverageQtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        this.liveBeveragePriceLabel = new JLabel("Drink Total: $2.50");

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
     * Assembles the two-column Menu Builder (left) and Styled Order Cart (right).
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

        JScrollPane leftScroll = new JScrollPane(leftColumn);
        leftScroll.setBorder(null);
        leftScroll.getViewport().setBackground(Theme.BG_APP);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel rightColumn = buildCartCard();

        JPanel mainSplit = new JPanel(new BorderLayout(18, 0));
        mainSplit.setOpaque(false);
        mainSplit.add(leftScroll, BorderLayout.CENTER);
        mainSplit.add(rightColumn, BorderLayout.EAST);

        add(mainSplit, BorderLayout.CENTER);
    }

    /**
     * Builds the custom Pizza Builder card with visual pill selectors and max-4 topping grid.
     *
     * @return constructed pizza builder {@link JPanel}
     */
    private JPanel buildPizzaBuilderCard() {
        Theme.RoundedCardPanel card = Theme.createCardPanel("Step 2A • Build Your Custom Pizza");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // 1. Sizes (4 options)
        card.add(leftLabel("1. SELECT SIZE (4 OPTIONS)"));
        JPanel sizeGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        sizeGrid.setOpaque(false);
        ButtonGroup sizeGroup = new ButtonGroup();
        for (int i = 0; i < MenuCatalog.PIZZA_SIZES.size(); i++) {
            MenuOption opt = MenuCatalog.PIZZA_SIZES.get(i);
            String txt = String.format("<html><center>%s<br/><small>$%.2f</small></center></html>", opt.getName(), opt.getPrice());
            Theme.TogglePillButton pill = new Theme.TogglePillButton(txt, i == 1);
            pill.setActionCommand(opt.getName());
            pill.addActionListener(e -> {
                selectedSize = opt;
                updateLivePizzaPrice();
            });
            sizeGroup.add(pill);
            sizePills.add(pill);
            sizeGrid.add(pill);
        }
        card.add(Box.createVerticalStrut(4));
        card.add(sizeGrid);
        card.add(Box.createVerticalStrut(10));

        // 2. Crusts (4 options)
        card.add(leftLabel("2. SELECT ARTISAN CRUST (4 OPTIONS)"));
        JPanel crustGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        crustGrid.setOpaque(false);
        ButtonGroup crustGroup = new ButtonGroup();
        for (int i = 0; i < MenuCatalog.CRUST_OPTIONS.size(); i++) {
            MenuOption opt = MenuCatalog.CRUST_OPTIONS.get(i);
            String priceTag = opt.getPrice() > 0 ? String.format("+$%.2f", opt.getPrice()) : opt.getDescription();
            String txt = String.format("<html><center>%s<br/><small>%s</small></center></html>", opt.getName(), priceTag);
            Theme.TogglePillButton pill = new Theme.TogglePillButton(txt, i == 0);
            pill.setActionCommand(opt.getName());
            pill.addActionListener(e -> {
                selectedCrust = opt;
                updateLivePizzaPrice();
            });
            crustGroup.add(pill);
            crustPills.add(pill);
            crustGrid.add(pill);
        }
        card.add(Box.createVerticalStrut(4));
        card.add(crustGrid);
        card.add(Box.createVerticalStrut(10));

        // 3. Sauce (3 levels)
        card.add(leftLabel("3. SELECT SAUCE LEVEL"));
        JPanel sauceGrid = new JPanel(new GridLayout(1, 3, 8, 0));
        sauceGrid.setOpaque(false);
        ButtonGroup sauceGroup = new ButtonGroup();
        for (int i = 0; i < MenuCatalog.SAUCE_LEVELS.size(); i++) {
            String lvl = MenuCatalog.SAUCE_LEVELS.get(i);
            Theme.TogglePillButton pill = new Theme.TogglePillButton(lvl, i == 1);
            pill.setActionCommand(lvl);
            pill.addActionListener(e -> selectedSauce = lvl);
            sauceGroup.add(pill);
            saucePills.add(pill);
            sauceGrid.add(pill);
        }
        card.add(Box.createVerticalStrut(4));
        card.add(sauceGrid);
        card.add(Box.createVerticalStrut(12));

        // 4. Toppings (Max 4 enforced)
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.add(leftLabel("4. SELECT TOPPINGS (MAX 4 PER PIZZA)"), BorderLayout.WEST);
        toppingCounterLabel.setFont(Theme.FONT_LABEL);
        toppingCounterLabel.setForeground(Theme.ACCENT_AMBER);
        topHeader.add(toppingCounterLabel, BorderLayout.EAST);
        card.add(topHeader);
        card.add(Box.createVerticalStrut(6));

        JPanel toppingSplit = new JPanel(new GridLayout(1, 2, 10, 0));
        toppingSplit.setOpaque(false);

        Theme.RoundedCardPanel veggieCard = Theme.createCardPanel("Garden Veggies");
        veggieCard.setLayout(new GridLayout(3, 2, 6, 6));
        for (MenuOption v : MenuCatalog.VEGGIE_TOPPINGS) {
            veggieCard.add(createToppingPill(v));
        }

        Theme.RoundedCardPanel meatCard = Theme.createCardPanel("Savory Meats");
        meatCard.setLayout(new GridLayout(3, 2, 6, 6));
        for (MenuOption m : MenuCatalog.MEAT_TOPPINGS) {
            meatCard.add(createToppingPill(m));
        }

        toppingSplit.add(veggieCard);
        toppingSplit.add(meatCard);
        card.add(toppingSplit);
        card.add(Box.createVerticalStrut(12));

        // Pizza Add Action Row
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);
        livePizzaPriceLabel.setFont(Theme.FONT_TITLE);
        livePizzaPriceLabel.setForeground(Theme.ACCENT_AMBER);
        addPizzaBtn.addActionListener(e -> saveCurrentPizzaToCart());
        actionBar.add(livePizzaPriceLabel, BorderLayout.WEST);
        actionBar.add(addPizzaBtn, BorderLayout.EAST);
        card.add(actionBar);

        return card;
    }

    /**
     * Creates a single toggleable topping pill button bound to the 4-topping cap rule.
     *
     * @param option topping {@link MenuOption}
     * @return styled {@link Theme.TogglePillButton}
     */
    private Theme.TogglePillButton createToppingPill(MenuOption option) {
        String label = String.format("<html><center>%s<br/><small>+$%.2f</small></center></html>",
                option.getName(), option.getPrice());
        Theme.TogglePillButton pill = new Theme.TogglePillButton(label, false);
        pill.setActionCommand(option.getName());
        pill.addActionListener(e -> handleToppingToggle(pill));
        toppingPills.add(pill);
        return pill;
    }

    /**
     * Builds the Craft Beverages selector card supporting 6 drinks across 3 drink sizes.
     *
     * @return constructed beverage {@link JPanel}
     */
    private JPanel buildBeverageCard() {
        Theme.RoundedCardPanel card = Theme.createCardPanel("Step 2B • Craft Beverages (3 Sizes)");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);
        Theme.styleComboBox(beverageCombo);
        beverageCombo.addActionListener(e -> updateLiveBeveragePrice());

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        qtyPanel.setOpaque(false);
        qtyPanel.add(Theme.createFieldLabel("QTY:"));
        beverageQtySpinner.addChangeListener(e -> updateLiveBeveragePrice());
        qtyPanel.add(beverageQtySpinner);

        topRow.add(beverageCombo, BorderLayout.CENTER);
        topRow.add(qtyPanel, BorderLayout.EAST);

        JPanel sizeRow = new JPanel(new GridLayout(1, 3, 8, 0));
        sizeRow.setOpaque(false);
        ButtonGroup bevGroup = new ButtonGroup();
        for (int i = 0; i < MenuCatalog.BEVERAGE_SIZES.size(); i++) {
            MenuOption sz = MenuCatalog.BEVERAGE_SIZES.get(i);
            String tag = sz.getPrice() > 0 ? String.format("+$%.2f", sz.getPrice()) : "Base";
            Theme.TogglePillButton pill = new Theme.TogglePillButton(sz.getName() + " (" + tag + ")", i == 0);
            pill.addActionListener(e -> {
                selectedBeverageSize = sz;
                updateLiveBeveragePrice();
            });
            bevGroup.add(pill);
            beverageSizePills.add(pill);
            sizeRow.add(pill);
        }

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        liveBeveragePriceLabel.setFont(Theme.FONT_MONO_BOLD);
        liveBeveragePriceLabel.setForeground(Theme.ACCENT_AMBER);

        JButton addBevBtn = Theme.createSecondaryButton("+ Add Drink to Order");
        addBevBtn.addActionListener(e -> addCurrentBeverageToCart());

        bottom.add(liveBeveragePriceLabel, BorderLayout.WEST);
        bottom.add(addBevBtn, BorderLayout.EAST);

        card.add(topRow);
        card.add(Box.createVerticalStrut(8));
        card.add(sizeRow);
        card.add(Box.createVerticalStrut(10));
        card.add(bottom);
        return card;
    }

    /**
     * Builds the right-hand Order Cart card with custom item cards and live totals.
     *
     * @return constructed cart {@link JPanel}
     */
    private JPanel buildCartCard() {
        Theme.RoundedCardPanel card = Theme.createAmberCardPanel("Order Cart");
        card.setPreferredSize(new java.awt.Dimension(385, 0));
        card.setLayout(new BorderLayout(0, 10));

        cartList.setBackground(Theme.BG_INPUT);
        cartList.setCellRenderer(new CartItemCellRenderer());
        JScrollPane scroll = new JScrollPane(cartList);
        Theme.styleScrollPane(scroll);

        JPanel cartActions = new JPanel(new GridLayout(1, 3, 6, 0));
        cartActions.setOpaque(false);

        JButton editBtn = Theme.createSecondaryButton("Edit Pizza");
        editBtn.addActionListener(e -> editSelectedItem());

        JButton removeBtn = Theme.createDangerButton("Remove");
        removeBtn.addActionListener(e -> removeSelectedItem());

        JButton clearBtn = Theme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> clearCart());

        cartActions.add(editBtn);
        cartActions.add(removeBtn);
        cartActions.add(clearBtn);

        JPanel summaryPanel = new JPanel(new GridLayout(4, 1, 0, 4));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 2));
        styleSummaryLabel(subtotalLabel, false);
        styleSummaryLabel(deliveryFeeLabel, false);
        styleSummaryLabel(taxLabel, false);
        styleSummaryLabel(estimatedTotalLabel, true);
        summaryPanel.add(subtotalLabel);
        summaryPanel.add(deliveryFeeLabel);
        summaryPanel.add(taxLabel);
        summaryPanel.add(estimatedTotalLabel);

        JPanel navActions = new JPanel(new GridLayout(2, 1, 0, 8));
        navActions.setOpaque(false);

        JButton checkoutBtn = Theme.createPrimaryButton("PROCEED TO PAYMENT →");
        checkoutBtn.addActionListener(e -> proceedToCheckout());

        JButton backCustomerBtn = Theme.createSecondaryButton("← Edit Customer Info");
        backCustomerBtn.addActionListener(e -> frame.showCard(PizzaAppFrame.CARD_CUSTOMER));

        navActions.add(checkoutBtn);
        navActions.add(backCustomerBtn);

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
     * Enforces the strict maximum of 4 toppings per pizza whenever a topping pill is toggled.
     *
     * @param source clicked topping toggle pill
     */
    private void handleToppingToggle(Theme.TogglePillButton source) {
        int count = countSelectedToppings();
        if (count > MenuCatalog.MAX_TOPPINGS_PER_PIZZA) {
            source.setSelected(false);
            count = countSelectedToppings();
            toppingCounterLabel.setForeground(Theme.STATUS_ERROR);
            toppingCounterLabel.setText("LIMIT: Max " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA + " toppings!");
            JOptionPane.showMessageDialog(
                    this,
                    "A maximum of " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA + " toppings is allowed per pizza.\n"
                            + "Deselect a topping first before adding another.",
                    "Maximum 4 Toppings Enforced",
                    JOptionPane.WARNING_MESSAGE
            );
        } else {
            toppingCounterLabel.setForeground(count == MenuCatalog.MAX_TOPPINGS_PER_PIZZA ? Theme.STATUS_SUCCESS : Theme.ACCENT_AMBER);
            toppingCounterLabel.setText("Selected Toppings: " + count + " / " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA);
        }
        updateLivePizzaPrice();
    }

    /**
     * Counts selected toppings.
     *
     * @return count of selected topping pills
     */
    private int countSelectedToppings() {
        int count = 0;
        for (Theme.TogglePillButton pill : toppingPills) {
            if (pill.isSelected()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Computes live price of the pizza currently configured.
     *
     * @return computed pizza price in USD
     */
    public double computeCurrentPizzaPrice() {
        double price = (selectedSize != null ? selectedSize.getPrice() : 0.0)
                + (selectedCrust != null ? selectedCrust.getPrice() : 0.0);
        for (Theme.TogglePillButton pill : toppingPills) {
            if (pill.isSelected()) {
                MenuOption top = MenuCatalog.findToppingByName(pill.getActionCommand());
                if (top != null) {
                    price += top.getPrice();
                }
            }
        }
        return MenuCatalog.round2(price);
    }

    /**
     * Computes the unit price of the currently selected beverage and size.
     *
     * @return computed unit price in USD
     */
    public double computeCurrentBeverageUnitPrice() {
        MenuOption bev = (MenuOption) beverageCombo.getSelectedItem();
        double unit = (bev != null ? bev.getPrice() : 0.0)
                + (selectedBeverageSize != null ? selectedBeverageSize.getPrice() : 0.0);
        return MenuCatalog.round2(unit);
    }

    /**
     * Updates the live pizza price display.
     */
    private void updateLivePizzaPrice() {
        livePizzaPriceLabel.setText("Item Price: " + Theme.formatCurrency(computeCurrentPizzaPrice()));
    }

    /**
     * Updates the live beverage total label.
     */
    private void updateLiveBeveragePrice() {
        double unit = computeCurrentBeverageUnitPrice();
        int qty = ((Number) beverageQtySpinner.getValue()).intValue();
        double total = MenuCatalog.round2(unit * qty);
        liveBeveragePriceLabel.setText(String.format("Drink Total: $%.2f ($%.2f ea)", total, unit));
    }

    /**
     * Saves the configured pizza to the cart or updates the edited cart line.
     */
    private void saveCurrentPizzaToCart() {
        List<String> selectedToppings = new ArrayList<>();
        for (Theme.TogglePillButton pill : toppingPills) {
            if (pill.isSelected()) {
                selectedToppings.add(pill.getActionCommand());
            }
        }
        if (selectedToppings.size() > MenuCatalog.MAX_TOPPINGS_PER_PIZZA) {
            JOptionPane.showMessageDialog(this, "Maximum 4 toppings allowed.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double price = computeCurrentPizzaPrice();
        OrderItem pizzaItem = OrderItem.createPizza(
                selectedSize.getName(),
                selectedCrust.getName(),
                selectedSauce,
                selectedToppings,
                price
        );

        if (editingCartIndex >= 0 && editingCartIndex < cartItems.size()) {
            cartItems.set(editingCartIndex, pizzaItem);
            cartListModel.set(editingCartIndex, pizzaItem);
            editingCartIndex = -1;
            addPizzaBtn.setText("+ Add Pizza to Order");
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
        int qty = ((Number) beverageQtySpinner.getValue()).intValue();
        double unit = computeCurrentBeverageUnitPrice();

        OrderItem bevItem = OrderItem.createBeverage(
                bev != null ? bev.getName() : "Beverage",
                selectedBeverageSize.getName(),
                unit,
                qty
        );
        cartItems.add(bevItem);
        cartListModel.addElement(bevItem);
        beverageQtySpinner.setValue(1);
        refreshCartSummary();
    }

    /**
     * Loads a selected pizza from the cart back into the builder for modification.
     */
    private void editSelectedItem() {
        int idx = cartList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select an item in the cart first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        OrderItem item = cartItems.get(idx);
        if (!item.isPizza()) {
            JOptionPane.showMessageDialog(this, "Only custom pizzas can be loaded into the builder. Use Remove to change beverages.", "Beverage Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        editingCartIndex = idx;
        addPizzaBtn.setText("Update Pizza in Order");

        for (int i = 0; i < sizePills.size(); i++) {
            if (sizePills.get(i).getActionCommand().equals(item.getSizeLabel())) {
                sizePills.get(i).setSelected(true);
                selectedSize = MenuCatalog.PIZZA_SIZES.get(i);
            }
        }
        for (int i = 0; i < crustPills.size(); i++) {
            if (crustPills.get(i).getActionCommand().equals(item.getCrustLabel())) {
                crustPills.get(i).setSelected(true);
                selectedCrust = MenuCatalog.CRUST_OPTIONS.get(i);
            }
        }
        for (int i = 0; i < saucePills.size(); i++) {
            if (saucePills.get(i).getActionCommand().equals(item.getSauceLabel())) {
                saucePills.get(i).setSelected(true);
                selectedSauce = MenuCatalog.SAUCE_LEVELS.get(i);
            }
        }
        for (Theme.TogglePillButton pill : toppingPills) {
            pill.setSelected(item.getToppings().contains(pill.getActionCommand()));
        }
        int count = countSelectedToppings();
        toppingCounterLabel.setForeground(Theme.ACCENT_AMBER);
        toppingCounterLabel.setText("Editing item #" + (idx + 1) + " (" + count + " / 4 toppings)");
        updateLivePizzaPrice();
    }

    /**
     * Removes the selected item from the cart.
     */
    private void removeSelectedItem() {
        int idx = cartList.getSelectedIndex();
        if (idx >= 0) {
            cartItems.remove(idx);
            cartListModel.remove(idx);
            if (editingCartIndex == idx) {
                editingCartIndex = -1;
                addPizzaBtn.setText("+ Add Pizza to Order");
            }
            refreshCartSummary();
        }
    }

    /**
     * Clears all items from the cart.
     */
    public void clearCart() {
        cartItems.clear();
        cartListModel.clear();
        editingCartIndex = -1;
        addPizzaBtn.setText("+ Add Pizza to Order");
        refreshCartSummary();
    }

    /**
     * Resets builder selections to Medium (12"), Classic Hand-Tossed, Normal Sauce, and 0 toppings.
     */
    private void resetPizzaBuilder() {
        sizePills.get(1).setSelected(true);
        selectedSize = MenuCatalog.PIZZA_SIZES.get(1);
        crustPills.get(0).setSelected(true);
        selectedCrust = MenuCatalog.CRUST_OPTIONS.get(0);
        saucePills.get(1).setSelected(true);
        selectedSauce = MenuCatalog.SAUCE_LEVELS.get(1);

        for (Theme.TogglePillButton pill : toppingPills) {
            pill.setSelected(false);
        }
        toppingCounterLabel.setForeground(Theme.ACCENT_AMBER);
        toppingCounterLabel.setText("Selected Toppings: 0 / 4 (Maximum 4 toppings per pizza)");
        updateLivePizzaPrice();
    }

    /**
     * Recalculates subtotal, delivery fee, tax, and navigation cart counter.
     */
    public void refreshCartSummary() {
        double sub = getCartSubtotal();
        Customer activeCustomer = frame.getActiveCustomer();
        String orderType = activeCustomer != null ? activeCustomer.getOrderType() : "delivery";
        double[] totals = DataStore.calculateTotals(sub, orderType, 0.0);

        subtotalLabel.setText("Subtotal: " + Theme.formatCurrency(totals[0]));
        deliveryFeeLabel.setText("Delivery Fee (" + orderType + "): " + Theme.formatCurrency(totals[1]));
        taxLabel.setText("Est. Tax (8%): " + Theme.formatCurrency(totals[2]));
        estimatedTotalLabel.setText("Estimated Total: " + Theme.formatCurrency(totals[4]));
        frame.updateCartBadge(cartItems.size(), totals[0]);
    }

    /**
     * Verifies cart is non-empty and moves to Checkout.
     */
    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Add a pizza or beverage first.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }
        frame.openCheckoutScreen();
    }

    /**
     * Computes subtotal across all cart line items.
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
     * Returns a copy of cart line items.
     *
     * @return list of {@link OrderItem}
     */
    public List<OrderItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    /**
     * Helper wrapping a left-aligned field caption in a transparent row.
     *
     * @param text caption text
     * @return panel containing left-aligned label
     */
    private JPanel leftLabel(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(Theme.createFieldLabel(text));
        return p;
    }

    /**
     * Styles a cart financial summary label.
     *
     * @param lbl    target label
     * @param isBold whether to highlight as final total
     */
    private void styleSummaryLabel(JLabel lbl, boolean isBold) {
        lbl.setFont(isBold ? Theme.FONT_MONO_BOLD : Theme.FONT_MONO);
        lbl.setForeground(isBold ? Theme.ACCENT_AMBER : Theme.TEXT_PRIMARY);
    }

    /**
     * Custom cell renderer displaying each {@link OrderItem} in the cart as a dark-bordered card.
     */
    private static class CartItemCellRenderer implements ListCellRenderer<OrderItem> {

        /**
         * Constructs the {@code CartItemCellRenderer}.
         */
        CartItemCellRenderer() {
        }

        /**
         * Renders a cart line item with golden price tag and multi-line details.
         *
         * @param list         parent list
         * @param value        item to render
         * @param index        cell index
         * @param isSelected   selection flag
         * @param cellHasFocus focus flag
         * @return styled cell panel
         */
        @Override
        public Component getListCellRendererComponent(JList<? extends OrderItem> list, OrderItem value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JPanel card = new JPanel(new BorderLayout(4, 3));
            card.setBackground(isSelected ? new ColorHighlight() : Theme.BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));

            String titleText = String.format("#%d  %s", index + 1, value.isPizza() ? value.getSizeLabel() + " Pizza" : value.getQuantity() + "x " + value.getBeverageName());
            JLabel title = new JLabel(titleText);
            title.setFont(Theme.FONT_SUBTITLE);
            title.setForeground(Theme.TEXT_PRIMARY);

            JLabel price = new JLabel(Theme.formatCurrency(value.getTotalPrice()));
            price.setFont(Theme.FONT_MONO_BOLD);
            price.setForeground(Theme.ACCENT_AMBER);

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            top.add(title, BorderLayout.WEST);
            top.add(price, BorderLayout.EAST);

            JLabel details = new JLabel("<html><body style='width:310px'>" + value.getDisplayDetails() + "</body></html>");
            details.setFont(Theme.FONT_BODY);
            details.setForeground(Theme.TEXT_MUTED);

            card.add(top, BorderLayout.NORTH);
            card.add(details, BorderLayout.SOUTH);
            return card;
        }

        /**
         * Warm highlight background color for selected cart items.
         */
        private static class ColorHighlight extends java.awt.Color {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            /**
             * Creates warm brown selection color (#362D20).
             */
            ColorHighlight() {
                super(54, 45, 32);
            }
        }
    }
}
