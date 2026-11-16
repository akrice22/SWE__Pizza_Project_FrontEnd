package com.momandpop.pizza.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single line item inside the customer's cart or a completed order
 * (either a custom {@code PIZZA} or a {@code BEVERAGE}).
 */
public class OrderItem {

    /** Type identifier: "PIZZA" or "BEVERAGE". */
    private String itemType;

    /** Selected pizza size label (e.g., Medium (12")). */
    private String sizeLabel;

    /** Selected pizza crust label (e.g., Classic Hand-Tossed). */
    private String crustLabel;

    /** Selected pizza sauce level (e.g., Normal Sauce). */
    private String sauceLabel;

    /** Selected pizza toppings list (maximum 4 toppings). */
    private List<String> toppings;

    /** Selected beverage name when {@code itemType} is "BEVERAGE". */
    private String beverageName;

    /** Selected beverage size label when {@code itemType} is "BEVERAGE". */
    private String beverageSize;

    /** Quantity of this line item. */
    private int quantity;

    /** Unit price for a single unit of this item. */
    private double unitPrice;

    /** Total price ({@code unitPrice * quantity}). */
    private double totalPrice;

    /**
     * Constructs an empty {@code OrderItem}.
     */
    public OrderItem() {
        this.toppings = new ArrayList<>();
        this.quantity = 1;
    }

    /**
     * Creates a customized pizza {@code OrderItem}.
     *
     * @param sizeLabel  selected size label
     * @param crustLabel selected crust label
     * @param sauceLabel selected sauce label
     * @param toppings   list of topping names (up to 4)
     * @param unitPrice  total computed price for one pizza
     * @return configured pizza {@code OrderItem}
     */
    public static OrderItem createPizza(String sizeLabel, String crustLabel, String sauceLabel,
                                        List<String> toppings, double unitPrice) {
        OrderItem item = new OrderItem();
        item.itemType = "PIZZA";
        item.sizeLabel = sizeLabel;
        item.crustLabel = crustLabel;
        item.sauceLabel = sauceLabel;
        item.toppings = toppings == null ? new ArrayList<>() : new ArrayList<>(toppings);
        item.quantity = 1;
        item.unitPrice = MenuCatalog.round2(unitPrice);
        item.totalPrice = item.unitPrice;
        return item;
    }

    /**
     * Creates a beverage {@code OrderItem}.
     *
     * @param beverageName selected beverage name
     * @param beverageSize selected beverage size label
     * @param unitPrice    computed price per beverage
     * @param quantity     quantity ordered
     * @return configured beverage {@code OrderItem}
     */
    public static OrderItem createBeverage(String beverageName, String beverageSize,
                                           double unitPrice, int quantity) {
        OrderItem item = new OrderItem();
        item.itemType = "BEVERAGE";
        item.beverageName = beverageName;
        item.beverageSize = beverageSize;
        item.quantity = Math.max(1, quantity);
        item.unitPrice = MenuCatalog.round2(unitPrice);
        item.totalPrice = MenuCatalog.round2(item.unitPrice * item.quantity);
        return item;
    }

    /**
     * Checks whether this line item is a pizza.
     *
     * @return {@code true} if item type is PIZZA
     */
    public boolean isPizza() {
        return "PIZZA".equalsIgnoreCase(itemType);
    }

    /**
     * Returns the item type ("PIZZA" or "BEVERAGE").
     *
     * @return item type string
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * Sets the item type.
     *
     * @param itemType "PIZZA" or "BEVERAGE"
     */
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    /**
     * Returns the pizza size label.
     *
     * @return size label
     */
    public String getSizeLabel() {
        return sizeLabel;
    }

    /**
     * Sets the pizza size label.
     *
     * @param sizeLabel size label
     */
    public void setSizeLabel(String sizeLabel) {
        this.sizeLabel = sizeLabel;
    }

    /**
     * Returns the pizza crust label.
     *
     * @return crust label
     */
    public String getCrustLabel() {
        return crustLabel;
    }

    /**
     * Sets the pizza crust label.
     *
     * @param crustLabel crust label
     */
    public void setCrustLabel(String crustLabel) {
        this.crustLabel = crustLabel;
    }

    /**
     * Returns the pizza sauce level label.
     *
     * @return sauce label
     */
    public String getSauceLabel() {
        return sauceLabel;
    }

    /**
     * Sets the pizza sauce level label.
     *
     * @param sauceLabel sauce label
     */
    public void setSauceLabel(String sauceLabel) {
        this.sauceLabel = sauceLabel;
    }

    /**
     * Returns an unmodifiable view of the pizza toppings list.
     *
     * @return list of topping names
     */
    public List<String> getToppings() {
        return Collections.unmodifiableList(toppings);
    }

    /**
     * Sets the toppings list for this pizza.
     *
     * @param toppings list of topping names
     */
    public void setToppings(List<String> toppings) {
        this.toppings = toppings == null ? new ArrayList<>() : new ArrayList<>(toppings);
    }

    /**
     * Returns the beverage name.
     *
     * @return beverage name
     */
    public String getBeverageName() {
        return beverageName;
    }

    /**
     * Sets the beverage name.
     *
     * @param beverageName beverage name
     */
    public void setBeverageName(String beverageName) {
        this.beverageName = beverageName;
    }

    /**
     * Returns the beverage size label.
     *
     * @return beverage size label
     */
    public String getBeverageSize() {
        return beverageSize;
    }

    /**
     * Sets the beverage size label.
     *
     * @param beverageSize beverage size label
     */
    public void setBeverageSize(String beverageSize) {
        this.beverageSize = beverageSize;
    }

    /**
     * Returns the item quantity.
     *
     * @return quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the item quantity and recalculates {@code totalPrice}.
     *
     * @param quantity item quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
        this.totalPrice = MenuCatalog.round2(this.unitPrice * this.quantity);
    }

    /**
     * Returns the unit price.
     *
     * @return unit price in USD
     */
    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Sets the unit price and recalculates {@code totalPrice}.
     *
     * @param unitPrice unit price in USD
     */
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = MenuCatalog.round2(unitPrice);
        this.totalPrice = MenuCatalog.round2(this.unitPrice * this.quantity);
    }

    /**
     * Returns the line total price.
     *
     * @return total price in USD
     */
    public double getTotalPrice() {
        return totalPrice;
    }

    /**
     * Returns a concise title suitable for cart lists and receipts.
     *
     * @return item title string
     */
    public String getDisplayTitle() {
        if (isPizza()) {
            return String.format("%s Pizza - $%.2f", sizeLabel, totalPrice);
        }
        return String.format("%dx %s (%s) - $%.2f", quantity, beverageName, beverageSize, totalPrice);
    }

    /**
     * Returns secondary detail text describing crust, sauce, and toppings.
     *
     * @return item details string
     */
    public String getDisplayDetails() {
        if (isPizza()) {
            String topStr = toppings.isEmpty() ? "Cheese Only" : String.join(", ", toppings);
            return crustLabel + " | " + sauceLabel + " | Toppings: " + topStr;
        }
        return String.format("Beverage @ $%.2f each", unitPrice);
    }

    /**
     * Formats this item as a compact string for human-readable receipt and {@code orders.txt} storage.
     *
     * @return serialized summary string
     */
    public String toCompactSummary() {
        if (isPizza()) {
            String topStr = toppings.isEmpty() ? "None" : String.join(", ", toppings);
            return String.format("Pizza [%s, %s, %s, Toppings: %s] ($%.2f)",
                    sizeLabel, crustLabel, sauceLabel, topStr, totalPrice);
        }
        return String.format("Drink [%dx %s - %s] ($%.2f)",
                quantity, beverageName, beverageSize, totalPrice);
    }

    /**
     * Returns the cart display representation.
     *
     * @return display string for Swing list rendering
     */
    @Override
    public String toString() {
        return getDisplayTitle() + "  (" + getDisplayDetails() + ")";
    }
}
