package com.group12.greengrocer.utils;

import com.group12.greengrocer.models.CartItem;
import com.group12.greengrocer.models.Product;
import com.group12.greengrocer.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ShoppingCart {
    
    private static ShoppingCart instance;
    private ObservableList<CartItem> items;
    private User currentUser;

    private ShoppingCart() {
        items = FXCollections.observableArrayList();
    }

    public static ShoppingCart getInstance() {
        if (instance == null) instance = new ShoppingCart();
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void addItem(Product product, double quantity) {
        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.addQuantity(quantity);
                return;
            }
        }
        items.add(new CartItem(product, quantity));
    }

    public void removeItem(CartItem item) {
        items.remove(item);
    }

    public void clear() {
        items.clear();
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public double calculateSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
    
    public int getItemCount() {
        return items.size();
    }
}