package com.group12.greengrocer.models;

public class CartItem {
    private Product product;
    private double quantity;

    public CartItem(Product product, double quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public double getQuantity() { return quantity; }
    
    public void addQuantity(double qty) {
        this.quantity += qty;
    }

    // Ürünün o anki fiyatı (Threshold durumuna göre) * miktar
    public double getTotalPrice() {
        return product.getCurrentPrice() * quantity;
    }
}