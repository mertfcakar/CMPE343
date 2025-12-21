package com.group12.greengrocer.models;

/**
 * User model class representing a user in the system
 * Supports three roles: customer, carrier, owner
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String address;
    private String contactDetails;
    
    /**
     * Default constructor
     */
    public User() {
    }
    
    /**
     * Constructor with basic parameters
     */
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    /**
     * Constructor with all parameters
     */
    public User(int id, String username, String password, String role, 
                String address, String contactDetails) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.contactDetails = contactDetails;
    }
    
    // Getters and Setters
    
    /**
     * Get user ID
     * @return user ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set user ID
     * @param id user ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get username
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Set username
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Get password
     * @return password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Set password
     * @param password password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Get user role
     * @return role (customer, carrier, or owner)
     */
    public String getRole() {
        return role;
    }
    
    /**
     * Set user role
     * @param role role (customer, carrier, or owner)
     */
    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * Get user address
     * @return address
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * Set user address
     * @param address address
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * Get contact details
     * @return contact details
     */
    public String getContactDetails() {
        return contactDetails;
    }
    
    /**
     * Set contact details
     * @param contactDetails contact details
     */
    public void setContactDetails(String contactDetails) {
        this.contactDetails = contactDetails;
    }
    
    /**
     * Check if user is a customer
     * @return true if role is customer
     */
    public boolean isCustomer() {
        return "customer".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is a carrier
     * @return true if role is carrier
     */
    public boolean isCarrier() {
        return "carrier".equalsIgnoreCase(role);
    }
    
    /**
     * Check if user is an owner
     * @return true if role is owner
     */
    public boolean isOwner() {
        return "owner".equalsIgnoreCase(role);
    }
    
    /**
     * String representation of User
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", address='" + address + '\'' +
                ", contactDetails='" + contactDetails + '\'' +
                '}';
    }
    
    /**
     * Check equality based on ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }
    
    /**
     * Generate hash code based on ID
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}