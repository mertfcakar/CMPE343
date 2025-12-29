package com.group12.greengrocer.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int id;
    private int userId;

    // Carrier UI Fields
    private String customerName;       
    private String deliveryNeighborhood; 
    private String deliveryAddress;      
    private int priorityLevel;           

    private Integer carrierId; 
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
    
    // --- YENİ EKLENEN ALAN ---
    private String paymentMethod; // "CASH_ON_DELIVERY" veya "ONLINE_PAYMENT"

    public Order() {
        this.items = new ArrayList<>();
        this.status = "PENDING"; 
        this.priorityLevel = 1;  
    }

    // --- Kurye Kazancı Hesaplama Mantığı ---
    // Formül: Sabit 25 TL + Sipariş Tutarının %5'i
    public double getCarrierEarnings() {
        double baseFee = 25.00; 
        double commission = totalCost * 0.05;
        return baseFee + commission;
    }

    // --- Getters / Setters ---

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getDeliveryNeighborhood() { return deliveryNeighborhood; }
    public void setDeliveryNeighborhood(String deliveryNeighborhood) { this.deliveryNeighborhood = deliveryNeighborhood; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public int getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getCarrierId() { return carrierId; }
    public void setCarrierId(Integer carrierId) { this.carrierId = carrierId; }

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
    public void setItems(List<OrderItem> items) {
        this.items = (items == null) ? new ArrayList<>() : items;
    }

    @Override
    public String toString() {
        return "Order #" + id + " [" + status + "] - " + deliveryNeighborhood;
    }
}