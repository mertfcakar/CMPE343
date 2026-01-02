package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.OrderDAO;
import com.group12.greengrocer.database.SettingsDAO;
import com.group12.greengrocer.models.CartItem;
import com.group12.greengrocer.models.Coupon;
import com.group12.greengrocer.models.User;
import com.group12.greengrocer.utils.ShoppingCart;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller class for the Shopping Cart and Checkout screen.
 * <p>
 * This class manages the user's shopping cart interactions, including:
<<<<<<< HEAD
 * </p>
=======
>>>>>>> b229e1c6e1976ed596a3e61a4421e674003c0746
 * <ul>
 * <li>Dynamically rendering cart items using a VBox layout.</li>
 * <li>Adjusting item quantities and removing items.</li>
 * <li>Calculating totals including VAT, coupon discounts, and loyalty rewards.</li>
 * <li>Handling the final checkout process and order creation.</li>
 * </ul>
<<<<<<< HEAD
=======
 * </p>
>>>>>>> b229e1c6e1976ed596a3e61a4421e674003c0746
 *
 * @author Group12
 * @version 1.0
 */
public class ShoppingCartController {

    /** Container for dynamically added cart item rows. Replaces the traditional TableView. */
    @FXML
    private VBox cartItemsContainer;

    @FXML private Label subtotalLabel;
    @FXML private Label vatLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    @FXML private TextField couponField;
    @FXML private Label couponMessageLabel;

    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<String> deliveryTimeCombo;
    @FXML private Label checkoutMessageLabel;

    @FXML private RadioButton rbCreditCard;
    @FXML private RadioButton rbCash;
    private ToggleGroup paymentGroup;

    private double discountAmount = 0.0;
    private final double VAT_RATE = 0.18;

    /**
     * Initializes the controller class.
     * Sets up delivery options, configures payment toggles, and renders the initial state of the cart.
     */
    @FXML
    private void initialize() {
        setupDeliveryOptions();
        setupPaymentOptions(); // Configure payment methods
        renderCartItems(); // Load visual cards instead of a table
    }

    /**
     * Configures the toggle group for payment methods (Credit Card vs Cash).
     * Sets 'Cash' as the default selection.
     */
    private void setupPaymentOptions() {
        paymentGroup = new ToggleGroup();
        rbCreditCard.setToggleGroup(paymentGroup);
        rbCash.setToggleGroup(paymentGroup);
        rbCash.setSelected(true); // Default to Cash
    }

    /**
     * Clears the current view and dynamically renders each item in the shopping cart.
     * If the cart is empty, displays a placeholder message.
     * Triggers a total calculation update after rendering.
     */
    private void renderCartItems() {
        cartItemsContainer.getChildren().clear();
        List<CartItem> items = ShoppingCart.getInstance().getItems();

        if (items.isEmpty()) {
            Label emptyLbl = new Label("Your cart is empty.");
            emptyLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 20;");
            cartItemsContainer.getChildren().add(emptyLbl);
        } else {
            for (CartItem item : items) {
                cartItemsContainer.getChildren().add(createCartItemRow(item));
            }
        }
        updateTotals();
    }

    /**
     * Creates a visual row (HBox) representing a single item in the cart.
     * Includes the product image, name, price, quantity controls (+/-), and a remove button.
     *
     * @param item The CartItem object to visualize.
     * @return An HBox containing the UI controls for the item.
     */
    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox(15);
        row.setStyle(
                "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1);");
        row.setAlignment(Pos.CENTER_LEFT);

        // 1. Image
        ImageView imgView = new ImageView();
        imgView.setFitHeight(60);
        imgView.setFitWidth(60);
        imgView.setPreserveRatio(true);
        if (item.getProduct().getImage() != null) {
            try {
                imgView.setImage(new Image(new ByteArrayInputStream(item.getProduct().getImage())));
            } catch (Exception e) {
                // Ignore image load errors
            }
        }

        // 2. Product Info
        VBox infoBox = new VBox(5);
        Label nameLbl = new Label(item.getProduct().getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label unitPriceLbl = new Label(String.format("%.2f TL / kg", item.getProduct().getCurrentPrice()));
        unitPriceLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(nameLbl, unitPriceLbl);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3. Quantity Controls (+ - buttons)
        HBox qtyBox = new HBox(5);
        qtyBox.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.setStyle("-fx-min-width: 30px; -fx-background-color: #eee; -fx-cursor: hand;");

        Label qtyLbl = new Label(String.format("%.1f", item.getQuantity()));
        qtyLbl.setStyle("-fx-min-width: 40px; -fx-alignment: center; -fx-font-weight: bold;");

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-min-width: 30px; -fx-background-color: #eee; -fx-cursor: hand;");

        // Button Actions
        minusBtn.setOnAction(e -> {
            if (item.getQuantity() > 0.5) {
                item.addQuantity(-0.5); // addQuantity method in CartItem model
                renderCartItems(); // Re-render
            } else {
                ShoppingCart.getInstance().removeItem(item);
                renderCartItems();
            }
        });

        plusBtn.setOnAction(e -> {
            if (item.getProduct().getStock() >= item.getQuantity() + 0.5) {
                item.addQuantity(0.5);
                renderCartItems();
            } else {
                // Stock warning could be added here
            }
        });

        qtyBox.getChildren().addAll(minusBtn, qtyLbl, plusBtn);

        // 4. Total Price for Item
        Label totalLbl = new Label(String.format("%.2f TL", item.getTotalPrice()));
        totalLbl.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2e7d32; -fx-min-width: 80px; -fx-alignment: center-right;");

        // 5. Delete Button
        Button delBtn = new Button("✕");
        delBtn.setStyle(
                "-fx-text-fill: #999; -fx-background-color: transparent; -fx-font-weight: bold; -fx-cursor: hand;");
        delBtn.setOnAction(e -> {
            ShoppingCart.getInstance().removeItem(item);
            renderCartItems();
        });

        // Add to HBox
        row.getChildren().addAll(imgView, infoBox, spacer, qtyBox, totalLbl, delBtn);
        return row;
    }

    /**
     * Recalculates and updates the checkout totals on the UI.
     * Computes Subtotal, VAT (18%), Coupon Discounts, and Loyalty Discounts.
     * <p>
     * <b>Loyalty Logic:</b> Users with 5 or more completed orders receive an automatic 10% discount.
     * </p>
     */
    private void updateTotals() {
        double subtotal = ShoppingCart.getInstance().calculateSubtotal();
        double vat = subtotal * VAT_RATE;

        // Calculate Loyalty Discount
        User user = ShoppingCart.getInstance().getCurrentUser();
        int completedOrders = OrderDAO.getCompletedOrderCount(user.getId());
        double loyaltyDiscount = 0;
        if (completedOrders >= 5) {
            loyaltyDiscount = subtotal * 0.10; // 10%
        }

        double totalDiscount = discountAmount + loyaltyDiscount;
        double total = subtotal + vat - totalDiscount;
        if (total < 0)
            total = 0;

        subtotalLabel.setText(String.format("%.2f TL", subtotal));
        vatLabel.setText(String.format("%.2f TL", vat));
        discountLabel.setText(String.format("-%.2f TL (Coupon: %.2f, Loyalty: %.2f)", totalDiscount, discountAmount, loyaltyDiscount));
        totalLabel.setText(String.format("%.2f TL", total));
    }

    /**
     * Configures the delivery date picker and time combo box.
     * Disables past dates and restricts selection to the next 2 days.
     */
    private void setupDeliveryOptions() {
        deliveryDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()) || date.isAfter(LocalDate.now().plusDays(2)));
            }
        });
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));

        deliveryTimeCombo.getItems().addAll(
                "09:00 - 11:00", "11:00 - 13:00", "13:00 - 15:00",
                "15:00 - 17:00", "17:00 - 19:00", "19:00 - 21:00");
        deliveryTimeCombo.getSelectionModel().select(0);
    }

    /**
     * Clears all items from the shopping cart and resets the UI.
     */
    @FXML
    private void handleClearCart() {
        ShoppingCart.getInstance().clear();
        discountAmount = 0;
        renderCartItems();
    }

    /**
     * Validates and applies a coupon code entered by the user.
     * Checks if the coupon exists, is active, and if the cart meets the minimum purchase amount.
     */
    @FXML
    private void handleApplyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty())
            return;

        List<Coupon> coupons = SettingsDAO.getAllCoupons();
        boolean found = false;

        for (Coupon c : coupons) {
            if (c.getCode().equalsIgnoreCase(code) && c.isActive()) {
                double subtotal = ShoppingCart.getInstance().calculateSubtotal();
                if (subtotal >= c.getMinPurchaseAmount()) {
                    discountAmount = subtotal * (c.getDiscountPercentage() / 100.0);
                    couponMessageLabel.setText("Coupon applied: " + c.getDiscountPercentage() + "% off");
                    couponMessageLabel.setStyle("-fx-text-fill: green;");
                    found = true;
                    updateTotals();
                } else {
                    couponMessageLabel.setText("Min purchase amount: " + c.getMinPurchaseAmount() + " TL");
                    couponMessageLabel.setStyle("-fx-text-fill: red;");
                    found = true;
                }
                break;
            }
        }

        if (!found) {
            couponMessageLabel.setText("Invalid coupon code.");
            couponMessageLabel.setStyle("-fx-text-fill: red;");
            discountAmount = 0;
            updateTotals();
        }
    }

    /**
     * Processes the final checkout.
     * <p>
     * Validations performed:
<<<<<<< HEAD
     * </p>
=======
>>>>>>> b229e1c6e1976ed596a3e61a4421e674003c0746
     * <ul>
     * <li>Cart is not empty.</li>
     * <li>Delivery date and time are selected.</li>
     * <li>Payment method is selected.</li>
     * <li>Total amount meets the minimum cart value (50 TL).</li>
     * </ul>
<<<<<<< HEAD
     * <p>
=======
>>>>>>> b229e1c6e1976ed596a3e61a4421e674003c0746
     * If validation passes, creates the order via {@link OrderDAO} and clears the cart.
     * </p>
     */
    @FXML
    private void handleCheckout() {
        if (ShoppingCart.getInstance().getItemCount() == 0) {
            checkoutMessageLabel.setText("Cart is empty!");
            return;
        }
        if (deliveryDatePicker.getValue() == null || deliveryTimeCombo.getValue() == null) {
            checkoutMessageLabel.setText("Select delivery date/time.");
            return;
        }
        if (paymentGroup.getSelectedToggle() == null) {
            checkoutMessageLabel.setText("Select a payment method.");
            return;
        }

        double subtotal = ShoppingCart.getInstance().calculateSubtotal();
        double vat = subtotal * VAT_RATE;

        // Loyalty Discount: 10% discount for 5+ completed orders
        User user = ShoppingCart.getInstance().getCurrentUser();
        int completedOrders = OrderDAO.getCompletedOrderCount(user.getId());
        double loyaltyDiscount = 0;
        if (completedOrders >= 5) {
            loyaltyDiscount = subtotal * 0.10; // 10% discount
        }

        double total = subtotal + vat - discountAmount - loyaltyDiscount;

        // Minimum cart value check (e.g., 50 TL)
        double MIN_CART_VALUE = 50.0;
        if (total < MIN_CART_VALUE) {
            checkoutMessageLabel.setText("Minimum order value is " + MIN_CART_VALUE + " TL. Current total: " + String.format("%.2f", total) + " TL.");
            return;
        }

        // Get Payment Method
        String paymentMethod = rbCreditCard.isSelected() ? "ONLINE_PAYMENT" : "CASH_ON_DELIVERY";

        boolean success = OrderDAO.createOrder(
                ShoppingCart.getInstance().getCurrentUser(),
                subtotal, vat, discountAmount, total,
                deliveryDatePicker.getValue(), deliveryTimeCombo.getValue(),
                paymentMethod, loyaltyDiscount
        );

        if (success) {
            ShoppingCart.getInstance().clear();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Order Placed!");
            alert.setContentText(
                    "Payment: " + (paymentMethod.equals("ONLINE_PAYMENT") ? "Credit Card" : "Cash on Delivery"));
            alert.showAndWait();
            ((Stage) checkoutMessageLabel.getScene().getWindow()).close();
        } else {
            checkoutMessageLabel.setText("Order failed. Database error.");
        }
    }

    /**
     * Closes the shopping cart window to allow the user to continue browsing.
     */
    @FXML
    private void handleContinueShopping() {
        ((Stage) checkoutMessageLabel.getScene().getWindow()).close();
    }
}