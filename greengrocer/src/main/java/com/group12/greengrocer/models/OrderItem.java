package com.group12.greengrocer.models;

/**
 * Represents a single line item within a customer's order.
 * <p>
 * This class captures the specific details of a product at the moment of purchase,
 * including its name, unit price, quantity, and the calculated total price for this line.
 * Storing these details (like price and name) ensures historical accuracy even if
 * the original product details change later.
 * </p>
 */
public class OrderItem {
    
    /**
     * The unique identifier for this order item.
     */
    private int id;
    
    /**
     * The ID of the parent {@code Order} to which this item belongs.
     */
    private int orderId;
    
    /**
     * The ID of the product purchased.
     */
    private int productId;
    
    /**
     * The name of the product at the time of purchase.
     */
    private String productName;
    
    /**
     * The quantity of the product purchased (e.g., units or weight).
     */
    private double quantity;
    
    /**
     * The price per unit of the product at the time of purchase.
     */
    private double unitPrice;
    
    /**
     * The total cost for this line item (Quantity * Unit Price).
     */
    private double totalPrice;
    
    /**
     * Default constructor.
     * <p>
     * Creates an empty OrderItem instance. typically used for database mapping.
     * </p>
     */
    public OrderItem() {}
    
    /**
     * Constructs a new OrderItem with specified details.
     * <p>
     * The {@code totalPrice} is automatically calculated upon initialization.
     * </p>
     *
     * @param productId   The ID of the product.
     * @param productName The name of the product.
     * @param quantity    The amount purchased.
     * @param unitPrice   The cost per unit.
     */
    public OrderItem(int productId, String productName, double quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
    }
    
    // Getters and Setters

    /**
     * Gets the unique ID of the order item.
     * @return The ID.
     */
    public int getId() { return id; }
    
    /**
     * Sets the unique ID of the order item.
     * @param id The ID to set.
     */
    public void setId(int id) { this.id = id; }
    
    /**
     * Gets the ID of the parent order.
     * @return The order ID.
     */
    public int getOrderId() { return orderId; }
    
    /**
     * Sets the ID of the parent order.
     * @param orderId The order ID to set.
     */
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    /**
     * Gets the product ID.
     * @return The product ID.
     */
    public int getProductId() { return productId; }
    
    /**
     * Sets the product ID.
     * @param productId The product ID to set.
     */
    public void setProductId(int productId) { this.productId = productId; }
    
    /**
     * Gets the product name.
     * @return The product name.
     */
    public String getProductName() { return productName; }
    
    /**
     * Sets the product name.
     * @param productName The product name to set.
     */
    public void setProductName(String productName) { this.productName = productName; }
    
    /**
     * Gets the quantity purchased.
     * @return The quantity.
     */
    public double getQuantity() { return quantity; }
    
    /**
     * Sets the quantity of the product.
     * <p>
     * <b>Note:</b> This method automatically recalculates the {@code totalPrice}
     * based on the new quantity and the existing unit price.
     * </p>
     *
     * @param quantity The new quantity.
     */
    public void setQuantity(double quantity) { 
        this.quantity = quantity;
        this.totalPrice = quantity * unitPrice;
    }
    
    /**
     * Gets the price per unit.
     * @return The unit price.
     */
    public double getUnitPrice() { return unitPrice; }
    
    /**
     * Sets the price per unit.
     * <p>
     * <b>Note:</b> This method automatically recalculates the {@code totalPrice}
     * based on the new unit price and the existing quantity.
     * </p>
     *
     * @param unitPrice The new unit price.
     */
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
    }
    
    /**
     * Gets the calculated total price for this line item.
     * @return The total price.
     */
    public double getTotalPrice() { return totalPrice; }
    
    /**
     * Sets the total price explicitly.
     * <p>
     * generally, this is calculated automatically, but this method allows
     * manual overrides if necessary (e.g., when loading from a database).
     * </p>
     *
     * @param totalPrice The total price to set.
     */
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    /**
     * Returns a string representation of the order item.
     * @return A formatted string containing the product name, quantity, and total price.
     */
    @Override
    public String toString() {
        return "OrderItem{" +
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                '}';
    }
}