package com.group12.greengrocer.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Updated Order model to support Carrier Features:
 * - Neighborhood filtering
 * - JOIN data (Customer Name, Address)
 * - Priority system for UI coloring
 */
public class Order {

    private int id;
    private int userId;

    // ✅ UI & JOIN Fields (Veritabanında JOIN ile çekip buraya dolduracağız)
    private String customerName;       // TableView'da "Müşteri" sütunu için
    private String deliveryNeighborhood; // Filtreleme ve Tablo için
    private String deliveryAddress;      // Kuryenin gideceği tam adres
    private int priorityLevel;           // 1: Normal, 2: High, 3: Urgent (Renk için)

    // ✅ Carrier related
    private Integer carrierId; 
    private Timestamp orderTime; 
    private Timestamp deliveryTime; 
    private Timestamp requestedDeliveryDate; 

    // ✅ Cost-related
    private double subtotal;
    private double vatAmount;
    private double discountAmount;
    private double totalCost;

    // ✅ Status (Suggested: "PENDING", "ASSIGNED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED")
    private String status;
    private String invoice; 
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.status = "PENDING"; // Varsayılanı PENDING yaptık
        this.priorityLevel = 1;  // Varsayılan normal öncelik
    }

    // ---------- NEW Getters / Setters for Carrier Features ----------

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getDeliveryNeighborhood() { return deliveryNeighborhood; }
    public void setDeliveryNeighborhood(String deliveryNeighborhood) { this.deliveryNeighborhood = deliveryNeighborhood; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public int getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }

    // ---------- Existing Getters / Setters ----------

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

    // ---------- Carrier Flow Helpers (Existing logic preserved) ----------

    public void assignCarrier(int carrierId) {
        if (isFinalState()) throw new IllegalStateException("Order in final state.");
        this.carrierId = carrierId;
        if (status == null || "PENDING".equalsIgnoreCase(status)) {
            this.status = "ASSIGNED";
        }
    }

    public void markOutForDelivery(int carrierId) {
        ensureCarrierOwnership(carrierId);
        if (isFinalState()) throw new IllegalStateException("Cannot change status.");
        this.status = "OUT_FOR_DELIVERY";
    }

    public void markDelivered(int carrierId) {
        ensureCarrierOwnership(carrierId);
        if ("CANCELLED".equalsIgnoreCase(status)) throw new IllegalStateException("Order is cancelled.");
        this.status = "DELIVERED";
        this.deliveryTime = new Timestamp(System.currentTimeMillis());
    }

    private boolean isFinalState() {
        return "DELIVERED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status);
    }

    private void ensureCarrierOwnership(int carrierId) {
        if (this.carrierId == null || this.carrierId != carrierId) {
            throw new IllegalStateException("Invalid carrier ownership.");
        }
    }

    @Override
    public String toString() {
        return "Order #" + id + " [" + status + "] - " + deliveryNeighborhood;
    }
}