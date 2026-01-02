package com.group12.greengrocer.models;

/**
 * Represents an individual entry in the shopping cart.
 * <p>
 * This class associates a specific {@link Product} with a selected quantity.
 * It also provides functionality to calculate the total price for this specific
 * line item based on the product's current unit price.
 * </p>
 */
public class CartItem {

    /**
     * The product associated with this cart item.
     */
    private Product product;

    /**
     * The quantity of the product (e.g., number of items or weight in kg).
     */
    private double quantity;

    /**
     * Constructs a new {@code CartItem} with the specified product and initial quantity.
     *
     * @param product  The product to be added to the cart.
     * @param quantity The amount of the product (e.g., 1.5 for kg or 2 for units).
     */
    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    /**
     * Retrieves the product object associated with this item.
     *
     * @return The {@link Product} entity.
     */
    public Product getProduct() { return product; }

    /**
     * Retrieves the current quantity of the product in the cart.
     *
     * @return The quantity as a double.
     */
    public double getQuantity() { return quantity; }
    
    /**
     * Increments the quantity of the product in this cart item.
     * <p>
     * This is typically used when the user adds the same product to the cart
     * multiple times, rather than creating a duplicate line item.
     * </p>
     *
     * @param qty The amount to add to the existing quantity.
     */
    public void addQuantity(double qty) {
        this.quantity += qty;
    }

    /**
     * Calculates the total cost for this line item.
     * <p>
     * The calculation relies on {@link Product#getCurrentPrice()}, which determines
     * the unit price based on specific logic (e.g., stock thresholds or promotions),
     * multiplied by the quantity.
     * </p>
     *
     * @return The total price for this item (Unit Price * Quantity).
     */
    public double getTotalPrice() {
        return product.getCurrentPrice() * quantity;
    }
}