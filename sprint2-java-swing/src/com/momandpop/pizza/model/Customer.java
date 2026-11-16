package com.momandpop.pizza.model;

/**
 * Represents a pizzeria customer profile persisted in {@code data/customers.txt}.
 */
public class Customer {

    /** Unique numeric identifier for the customer record. */
    private int id;

    /** Customer full name. */
    private String name;

    /** Raw 10-digit telephone number used as the primary lookup key. */
    private String phone;

    /** Preferred fulfillment method: "delivery" or "pickup". */
    private String orderType;

    /** Delivery street address. */
    private String streetAddress;

    /** Optional subdivision, neighborhood, or apartment complex name. */
    private String subdivision;

    /** Nearest major intersection to assist delivery drivers. */
    private String nearestIntersection;

    /**
     * Constructs an empty {@code Customer} with delivery as the default order type.
     */
    public Customer() {
        this.orderType = "delivery";
    }

    /**
     * Constructs a {@code Customer} with complete contact and delivery fields.
     *
     * @param id                  unique customer ID
     * @param name                customer full name
     * @param phone               10-digit phone number
     * @param orderType           "delivery" or "pickup"
     * @param streetAddress       street address for delivery
     * @param subdivision         subdivision or complex name
     * @param nearestIntersection nearest major intersection
     */
    public Customer(int id, String name, String phone, String orderType,
                    String streetAddress, String subdivision, String nearestIntersection) {
        this.id = id;
        this.name = name;
        this.phone = normalizeDigits(phone);
        this.orderType = (orderType == null || orderType.isEmpty()) ? "delivery" : orderType;
        this.streetAddress = streetAddress == null ? "" : streetAddress;
        this.subdivision = subdivision == null ? "" : subdivision;
        this.nearestIntersection = nearestIntersection == null ? "" : nearestIntersection;
    }

    /**
     * Returns the unique customer ID.
     *
     * @return customer ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique customer ID.
     *
     * @param id customer ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the customer's full name.
     *
     * @return full name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the customer's full name.
     *
     * @param name full name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the normalized 10-digit phone number.
     *
     * @return 10-digit phone string
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets and normalizes the phone number to digits only.
     *
     * @param phone raw or formatted phone number
     */
    public void setPhone(String phone) {
        this.phone = normalizeDigits(phone);
    }

    /**
     * Returns the phone number formatted as {@code XXX-XXX-XXXX} when 10 digits are present.
     *
     * @return formatted phone number string
     */
    public String getFormattedPhone() {
        String digits = normalizeDigits(phone);
        if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        return digits;
    }

    /**
     * Returns the order fulfillment type ("delivery" or "pickup").
     *
     * @return order type
     */
    public String getOrderType() {
        return orderType;
    }

    /**
     * Sets the order fulfillment type ("delivery" or "pickup").
     *
     * @param orderType order type
     */
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    /**
     * Returns the delivery street address.
     *
     * @return street address
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Sets the delivery street address.
     *
     * @param streetAddress street address
     */
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    /**
     * Returns the subdivision or apartment complex.
     *
     * @return subdivision
     */
    public String getSubdivision() {
        return subdivision;
    }

    /**
     * Sets the subdivision or apartment complex.
     *
     * @param subdivision subdivision
     */
    public void setSubdivision(String subdivision) {
        this.subdivision = subdivision;
    }

    /**
     * Returns the nearest major intersection.
     *
     * @return nearest intersection
     */
    public String getNearestIntersection() {
        return nearestIntersection;
    }

    /**
     * Sets the nearest major intersection.
     *
     * @param nearestIntersection nearest intersection
     */
    public void setNearestIntersection(String nearestIntersection) {
        this.nearestIntersection = nearestIntersection;
    }

    /**
     * Builds a single-line summary of the delivery address.
     *
     * @return combined address string or "Pickup at Counter"
     */
    public String getFullAddressSummary() {
        if ("pickup".equalsIgnoreCase(orderType)) {
            return "Pickup at Counter";
        }
        StringBuilder sb = new StringBuilder();
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            sb.append(streetAddress.trim());
        }
        if (subdivision != null && !subdivision.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(subdivision.trim());
        }
        if (nearestIntersection != null && !nearestIntersection.trim().isEmpty()) {
            sb.append(" (Near ").append(nearestIntersection.trim()).append(")");
        }
        return sb.length() == 0 ? "N/A" : sb.toString();
    }

    /**
     * Extracts numeric digits from a telephone string.
     *
     * @param raw raw phone text
     * @return digits-only string
     */
    public static String normalizeDigits(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\D", "");
    }

    /**
     * Serializes this customer record into a pipe-delimited line for {@code customers.txt}.
     *
     * @return pipe-delimited representation
     */
    public String toFileLine() {
        return String.join("|",
                String.valueOf(id),
                sanitize(name),
                sanitize(phone),
                sanitize(orderType),
                sanitize(streetAddress),
                sanitize(subdivision),
                sanitize(nearestIntersection));
    }

    /**
     * Parses a pipe-delimited line from {@code customers.txt} into a {@code Customer}.
     *
     * @param line raw line from {@code customers.txt}
     * @return parsed {@code Customer}, or {@code null} if invalid
     */
    public static Customer fromFileLine(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            return null;
        }
        try {
            int parsedId = Integer.parseInt(parts[0].trim());
            return new Customer(
                    parsedId,
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim(),
                    parts[6].trim()
            );
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Sanitizes field text to prevent pipe delimiters or line breaks.
     *
     * @param value input string
     * @return sanitized string
     */
    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "/").replace("\n", " ").replace("\r", " ").trim();
    }
}
