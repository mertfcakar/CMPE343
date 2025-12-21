package com.group12.greengrocer.database;

import com.group12.greengrocer.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for User operations
 */
public class UserDAO {
    
    /**
     * Authenticate user login
     * @param username Username
     * @param password Password
     * @return User object if login successful, null otherwise
     */
    public static User login(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            // Get database connection
            conn = DatabaseConnection.getConnection();
            
            if (conn == null) {
                System.err.println("Database connection failed!");
                return null;
            }
            
            // SQL query to check username and password
            String sql = "SELECT id, username, password, role, address, contact_details " +
                        "FROM users WHERE username = ? AND password = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            // Execute query
            rs = stmt.executeQuery();
            
            // If user found
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setAddress(rs.getString("address"));
                user.setContactDetails(rs.getString("contact_details"));
                
                System.out.println("Login successful for user: " + username);
                return user;
            } else {
                System.out.println("Login failed: Invalid username or password");
                return null;
            }
            
        } catch (SQLException e) {
            System.err.println("Error during login!");
            e.printStackTrace();
            return null;
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // Note: Don't close connection here, we'll reuse it
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Check if username already exists
     * @param username Username to check
     * @return true if username exists
     */
    public static boolean usernameExists(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return false;
    }
}