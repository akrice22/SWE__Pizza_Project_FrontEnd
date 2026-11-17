package com.momandpop.pizza.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a completed pizzeria order persisted to {@code data/orders.txt},
 * including customer details, itemized products, payment tender, financial breakdown,
 * and digital signature confirmation.
 */
public class OrderRecord {

    /** Unique sequential order identifier. */
    private int orderId;

    /** Formatted timestamp when the order was completed. */
    private String timestamp;

    /** Customer full name at the time of checkout. */
    private String customerName;

    /** Customer 10-digit phone number. */
    private String customerPhone;

    /** Fulfillment method ("delivery" or "pickup"). */
    private String orderType;

    /** Full delivery address or pickup notice. */
    private String deliveryAddress;

    /** Payment method used ("Credit", "Debit", "Cash", or "Check"). */
    private String paymentMethod;

    /** Tender metadata such as check number, masked card digits, or cash tendered. */
    private String paymentDetails;

    /** Food and beverage subtotal before fees, tax, and tip. */
    private double subtotal;

    /** Delivery surcharge ($3.99 for delivery, $0.00 for pickup). */
    private double deliveryFee;

    /** Sales tax (8% of subtotal). */
    private double tax;

    /** Customer tip amount. */
    private double tip;

    /** Final total charged to the customer. */
    private double grandTotal;

    /** Authorized customer signature text. */
    private String signature;

    /** Single-line summary of items stored in {@code orders.txt}. */
    private String itemsSummary;

    /** In-memory itemized list used for generating detailed receipts. */
    private List<OrderItem> items;

    /**
     * Constructs an empty {@code OrderRecord}.
     */
    public OrderRecord() {
        this.items = new ArrayList<>();
    }

    /**
     * Constructs a complete {@code OrderRecord}.
     *
     * @param orderId         unique order identifier
     * @param timestamp       order completion timestamp
     * @param customerName    customer full name
     * @param customerPhone   10-digit customer phone number
     * @param orderType       "delivery" or "pickup"
     * @param deliveryAddress delivery street address or pickup location
     * @param paymentMethod   payment method label
     * @param paymentDetails  check number, cash tendered, or card details
     * @param subtotal        item subtotal
     * @param deliveryFee     delivery surcharge
     * @param tax             sales tax
     * @param tip             gratuity amount
     * @param grandTotal      final total
     * @param signature       customer authorization signature
     * @param itemsSummary    summary text of all items
     * @param items           list of line items
     */
    public OrderRecord(int orderId, String timestamp, String customerName, String customerPhone,
                       String orderType, String deliveryAddress, String paymentMethod,
                       String paymentDetails, double subtotal, double deliveryFee, double tax,
                       double tip, double grandTotal, String signature, String itemsSummary,
                       List<OrderItem> items) {
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.orderType = orderType;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.paymentDetails = paymentDetails;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.tax = tax;
        this.tip = tip;
        this.grandTotal = grandTotal;
        this.signature = signature;
        this.itemsSummary = itemsSummary;
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    /**
     * Returns the unique order ID.
     *
     * @return order ID
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the unique order ID.
     *
     * @param orderId order ID
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Returns the order timestamp.
     *
     * @return timestamp string
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the order timestamp.
     *
     * @param timestamp timestamp string
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the customer name.
     *
     * @return customer name
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the customer name.
     *
     * @param customerName customer name
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Returns the customer phone number.
     *
     * @return 10-digit phone number
     */
    public String getCustomerPhone() {
        return customerPhone;
    }

    /**
     * Sets the customer phone number.
     *
     * @param customerPhone phone number
     */
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    /**
     * Returns the fulfillment order type ("delivery" or "pickup").
     *
     * @return order type
     */
    public String getOrderType() {
        return orderType;
    }

    /**
     * Sets the fulfillment order type.
     *
     * @param orderType "delivery" or "pickup"
     */
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    /**
     * Returns the delivery address.
     *
     * @return delivery address
     */
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    /**
     * Sets the delivery address.
     *
     * @param deliveryAddress delivery address
     */
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    /**
     * Returns the payment method.
     *
     * @return payment method name
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Sets the payment method.
     *
     * @param paymentMethod payment method name
     */
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * Returns extra payment details (e.g., check number or card last 4).
     *
     * @return payment details string
     */
    public String getPaymentDetails() {
        return paymentDetails;
    }

    /**
     * Sets extra payment details.
     *
     * @param paymentDetails payment details string
     */
    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    /**
     * Returns the order subtotal.
     *
     * @return subtotal in USD
     */
    public double getSubtotal() {
        return subtotal;
    }

    /**
     * Sets the order subtotal.
     *
     * @param subtotal subtotal in USD
     */
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * Returns the delivery fee.
     *
     * @return delivery fee in USD
     */
    public double getDeliveryFee() {
        return deliveryFee;
    }

    /**
     * Sets the delivery fee.
     *
     * @param deliveryFee delivery fee in USD
     */
    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    /**
     * Returns the calculated sales tax.
     *
     * @return sales tax in USD
     */
    public double getTax() {
        return tax;
    }

    /**
     * Sets the calculated sales tax.
     *
     * @param tax sales tax in USD
     */
    public void setTax(double tax) {
        this.tax = tax;
    }

    /**
     * Returns the tip amount.
     *
     * @return tip in USD
     */
    public double getTip() {
        return tip;
    }

    /**
     * Sets the tip amount.
     *
     * @param tip tip in USD
     */
    public void setTip(double tip) {
        this.tip = tip;
    }

    /**
     * Returns the grand total.
     *
     * @return grand total in USD
     */
    public double getGrandTotal() {
        return grandTotal;
    }

    /**
     * Sets the grand total.
     *
     * @param grandTotal grand total in USD
     */
    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    /**
     * Returns the customer signature string.
     *
     * @return signature string
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the customer signature string.
     *
     * @param signature signature string
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Returns the serialized summary of ordered items.
     *
     * @return items summary string
     */
    public String getItemsSummary() {
        return itemsSummary;
    }

    /**
     * Sets the serialized summary of ordered items.
     *
     * @param itemsSummary items summary string
     */
    public void setItemsSummary(String itemsSummary) {
        this.itemsSummary = itemsSummary;
    }

    /**
     * Returns an unmodifiable list of {@code OrderItem} entries.
     *
     * @return list of line items
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Formats an itemized thermal-style register receipt matching the store's layout.
     *
     * @return multi-line formatted receipt text
     */
    public String formatReceiptText() {
        StringBuilder sb = new StringBuilder();
        String divider = "--------------------------------------------------\n";
        sb.append("              MOM & POP PIZZERIA\n");
        sb.append("           Stone Oven Hearth & Kitchen\n");
        sb.append(String.format("               Order #%d\n", orderId));
        sb.append(String.format("          %s\n", timestamp));
        sb.append(divider);
        sb.append(String.format("Customer:     %s\n", customerName));
        sb.append(String.format("Phone:        %s\n", customerPhone));
        sb.append(String.format("Fulfillment:  %s\n", orderType != null ? orderType.toUpperCase() : "DELIVERY"));
        if ("delivery".equalsIgnoreCase(orderType)) {
            sb.append(String.format("Address:      %s\n", deliveryAddress));
        }
        sb.append(String.format("Payment:      %s", paymentMethod != null ? paymentMethod.toUpperCase() : "CASH"));
        if (paymentDetails != null && !paymentDetails.isEmpty()) {
            sb.append(" (").append(paymentDetails).append(")");
        }
        sb.append("\n");
        sb.append(divider);
        sb.append("ORDERED ITEMS:\n");
        if (items != null && !items.isEmpty()) {
            int idx = 1;
            for (OrderItem item : items) {
                sb.append(String.format("  #%d %s\n", idx++, item.getDisplayTitle()));
                sb.append(String.format("     %s\n", item.getDisplayDetails()));
            }
        } else if (itemsSummary != null && !itemsSummary.isEmpty()) {
            String[] split = itemsSummary.split(" ;; ");
            int idx = 1;
            for (String s : split) {
                sb.append(String.format("  #%d %s\n", idx++, s));
            }
        }
        sb.append(divider);
        sb.append(String.format("%-32s %17s\n", "Food & Drink Subtotal:", String.format("$%.2f", subtotal)));
        if (deliveryFee > 0.0) {
            sb.append(String.format("%-32s %17s\n", "Delivery Fee:", String.format("$%.2f", deliveryFee)));
        }
        sb.append(String.format("%-32s %17s\n", "Sales Tax (8%):", String.format("$%.2f", tax)));
        sb.append(String.format("%-32s %17s\n", "Tip:", String.format("$%.2f", tip)));
        sb.append(divider);
        sb.append(String.format("%-32s %17s\n", "GRAND TOTAL:", String.format("$%.2f", grandTotal)));
        sb.append(String.format("%-32s %17s\n", "AMOUNT PAID (" + (paymentMethod != null ? paymentMethod.toUpperCase() : "CASH") + "):", String.format("$%.2f", grandTotal)));
        sb.append(divider);
        sb.append("CUSTOMER / CREDIT CARD SIGNATURE:\n\n");
        sb.append("  X _____________________________________________\n");
        sb.append(String.format("    Signed: /s/ %s\n", (signature == null || signature.isEmpty()) ? customerName : signature));
        sb.append("\nThank you for ordering from Mom & Pop Pizzeria!\n");
        return sb.toString();
    }

    /**
     * Formats a kitchen preparation order slip for the pizzeria cook.
     *
     * @return formatted kitchen cook order slip text
     */
    public String formatCookOrderSlipText() {
        StringBuilder sb = new StringBuilder();
        String divider = "==================================================\n";
        String thin = "--------------------------------------------------\n";
        sb.append(divider);
        sb.append(String.format("       KITCHEN COOK SLIP — ORDER #%d\n", orderId));
        sb.append(divider);
        sb.append(String.format("TIME:        %s\n", timestamp));
        sb.append(String.format("TYPE:        %s\n", orderType != null ? orderType.toUpperCase() : "DELIVERY"));
        sb.append(String.format("CUSTOMER:    %s (%s)\n", customerName, customerPhone));
        if ("delivery".equalsIgnoreCase(orderType)) {
            sb.append(String.format("DELIVER TO:  %s\n", deliveryAddress));
        }
        sb.append(thin);
        sb.append("ITEMS TO PREPARE:\n\n");

        if (items != null && !items.isEmpty()) {
            int idx = 1;
            for (OrderItem item : items) {
                if (item.isPizza()) {
                    sb.append(String.format("  [ ] #%d PIZZA: %s\n", idx++, item.getSizeLabel().toUpperCase()));
                    sb.append(String.format("         CRUST : %s\n", item.getCrustLabel()));
                    sb.append(String.format("         SAUCE : %s\n", item.getSauceLabel()));
                    String tops = item.getToppings().isEmpty() ? "CHEESE ONLY" : String.join(", ", item.getToppings()).toUpperCase();
                    sb.append(String.format("         TOPS  : %s\n\n", tops));
                } else {
                    sb.append(String.format("  [ ] #%d DRINK: %dx %s (%s)\n\n",
                            idx++, item.getQuantity(), item.getBeverageName(), item.getBeverageSize()));
                }
            }
        } else if (itemsSummary != null && !itemsSummary.isEmpty()) {
            String[] split = itemsSummary.split(" ;; ");
            int idx = 1;
            for (String s : split) {
                sb.append(String.format("  [ ] #%d %s\n", idx++, s));
            }
            sb.append("\n");
        }
        sb.append(thin);
        sb.append("PREP CHECKLIST:\n");
        sb.append("  [ ] Dough Stretched   [ ] Sauced & Topped\n");
        sb.append("  [ ] Stone-Fired       [ ] Boxed & Checked\n");
        sb.append(divider);
        return sb.toString();
    }

    /**
     * Serializes this order record into a pipe-delimited line for {@code orders.txt}.
     *
     * @return pipe-delimited string
     */
    public String toFileLine() {
        return String.join("|",
                String.valueOf(orderId),
                sanitize(timestamp),
                sanitize(customerName),
                sanitize(customerPhone),
                sanitize(orderType),
                sanitize(deliveryAddress),
                sanitize(paymentMethod),
                sanitize(paymentDetails),
                String.format("%.2f", subtotal),
                String.format("%.2f", deliveryFee),
                String.format("%.2f", tax),
                String.format("%.2f", tip),
                String.format("%.2f", grandTotal),
                sanitize(signature),
                sanitize(itemsSummary));
    }

    /**
     * Parses a pipe-delimited line from {@code orders.txt} into an {@code OrderRecord}.
     *
     * @param line raw line read from {@code orders.txt}
     * @return parsed {@code OrderRecord}, or {@code null} if malformed
     */
    public static OrderRecord fromFileLine(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) {
            return null;
        }
        String[] p = line.split("\\|", -1);
        if (p.length < 15) {
            return null;
        }
        try {
            return new OrderRecord(
                    Integer.parseInt(p[0].trim()),
                    p[1].trim(),
                    p[2].trim(),
                    p[3].trim(),
                    p[4].trim(),
                    p[5].trim(),
                    p[6].trim(),
                    p[7].trim(),
                    Double.parseDouble(p[8].trim()),
                    Double.parseDouble(p[9].trim()),
                    Double.parseDouble(p[10].trim()),
                    Double.parseDouble(p[11].trim()),
                    Double.parseDouble(p[12].trim()),
                    p[13].trim(),
                    p[14].trim(),
                    new ArrayList<>()
            );
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Sanitizes field text to strip pipe characters and line breaks.
     *
     * @param value raw text
     * @return sanitized string
     */
    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "/").replace("\n", " ").replace("\r", " ").trim();
    }
}
