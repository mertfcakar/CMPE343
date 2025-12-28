package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.ProductDAO;
import com.group12.greengrocer.models.Product;
import com.group12.greengrocer.models.User;
import com.group12.greengrocer.utils.ShoppingCart; // YENİ EKLENDİ

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.ByteArrayInputStream;
import java.util.List;

public class CustomerController {
    
    private User currentUser;
    
    @FXML private Label usernameLabel;
    @FXML private Label cartItemsLabel; // FXML'de sol menüdeki label (varsa)
    @FXML private GridPane vegetablesGrid;
    @FXML private GridPane fruitsGrid;
    
    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Welcome, " + user.getUsername());
        
        // [HATA 1 ÇÖZÜMÜ]: currentUser'ı Sepet yöneticisine gönderiyoruz
        ShoppingCart.getInstance().setCurrentUser(user);
        
        loadProducts();
        updateCartLabel();
    }
    
    // ... initialize methodu aynı kalabilir ...

    private void loadProducts() {
        List<Product> products = ProductDAO.getAllProducts();
        
        int vegCol = 0, vegRow = 0;
        int fruitCol = 0, fruitRow = 0;

        for (Product p : products) {
            VBox productCard = createProductCard(p);

            if ("vegetable".equalsIgnoreCase(p.getType())) {
                vegetablesGrid.add(productCard, vegCol, vegRow);
                vegCol++;
                if (vegCol == 3) { vegCol = 0; vegRow++; }
            } else if ("fruit".equalsIgnoreCase(p.getType())) {
                fruitsGrid.add(productCard, fruitCol, fruitRow);
                fruitCol++;
                if (fruitCol == 3) { fruitCol = 0; fruitRow++; }
            }
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);

        // Resim
        ImageView imgView = new ImageView();
        imgView.setFitHeight(100); imgView.setFitWidth(120); imgView.setPreserveRatio(true);

        if (p.getImage() != null && p.getImage().length > 0) {
            try { imgView.setImage(new Image(new ByteArrayInputStream(p.getImage()))); } catch (Exception e) {}
        } else {
            imgView.setStyle("-fx-background-color: #eee;"); 
        }

        // İsim ve Fiyat
        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        double displayPrice = p.getCurrentPrice(); 
        Label priceLbl = new Label(String.format("%.2f TL / kg", displayPrice));
        if (p.getStock() <= p.getThreshold()) {
             priceLbl.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
             priceLbl.setText(priceLbl.getText() + " (LOW STOCK!)");
        } else {
             priceLbl.setStyle("-fx-text-fill: green;");
        }

        // Miktar
        Spinner<Double> amountSpinner = new Spinner<>(0.5, 20.0, 1.0, 0.5);
        amountSpinner.setEditable(true);
        amountSpinner.setPrefWidth(100);

        // Ekle Butonu
        Button addBtn = new Button("Add to Cart");
        addBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-cursor: hand;");
        
        // [HATA 2 ÇÖZÜMÜ]: TODO silindi, gerçek kod eklendi
        addBtn.setOnAction(e -> {
            double qty = amountSpinner.getValue();
            
            // Stok Kontrolü
            if (qty > p.getStock()) {
                showAlert("Stock Error", "Not enough stock! Available: " + p.getStock() + " kg");
                return;
            }

            // Sepete Ekle
            ShoppingCart.getInstance().addItem(p, qty);
            
            // Kullanıcıya bilgi ver
            updateCartLabel();
            showAlert("Success", p.getName() + " (" + qty + " kg) added to cart.");
        });

        card.getChildren().addAll(imgView, nameLbl, priceLbl, amountSpinner, addBtn);
        return card;
    }

    private void updateCartLabel() {
        if (cartItemsLabel != null) {
            int count = ShoppingCart.getInstance().getItemCount();
            cartItemsLabel.setText("Cart: " + count + " items");
        }
    }

    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shopping_cart.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Shopping Cart");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Stage currentStage = (Stage) usernameLabel.getScene().getWindow();
            currentStage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Diğer boş metodlar...
    @FXML private void handleBrowseProducts() {}
    @FXML private void handleViewOrders() {}
    @FXML private void handleViewDeliveries() {}
    @FXML private void handleSendMessage() {}
    @FXML private void handleEditProfile() {}
    @FXML private void handleSearch() {}
    @FXML private void handleClearSearch() {}
}