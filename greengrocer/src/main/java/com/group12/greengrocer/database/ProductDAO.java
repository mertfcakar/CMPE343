package com.group12.greengrocer.database;

import com.group12.greengrocer.models.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    // TÜM ÜRÜNLERİ ÇEK
    public static List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE is_active = TRUE";

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

                // Resim verisini al
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

    // YENİ ÜRÜN EKLE (RESİMLİ)
    public static boolean addProduct(String name, String type, double price, double stock, double threshold, File imageFile) {
        String sql = "INSERT INTO products (name, type, price, stock, threshold, image) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setDouble(3, price);
            ps.setDouble(4, stock);
            ps.setDouble(5, threshold);
            
            // Resim dosyasını Binary Stream olarak gönderiyoruz
            if (imageFile != null) {
                FileInputStream fis = new FileInputStream(imageFile);
                ps.setBinaryStream(6, fis, (int) imageFile.length());
            } else {
                ps.setBinaryStream(6, null);
            }
            
            return ps.executeUpdate() > 0;

        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ÜRÜN SİL (SOFT DELETE - is_active = FALSE yapılır)
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
    
    // ÜRÜN GÜNCELLE (Basit versiyon - Fiyat ve Stok)
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
}