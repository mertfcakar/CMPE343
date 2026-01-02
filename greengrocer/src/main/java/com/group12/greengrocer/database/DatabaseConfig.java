package com.group12.greengrocer.database;

/**
 * Provides configuration constants for the database connection.
 * <p>
 * This class holds the necessary parameters such as the URL, username,
 * password, and driver class name required to establish a JDBC connection
 * to the Green Grocer application's MySQL database.
 * </p>
 */
public class DatabaseConfig {

    /**
     * The JDBC URL for connecting to the MySQL database.
     * <p>
     * It specifies the protocol, host (localhost), port (3306), and the specific
     * database name (greengrocer).
     * </p>
     */
    public static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer";

    /**
     * The username used for database authentication.
     */
    public static final String DB_USER = "myuser";

    /**
     * The password used for database authentication.
     */
    public static final String DB_PASSWORD = "1234";

    /**
     * The fully qualified class name of the MySQL JDBC driver.
     * <p>
     * This string is typically used with {@code Class.forName()} to load
     * the driver dynamically at runtime.
     * </p>
     */
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
}