package com.group12.greengrocer.controllers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Stack; // UNDO için Stack
import java.util.stream.Collectors;

import com.group12.greengrocer.database.OrderDAO;
import com.group12.greengrocer.models.Order;
import com.group12.greengrocer.models.User;

import javafx.animation.FadeTransition; // Bildirim animasyonu için
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color; // Bildirim rengi için
import javafx.stage.Stage;


public class CarrierController {

    @FXML private VBox availableDeliveriesBox, currentDeliveriesBox, completedDeliveriesBox;
    @FXML private ComboBox<String> neighborhoodCombo, completedFilterCombo;
    @FXML private TextField searchField;
    @FXML private Label lblActiveOrders, lblTotalEarnings, lblAvgSpeed, lblUsername, lblCarrierRegion;
    @FXML private Button undoButton; // FXML'e eklediğimiz Undo butonu
    @FXML private Label notificationLabel; // FXML'e eklediğimiz bildirim etiketi

    private User currentUser;
    private List<Order> allOrders;

    // --- GEÇMİŞ YÖNETİMİ (UNDO SİSTEMİ) ---
    private Stack<ActionRecord> historyStack = new Stack<>();

    private enum ActionType {
        PICKUP,     // Havuzdan sipariş alındı
        RELEASE,    // Sipariş havuza geri bırakıldı
        COMPLETE    // Sipariş teslim edildi olarak işaretlendi
    }

    // Yapılan işlemin kaydını tutan iç sınıf
    private class ActionRecord {
        ActionType type;
        int orderId; // Sadece ID'yi tutmak yeterli
        String description;

        public ActionRecord(ActionType type, int orderId, String description) {
            this.type = type;
            this.orderId = orderId;
            this.description = description;
        }
    }
    // --------------------------------------

    private static final String STATUS_OUT = "assigned";
    private static final String STATUS_DELIVERED = "completed";
    private static final String STATUS_POOL = "pending";

    // --- VERİ ALMA (LoginController'dan) ---
    public void initData(User user) {
        this.currentUser = user;
        lblUsername.setText("👤 " + user.getUsername());
        
        String myNeighborhood = user.getNeighborhood();
        lblCarrierRegion.setText("Bölge: " + (myNeighborhood != null ? myNeighborhood : "Atanmamış"));

        neighborhoodCombo.setItems(javafx.collections.FXCollections.observableArrayList(
            "Tüm İstanbul", "Beşiktaş", "Kadıköy", "Şişli", "Üsküdar", "Fatih", "Maltepe"
        ));

        if (myNeighborhood != null && neighborhoodCombo.getItems().contains(myNeighborhood)) {
            neighborhoodCombo.setValue(myNeighborhood);
        } else {
            neighborhoodCombo.setValue("Tüm İstanbul");
        }
        refreshData();
    }

    @FXML
    public void initialize() {
        neighborhoodCombo.setOnAction(e -> refreshData());
        completedFilterCombo.setItems(javafx.collections.FXCollections.observableArrayList("Son 24 Saat", "Son 30 Gün"));
        completedFilterCombo.setValue("Son 24 Saat");
        completedFilterCombo.setOnAction(e -> updateUI(searchField.getText()));
        searchField.textProperty().addListener((obs, old, val) -> updateUI(val));
        
        // Başlangıçta Undo butonu pasif, bildirim gizli
        if(undoButton != null) undoButton.setDisable(true);
        if(notificationLabel != null) notificationLabel.setVisible(false);
    }

    @FXML
    public void refreshData() {
        if (currentUser == null) return;
        allOrders = OrderDAO.getCarrierDashboardOrders(currentUser.getId(), neighborhoodCombo.getValue());
        updateUI(searchField.getText());
        updateStats();
        updateUndoButtonState();
    }

    private void updateUndoButtonState() {
        if(undoButton != null) {
            undoButton.setDisable(historyStack.isEmpty());
            if (!historyStack.isEmpty()) {
                undoButton.setText("↩ Geri Al (" + historyStack.size() + ")");
            } else {
                undoButton.setText("↩ Geri Al");
            }
        }
    }

    // --- GLOBAL UNDO (GERİ ALMA) METODU ---
    @FXML
    public void handleGlobalUndo() {
        if (historyStack.isEmpty()) {
            showNotification("Geri alınacak bir işlem bulunmuyor.", false);
            return;
        }

        ActionRecord lastAction = historyStack.peek(); // Son işlemi getir ama silme (onay lazım)
        
        String message = "Son yapılan işlem: \n" + lastAction.description + "\n\nBu işlemi geri almak istediğinizden emin misiniz?";
        
        if (showConfirm("İşlemi Geri Al Onayı", message)) {
            boolean success = false;
            
            // İşlem tipine göre TERSİNİ yap
            switch (lastAction.type) {
                case PICKUP: // Almıştık -> Geri Bırak (Release)
                    success = OrderDAO.releaseOrderToPool(lastAction.orderId, currentUser.getId());
                    break;
                    
                case RELEASE: // Bırakmıştık -> Geri Al (Pickup)
                    success = OrderDAO.assignAndPickUp(lastAction.orderId, currentUser.getId());
                    break;
                    
                case COMPLETE: // Tamamlamıştık -> Geri Assigned Yap (Undo Complete)
                    success = OrderDAO.undoCompleteOrder(lastAction.orderId, currentUser.getId());
                    break;
            }

            if (success) {
                historyStack.pop(); // Stack'ten sil
                refreshData();
                showNotification("Son işlem başarıyla geri alındı.", true);
            } else {
                showNotification("İşlem geri alınamadı. Sipariş durumu değişmiş olabilir.", false);
                historyStack.pop(); // Başarısız olsa da stackten çıkar ki döngüye girmesin
                refreshData();
            }
        }
    }
    // --------------------------------------

    private void updateUI(String filterText) {
        availableDeliveriesBox.getChildren().clear();
        currentDeliveriesBox.getChildren().clear();
        completedDeliveriesBox.getChildren().clear();

        if (allOrders == null) return;
        LocalDateTime now = LocalDateTime.now();

        for (Order o : allOrders) {
            if (!matchesSearch(o, filterText)) continue;
            VBox card = createOrderCard(o);

            if (isPool(o)) {
                availableDeliveriesBox.getChildren().add(card);
            } else if (isActiveMine(o)) {
                currentDeliveriesBox.getChildren().add(card);
            } else if (isDeliveredMine(o)) {
                if (checkDateFilter(o, now)) {
                    completedDeliveriesBox.getChildren().add(card);
                }
            }
        }
    }

    // --- KART OLUŞTURMA (GELİŞMİŞ VE DÜZELTİLMİŞ) ---
    private VBox createOrderCard(Order o) {
        VBox card = new VBox(8);
        String borderColor = "#e0e0e0"; 
        if (o.getPriorityLevel() == 2) borderColor = "#ffa726";
        if (o.getPriorityLevel() == 3) borderColor = "#d32f2f";
        
        card.setStyle("-fx-background-color: white; -fx-border-color: " + borderColor +
                "; -fx-border-width: 2; -fx-padding: 12; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        // GÖRÜNMESİ GEREKEN BİLGİLER (Renk kodları ile belirginleştirildi)
        Label lblId = new Label("📦 Sipariş #" + o.getId());
        lblId.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;"); 

        Label lblName = new Label("👤 Müşteri: " + safe(o.getCustomerName()));
        lblName.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;"); 

        Label lblAddr = new Label("📍 " + safe(o.getDeliveryNeighborhood()) + "\n" + safe(o.getDeliveryAddress()));
        lblAddr.setWrapText(true); 
        lblAddr.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;"); 

        Label lblPrice = new Label("💰 Tutar: " + String.format("%.2f", o.getTotalCost()) + " TL (KDV Dahil)");
        lblPrice.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); 
        
        Label lblDate = new Label("📅 İstenen: " + (o.getRequestedDeliveryDate() != null ? o.getRequestedDeliveryDate().toLocalDateTime().toLocalDate().toString() : "Belirtilmemiş"));
        lblDate.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 11px;");

        card.getChildren().addAll(lblId, lblName, lblAddr, lblDate, lblPrice);

        List<String> products = OrderDAO.getOrderItemsAsText(o.getId());
        if (!products.isEmpty()) {
            VBox productsBox = new VBox(2);
            productsBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #dee2e6;");
            productsBox.getChildren().add(new Label("🛒 İçerik:"));
            for (String item : products) {
                Label itemLbl = new Label(item);
                itemLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #495057;");
                productsBox.getChildren().add(itemLbl);
            }
            card.getChildren().add(productsBox);
        }

        if (STATUS_DELIVERED.equals(o.getStatus()) && o.getDeliveryTime() != null) {
            Label lblDelivered = new Label("✅ Teslim Edildi: " + o.getDeliveryTime().toLocalDateTime().toLocalDate().toString() + " " + o.getDeliveryTime().toLocalDateTime().toLocalTime().toString());
            lblDelivered.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            card.getChildren().add(lblDelivered);
        }

        // --- BUTONLAR VE AKSİYONLAR ---
        if (isPool(o)) {
            Button pickUpBtn = new Button("Teslim Al");
            pickUpBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            pickUpBtn.setMaxWidth(Double.MAX_VALUE);
            pickUpBtn.setOnAction(e -> handlePickUpInline(o));
            card.getChildren().add(pickUpBtn);
            
        } else if (isActiveMine(o)) {
            ComboBox<String> statusAction = new ComboBox<>();
            statusAction.getItems().addAll("Teslim Edildi", "İptal Et (Havuza Bırak)");
            statusAction.setPromptText("İşlem Seç...");
            statusAction.setMaxWidth(Double.MAX_VALUE);
            
            statusAction.setOnAction(e -> {
                String sel = statusAction.getValue();
                if (sel == null) return;
                
                if ("Teslim Edildi".equals(sel)) handleCompleteOrderWithDate(o);
                else if ("İptal Et (Havuza Bırak)".equals(sel)) handleReleaseOrder(o);
                
                Platform.runLater(() -> statusAction.getSelectionModel().clearSelection());
            });
            card.getChildren().add(statusAction);
            
        } else if (isDeliveredMine(o)) {
            // Tamamlananlar sütununda UNDO butonu
            // Bu buton artık global undo'dan bağımsız.
            // Global Undo'da zaten bu tipi kontrol ediyoruz.
        }
        
        return card;
    }

    private void handlePickUpInline(Order o) {
        if (showConfirm("Onay", "Siparişi üzerinize almak istiyor musunuz?")) {
            if (OrderDAO.assignAndPickUp(o.getId(), currentUser.getId())) {
                historyStack.push(new ActionRecord(ActionType.PICKUP, o.getId(), "Sipariş #" + o.getId() + " teslim alındı."));
                refreshData();
                showNotification("Sipariş #" + o.getId() + " üzerinize alındı.", true);
            } else {
                showNotification("Sipariş alınamadı. Başkası almış olabilir.", false);
            }
        }
    }

    private void handleCompleteOrderWithDate(Order o) {
        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Teslimat Tamamlama");
        dialog.setHeaderText("Sipariş #" + o.getId() + " için teslim detayları:");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setEditable(false);

        Spinner<Integer> hourSpinner = new Spinner<>();
        hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalTime.now().getHour()));
        hourSpinner.setEditable(true); hourSpinner.setPrefWidth(70);

        Spinner<Integer> minSpinner = new Spinner<>();
        minSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, LocalTime.now().getMinute()));
        minSpinner.setEditable(true); minSpinner.setPrefWidth(70);

        VBox content = new VBox(15);
        VBox dateBox = new VBox(5, new Label("Tarih:"), datePicker);
        HBox timeSpinners = new HBox(10, hourSpinner, new Label(":"), minSpinner);
        timeSpinners.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox timeBox = new VBox(5, new Label("Saat:"), timeSpinners);
        content.getChildren().addAll(dateBox, timeBox);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return LocalDateTime.of(datePicker.getValue(), LocalTime.of(hourSpinner.getValue(), minSpinner.getValue()));
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dt -> {
            if (dt != null && OrderDAO.completeOrder(o.getId(), currentUser.getId(), dt)) {
                historyStack.push(new ActionRecord(ActionType.COMPLETE, o.getId(), "Sipariş #" + o.getId() + " teslim edildi."));
                refreshData();
                showNotification("Sipariş #" + o.getId() + " başarıyla teslim edildi!", true);
            } else {
                showNotification("Sipariş teslim edilirken bir hata oluştu.", false);
            }
        });
    }

    private void handleReleaseOrder(Order o) {
        if (showConfirm("İptal", "Siparişi iptal edip havuza geri bırakmak istiyor musunuz?")) {
            if (OrderDAO.releaseOrderToPool(o.getId(), currentUser.getId())) {
                historyStack.push(new ActionRecord(ActionType.RELEASE, o.getId(), "Sipariş #" + o.getId() + " iptal edilip havuza bırakıldı."));
                refreshData();
                showNotification("Sipariş #" + o.getId() + " havuza geri bırakıldı.", true);
            } else {
                showNotification("Sipariş havuza bırakılamadı.", false);
            }
        }
    }
    
    private void showNotification(String message, boolean isSuccess) {
        if (notificationLabel == null) return;

        Platform.runLater(() -> {
            notificationLabel.setText(message);
            notificationLabel.setStyle(
                "-fx-background-color: " + (isSuccess ? "rgba(46,179,101,0.8)" : "rgba(220,53,69,0.8)") + ";" +
                "-fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5; -fx-font-size: 13px;"
            );
            notificationLabel.setVisible(true);
            notificationLabel.setOpacity(1.0);

            // DÜZELTME BURADA: javafx.util.Duration kullanıyoruz
            FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.seconds(4), notificationLabel); 
            
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> notificationLabel.setVisible(false));
            fadeOut.play();
        });
    }

    private void updateStats() {
        if (currentUser == null || allOrders == null) return;
        long active = allOrders.stream().filter(this::isActiveMine).count();
        List<Order> myDone = allOrders.stream().filter(this::isDeliveredMine).collect(Collectors.toList());
        double earnings = myDone.stream().mapToDouble(Order::getTotalCost).sum();
        double avgMin = myDone.stream()
                .filter(o -> o.getOrderTime() != null && o.getDeliveryTime() != null)
                .mapToLong(o -> Duration.between(o.getOrderTime().toLocalDateTime(), o.getDeliveryTime().toLocalDateTime()).toMinutes())
                .average().orElse(0.0);

        lblActiveOrders.setText("Üzerimde: " + active);
        lblTotalEarnings.setText("Toplam Ciro: " + String.format("%.2f", earnings) + " TL");
        lblAvgSpeed.setText("Ort. Hız: " + String.format("%.0f", avgMin) + " dk");
    }

    private boolean isPool(Order o) { return STATUS_POOL.equals(o.getStatus()) && (o.getCarrierId() == null || o.getCarrierId() == 0); }
    private boolean isActiveMine(Order o) { return o.getCarrierId() != null && o.getCarrierId() == currentUser.getId() && STATUS_OUT.equals(o.getStatus()); }
    private boolean isDeliveredMine(Order o) { return o.getCarrierId() != null && o.getCarrierId() == currentUser.getId() && STATUS_DELIVERED.equals(o.getStatus()); }
    
    private boolean checkDateFilter(Order o, LocalDateTime now) {
        if (o.getDeliveryTime() == null) return true;
        LocalDateTime dt = o.getDeliveryTime().toLocalDateTime();
        return completedFilterCombo.getValue().equals("Son 24 Saat") ? 
                dt.isAfter(now.minusHours(24)) : dt.isAfter(now.minusDays(30));
    }
    
    private boolean matchesSearch(Order o, String filterText) {
        if (filterText == null || filterText.isEmpty()) return true;
        java.util.Locale trLocale = java.util.Locale.forLanguageTag("tr-TR");
        String lower = filterText.toLowerCase(trLocale);
        return safe(o.getCustomerName()).toLowerCase(trLocale).contains(lower) || 
               safe(o.getDeliveryAddress()).toLowerCase(trLocale).contains(lower) ||
               safe(o.getDeliveryNeighborhood()).toLowerCase(trLocale).contains(lower);
    }
    
    private String safe(String s) { return (s == null) ? "" : s; }
    
    private boolean showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title); alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    @FXML public void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) lblActiveOrders.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
}