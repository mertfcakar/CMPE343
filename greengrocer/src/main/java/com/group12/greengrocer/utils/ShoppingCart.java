package com.group12.greengrocer.utils;

import com.group12.greengrocer.models.CartItem;
import com.group12.greengrocer.models.Product;
import com.group12.greengrocer.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Manages the global shopping cart state for the application.
 * <p>
 * This class implements the <b>Singleton Pattern</b> to ensure that only one
 * instance of the shopping cart exists throughout the application lifecycle.
 * It handles the storage of selected items, calculation of totals, and association
 * with the currently logged-in user.
 * </p>
 * <p>
 * It uses JavaFX {@link ObservableList} to allow UI components to bind directly
 * to the cart's contents and update automatically.
 * </p>
 */
public class ShoppingCart {
    
    /**
     * The single static instance of the ShoppingCart.
     */
    private static ShoppingCart instance;

    /**
     * The list of items currently in the cart.
     * Use of ObservableList allows for real-time UI updates in JavaFX.
     */
    private ObservableList<CartItem> items;

    /**
     * The user currently owning this shopping session.
     */
    private User currentUser;

    /**
     * Private constructor to prevent direct instantiation.
     * <p>
     * Initializes the empty observable list for cart items.
     * </p>
     */
    private ShoppingCart() {
        items = FXCollections.observableArrayList();
    }

    /**
     * Retrieves the single global instance of the ShoppingCart.
     * <p>
     * If the instance does not exist, it is created (Lazy Initialization).
     * </p>
     *
     * @return The singleton {@code ShoppingCart} instance.
     */
    public static ShoppingCart getInstance() {
        if (instance == null) instance = new ShoppingCart();
        return instance;
    }

    /**
     * Associates the current shopping session with a specific user.
     *
     * @param user The user who is currently logged in.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Retrieves the user associated with the current shopping session.
     *
     * @return The current {@link User} object, or {@code null} if no user is logged in.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Adds a product to the shopping cart.
     * <p>
     * Logic:
     * <ul>
     *   <li>If the product already exists in the cart, the quantity is updated
     *       (incremented) rather than adding a duplicate row.</li>
     *   <li>If it is a new product, a new {@link CartItem} is created and added to the list.</li>
     * </ul>
     * </p>
     *
     * @param product  The product to add.
     * @param quantity The amount of the product (e.g., kg or units).
     */
    public void addItem(Product product, double quantity) {
        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.addQuantity(quantity);
                return;
            }
        }
        items.add(new CartItem(product, quantity));
    }

    /**
     * Removes a specific item from the shopping cart.
     *
     * @param item The {@link CartItem} object to remove.
     */
    public void removeItem(CartItem item) {
        items.remove(item);
    }

    /**
     * Empties the shopping cart completely.
     * <p>
     * This removes all items from the observable list.
     * </p>
     */
    public void clear() {
        items.clear();
    }

    /**
     * Retrieves the live list of items in the cart.
     * <p>
     * Returns an {@link ObservableList}, which allows JavaFX UI components
     * (like TableView) to automatically listen for changes (additions/removals).
     * </p>
     *
     * @return The observable list of cart items.
     */
    public ObservableList<CartItem> getItems() {
        return items;
    }

    /**
     * Calculates the total cost of all items currently in the cart.
     * <p>
     * This sums the total price of each individual {@code CartItem}.
     * </p>
     *
     * @return The total subtotal amount.
     */
    public double calculateSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
    
    /**
     * Gets the number of unique product entries in the cart.
     * 
     * @return The size of the cart item list.
     */
    public int getItemCount() {
        return items.size();
    }
}