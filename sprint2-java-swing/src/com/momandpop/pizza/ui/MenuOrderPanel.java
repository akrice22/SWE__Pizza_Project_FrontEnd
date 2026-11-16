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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Step 2 ordering screen matching Figure 8 &amp; Figure 9A with zero legacy Swing dropdowns or spinners:
 * interactive card toggles for Sizes (4), Crusts (4), Sauces (3), Toppings (13 with strict max-4 cap),
 * a 3x2 Craft Drink Grid across 3 Beverage Sizes, and a custom-rendered Order Cart.
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

    /** Horizontal toggle rows representing all available toppings. */
    private final List<Theme.MenuRowToggle> toppingRows;

    /** Warning label displayed when user hits or exceeds the 4-topping limit. */
    private final JLabel toppingCounterLabel;

    /** Live price label for the currently configured pizza. */
    private final JLabel livePizzaPriceLabel;

    /** Button to add or update the configured pizza in the cart. */
    private final JButton addPizzaBtn;

    /** Index of the pizza currently being edited, or {@code -1} when adding new. */
    private int editingCartIndex;

    /** Toggle pills for the 3 beverage sizes. */
    private final List<Theme.TogglePillButton> beverageSizePills;

    /** Drink price labels inside the 6 drink cards so they update live when size changes. */
    private final List<JLabel> drinkCardPriceLabels;

    /** Currently selected beverage size option. */
    private transient MenuOption selectedBeverageSize;

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
        this.toppingRows = new ArrayList<>();
        this.beverageSizePills = new ArrayList<>();
        this.drinkCardPriceLabels = new ArrayList<>();

        this.selectedSize = MenuCatalog.PIZZA_SIZES.get(1);
        this.selectedCrust = MenuCatalog.CRUST_OPTIONS.get(0);
        this.selectedSauce = MenuCatalog.SAUCE_LEVELS.get(1);
        this.selectedBeverageSize = MenuCatalog.BEVERAGE_SIZES.get(0);

        this.toppingCounterLabel = new JLabel("Selected Toppings: 0 / 4 (Max 4)");
        this.livePizzaPriceLabel = new JLabel("Item Price: $13.99");
        this.addPizzaBtn = Theme.createPrimaryButton("ADD TO ORDER");
        this.editingCartIndex = -1;

        this.subtotalLabel = new JLabel("Subtotal: $0.00");
        this.deliveryFeeLabel = new JLabel("Delivery Fee: $3.99");
        this.taxLabel = new JLabel("Est. Tax (8%): $0.00");
        this.estimatedTotalLabel = new JLabel("Estimated Total: $0.00");

        buildLayout();
        resetPizzaBuilder();
        updateDrinkCardPrices();
        refreshCartSummary();
    }

    /**
     * Assembles the scrollable Left Customizer Column (Pizza + Drinks) and Right Order Cart Column.
     */
    private void buildLayout() {
        setLayout(new BorderLayout(16, 12));
        setBackground(Theme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setBackground(Theme.BG_APP);
        leftColumn.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 8));
        leftColumn.add(buildPizzaBuilderCard());
        leftColumn.add(Box.createVerticalStrut(14));
        leftColumn.add(buildBeverageGridCard());

        JScrollPane leftScroll = new JScrollPane(leftColumn);
        Theme.styleScrollPane(leftScroll);
        leftScroll.setBorder(null);

        JPanel rightColumn = buildCartCard();

        JPanel mainSplit = new JPanel(new BorderLayout(18, 0));
        mainSplit.setOpaque(false);
        mainSplit.add(leftScroll, BorderLayout.CENTER);
        mainSplit.add(rightColumn, BorderLayout.EAST);

        add(mainSplit, BorderLayout.CENTER);
    }

    /**
     * Builds the custom Pizza Builder card matching Figure 8.
     *
     * @return constructed pizza builder {@link JPanel}
     */
    private JPanel buildPizzaBuilderCard() {
        Theme.RoundedCardPanel card = Theme.createTitledCard("Step 02", "Build Custom Pie", false);
        JPanel content = card.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // 1. Sizes (4 options)
        content.add(leftLabel("1. PIZZA SIZE (4 OPTIONS)"));
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
        content.add(Box.createVerticalStrut(4));
        content.add(sizeGrid);
        content.add(Box.createVerticalStrut(12));

        // 2. Crusts (4 options)
        content.add(leftLabel("2. ARTISAN CRUST (4 OPTIONS)"));
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
        content.add(Box.createVerticalStrut(4));
        content.add(crustGrid);
        content.add(Box.createVerticalStrut(12));

        // 3. Sauce (3 levels)
        content.add(leftLabel("3. SAUCE LEVEL"));
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
        content.add(Box.createVerticalStrut(4));
        content.add(sauceGrid);
        content.add(Box.createVerticalStrut(14));

        // 4. Toppings Selection (Two vertical columns matching Figure 8)
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.add(leftLabel("4. TOPPINGS SELECTION (MAX 4 PER PIZZA)"), BorderLayout.WEST);
        toppingCounterLabel.setFont(Theme.FONT_LABEL);
        toppingCounterLabel.setForeground(Theme.ACCENT_AMBER);
        topHeader.add(toppingCounterLabel, BorderLayout.EAST);
        content.add(topHeader);
        content.add(Box.createVerticalStrut(6));

        JPanel toppingSplit = new JPanel(new GridLayout(1, 2, 12, 0));
        toppingSplit.setOpaque(false);

        Theme.RoundedCardPanel veggieCard = Theme.createTitledCard(null, "Garden Veggies & Cheese", false);
        JPanel veggieBody = veggieCard.getContentPane();
        veggieBody.setLayout(new GridLayout(MenuCatalog.VEGGIE_TOPPINGS.size(), 1, 0, 6));
        for (MenuOption v : MenuCatalog.VEGGIE_TOPPINGS) {
            veggieBody.add(createToppingRow(v, Theme.BORDER_EMERALD, Theme.BG_EMERALD_TINT));
        }

        Theme.RoundedCardPanel meatCard = Theme.createTitledCard(null, "Savory Meats", false);
        JPanel meatBody = meatCard.getContentPane();
        meatBody.setLayout(new GridLayout(MenuCatalog.MEAT_TOPPINGS.size(), 1, 0, 6));
        for (MenuOption m : MenuCatalog.MEAT_TOPPINGS) {
            meatBody.add(createToppingRow(m, Theme.STATUS_ERROR, Theme.BG_DANGER));
        }

        toppingSplit.add(veggieCard);
        toppingSplit.add(meatCard);
        content.add(toppingSplit);
        content.add(Box.createVerticalStrut(14));

        // Pizza Add Action Row
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);
        livePizzaPriceLabel.setFont(Theme.FONT_TITLE);
        livePizzaPriceLabel.setForeground(Theme.ACCENT_AMBER);
        addPizzaBtn.addActionListener(e -> saveCurrentPizzaToCart());
        actionBar.add(livePizzaPriceLabel, BorderLayout.WEST);
        actionBar.add(addPizzaBtn, BorderLayout.EAST);
        content.add(actionBar);

        return card;
    }

    /**
     * Creates a styled horizontal {@link Theme.MenuRowToggle} for a pizza topping.
     *
     * @param option            topping menu option
     * @param activeBorderColor highlight border color when selected
     * @param activeBgColor     highlight background tint when selected
     * @return configured {@link Theme.MenuRowToggle}
     */
    private Theme.MenuRowToggle createToppingRow(MenuOption option, Color activeBorderColor, Color activeBgColor) {
        String priceStr = String.format("+$%.2f", option.getPrice());
        Theme.MenuRowToggle row = new Theme.MenuRowToggle(option.getName(), priceStr, activeBorderColor, activeBgColor);
        row.addActionListener(e -> handleToppingToggle(row));
        toppingRows.add(row);
        return row;
    }

    /**
     * Builds the Craft Beverages card featuring 3 Drink Size pills and a 3x2 grid of one-click Drink Cards
     * matching Figure 8 section "5. PAIR WITH REFRESHING DRINKS".
     *
     * @return constructed beverage grid card {@link JPanel}
     */
    private JPanel buildBeverageGridCard() {
        Theme.RoundedCardPanel card = Theme.createTitledCard("Optional Add-Ons", "5. Pair With Refreshing Drinks (3 Sizes)", false);
        JPanel content = card.getContentPane();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // 3 Drink Sizes Row
        JPanel sizeRow = new JPanel(new GridLayout(1, 3, 8, 0));
        sizeRow.setOpaque(false);
        ButtonGroup bevGroup = new ButtonGroup();
        for (int i = 0; i < MenuCatalog.BEVERAGE_SIZES.size(); i++) {
            MenuOption sz = MenuCatalog.BEVERAGE_SIZES.get(i);
            String tag = sz.getPrice() > 0 ? String.format("+$%.2f", sz.getPrice()) : "Base";
            Theme.TogglePillButton pill = new Theme.TogglePillButton(sz.getName() + " (" + tag + ")", i == 0);
            pill.addActionListener(e -> {
                selectedBeverageSize = sz;
                updateDrinkCardPrices();
            });
            bevGroup.add(pill);
            beverageSizePills.add(pill);
            sizeRow.add(pill);
        }

        // 3x2 Grid of 6 Craft Drink Cards
        JPanel drinksGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        drinksGrid.setOpaque(false);
        for (MenuOption bev : MenuCatalog.BEVERAGES) {
            Theme.RoundedCardPanel drinkCard = Theme.createCardPanel(null);
            drinkCard.setLayout(new BorderLayout(4, 8));

            JPanel textStack = new JPanel();
            textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
            textStack.setOpaque(false);
            JLabel nameLbl = new JLabel("<html><body style='width:165px'><b>" + bev.getName() + "</b></body></html>");
            nameLbl.setFont(Theme.FONT_BODY);
            nameLbl.setForeground(Theme.TEXT_PRIMARY);
            JLabel descLbl = new JLabel(bev.getDescription());
            descLbl.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
            descLbl.setForeground(Theme.TEXT_MUTED);
            textStack.add(nameLbl);
            textStack.add(Box.createVerticalStrut(2));
            textStack.add(descLbl);

            JPanel bottomRow = new JPanel(new BorderLayout());
            bottomRow.setOpaque(false);
            JLabel priceLbl = new JLabel(Theme.formatCurrency(bev.getPrice()));
            priceLbl.setFont(Theme.FONT_MONO_BOLD);
            priceLbl.setForeground(Theme.ACCENT_AMBER);
            drinkCardPriceLabels.add(priceLbl);

            JButton addBtn = Theme.createSecondaryButton("+ ADD");
            addBtn.addActionListener(e -> addDrinkByOption(bev));

            bottomRow.add(priceLbl, BorderLayout.WEST);
            bottomRow.add(addBtn, BorderLayout.EAST);

            drinkCard.add(textStack, BorderLayout.CENTER);
            drinkCard.add(bottomRow, BorderLayout.SOUTH);
            drinksGrid.add(drinkCard);
        }

        content.add(sizeRow);
        content.add(Box.createVerticalStrut(10));
        content.add(drinksGrid);
        return card;
    }

    /**
     * Builds the right-hand Order Cart card with custom-rendered item cards and live totals.
     *
     * @return constructed cart {@link JPanel}
     */
    private JPanel buildCartCard() {
        Theme.RoundedCardPanel card = Theme.createTitledCard("Live Ticket", "Order Cart", true);
        card.setPreferredSize(new Dimension(380, 0));
        JPanel content = card.getContentPane();
        content.setLayout(new BorderLayout(0, 10));

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

        content.add(scroll, BorderLayout.CENTER);
        content.add(southContainer, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Enforces the strict maximum of 4 toppings per pizza whenever a topping row is toggled.
     *
     * @param source clicked topping toggle row
     */
    private void handleToppingToggle(Theme.MenuRowToggle source) {
        int count = countSelectedToppings();
        if (count > MenuCatalog.MAX_TOPPINGS_PER_PIZZA) {
            source.setSelected(false);
            count = countSelectedToppings();
            toppingCounterLabel.setForeground(Theme.STATUS_ERROR);
            toppingCounterLabel.setText("LIMIT REACHED: Max " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA + " toppings!");
            JOptionPane.showMessageDialog(
                    this,
                    "A maximum of " + MenuCatalog.MAX_TOPPINGS_PER_PIZZA + " toppings is allowed per pizza.\n"
                            + "Deselect an existing topping first before adding another.",
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
     * @return count of selected topping rows
     */
    private int countSelectedToppings() {
        int count = 0;
        for (Theme.MenuRowToggle row : toppingRows) {
            if (row.isSelected()) {
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
        for (Theme.MenuRowToggle row : toppingRows) {
            if (row.isSelected()) {
                MenuOption top = MenuCatalog.findToppingByName(row.getActionCommand());
                if (top != null) {
                    price += top.getPrice();
                }
            }
        }
        return MenuCatalog.round2(price);
    }

    /**
     * Updates the live pizza price display.
     */
    private void updateLivePizzaPrice() {
        livePizzaPriceLabel.setText("Item Price: " + Theme.formatCurrency(computeCurrentPizzaPrice()));
    }

    /**
     * Updates the displayed price on all 6 drink cards when the user switches beverage size (S / M / L).
     */
    private void updateDrinkCardPrices() {
        double sizeUpcharge = selectedBeverageSize != null ? selectedBeverageSize.getPrice() : 0.0;
        for (int i = 0; i < MenuCatalog.BEVERAGES.size() && i < drinkCardPriceLabels.size(); i++) {
            double total = MenuCatalog.round2(MenuCatalog.BEVERAGES.get(i).getPrice() + sizeUpcharge);
            drinkCardPriceLabels.get(i).setText("+" + Theme.formatCurrency(total));
        }
    }

    /**
     * Saves the configured pizza to the cart or updates the edited cart line.
     */
    private void saveCurrentPizzaToCart() {
        List<String> selectedToppings = new ArrayList<>();
        for (Theme.MenuRowToggle row : toppingRows) {
            if (row.isSelected()) {
                selectedToppings.add(row.getActionCommand());
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
            addPizzaBtn.setText("ADD TO ORDER");
        } else {
            cartItems.add(pizzaItem);
            cartListModel.addElement(pizzaItem);
        }
        resetPizzaBuilder();
        refreshCartSummary();
    }

    /**
     * Adds one unit of the clicked beverage using the active beverage size (S / M / L) into the cart,
     * incrementing quantity if the exact beverage and size already exist in the cart.
     *
     * @param bev clicked beverage {@link MenuOption}
     */
    private void addDrinkByOption(MenuOption bev) {
        double unit = MenuCatalog.round2(bev.getPrice() + (selectedBeverageSize != null ? selectedBeverageSize.getPrice() : 0.0));
        String sizeName = selectedBeverageSize != null ? selectedBeverageSize.getName() : "Small (12 oz)";

        for (int i = 0; i < cartItems.size(); i++) {
            OrderItem existing = cartItems.get(i);
            if (!existing.isPizza()
                    && existing.getBeverageName().equals(bev.getName())
                    && existing.getBeverageSize().equals(sizeName)) {
                existing.setQuantity(existing.getQuantity() + 1);
                cartListModel.set(i, existing);
                refreshCartSummary();
                return;
            }
        }

        OrderItem bevItem = OrderItem.createBeverage(bev.getName(), sizeName, unit, 1);
        cartItems.add(bevItem);
        cartListModel.addElement(bevItem);
        refreshCartSummary();
    }

    /**
     * Appends an {@link OrderItem} directly to the active cart (e.g., from quick reorder).
     *
     * @param item item to add to cart
     */
    public void addItemDirectlyToCart(OrderItem item) {
        if (item == null) {
            return;
        }
        cartItems.add(item);
        cartListModel.addElement(item);
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
            JOptionPane.showMessageDialog(this, "Only custom pizzas can be loaded into the builder. Use Remove to adjust drinks.", "Beverage Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        editingCartIndex = idx;
        addPizzaBtn.setText("UPDATE PIZZA IN ORDER");

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
        for (Theme.MenuRowToggle row : toppingRows) {
            row.setSelected(item.getToppings().contains(row.getActionCommand()));
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
                addPizzaBtn.setText("ADD TO ORDER");
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
        addPizzaBtn.setText("ADD TO ORDER");
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

        for (Theme.MenuRowToggle row : toppingRows) {
            row.setSelected(false);
        }
        toppingCounterLabel.setForeground(Theme.ACCENT_AMBER);
        toppingCounterLabel.setText("Selected Toppings: 0 / 4 (Max 4)");
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
     * Custom cell renderer displaying each {@link OrderItem} in the cart as a sleek rounded item card.
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
            JPanel card = new JPanel(new BorderLayout(4, 4));
            card.setBackground(isSelected ? new ColorHighlight() : Theme.BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));

            String titleText = String.format("#%d: %s", index + 1,
                    value.isPizza() ? value.getSizeLabel() : value.getQuantity() + "x " + value.getBeverageName());
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

            JLabel details = new JLabel("<html><body style='width:240px'>" + value.getDisplayDetails() + "</body></html>");
            details.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
            details.setForeground(Theme.TEXT_MUTED);

            card.add(top, BorderLayout.NORTH);
            card.add(details, BorderLayout.SOUTH);
            return card;
        }

        /**
         * Warm highlight background color for selected cart items.
         */
        private static class ColorHighlight extends Color {
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
