package com.momandpop.pizza.model;

/**
 * Represents a restaurant staff member, manager, or owner stored in {@code data/staff.txt}.
 */
public class StaffUser {

    /** Unique numeric identifier for the staff member. */
    private int id;

    /** Unique login username (stored in lowercase). */
    private String username;

    /** Account password for staff terminal login. */
    private String password;

    /** Employee full legal name. */
    private String fullName;

    /** Authorization role such as "owner", "manager", or "worker". */
    private String role;

    /** Contact phone number for the employee. */
    private String phone;

    /** Contact email address for the employee. */
    private String email;

    /** Date the employee was hired in YYYY-MM-DD format. */
    private String dateHired;

    /** Indicates whether the employee account is currently active. */
    private boolean active;

    /**
     * Constructs an empty {@code StaffUser} instance with default values.
     */
    public StaffUser() {
        this.active = true;
        this.role = "worker";
    }

    /**
     * Constructs a fully populated {@code StaffUser} instance.
     *
     * @param id        unique staff identifier
     * @param username  login username
     * @param password  login password
     * @param fullName  employee full name
     * @param role      staff role ("owner", "manager", or "worker")
     * @param phone     contact phone number
     * @param email     contact email address
     * @param dateHired hire date in YYYY-MM-DD format
     * @param active    whether the staff account is active
     */
    public StaffUser(int id, String username, String password, String fullName,
                     String role, String phone, String email, String dateHired, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.dateHired = dateHired;
        this.active = active;
    }

    /**
     * Returns the staff member ID.
     *
     * @return unique staff ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the staff member ID.
     *
     * @param id unique staff ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the login username.
     *
     * @return login username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the login username.
     *
     * @param username login username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the login password.
     *
     * @return login password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the login password.
     *
     * @param password login password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the employee full name.
     *
     * @return full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the employee full name.
     *
     * @param fullName full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the employee role.
     *
     * @return role ("owner", "manager", or "worker")
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the employee role.
     *
     * @param role role ("owner", "manager", or "worker")
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the employee phone number.
     *
     * @return phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the employee phone number.
     *
     * @param phone phone number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the employee email address.
     *
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the employee email address.
     *
     * @param email email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the hire date string.
     *
     * @return hire date (YYYY-MM-DD)
     */
    public String getDateHired() {
        return dateHired;
    }

    /**
     * Sets the hire date string.
     *
     * @param dateHired hire date (YYYY-MM-DD)
     */
    public void setDateHired(String dateHired) {
        this.dateHired = dateHired;
    }

    /**
     * Checks whether this account is active.
     *
     * @return {@code true} if active, {@code false} if archived
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether this account is active.
     *
     * @param active {@code true} if active, {@code false} if archived
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Serializes this staff record into a pipe-delimited line for {@code staff.txt}.
     *
     * @return pipe-delimited string representation
     */
    public String toFileLine() {
        return String.join("|",
                String.valueOf(id),
                sanitize(username),
                sanitize(password),
                sanitize(fullName),
                sanitize(role),
                sanitize(phone),
                sanitize(email),
                sanitize(dateHired),
                String.valueOf(active));
    }

    /**
     * Parses a pipe-delimited line from {@code staff.txt} into a {@code StaffUser} object.
     *
     * @param line raw line read from {@code staff.txt}
     * @return parsed {@code StaffUser}, or {@code null} if malformed
     */
    public static StaffUser fromFileLine(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) {
            return null;
        }
        try {
            int parsedId = Integer.parseInt(parts[0].trim());
            boolean isActive = Boolean.parseBoolean(parts[8].trim());
            return new StaffUser(
                    parsedId,
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim(),
                    parts[4].trim(),
                    parts[5].trim(),
                    parts[6].trim(),
                    parts[7].trim(),
                    isActive
            );
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Strips pipe characters and newlines so file storage remains well-formed.
     *
     * @param value raw text value
     * @return sanitized non-null string
     */
    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("|", "/").replace("\n", " ").replace("\r", " ").trim();
    }
}
