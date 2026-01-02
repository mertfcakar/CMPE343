package com.group12.greengrocer.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order within the GreenGrocer application.
 * <p>
 * This class encapsulates all details regarding the order, including customer information,
 * delivery details (address, neighborhood), financial breakdown (subtotal, VAT, total),
 * payment methods, and carrier assignment status.
 * </p>
 * * @author Group12
 */
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
    private String paymentMethod; // "CASH_ON_DELIVERY" or "ONLINE_PAYMENT"

    /**
     * Default constructor for the Order class.
     * <p>
     * Initializes the {@code items} list as an empty ArrayList, sets the default
     * status to "PENDING", and sets the default priority level to 1.
     * </p>
     */
    public Order() {
        this.items = new ArrayList<>();
        this.status = "PENDING"; 
        this.priorityLevel = 1;  
    }

    /**
     * Calculates the earnings for the carrier based on the order's total cost.
     * <p>
     * <b>Calculation Logic:</b>
     * </p>
     * <ul>
     * <li>Base Fee: 25.00 TL</li>
     * <li>Commission: 5% of the {@code totalCost}</li>
     * </ul>
     *
     * @return The calculated earning amount for the carrier (Base Fee + Commission).
     */
    public double getCarrierEarnings() {
        double baseFee = 25.00; 
        double commission = totalCost * 0.05;
        return baseFee + commission;
    }

    // --- Getters / Setters ---

    /**
     * Gets the payment method used for this order.
     * * @return The payment method (e.g., "CASH_ON_DELIVERY", "ONLINE_PAYMENT").
     */
    public String getPaymentMethod() { return paymentMethod; }

    /**
     * Sets the payment method for this order.
     * * @param paymentMethod The payment method string to set.
     */
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    /**
     * Gets the name of the customer who placed the order.
     * * @return The customer's full name.
     */
    public String getCustomerName() { return customerName; }

    /**
     * Sets the name of the customer.
     * * @param customerName The customer's name.
     */
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    /**
     * Gets the neighborhood where the order should be delivered.
     * * @return The delivery neighborhood name.
     */
    public String getDeliveryNeighborhood() { return deliveryNeighborhood; }

    /**
     * Sets the delivery neighborhood.
     * * @param deliveryNeighborhood The name of the neighborhood.
     */
    public void setDeliveryNeighborhood(String deliveryNeighborhood) { this.deliveryNeighborhood = deliveryNeighborhood; }

    /**
     * Gets the full delivery address.
     * * @return The detailed delivery address string.
     */
    public String getDeliveryAddress() { return deliveryAddress; }

    /**
     * Sets the full delivery address.
     * * @param deliveryAddress The address string to set.
     */
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    /**
     * Gets the priority level of the order.
     * Higher values may indicate higher urgency.
     * * @return The priority level integer.
     */
    public int getPriorityLevel() { return priorityLevel; }

    /**
     * Sets the priority level of the order.
     * * @param priorityLevel The priority level to set.
     */
    public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }

    /**
     * Gets the unique identifier for the order.
     * * @return The order ID.
     */
    public int getId() { return id; }

    /**
     * Sets the unique identifier for the order.
     * * @param id The order ID to set.
     */
    public void setId(int id) { this.id = id; }

    /**
     * Gets the user ID of the customer associated with this order.
     * * @return The user ID.
     */
    public int getUserId() { return userId; }

    /**
     * Sets the user ID for this order.
     * * @param userId The user ID to set.
     */
    public void setUserId(int userId) { this.userId = userId; }

    /**
     * Gets the ID of the carrier assigned to deliver this order.
     * * @return The carrier's ID, or {@code null} if no carrier is assigned yet.
     */
    public Integer getCarrierId() { return carrierId; }

    /**
     * Sets the carrier ID for this order.
     * * @param carrierId The carrier ID to assign.
     */
    public void setCarrierId(Integer carrierId) { this.carrierId = carrierId; }

    /**
     * Gets the timestamp when the order was placed.
     * * @return The order creation timestamp.
     */
    public Timestamp getOrderTime() { return orderTime; }

    /**
     * Sets the timestamp when the order was placed.
     * * @param orderTime The timestamp to set.
     */
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }

    /**
     * Gets the actual timestamp when the order was delivered.
     * * @return The delivery timestamp, or {@code null} if not yet delivered.
     */
    public Timestamp getDeliveryTime() { return deliveryTime; }

    /**
     * Sets the actual delivery timestamp.
     * * @param deliveryTime The timestamp of delivery.
     */
    public void setDeliveryTime(Timestamp deliveryTime) { this.deliveryTime = deliveryTime; }

    /**
     * Gets the requested delivery date/time preferred by the customer.
     * * @return The requested delivery timestamp.
     */
    public Timestamp getRequestedDeliveryDate() { return requestedDeliveryDate; }

    /**
     * Sets the requested delivery date/time.
     * * @param requestedDeliveryDate The requested timestamp to set.
     */
    public void setRequestedDeliveryDate(Timestamp requestedDeliveryDate) {
        this.requestedDeliveryDate = requestedDeliveryDate;
    }

    /**
     * Gets the subtotal cost of items before tax and discounts.
     * * @return The subtotal amount.
     */
    public double getSubtotal() { return subtotal; }

    /**
     * Sets the subtotal cost.
     * * @param subtotal The subtotal amount to set.
     */
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    /**
     * Gets the Value Added Tax (VAT) amount for the order.
     * * @return The VAT amount.
     */
    public double getVatAmount() { return vatAmount; }

    /**
     * Sets the VAT amount.
     * * @param vatAmount The VAT amount to set.
     */
    public void setVatAmount(double vatAmount) { this.vatAmount = vatAmount; }

    /**
     * Gets the discount amount applied to the order.
     * * @return The discount amount.
     */
    public double getDiscountAmount() { return discountAmount; }

    /**
     * Sets the discount amount.
     * * @param discountAmount The discount amount to set.
     */
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    /**
     * Gets the final total cost of the order (Subtotal + VAT - Discount).
     * * @return The total cost.
     */
    public double getTotalCost() { return totalCost; }

    /**
     * Sets the final total cost of the order.
     * * @param totalCost The total cost to set.
     */
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    /**
     * Gets the current status of the order.
     * Common statuses: PENDING, PREPARING, ON_WAY, DELIVERED, CANCELLED.
     * * @return The status string.
     */
    public String getStatus() { return status; }

    /**
     * Sets the status of the order.
     * * @param status The status string to set.
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Gets the invoice identifier or details associated with the order.
     * * @return The invoice string.
     */
    public String getInvoice() { return invoice; }

    /**
     * Sets the invoice details for the order.
     * * @param invoice The invoice string to set.
     */
    public void setInvoice(String invoice) { this.invoice = invoice; }

    /**
     * Gets the list of items contained in this order.
     * * @return A list of {@link OrderItem} objects.
     */
    public List<OrderItem> getItems() { return items; }

    /**
     * Sets the list of items for this order.
     * <p>
     * If the provided list is {@code null}, an empty ArrayList is initialized
     * to prevent NullPointerExceptions.
     * </p>
     * * @param items The list of OrderItems to set.
     */
    public void setItems(List<OrderItem> items) {
        this.items = (items == null) ? new ArrayList<>() : items;
    }

    /**
     * Returns a string representation of the order.
     * Includes ID, status, and delivery neighborhood.
     * * @return A string summary of the order.
     */
    @Override
    public String toString() {
        return "Order #" + id + " [" + status + "] - " + deliveryNeighborhood;
    }
}