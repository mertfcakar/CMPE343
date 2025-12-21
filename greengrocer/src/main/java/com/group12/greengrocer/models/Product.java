package com.group12.greengrocer.models;

/**
 * Product model representing vegetables and fruits
 */
public class Product {
    private int id;
    private String name;
    private String type; // "vegetable" or "fruit"
    private double price;
    private double stock;
    private double threshold;
    private byte[] image;
    private String imageType;
    private boolean isActive;
    
    public Product() {}
    
    public Product(int id, String name, String type, double price, double stock, double threshold) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }
    
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
    
    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    /**
     * Calculate current price based on stock threshold
     */
    public double getCurrentPrice() {
        if (stock <= threshold) {
            return price * 2; // Double price when stock is low
        }
        return price;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}