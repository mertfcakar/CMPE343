package com.group12.greengrocer.controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.group12.greengrocer.database.OrderDAO;
import com.group12.greengrocer.models.Order;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CarrierController {

    @FXML private VBox availableDeliveriesBox, currentDeliveriesBox, completedDeliveriesBox;
    @FXML private ComboBox<String> neighborhoodCombo, completedFilterCombo;
    @FXML private TextField searchField;
    @FXML private Label lblActiveOrders, lblTotalEarnings, lblAvgSpeed, lblUsername, lblCarrierRegion;

    private List<Order> allOrders;

    // VERİTABANI İLE TAM UYUMLU STATÜLER (KÜÇÜK HARF)
    private static final String STATUS_OUT = "assigned";    // DB'deki karşılığı
    private static final String STATUS_DELIVERED = "completed"; // DB'deki karşılığı
    private static final String STATUS_POOL = "pending";      // DB'deki karşılığı

    @FXML
    public void initialize() {
        // [PROFİL VE BÖLGE AYARI]
        if (LoginController.loggedInUser != null) {
            lblUsername.setText("👤 " + LoginController.loggedInUser.getUsername());
            String myNeighborhood = LoginController.loggedInUser.getNeighborhood();
            lblCarrierRegion.setText("Bölge: " + (myNeighborhood != null ? myNeighborhood : "Atanmamış"));
            
            neighborhoodCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                "Tüm İstanbul", "Beşiktaş", "Kadıköy", "Şişli", "Üsküdar", "Fatih", "Maltepe"
            ));
            
            // Login olan kuryenin bölgesini otomatik seçiyoruz
            if (myNeighborhood != null) {
                neighborhoodCombo.setValue(myNeighborhood);
            } else {
                neighborhoodCombo.setValue("Tüm İstanbul");
            }
        }

        neighborhoodCombo.setOnAction(e -> refreshData());

        completedFilterCombo.setItems(javafx.collections.FXCollections.observableArrayList("Son 24 Saat", "Son 30 Gün"));
        completedFilterCombo.setValue("Son 24 Saat");
        completedFilterCombo.setOnAction(e -> updateUI(searchField.getText()));

        searchField.textProperty().addListener((obs, old, val) -> updateUI(val));

        refreshData();
    }

    @FXML
    public void refreshData() {
        if (LoginController.loggedInUser == null) return;

        int myId = LoginController.loggedInUser.getId();
        // Database'den güncel verileri çek (DAO artık küçük harf statülere göre çalışıyor)
        allOrders = OrderDAO.getCarrierDashboardOrders(myId, neighborhoodCombo.getValue());

        updateUI(searchField.getText());
        updateStats();
    }

    private void updateUI(String filterText) {
        availableDeliveriesBox.getChildren().clear();
        currentDeliveriesBox.getChildren().clear();
        completedDeliveriesBox.getChildren().clear();

        if (allOrders == null || LoginController.loggedInUser == null) return;

        int myId = LoginController.loggedInUser.getId();
        LocalDateTime now = LocalDateTime.now();

        for (Order o : allOrders) {
            if (!matchesSearch(o, filterText)) continue;

            VBox card = createOrderCard(o);

            // DURUMA GÖRE KOLONLARA DAĞITIM
            if (isPool(o)) {
                availableDeliveriesBox.getChildren().add(card);
            } else if (isDeliveredMine(o, myId)) {
                if (checkDateFilter(o, now)) {
                    completedDeliveriesBox.getChildren().add(card);
                }
            } else if (isActiveMine(o, myId)) {
                currentDeliveriesBox.getChildren().add(card);
            }
        }
    }

    private VBox createOrderCard(Order o) {
        VBox card = new VBox(8);
        String border = (o.getPriorityLevel() == 3) ? "#ff5252" : "#e0e0e0";
        card.setStyle("-fx-background-color: white; -fx-border-color: " + border +
                "; -fx-border-width: 2; -fx-padding: 12; -fx-background-radius: 10;");

        card.getChildren().addAll(
            new Label("📦 Sipariş #" + o.getId()),
            new Label("👤 " + safe(o.getCustomerName())),
            new Label("📍 " + safe(o.getDeliveryAddress())),
            new Label("💰 Tutar: " + String.format("%.2f", o.getTotalCost()) + " TL")
        );

        if (STATUS_DELIVERED.equals(o.getStatus()) && o.getDeliveryTime() != null) {
            card.getChildren().add(new Label("⏱ Teslim: " + o.getDeliveryTime().toString()));
        }

        // AKSIYON BUTONLARI
        if (isPool(o)) {
            Button pickUpBtn = new Button("Üstüne Al");
            pickUpBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            pickUpBtn.setMaxWidth(Double.MAX_VALUE);
            pickUpBtn.setOnAction(e -> handlePickUpInline(o));
            card.getChildren().add(pickUpBtn);
        } else if (isActiveMine(o, LoginController.loggedInUser.getId())) {
            ComboBox<String> statusAction = new ComboBox<>();
            statusAction.getItems().addAll(
                "Yolda (assigned)",
                "Teslim Edildi (completed)",
                "İptal/Havuza Bırak (pending)"
            );
            statusAction.setPromptText("Durum Güncelle");
            statusAction.setMaxWidth(Double.MAX_VALUE);
            statusAction.setOnAction(e -> handleStatusChange(o, statusAction.getValue()));
            card.getChildren().add(statusAction);
        }

        return card;
    }

    private void handlePickUpInline(Order o) {
        if (LoginController.loggedInUser == null) return;
        if (!showConfirm("Onay", "Siparişi üstlenmek istiyor musunuz?")) return;

        if (OrderDAO.assignAndPickUp(o.getId(), LoginController.loggedInUser.getId())) {
            refreshData(); 
        } else {
            showAlert("Hata", "Sipariş alınamadı.");
        }
    }

    private void handleStatusChange(Order o, String selection) {
        if (selection == null || LoginController.loggedInUser == null) return;
        int myId = LoginController.loggedInUser.getId();

        if (selection.contains("assigned")) return;

        if (selection.contains("completed")) {
            if (!showConfirm("Teslimat", "Sipariş teslim edildi mi?")) return;
            if (OrderDAO.completeOrder(o.getId(), myId)) refreshData();
        } else if (selection.contains("pending")) {
            if (!showConfirm("İptal", "Sipariş havuza bırakılsın mı?")) return;
            if (OrderDAO.releaseOrderToPool(o.getId(), myId)) refreshData();
        }
    }

    private void updateStats() {
        if (LoginController.loggedInUser == null || allOrders == null) return;
        int myId = LoginController.loggedInUser.getId();

        long active = allOrders.stream().filter(o -> isActiveMine(o, myId)).count();
        List<Order> myDone = allOrders.stream().filter(o -> isDeliveredMine(o, myId)).collect(Collectors.toList());
        double earnings = myDone.stream().mapToDouble(Order::getTotalCost).sum();
        
        double avgMin = myDone.stream()
                .filter(o -> o.getOrderTime() != null && o.getDeliveryTime() != null)
                .mapToLong(o -> Duration.between(o.getOrderTime().toLocalDateTime(), o.getDeliveryTime().toLocalDateTime()).toMinutes())
                .average().orElse(0.0);

        lblActiveOrders.setText("Üzerimde: " + active);
        lblTotalEarnings.setText("Kazanç: " + String.format("%.2f", earnings) + " TL");
        lblAvgSpeed.setText("Ort. Hız: " + String.format("%.0f", avgMin) + " dk");
    }

    private boolean checkDateFilter(Order o, LocalDateTime now) {
        if (o.getDeliveryTime() == null) return true;
        LocalDateTime dt = o.getDeliveryTime().toLocalDateTime();
        return completedFilterCombo.getValue().equals("Son 24 Saat") ? 
                dt.isAfter(now.minusHours(24)) : dt.isAfter(now.minusDays(30));
    }

    private boolean matchesSearch(Order o, String filterText) {
        if (filterText == null || filterText.isEmpty()) return true;
        String lower = filterText.toLowerCase();
        return safe(o.getCustomerName()).toLowerCase().contains(lower) || 
                safe(o.getDeliveryAddress()).toLowerCase().contains(lower);
    }

    private boolean isPool(Order o) {
        return STATUS_POOL.equals(o.getStatus()) && (o.getCarrierId() == null || o.getCarrierId() == 0);
    }

    private boolean isActiveMine(Order o, int myId) {
        return o.getCarrierId() != null && o.getCarrierId() == myId && STATUS_OUT.equals(o.getStatus());
    }

    private boolean isDeliveredMine(Order o, int myId) {
        return o.getCarrierId() != null && o.getCarrierId() == myId && STATUS_DELIVERED.equals(o.getStatus());
    }

    private String safe(String s) { return (s == null) ? "" : s; }

    private boolean showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    @FXML
    public void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) lblActiveOrders.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
}