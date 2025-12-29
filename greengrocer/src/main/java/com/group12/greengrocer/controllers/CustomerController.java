package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.MessageDAO;
import com.group12.greengrocer.database.OrderDAO;
import com.group12.greengrocer.database.ProductDAO;
import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.Order;
import com.group12.greengrocer.models.Product;
import com.group12.greengrocer.models.User;
import com.group12.greengrocer.utils.ShoppingCart;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerController {
    
    private User currentUser;
    private List<Product> allProducts;
    
    @FXML private Label usernameLabel;
    @FXML private Label cartItemsLabel;
    
    // YENİ: GridPane yerine FlowPane (Otomatik satır atlama için)
    @FXML private FlowPane vegetablesFlowPane;
    @FXML private FlowPane fruitsFlowPane;
    
    @FXML private TextField searchField;
    
    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Hello, " + user.getUsername());
        
        // Sepeti başlat
        ShoppingCart.getInstance().setCurrentUser(user);
        
        loadProducts();
        updateCartLabel();
    }
    
    private void loadProducts() {
        allProducts = ProductDAO.getAllProducts();
        displayProducts(allProducts);
    }

    private void displayProducts(List<Product> products) {
        if (vegetablesFlowPane != null) vegetablesFlowPane.getChildren().clear();
        if (fruitsFlowPane != null) fruitsFlowPane.getChildren().clear();

        for (Product p : products) {
            VBox productCard = createProductCard(p);

            if ("vegetable".equalsIgnoreCase(p.getType())) {
                if (vegetablesFlowPane != null) vegetablesFlowPane.getChildren().add(productCard);
            } else if ("fruit".equalsIgnoreCase(p.getType())) {
                if (fruitsFlowPane != null) fruitsFlowPane.getChildren().add(productCard);
            }
        }
    }

    // --- PROFESYONEL ÜRÜN KARTI TASARIMI ---
    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #eceff1; -fx-border-width: 1;");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setPrefHeight(280);
        
        // Hafif gölge efekti
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(5);
        shadow.setOffsetY(3);
        card.setEffect(shadow);

        // Resim Alanı
        ImageView imgView = new ImageView();
        imgView.setFitHeight(120); 
        imgView.setFitWidth(160); 
        imgView.setPreserveRatio(true);

        if (p.getImage() != null && p.getImage().length > 0) {
            try { imgView.setImage(new Image(new ByteArrayInputStream(p.getImage()))); } catch (Exception e) {}
        } else {
            // Placeholder (Boşsa gri kutu)
             imgView.setImage(null); 
        }
        
        // Stok Durumu Etiketi (Resmin üzerine gelebilir ama basitlik için alta koyuyoruz)
        Label stockLabel = new Label();
        if (p.getStock() <= 0) {
            stockLabel.setText("OUT OF STOCK");
            stockLabel.setStyle("-fx-text-fill: white; -fx-background-color: #c62828; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px;");
        } else if (p.getStock() <= p.getThreshold()) {
            stockLabel.setText("LOW STOCK");
            stockLabel.setStyle("-fx-text-fill: white; -fx-background-color: #f57c00; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px;");
        } else {
            stockLabel.setText("IN STOCK");
            stockLabel.setStyle("-fx-text-fill: white; -fx-background-color: #4caf50; -fx-padding: 3 8; -fx-background-radius: 3; -fx-font-size: 10px;");
        }

        // İsim
        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #37474f;");
        nameLbl.setWrapText(true);
        
        // Fiyat
        double displayPrice = p.getCurrentPrice(); 
        Label priceLbl = new Label(String.format("%.2f TL / kg", displayPrice));
        priceLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        
        if (p.getStock() <= p.getThreshold() && p.getStock() > 0) {
             priceLbl.setText(priceLbl.getText() + " (x2)");
             priceLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        }

        // Miktar Seçimi ve Buton
        HBox actionBox = new HBox(5);
        actionBox.setAlignment(Pos.CENTER);
        
        Spinner<Double> amountSpinner = new Spinner<>(0.5, 20.0, 1.0, 0.5);
        amountSpinner.setEditable(true);
        amountSpinner.setPrefWidth(70);
        amountSpinner.setStyle("-fx-font-size: 12px;");

        Button addBtn = new Button("Add");
        addBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
        addBtn.setPrefWidth(60);
        
        if (p.getStock() <= 0) {
            addBtn.setDisable(true);
            amountSpinner.setDisable(true);
        }
        
        addBtn.setOnAction(e -> {
            double qty = amountSpinner.getValue();
            if (qty > p.getStock()) {
                showAlert("Stock Warning", "Only " + p.getStock() + " kg available.");
                return;
            }
            ShoppingCart.getInstance().addItem(p, qty);
            updateCartLabel();
            // Basit bir geri bildirim animasyonu (Buton rengi değişir)
            addBtn.setStyle("-fx-background-color: #1b5e20; -fx-text-fill: white;");
            addBtn.setText("OK");
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override public void run() {
                    javafx.application.Platform.runLater(() -> {
                        addBtn.setText("Add");
                        addBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                    });
                }
            }, 1000);
        });

        actionBox.getChildren().addAll(amountSpinner, addBtn);

        // Kart yapısı
        VBox topBox = new VBox(5, stockLabel, imgView, nameLbl, priceLbl);
        topBox.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(topBox, Priority.ALWAYS); // Yukarıyı doldur

        card.getChildren().addAll(topBox, new Separator(), actionBox);
        return card;
    }

    private void updateCartLabel() {
        if (cartItemsLabel != null) {
            int count = ShoppingCart.getInstance().getItemCount();
            cartItemsLabel.setText(count + " items");
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            displayProducts(allProducts);
        } else {
            List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(query))
                .collect(Collectors.toList());
            displayProducts(filtered);
        }
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        displayProducts(allProducts);
    }

    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shopping_cart.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Shopping Cart - " + currentUser.getUsername());
            stage.setScene(new Scene(root));
            stage.show();
            stage.setOnHidden(e -> updateCartLabel());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- POPUP PENCERELER (DIALOGS) ---

    @FXML 
    private void handleViewOrders() {
        Stage stage = new Stage();
        stage.setTitle("My Orders");
        stage.initModality(Modality.APPLICATION_MODAL);

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Order, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        
        TableColumn<Order, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Order, String> itemsCol = new TableColumn<>("Details");
        itemsCol.setCellValueFactory(cell -> {
            List<String> items = OrderDAO.getOrderItemsAsText(cell.getValue().getId());
            return new SimpleStringProperty(items.toString());
        });

        table.getColumns().addAll(idCol, dateCol, totalCol, statusCol, itemsCol);
        table.setItems(FXCollections.observableArrayList(OrderDAO.getOrdersByUserId(currentUser.getId())));

        VBox layout = new VBox(10, table);
        layout.setPadding(new Insets(10));
        stage.setScene(new Scene(layout, 700, 400));
        stage.show();
    }

    @FXML 
    private void handleSendMessage() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Contact Support");
        dialog.setHeaderText("Send message to Owner");

        ButtonType sendBtn = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField subjectField = new TextField(); subjectField.setPromptText("Subject");
        TextArea messageArea = new TextArea(); messageArea.setPromptText("Type your message...");

        grid.add(new Label("Subject:"), 0, 0); grid.add(subjectField, 1, 0);
        grid.add(new Label("Message:"), 0, 1); grid.add(messageArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == sendBtn) {
                int ownerId = UserDAO.getOwnerId();
                if(ownerId == 0) return false;
                return MessageDAO.sendMessage(currentUser.getId(), ownerId, subjectField.getText(), messageArea.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(success -> {
            if(success) showAlert("Success", "Message sent.");
            else showAlert("Error", "Failed to send message.");
        });
    }

    @FXML 
    private void handleEditProfile() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update Info");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField addrField = new TextField(currentUser.getAddress());
        TextField contactField = new TextField(currentUser.getContactDetails());
        PasswordField passField = new PasswordField(); passField.setPromptText("New Password (Optional)");

        grid.add(new Label("Address:"), 0, 0); grid.add(addrField, 1, 0);
        grid.add(new Label("Contact:"), 0, 1); grid.add(contactField, 1, 1);
        grid.add(new Label("Password:"), 0, 2); grid.add(passField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String pass = passField.getText().isEmpty() ? currentUser.getPassword() : passField.getText();
                boolean success = UserDAO.updateUserProfile(currentUser.getId(), addrField.getText(), contactField.getText(), pass);
                if(success) {
                    currentUser.setAddress(addrField.getText());
                    currentUser.setContactDetails(contactField.getText());
                    currentUser.setPassword(pass);
                }
                return success;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(success -> {
            if(success) showAlert("Success", "Profile updated.");
            else showAlert("Error", "Update failed.");
        });
    }
    
    @FXML private void handleViewDeliveries() { handleViewOrders(); }
    @FXML private void handleBrowseProducts() { /* Already Home */ }

    @FXML
    private void handleLogout() {
        try {
            ((Stage) usernameLabel.getScene().getWindow()).close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            new Stage().setScene(new Scene(root));
            ((Stage)root.getScene().getWindow()).show();
        } catch (Exception e) {}
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}