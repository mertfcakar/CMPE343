package com.group12.greengrocer.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.group12.greengrocer.models.Product;

/**
 * Data Access Object (DAO) for managing {@link Product} entities.
 * <p>
 * This class provides methods to perform CRUD (Create, Read, Update, Delete)
 * operations on the 'products' table in the database, including handling
 * binary image data.
 * </p>
 */
public class ProductDAO {

    /**
     * Retrieves a list of all active products from the database.
     * <p>
     * This method queries for products where {@code is_active} is TRUE,
     * orders them alphabetically by name, and converts the Blob image data
     * into byte arrays for the Product model.
     * </p>
     *
     * @return A {@link List} of {@link Product} objects. Returns an empty list
     *         if no products are found or if a database error occurs.
     */
    public static List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setType(rs.getString("type"));
                p.setPrice(rs.getDouble("price"));
                p.setStock(rs.getDouble("stock"));
                p.setThreshold(rs.getDouble("threshold"));

                Blob blob = rs.getBlob("image");
                if (blob != null) {
                    p.setImage(blob.getBytes(1, (int) blob.length()));
                }
                products.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Checks if a product with the specific name and type already exists.
     * <p>
     * This is typically used to prevent duplicate entries before adding a new product.
     * </p>
     *
     * @param name The name of the product.
     * @param type The category or type of the product.
     * @return {@code true} if the product exists; {@code false} otherwise.
     */
    public static boolean productExists(String name, String type) {
        String sql = "SELECT COUNT(*) FROM products WHERE name = ? AND type = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Adds a new product to the database.
     * <p>
     * It first checks if the product already exists. If not, it inserts the
     * product details and the image file (if provided) into the database.
     * </p>
     *
     * @param name      The name of the product.
     * @param type      The category/type of the product.
     * @param price     The price per unit.
     * @param stock     The current stock quantity.
     * @param threshold The low-stock alert threshold.
     * @param imageFile The image file to be stored as a BLOB (can be null).
     * @return {@code true} if the product was successfully added;
     *         {@code false} if the product exists or a database error occurs.
     */
    public static boolean addProduct(String name, String type, double price, double stock, double threshold,
                                     File imageFile) {

        if (productExists(name, type)) {
            return false;
        }
        String sql = "INSERT INTO products (name, type, price, stock, threshold, image, image_type) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, type);
            ps.setDouble(3, price);
            ps.setDouble(4, stock);
            ps.setDouble(5, threshold);

            if (imageFile != null) {
                FileInputStream fis = new FileInputStream(imageFile);
                ps.setBinaryStream(6, fis, (int) imageFile.length());
                ps.setString(7, getFileExtension(imageFile.getName()));
            } else {
                ps.setNull(6, java.sql.Types.BLOB);
                ps.setNull(7, java.sql.Types.VARCHAR);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Performs a "Soft Delete" on a product.
     * <p>
     * Instead of permanently removing the record from the database, this method
     * sets the {@code is_active} flag to {@code FALSE}. This preserves historical data.
     * </p>
     *
     * @param productId The unique ID of the product to delete.
     * @return {@code true} if the operation was successful; {@code false} otherwise.
     */
    public static boolean deleteProduct(int productId) {
        String sql = "UPDATE products SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates only the price and stock quantity of a product.
     *
     * @param id    The unique ID of the product.
     * @param price The new price.
     * @param stock The new stock quantity.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     */
    public static boolean updateProductStockAndPrice(int id, double price, double stock) {
        String sql = "UPDATE products SET price = ?, stock = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setDouble(2, stock);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates all details of an existing product.
     * <p>
     * This method dynamically constructs the SQL query. If {@code imageFile} is provided,
     * the image BLOB is updated; otherwise, the existing image remains unchanged.
     * </p>
     *
     * @param id        The unique ID of the product to update.
     * @param name      The new name.
     * @param type      The new type.
     * @param price     The new price.
     * @param stock     The new stock quantity.
     * @param threshold The new threshold value.
     * @param imageFile The new image file (pass {@code null} to keep the current image).
     * @return {@code true} if the update was successful; {@code false} otherwise.
     */
    public static boolean updateProduct(int id, String name, String type, double price, double stock, double threshold, File imageFile) {
        String sql;
        if (imageFile != null) {
            sql = "UPDATE products SET name = ?, type = ?, price = ?, stock = ?, threshold = ?, image = ? WHERE id = ?";
        } else {
            sql = "UPDATE products SET name = ?, type = ?, price = ?, stock = ?, threshold = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setDouble(3, price);
            ps.setDouble(4, stock);
            ps.setDouble(5, threshold);

            if (imageFile != null) {
                FileInputStream fis = new FileInputStream(imageFile);
                ps.setBinaryStream(6, fis, (int) imageFile.length());
                ps.setInt(7, id);
            } else {
                ps.setInt(6, id);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extracts the file extension from a given filename.
     *
     * @param fileName The name of the file (e.g., "image.png").
     * @return The extension (e.g., "png") or an empty string if no extension is found.
     */
    private static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index + 1);
        }
        return "";
    }
}