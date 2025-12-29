package com.group12.greengrocer.models;

import java.sql.Timestamp;

public class Coupon {
    private int id;
    private String code;
    private double discountPercentage;
    private double minPurchaseAmount;
    private Timestamp validUntil;
    private boolean isActive;

    public Coupon(int id, String code, double discountPercentage, double minPurchaseAmount, Timestamp validUntil, boolean isActive) {
        this.id = id;
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.minPurchaseAmount = minPurchaseAmount;
        this.validUntil = validUntil;
        this.isActive = isActive;
    }

    // Getters
    public int getId() { return id; }
    public String getCode() { return code; }
    public double getDiscountPercentage() { return discountPercentage; }
    public double getMinPurchaseAmount() { return minPurchaseAmount; }
    public Timestamp getValidUntil() { return validUntil; }
    public boolean isActive() { return isActive; }
    public String getStatus() { return isActive ? "Active" : "Expired"; }
}