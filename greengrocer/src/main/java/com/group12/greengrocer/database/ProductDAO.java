package com.group12.greengrocer.database;

import com.group12.greengrocer.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product operations
 */
public class ProductDAO {
    
    /**
     * Get all active products
     */
    public static List<Product> getAllProducts() {
        // TODO: Implement
        return new ArrayList<>();
    }
    
    /**
     * Get products by type (vegetable or fruit)
     */
    public static List<Product> getProductsByType(String type) {
        // TODO: Implement
        return new ArrayList<>();
    }
    
    /**
     * Get product by ID
     */
    public static Product getProductById(int id) {
        // TODO: Implement
        return null;
    }
    
    /**
     * Add new product
     */
    public static boolean addProduct(Product product) {
        // TODO: Implement
        return false;
    }
    
    /**
     * Update existing product
     */
    public static boolean updateProduct(Product product) {
        // TODO: Implement
        return false;
    }
    
    /**
     * Delete product (set inactive)
     */
    public static boolean deleteProduct(int productId) {
        // TODO: Implement
        return false;
    }
    
    /**
     * Update product stock
     */
    public static boolean updateStock(int productId, double newStock) {
        // TODO: Implement
        return false;
    }
    
    /**
     * Search products by name
     */
    public static List<Product> searchProducts(String keyword) {
        // TODO: Implement
        return new ArrayList<>();
    }
}