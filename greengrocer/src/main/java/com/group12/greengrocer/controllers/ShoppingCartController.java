package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.OrderDAO;
import com.group12.greengrocer.database.SettingsDAO;
import com.group12.greengrocer.models.CartItem;
import com.group12.greengrocer.models.Coupon;
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

public class ShoppingCartController {

    // Artık TableView yok, VBox var
    @FXML
    private VBox cartItemsContainer;

    @FXML
    private Label subtotalLabel;
    @FXML
    private Label vatLabel;
    @FXML
    private Label discountLabel;
    @FXML
    private Label totalLabel;

    @FXML
    private TextField couponField;
    @FXML
    private Label couponMessageLabel;

    @FXML
    private DatePicker deliveryDatePicker;
    @FXML
    private ComboBox<String> deliveryTimeCombo;
    @FXML
    private Label checkoutMessageLabel;

    private double discountAmount = 0.0;
    private final double VAT_RATE = 0.18;

    @FXML
    private void initialize() {
        setupDeliveryOptions();
        renderCartItems(); // Tablo yerine görsel kartları yükle
    }

    // --- YENİ GÖRSEL SEPET MANTIĞI ---
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

    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox(15);
        row.setStyle(
                "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1);");
        row.setAlignment(Pos.CENTER_LEFT);

        // 1. Resim
        ImageView imgView = new ImageView();
        imgView.setFitHeight(60);
        imgView.setFitWidth(60);
        imgView.setPreserveRatio(true);
        if (item.getProduct().getImage() != null) {
            try {
                imgView.setImage(new Image(new ByteArrayInputStream(item.getProduct().getImage())));
            } catch (Exception e) {
            }
        }

        // 2. Ürün Bilgisi
        VBox infoBox = new VBox(5);
        Label nameLbl = new Label(item.getProduct().getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label unitPriceLbl = new Label(String.format("%.2f TL / kg", item.getProduct().getCurrentPrice()));
        unitPriceLbl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(nameLbl, unitPriceLbl);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 3. Miktar Kontrolleri (+ - butonları)
        HBox qtyBox = new HBox(5);
        qtyBox.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.setStyle("-fx-min-width: 30px; -fx-background-color: #eee; -fx-cursor: hand;");

        Label qtyLbl = new Label(String.format("%.1f", item.getQuantity()));
        qtyLbl.setStyle("-fx-min-width: 40px; -fx-alignment: center; -fx-font-weight: bold;");

        Button plusBtn = new Button("+");
        plusBtn.setStyle("-fx-min-width: 30px; -fx-background-color: #eee; -fx-cursor: hand;");

        // Buton Aksiyonları
        minusBtn.setOnAction(e -> {
            if (item.getQuantity() > 0.5) {
                item.addQuantity(-0.5); // CartItem modelinde addQuantity var
                renderCartItems(); // Yeniden çiz
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
                // Stok uyarısı basitçe console veya label (burada sessiz kalıyoruz, UI
                // bozulmasın)
            }
        });

        qtyBox.getChildren().addAll(minusBtn, qtyLbl, plusBtn);

        // 4. Toplam Tutar
        Label totalLbl = new Label(String.format("%.2f TL", item.getTotalPrice()));
        totalLbl.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2e7d32; -fx-min-width: 80px; -fx-alignment: center-right;");

        // 5. Silme Butonu
        Button delBtn = new Button("✕");
        delBtn.setStyle(
                "-fx-text-fill: #999; -fx-background-color: transparent; -fx-font-weight: bold; -fx-cursor: hand;");
        delBtn.setOnAction(e -> {
            ShoppingCart.getInstance().removeItem(item);
            renderCartItems();
        });

        // HBox'a ekle
        row.getChildren().addAll(imgView, infoBox, spacer, qtyBox, totalLbl, delBtn);
        return row;
    }

    private void updateTotals() {
        double subtotal = ShoppingCart.getInstance().calculateSubtotal();
        double vat = subtotal * VAT_RATE;

        // Kuponu tekrar kontrol et (Tutar değiştiyse limit altına düşmüş olabilir)
        if (discountAmount > 0) {
            // Basitlik için burada tekrar hesaplamıyoruz ama gerçekte yapılmalı.
            // Şimdilik sadece matematiksel işlem:
        }

        double total = subtotal + vat - discountAmount;
        if (total < 0)
            total = 0;

        subtotalLabel.setText(String.format("%.2f TL", subtotal));
        vatLabel.setText(String.format("%.2f TL", vat));
        discountLabel.setText(String.format("-%.2f TL", discountAmount));
        totalLabel.setText(String.format("%.2f TL", total));
    }

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

    @FXML
    private void handleClearCart() {
        ShoppingCart.getInstance().clear();
        discountAmount = 0;
        renderCartItems();
    }

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

    @FXML
    private void handleCheckout() {
        if (ShoppingCart.getInstance().getItemCount() == 0) {
            checkoutMessageLabel.setText("Cart is empty!");
            return;
        }

        LocalDate date = deliveryDatePicker.getValue();
        String time = deliveryTimeCombo.getValue();

        if (date == null || time == null) {
            checkoutMessageLabel.setText("Select delivery date/time.");
            return;
        }

        double subtotal = ShoppingCart.getInstance().calculateSubtotal();
        double vat = subtotal * VAT_RATE;
        double total = subtotal + vat - discountAmount;

        boolean success = OrderDAO.createOrder(
                ShoppingCart.getInstance().getCurrentUser(),
                subtotal, vat, discountAmount, total,
                date, time);

        if (success) {
            ShoppingCart.getInstance().clear();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Order Placed!");
            alert.setContentText("Your fresh products will be delivered on " + date + " at " + time);
            alert.showAndWait();

            ((Stage) checkoutMessageLabel.getScene().getWindow()).close();
        } else {
            checkoutMessageLabel.setText("Order failed. Database error.");
        }
    }

    @FXML
    private void handleContinueShopping() {
        ((Stage) checkoutMessageLabel.getScene().getWindow()).close();
    }

}