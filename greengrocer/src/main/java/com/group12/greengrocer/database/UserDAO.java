package com.group12.greengrocer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.group12.greengrocer.models.CartItem;
import com.group12.greengrocer.models.User;
import com.group12.greengrocer.utils.ShoppingCart;

/**
 * Data Access Object (DAO) for managing User-related database operations.
 * This class handles CRUD operations for Users, Carriers, and Customers,
 * as well as Order creation transactions.
 */
public class UserDAO {

    /**
     * Authenticates a user based on their username and password.
     * * @param username The username of the user.
     * @param password The raw password of the user.
     * @return A {@link User} object if credentials are correct, or null if login fails.
     */
    public static User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE BINARY username = ? AND BINARY password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("address"),
                        rs.getString("email"),       
                        rs.getString("phone_number"),
                        rs.getString("neighborhood")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login operation: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if a user with the given username already exists in the database.
     * Useful for preventing duplicate registrations.
     * * @param username The username to check.
     * @return true if the user exists, false otherwise.
     */
    public static boolean isUserExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE BINARY username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registers a new customer into the database.
     * * @param username     The desired username.
     * @param password     The password.
     * @param address      The physical delivery address.
     * @param neighborhood The neighborhood region.
     * @param email        The customer's email address.
     * @param phone        The customer's phone number.
     * @return true if the registration was successful, false otherwise.
     */
    public static boolean registerCustomer(String username, String password, String address, String neighborhood, String email, String phone) {
        String sql = "INSERT INTO users (username, password, role, address, neighborhood, email, phone_number) VALUES (?, ?, 'customer', ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, address);
            ps.setString(4, neighborhood);
            ps.setString(5, email);
            ps.setString(6, phone);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Securely resets a user's password.
     * Requires the username, email, and phone number to match the database record.
     * * @param username    The user's username.
     * @param email       The registered email address.
     * @param phone       The registered phone number.
     * @param newPassword The new password to set.
     * @return true if the password was updated, false if the verification details were incorrect.
     */
    public static boolean resetPasswordSecure(String username, String email, String phone, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE BINARY username = ? AND email = ? AND phone_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, phone);
            
            return ps.executeUpdate() > 0; 
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- OWNER OPERATIONS ---

    /**
     * Retrieves all users with the 'carrier' role.
     * * @return A list of User objects representing carriers.
     */
    public static List<User> getAllCarriers() {
        List<User> carriers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'carrier'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                carriers.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    "carrier",
                    rs.getString("address"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("neighborhood")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carriers;
    }

    /**
     * Adds a new carrier to the system.
     * * @param username The carrier's username.
     * @param password The carrier's password.
     * @param email    The carrier's email address.
     * @param phone    The carrier's phone number.
     * @return true if the carrier was added successfully.
     */
    public static boolean addCarrier(String username, String password, String email, String phone) {
        String sql = "INSERT INTO users (username, password, role, email, phone_number) VALUES (?, ?, 'carrier', ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, phone);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user from the database by their unique ID.
     * * @param userId The ID of the user to delete.
     * @return true if the user was deleted successfully.
     */
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- ORDER CREATION ---

    /**
     * Creates a new order and saves all order items in a single transaction.
     * <p>
     * This method uses JDBC Transaction management (setAutoCommit(false)).
     * If saving the order or any of the items fails, the entire operation is rolled back
     * to ensure data integrity.
     * </p>
     * * @param user     The user placing the order.
     * @param subtotal The subtotal amount before taxes/discounts.
     * @param vat      The calculated VAT amount.
     * @param discount The calculated discount amount.
     * @param total    The final total cost.
     * @param date     The requested delivery date.
     * @param timeSlot The requested delivery time slot (Format expected: "HH:mm - HH:mm").
     * @return true if the order is successfully created, false otherwise.
     */
    public static boolean createOrder(User user, double subtotal, double vat, double discount, double total, 
                                      LocalDate date, String timeSlot) {
        
        String orderSql = "INSERT INTO orders (user_id, status, subtotal, vat_amount, discount_amount, total_cost, " +
                          "order_time, requested_delivery_date, delivery_neighborhood, delivery_address, payment_method) " +
                          "VALUES (?, 'pending', ?, ?, ?, ?, NOW(), ?, ?, ?, 'CASH_ON_DELIVERY')";
        
        String itemSql = "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price, total_price) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

        // Parse time slot (Expects format like "09:00 - 11:00")
        String startTime = timeSlot.split(" - ")[0];
        if (startTime.length() == 4) startTime = "0" + startTime; 
        
        Timestamp deliveryTs = Timestamp.valueOf(date.atTime(LocalTime.parse(startTime)));

        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psItem = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Save the Order Header
            psOrder = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, user.getId());
            psOrder.setDouble(2, subtotal);
            psOrder.setDouble(3, vat);
            psOrder.setDouble(4, discount);
            psOrder.setDouble(5, total);
            psOrder.setTimestamp(6, deliveryTs);
            psOrder.setString(7, user.getNeighborhood());
            psOrder.setString(8, user.getAddress());
            
            int affectedRows = psOrder.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating order failed, no rows affected.");

            int orderId = 0;
            rs = psOrder.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getInt(1);
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // 2. Save Order Items (Batch Processing)
            psItem = conn.prepareStatement(itemSql);
            for (CartItem item : ShoppingCart.getInstance().getItems()) {
                psItem.setInt(1, orderId);
                psItem.setInt(2, item.getProduct().getId());
                psItem.setString(3, item.getProduct().getName());
                psItem.setDouble(4, item.getQuantity());
                psItem.setDouble(5, item.getProduct().getCurrentPrice());
                psItem.setDouble(6, item.getTotalPrice());
                psItem.addBatch();
            }
            psItem.executeBatch();

            conn.commit(); // Commit Transaction
            return true;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (psOrder != null) psOrder.close(); } catch (SQLException e) {}
            try { if (psItem != null) psItem.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    /**
     * Updates the user's profile information.
     * * @param userId      The ID of the user to update.
     * @param newAddress  The new address.
     * @param newEmail    The new email address.
     * @param newPhone    The new phone number.
     * @param newPassword The new password.
     * @return true if the update was successful.
     */
    public static boolean updateUserProfile(int userId, String newAddress, String newEmail, String newPhone, String newPassword) {
        String sql = "UPDATE users SET address = ?, email = ?, phone_number = ?, password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newAddress);
            ps.setString(2, newEmail);
            ps.setString(3, newPhone);
            ps.setString(4, newPassword);
            ps.setInt(5, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * Retrieves the ID of the store owner.
     * Assumes there is only one user with the 'owner' role.
     * * @return The ID of the owner, or 0 if not found.
     */
    public static int getOwnerId() {
        String sql = "SELECT id FROM users WHERE role = 'owner' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0; 
    }

    /**
     * Retrieves all users with the 'customer' role.
     * * @return A list of User objects representing customers.
     */
    public static List<User> getAllCustomers() {
        List<User> customers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'customer'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    "customer",
                    rs.getString("address"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("neighborhood")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
}