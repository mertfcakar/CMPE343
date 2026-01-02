package com.group12.greengrocer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Utility class responsible for batch loading product images into the database.
 * <p>
 * This class connects to the MySQL database and updates the 'image' BLOB column
 * for existing products by reading image files from a specified local directory.
 * It is intended to be run as a standalone script for database initialization or maintenance.
 * </p>
 * * @author Group12
 */
public class ImageLoader {

    // Database Connection Constants
    private static final String URL = "jdbc:mysql://localhost:3306/greengrocer";
    private static final String USER = "myuser";
    private static final String PASS = "1234";

    /**
     * The absolute path to the directory containing product images.
     * <p>
     * <b>Warning:</b> This path is hardcoded. Ensure this path exists on the machine
     * running this code, or update it to match your project structure.
     * </p>
     */
    private static final String IMAGE_DIR = "C:\\Users\\mertf\\Documents\\CMPE343\\greengrocer\\src\\main\\resources\\images\\";

    /**
     * The main entry point for the image loading utility.
     * <p>
     * Executing this method will trigger the update process for a predefined list
     * of products (Fruits and Vegetables). It maps Turkish product names to their
     * corresponding image filenames.
     * </p>
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("Resim yükleme işlemi başlıyor...");

        // Vegetables
        updateImage("Domates", "domates.jpg");
        updateImage("Salatalık", "salatalik.jpg");
        updateImage("Biber", "biber.jpg");
        updateImage("Patlıcan", "patlican.jpg");
        updateImage("Patates", "patates.jpg");
        updateImage("Soğan", "sogan.jpg");
        updateImage("Havuç", "havuc.jpg");
        updateImage("Ispanak", "ispanak.jpg");
        updateImage("Marul", "marul.jpg");
        updateImage("Brokoli", "brokoli.jpg");
        updateImage("Kabak", "kabak.jpg");
        updateImage("Sarımsak", "sarimsak.jpg");

        // Fruits
        updateImage("Elma", "elma.jpg");
        updateImage("Armut", "armut.jpg");
        updateImage("Muz", "muz.jpg");
        updateImage("Çilek", "cilek.jpg");
        updateImage("Kiraz", "kiraz.jpg");
        updateImage("Karpuz", "karpuz.jpg");
        updateImage("Kavun", "kavun.jpg");
        updateImage("Üzüm", "uzum.jpg");
        updateImage("Portakal", "portakal.jpg");
        updateImage("Mandalina", "mandalina.jpg");
        updateImage("Şeftali", "seftali.jpg");
        updateImage("Erik", "erik.jpg");

        System.out.println("İşlem Tamamlandı!");
    }

    /**
     * Updates the image for a specific product in the database.
     * <p>
     * This method reads the specified image file from {@code IMAGE_DIR}, converts it into
     * a binary stream, and updates the 'image' column of the 'products' table where the
     * product name matches the provided {@code productName}.
     * </p>
     *
     * @param productName The name of the product in the database (e.g., "Domates").
     * @param fileName    The filename of the image (e.g., "domates.jpg").
     */
    private static void updateImage(String productName, String fileName) {
        String sql = "UPDATE products SET image = ? WHERE name = ?";
        File imageFile = new File(IMAGE_DIR + fileName);

        if (!imageFile.exists()) {
            System.err.println("HATA: Dosya bulunamadı -> " + fileName);
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql);
             FileInputStream fis = new FileInputStream(imageFile)) {

            // Set the image as a binary stream
            ps.setBinaryStream(1, fis, (int) imageFile.length());
            ps.setString(2, productName);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Başarılı: " + productName + " -> " + fileName);
            } else {
                System.out.println("Uyarı: Veritabanında bulunamadı -> " + productName);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}