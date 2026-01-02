package com.group12.greengrocer.models;

import java.sql.Timestamp;

/**
 * Represents a discount coupon entity.
 * <p>
 * This model class holds all the necessary information regarding a voucher,
 * including its unique code, discount value, usage constraints (minimum purchase),
 * and validity period.
 * </p>
 */
public class Coupon {

    /**
     * The unique identifier for the coupon in the database.
     */
    private int id;

    /**
     * The alphanumeric code that customers enter to claim the discount (e.g., "SAVE20").
     */
    private String code;

    /**
     * The percentage of the discount (e.g., 10.0 for 10%).
     */
    private double discountPercentage;

    /**
     * The minimum total cart value required to apply this coupon.
     */
    private double minPurchaseAmount;

    /**
     * The expiration date and time of the coupon.
     */
    private Timestamp validUntil;

    /**
     * The current status of the coupon (true if active/usable, false otherwise).
     */
    private boolean isActive;

    /**
     * Constructs a new Coupon object with the specified details.
     *
     * @param id                 The unique ID of the coupon.
     * @param code               The discount code string.
     * @param discountPercentage The percentage value of the discount.
     * @param minPurchaseAmount  The minimum cart amount required.
     * @param validUntil         The expiration timestamp.
     * @param isActive           The initial active status.
     */
    public Coupon(int id, String code, double discountPercentage, double minPurchaseAmount, Timestamp validUntil, boolean isActive) {
        this.id = id;
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.minPurchaseAmount = minPurchaseAmount;
        this.validUntil = validUntil;
        this.isActive = isActive;
    }

    // Getters

    /**
     * Gets the unique identifier of the coupon.
     *
     * @return The coupon ID.
     */
    public int getId() { return id; }

    /**
     * Gets the alphanumeric code used to claim the discount.
     *
     * @return The coupon code.
     */
    public String getCode() { return code; }

    /**
     * Gets the discount percentage offered by this coupon.
     *
     * @return The discount percentage.
     */
    public double getDiscountPercentage() { return discountPercentage; }

    /**
     * Gets the minimum total purchase amount required to use this coupon.
     *
     * @return The minimum purchase amount.
     */
    public double getMinPurchaseAmount() { return minPurchaseAmount; }

    /**
     * Gets the expiration date and time of the coupon.
     *
     * @return The validity timestamp.
     */
    public Timestamp getValidUntil() { return validUntil; }

    /**
     * Checks if the coupon is currently marked as active in the system.
     *
     * @return {@code true} if the coupon is active; {@code false} otherwise.
     */
    public boolean isActive() { return isActive; }

    /**
     * Returns a human-readable string representation of the coupon's status.
     * <p>
     * Useful for displaying status in User Interfaces (tables, labels, etc.).
     * </p>
     *
     * @return "Active" if {@code isActive} is true, otherwise "Expired".
     */
    public String getStatus() { return isActive ? "Active" : "Expired"; }
}