package com.group12.greengrocer.database;

import com.group12.greengrocer.models.User;

/**
 * Test database connection and login
 */
public class TestConnection {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===\n");
        
        // Test 1: Connection
        System.out.println("Test 1: Testing database connection...");
        if (DatabaseConnection.testConnection()) {
            System.out.println("✓ Connection successful!\n");
        } else {
            System.out.println("✗ Connection failed!\n");
            return;
        }
        
        // Test 2: Login with valid credentials
        System.out.println("Test 2: Testing login with valid credentials...");
        User user1 = UserDAO.login("cust", "cust");
        if (user1 != null) {
            System.out.println("✓ Login successful!");
            System.out.println("  Username: " + user1.getUsername());
            System.out.println("  Role: " + user1.getRole());
            System.out.println("  Address: " + user1.getAddress());
        } else {
            System.out.println("✗ Login failed!");
        }
        System.out.println();
        
        // Test 3: Login with invalid credentials
        System.out.println("Test 3: Testing login with invalid credentials...");
        User user2 = UserDAO.login("wronguser", "wrongpass");
        if (user2 == null) {
            System.out.println("✓ Correctly rejected invalid credentials!\n");
        } else {
            System.out.println("✗ Should have rejected invalid credentials!\n");
        }
        
        // Test 4: Check username exists
        System.out.println("Test 4: Testing username check...");
        boolean exists = UserDAO.usernameExists("cust");
        System.out.println("Username 'cust' exists: " + exists);
        System.out.println();
        
        System.out.println("=== All Tests Complete ===");
        
        // Close connection
        DatabaseConnection.closeConnection();
    }
}