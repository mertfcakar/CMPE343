package com.group12.greengrocer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ImageLoader {

    // Veritabanı Bilgileri
    private static final String URL = "jdbc:mysql://localhost:3306/greengrocer";
    private static final String USER = "myuser";
    private static final String PASS = "1234";

    // Resimlerin olduğu klasör (Projenin resources klasörü)
    // Eğer çalışmazsa tam dosya yolunu yaz: "C:/Users/Adın/Project/src/main/resources/images/"
    private static final String IMAGE_DIR = "C:\\Users\\mertf\\Documents\\CMPE343\\greengrocer\\src\\main\\resources\\images\\";

    public static void main(String[] args) {
        System.out.println("Resim yükleme işlemi başlıyor...");

        // Türkçe ürün isimleri ve karşılık gelen dosya adları (Türkçe karakter sorunu olmasın diye mapliyoruz)
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

            // Resmi binary stream olarak ayarla
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