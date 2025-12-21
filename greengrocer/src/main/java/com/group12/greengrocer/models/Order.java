package com.group12.greengrocer.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model representing customer orders
 */
public class Order {
    private int id;
    private int userId;
    private int carrierId;
    private Timestamp orderTime;
    private Timestamp deliveryTime;
    private Timestamp requestedDeliveryDate;
    private double subtotal;
    private double vatAmount;
    private double discountAmount;
    private double totalCost;
    private String status;
    private String invoice;
    private List<OrderItem> items;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getCarrierId() { return carrierId; }
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }
    
    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }
    
    public Timestamp getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(Timestamp deliveryTime) { this.deliveryTime = deliveryTime; }
    
    public Timestamp getRequestedDeliveryDate() { return requestedDeliveryDate; }
    public void setRequestedDeliveryDate(Timestamp requestedDeliveryDate) { 
        this.requestedDeliveryDate = requestedDeliveryDate; 
    }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public double getVatAmount() { return vatAmount; }
    public void setVatAmount(double vatAmount) { this.vatAmount = vatAmount; }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getInvoice() { return invoice; }
    public void setInvoice(String invoice) { this.invoice = invoice; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    public void addItem(OrderItem item) { this.items.add(item); }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", totalCost=" + totalCost +
                '}';
    }
}