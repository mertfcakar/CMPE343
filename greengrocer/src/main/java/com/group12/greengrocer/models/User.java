package com.group12.greengrocer.models;

/**
 * Represents a registered user in the GreenGrocer system.
 * <p>
 * This class serves as the base entity for all user types (e.g., Customers, Carriers, Admins).
 * It stores authentication credentials, personal information, and contact details.
 * </p>
 * * @author Group12
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String address;
    private String email;        // Replaces the old ContactDetails object
    private String phoneNumber;  // Replaces the old ContactDetails object
    private String neighborhood;

    /**
     * Default constructor.
     * <p>
     * Creates an empty User instance. Needed for serialization and database mapping frameworks.
     * </p>
     */
    public User() {}

    /**
     * Fully parameterized constructor to initialize a User object.
     *
     * @param id           The unique identifier for the user.
     * @param username     The username used for logging in.
     * @param password     The user's password (should be hashed in a real scenario).
     * @param role         The role of the user (e.g., "CUSTOMER", "CARRIER", "MANAGER").
     * @param address      The physical address of the user.
     * @param email        The user's email address.
     * @param phoneNumber  The user's phone number.
     * @param neighborhood The neighborhood where the user resides.
     */
    public User(int id, String username, String password, String role, String address, String email, String phoneNumber, String neighborhood) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.neighborhood = neighborhood;
    }

    // --- GETTER & SETTER METHODS ---

    /**
     * Gets the unique ID of the user.
     * @return The user ID.
     */
    public int getId() { return id; }

    /**
     * Sets the unique ID of the user.
     * @param id The user ID to set.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the username.
     * @return The username string.
     */
    public String getUsername() { return username; }

    /**
     * Sets the username.
     * @param username The username to set.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Gets the user's password.
     * @return The password string.
     */
    public String getPassword() { return password; }

    /**
     * Sets the user's password.
     * @param password The password to set.
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Gets the user's role in the system.
     * @return The role (e.g., "CUSTOMER", "CARRIER").
     */
    public String getRole() { return role; }

    /**
     * Sets the user's role.
     * @param role The role to set.
     */
    public void setRole(String role) { this.role = role; }

    /**
     * Gets the user's physical address.
     * @return The address string.
     */
    public String getAddress() { return address; }

    /**
     * Sets the user's physical address.
     * @param address The address to set.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Gets the user's email address.
     * @return The email string.
     */
    public String getEmail() { return email; }

    /**
     * Sets the user's email address.
     * @param email The email to set.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the user's phone number.
     * @return The phone number string.
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * Sets the user's phone number.
     * @param phoneNumber The phone number to set.
     */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * Gets the neighborhood of the user.
     * @return The neighborhood name.
     */
    public String getNeighborhood() { return neighborhood; }

    /**
     * Sets the neighborhood of the user.
     * @param neighborhood The neighborhood name to set.
     */
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    
    /**
     * Provides a formatted string containing contact details (Email and Phone).
     * <p>
     * <b>Note:</b> This is a compatibility method provided to prevent legacy code from breaking
     * after the removal of the specific {@code ContactDetails} object.
     * </p>
     * * @return A string in the format "email / phoneNumber".
     */
    public String getContactDetails() { return email + " / " + phoneNumber; }
}