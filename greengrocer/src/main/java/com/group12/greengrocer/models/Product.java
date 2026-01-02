package com.group12.greengrocer.models;

/**
 * Represents a product entity (vegetable or fruit) in the inventory.
 * <p>
 * This class encapsulates all product details including pricing, inventory management
 * (stock levels and thresholds), binary image data, and dynamic pricing logic based
 * on scarcity.
 * </p>
 */
public class Product {
    
    /** Unique identifier for the product. */
    private int id;
    
    /** The display name of the product (e.g., "Tomato"). */
    private String name;
    
    /** The category of the product (e.g., "vegetable" or "fruit"). */
    private String type; 
    
    /** The base unit price of the product. */
    private double price;
    
    /** The current quantity available in stock (e.g., in kg or units). */
    private double stock;
    
    /** * The stock level limit that triggers dynamic pricing.
     * If stock falls below this value, the price increases.
     */
    private double threshold;
    
    /** The image of the product stored as a byte array (BLOB). */
    private byte[] image;
    
    /** The file extension/type of the image (e.g., "jpg", "png"). */
    private String imageType;
    
    /** Flag indicating if the product is active (true) or soft-deleted (false). */
    private boolean isActive;
    
    /**
     * Default constructor.
     */
    public Product() {}
    
    /**
     * Constructs a new Product with core details.
     * <p>
     * The {@code isActive} flag is set to {@code true} by default.
     * </p>
     *
     * @param id        The unique product ID.
     * @param name      The name of the product.
     * @param type      The category type.
     * @param price     The base price.
     * @param stock     The initial stock quantity.
     * @param threshold The low-stock threshold value.
     */
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

    /**
     * Gets the unique identifier.
     * @return The product ID.
     */
    public int getId() { return id; }

    /**
     * Sets the unique identifier.
     * @param id The product ID to set.
     */
    public void setId(int id) { this.id = id; }
    
    /**
     * Gets the product name.
     * @return The name.
     */
    public String getName() { return name; }

    /**
     * Sets the product name.
     * @param name The name to set.
     */
    public void setName(String name) { this.name = name; }
    
    /**
     * Gets the product category/type.
     * @return The type string (e.g., "fruit").
     */
    public String getType() { return type; }

    /**
     * Sets the product category/type.
     * @param type The type to set.
     */
    public void setType(String type) { this.type = type; }
    
    /**
     * Gets the base price of the product.
     * @return The price.
     */
    public double getPrice() { return price; }

    /**
     * Sets the base price of the product.
     * @param price The price to set.
     */
    public void setPrice(double price) { this.price = price; }
    
    /**
     * Gets the current stock quantity.
     * @return The stock amount.
     */
    public double getStock() { return stock; }

    /**
     * Sets the current stock quantity.
     * @param stock The stock amount to set.
     */
    public void setStock(double stock) { this.stock = stock; }
    
    /**
     * Gets the low-stock threshold.
     * @return The threshold value.
     */
    public double getThreshold() { return threshold; }

    /**
     * Sets the low-stock threshold.
     * @param threshold The threshold value to set.
     */
    public void setThreshold(double threshold) { this.threshold = threshold; }
    
    /**
     * Gets the product image data.
     * @return A byte array representing the image.
     */
    public byte[] getImage() { return image; }

    /**
     * Sets the product image data.
     * @param image The byte array containing image data.
     */
    public void setImage(byte[] image) { this.image = image; }
    
    /**
     * Gets the image file extension/type.
     * @return The image type string.
     */
    public String getImageType() { return imageType; }

    /**
     * Sets the image file extension/type.
     * @param imageType The image type string.
     */
    public void setImageType(String imageType) { this.imageType = imageType; }
    
    /**
     * Checks if the product is active.
     * @return {@code true} if active; {@code false} if soft-deleted.
     */
    public boolean isActive() { return isActive; }

    /**
     * Sets the active status of the product.
     * @param active The status to set.
     */
    public void setActive(boolean active) { isActive = active; }
    
    /**
     * Calculates the dynamic selling price based on inventory levels.
     * <p>
     * Implements a supply-and-demand pricing strategy:
     * </p>
     * <ul>
     * <li>If {@code stock} &lt;= {@code threshold}: The price is <b>doubled</b> 
     * to reflect scarcity.</li>
     * <li>Otherwise: The base {@code price} is returned.</li>
     * </ul>
     * * @return The calculated current price.
     */
    public double getCurrentPrice() {
        if (stock <= threshold) {
            return price * 2; // Double price when stock is low
        }
        return price;
    }
    
    /**
     * Returns a string representation of the product.
     * @return A formatted string containing ID, name, type, price, and stock.
     */
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