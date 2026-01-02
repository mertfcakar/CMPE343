package com.group12.greengrocer.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the lifecycle of the JDBC database connection.
 * <p>
 * This utility class provides static methods to establish, retrieve, close,
 * and test the connection to the database using configurations defined
 * in {@link DatabaseConfig}.
 * </p>
 */
public class DatabaseConnection {

    /**
     * The single static connection instance shared across the application.
     */
    private static Connection connection = null;

    /**
     * Retrieves the active database connection.
     * <p>
     * This method implements a lazy initialization pattern. If the connection
     * is null or closed, it attempts to load the JDBC driver and establish a
     * new connection using the credentials from {@link DatabaseConfig}.
     * </p>
     *
     * @return The active {@link Connection} object, or {@code null} if the
     *         connection could not be established due to a driver or SQL error.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load MySQL JDBC Driver
                Class.forName(DatabaseConfig.JDBC_DRIVER);

                // Create connection
                connection = DriverManager.getConnection(
                        DatabaseConfig.DB_URL,
                        DatabaseConfig.DB_USER,
                        DatabaseConfig.DB_PASSWORD);

                System.out.println("Database connection established successfully!");
            }
            return connection;

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes the current database connection safely.
     * <p>
     * It checks if the connection exists and is open before attempting to close it.
     * Any {@code SQLException} occurring during this process is caught and logged.
     * </p>
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection!");
            e.printStackTrace();
        }
    }

    /**
     * Tests the validity of the database connection.
     * <p>
     * This method attempts to retrieve a connection instance and checks if it
     * is open and valid.
     * </p>
     *
     * @return {@code true} if a valid connection is established;
     *         {@code false} otherwise.
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}