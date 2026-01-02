package com.group12.greengrocer.controllers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import com.group12.greengrocer.database.OrderDAO;
import com.group12.greengrocer.models.Order;
import com.group12.greengrocer.models.User;

import javafx.animation.FadeTransition;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller class for the Carrier (Delivery Driver) Dashboard.
 * <p>
 * This class manages the user interface and logic for the carrier's workflow, including:
 * <ul>
 * <li>Viewing available orders in the pool.</li>
 * <li>Picking up orders for delivery.</li>
 * <li>Marking orders as completed/delivered.</li>
 * <li>Filtering orders by neighborhood and time.</li>
 * <li>Tracking earnings and delivery statistics.</li>
 * <li>Undoing recent actions.</li>
 * </ul>
 */
public class CarrierController {

    // UI Containers for different order statuses
    @FXML
    private VBox availableDeliveriesBox, currentDeliveriesBox, completedDeliveriesBox;
    
    // UI Filters and inputs
    @FXML
    private ComboBox<String> neighborhoodCombo, completedFilterCombo;
    @FXML
    private TextField searchField;
    
    // Statistics and Info Labels
    @FXML
    private Label lblActiveOrders, lblTotalEarnings, lblAvgSpeed, lblUsername, lblCarrierRegion;
    @FXML
    private Button undoButton;
    @FXML
    private Label notificationLabel;

    /** The currently logged-in carrier user. */
    private User currentUser;
    
    /** Local cache of all orders relevant to this carrier context. */
    private List<Order> allOrders;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /** Stack to keep track of actions for the global "Undo" functionality. */
    private Stack<ActionRecord> historyStack = new Stack<>();

    /**
     * Enumeration representing the types of actions a carrier can perform.
     */
    private enum ActionType {
        PICKUP, RELEASE, COMPLETE
    }

    /**
     * Inner class to record details of an action for undo purposes.
     */
    private class ActionRecord {
        ActionType type;
        int orderId;
        String description;

        public ActionRecord(ActionType type, int orderId, String description) {
            this.type = type;
            this.orderId = orderId;
            this.description = description;
        }
    }

    // Constants for order status strings in the database
    private static final String STATUS_OUT = "assigned";
    private static final String STATUS_DELIVERED = "completed";
    private static final String STATUS_POOL = "pending";

    /**
     * Initializes the controller with the logged-in user's data.
     * Sets up the neighborhood filter and welcomes the user.
     *
     * @param user The User object representing the logged-in carrier.
     */
    public void initData(User user) {
        this.currentUser = user;
        lblUsername.setText("👤 " + user.getUsername());

        double rating = OrderDAO.getCarrierAverageRating(user.getId());
        lblCarrierRegion.setText(String.format("Bölge: %s | ⭐ Puan: %.1f", 
            (user.getNeighborhood() != null ? user.getNeighborhood() : "Yok"), rating));

        String myNeighborhood = user.getNeighborhood();
        lblCarrierRegion.setText("Bölge: " + (myNeighborhood != null ? myNeighborhood : "Atanmamış"));

        neighborhoodCombo.setItems(javafx.collections.FXCollections.observableArrayList(
                "Tüm İstanbul", "Beşiktaş", "Kadıköy", "Şişli", "Üsküdar", "Fatih", "Maltepe"));

        if (myNeighborhood != null && neighborhoodCombo.getItems().contains(myNeighborhood)) {
            neighborhoodCombo.setValue(myNeighborhood);
        } else {
            neighborhoodCombo.setValue("Tüm İstanbul");
        }
        refreshData();
    }

    /**
     * Standard JavaFX initialize method.
     * Sets up event listeners for filters and search fields.
     */
    @FXML
    public void initialize() {
        neighborhoodCombo.setOnAction(e -> refreshData());
        completedFilterCombo
                .setItems(javafx.collections.FXCollections.observableArrayList("Son 24 Saat", "Son 30 Gün"));
        completedFilterCombo.setValue("Son 24 Saat");
        completedFilterCombo.setOnAction(e -> updateUI(searchField.getText()));
        searchField.textProperty().addListener((obs, old, val) -> updateUI(val));

        if (undoButton != null)
            undoButton.setDisable(true);
        if (notificationLabel != null)
            notificationLabel.setVisible(false);
    }

    /**
     * Fetches the latest order data from the database and refreshes the UI.
     * Updates statistics and the state of the undo button.
     */
    @FXML
    public void refreshData() {
        if (currentUser == null)
            return;
        try {
            allOrders = OrderDAO.getCarrierDashboardOrders(currentUser.getId(), neighborhoodCombo.getValue());
            updateUI(searchField.getText());
            updateStats();
            updateUndoButtonState();
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Veriler güncellenirken hata oluştu!", false);
        }
    }

    /**
     * Updates the text and disable state of the global Undo button based on the history stack.
     */
    private void updateUndoButtonState() {
        if (undoButton != null) {
            undoButton.setDisable(historyStack.isEmpty());
            undoButton.setText(historyStack.isEmpty() ? "↩ Geri Al" : "↩ Geri Al (" + historyStack.size() + ")");
        }
    }

    /**
     * Handles the global undo action.
     * Reverses the last action (PICKUP, RELEASE, or COMPLETE) performed by the user.
     */
    @FXML
    public void handleGlobalUndo() {
        if (historyStack.isEmpty()) {
            showNotification("Geri alınacak bir işlem bulunmuyor.", false);
            return;
        }

        ActionRecord lastAction = historyStack.peek();
        String message = "Son yapılan işlem: \n" + lastAction.description
                + "\n\nBu işlemi geri almak istediğinizden emin misiniz?";

        if (showConfirm("İşlemi Geri Al Onayı", message)) {
            boolean success = false;
            switch (lastAction.type) {
                case PICKUP:
                    success = OrderDAO.releaseOrderToPool(lastAction.orderId, currentUser.getId());
                    break;
                case RELEASE:
                    success = OrderDAO.assignAndPickUp(lastAction.orderId, currentUser.getId());
                    break;
                case COMPLETE:
                    success = OrderDAO.undoCompleteOrder(lastAction.orderId, currentUser.getId());
                    break;
            }

            if (success) {
                historyStack.pop();
                refreshData();
                showNotification("Son işlem başarıyla geri alındı.", true);
            } else {
                showNotification("İşlem geri alınamadı. Durum değişmiş olabilir.", false);
                historyStack.pop();
                refreshData();
            }
        }
    }

    /**
     * Refreshes the VBoxes for Available, Current, and Completed deliveries.
     * Applies search filtering and date filtering logic.
     *
     * @param filterText The text from the search bar to filter orders by name/address.
     */
    private void updateUI(String filterText) {
        availableDeliveriesBox.getChildren().clear();
        currentDeliveriesBox.getChildren().clear();
        completedDeliveriesBox.getChildren().clear();

        if (allOrders == null)
            return;
        LocalDateTime now = LocalDateTime.now();

        for (Order o : allOrders) {
            if (!matchesSearch(o, filterText))
                continue;
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

    /**
     * Creates a graphical card (VBox) representing a single order.
     * The card includes details like customer name, address, price, and action buttons.
     *
     * @param o The Order object to visualize.
     * @return A VBox containing the order details and controls.
     */
    private VBox createOrderCard(Order o) {
        VBox card = new VBox(8);
        String borderColor = "#e0e0e0";
        if (o.getPriorityLevel() == 2)
            borderColor = "#ffa726";
        if (o.getPriorityLevel() == 3)
            borderColor = "#d32f2f";

        card.setStyle("-fx-background-color: white; -fx-border-color: " + borderColor +
                "; -fx-border-width: 2; -fx-padding: 12; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        Label lblId = new Label("📦 Sipariş #" + o.getId());
        lblId.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        Label lblName = new Label("👤 Müşteri: " + safe(o.getCustomerName()));
        lblName.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

        Label lblAddr = new Label("📍 " + safe(o.getDeliveryNeighborhood()) + "\n" + safe(o.getDeliveryAddress()));
        lblAddr.setWrapText(true);
        lblAddr.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");

        // --- PAYMENT TYPE AND AMOUNT ---
        String paymentText = "";
        String paymentColor = "";
        if ("ONLINE_PAYMENT".equalsIgnoreCase(o.getPaymentMethod())) {
            paymentText = "💳 ÖDENDİ (ONLINE)";
            paymentColor = "#27ae60"; // Green
        } else {
            paymentText = "💵 NAKİT TAHSİL ET";
            paymentColor = "#c0392b"; // Red
        }

        Label lblPrice = new Label("Tutar: " + String.format("%.2f", o.getTotalCost()) + " TL");
        lblPrice.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        Label lblPaymentStatus = new Label(paymentText);
        lblPaymentStatus.setStyle("-fx-text-fill: white; -fx-background-color: " + paymentColor
                + "; -fx-padding: 3 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox priceBox = new HBox(10, lblPrice, lblPaymentStatus);

        // --- EARNINGS INDICATOR ---
        Label lblEarnings = new Label("✨ Kazancın: " + String.format("%.2f", o.getCarrierEarnings()) + " TL");
        lblEarnings.setStyle("-fx-text-fill: #8e44ad; -fx-font-size: 11px; -fx-font-weight: bold;");

        // --- DATE ---
        String reqDateStr = "Belirtilmemiş";
        if (o.getRequestedDeliveryDate() != null) {
            reqDateStr = o.getRequestedDeliveryDate().toLocalDateTime().format(dtf);
        }
        Label lblDate = new Label("📅 İstenen: " + reqDateStr);
        lblDate.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 11px;");

        card.getChildren().addAll(lblId, lblName, lblAddr, priceBox, lblEarnings, lblDate);

        // Add product list to card
        List<String> products = OrderDAO.getOrderItemsAsText(o.getId());
        if (!products.isEmpty()) {
            VBox productsBox = new VBox(2);
            productsBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 5; -fx-background-radius: 5;");
            for (String item : products) {
                Label itemLbl = new Label(item);
                itemLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");
                productsBox.getChildren().add(itemLbl);
            }
            card.getChildren().add(productsBox);
        }

        // Add buttons based on order status
        if (isPool(o)) {
            Button pickUpBtn = new Button("Teslim Al");
            pickUpBtn.setStyle(
                    "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
            pickUpBtn.setMaxWidth(Double.MAX_VALUE);
            pickUpBtn.setOnAction(e -> handlePickUpInline(o));
            card.getChildren().add(pickUpBtn);

        } else if (isActiveMine(o)) {
            HBox actionBox = new HBox(5);
            Button btnComplete = new Button("Teslim Et");
            btnComplete.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-cursor: hand;");
            btnComplete.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(btnComplete, javafx.scene.layout.Priority.ALWAYS);

            Button btnCancel = new Button("İptal");
            btnCancel.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-cursor: hand;");

            btnComplete.setOnAction(e -> handleCompleteOrderWithDate(o));
            btnCancel.setOnAction(e -> handleReleaseOrder(o));

            actionBox.getChildren().addAll(btnComplete, btnCancel);
            card.getChildren().add(actionBox);

        } else if (isDeliveredMine(o)) {
            Label lblDelivered = new Label(
                    "✅ " + (o.getDeliveryTime() != null ? o.getDeliveryTime().toLocalDateTime().format(dtf) : ""));
            lblDelivered.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 11px; -fx-font-weight: bold;");

            Button btnUndoDelivery = new Button("↩ Hatalı - Geri Al");
            btnUndoDelivery.setStyle(
                    "-fx-background-color: #ffb74d; -fx-text-fill: #3e2723; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-radius: 5;");
            btnUndoDelivery.setMaxWidth(Double.MAX_VALUE);
            btnUndoDelivery.setOnAction(e -> handleUndoSpecificOrder(o));

            card.getChildren().addAll(lblDelivered, btnUndoDelivery);
        }

        return card;
    }

    /**
     * Assigns the selected order to the current carrier (Pickup).
     *
     * @param o The order to be picked up.
     */
    private void handlePickUpInline(Order o) {
        try {
            if (OrderDAO.assignAndPickUp(o.getId(), currentUser.getId())) {
                historyStack.push(
                        new ActionRecord(ActionType.PICKUP, o.getId(), "Sipariş #" + o.getId() + " teslim alındı."));
                showNotification("Sipariş #" + o.getId() + " alındı.", true);
                refreshData();
            } else {
                showNotification("Sipariş alınamadı! Başka kurye almış olabilir.", false);
                refreshData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Bağlantı hatası!", false);
        }
    }

    /**
     * Opens a dialog for the carrier to specify the delivery time and marks the order as complete.
     *
     * @param o The order to be completed.
     */
    private void handleCompleteOrderWithDate(Order o) {
        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Teslimat");
        dialog.setHeaderText("Teslimat Zamanı Onayı");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, LocalTime.now().getHour());
        Spinner<Integer> minSpinner = new Spinner<>(0, 59, LocalTime.now().getMinute());

        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(60);
        minSpinner.setEditable(true);
        minSpinner.setPrefWidth(60);

        HBox timeBox = new HBox(5, hourSpinner, new Label(":"), minSpinner);
        VBox content = new VBox(10, new Label("Tarih:"), datePicker, new Label("Saat:"), timeBox);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return LocalDateTime.of(datePicker.getValue(),
                        LocalTime.of(hourSpinner.getValue(), minSpinner.getValue()));
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dt -> {
            if (OrderDAO.completeOrder(o.getId(), currentUser.getId(), dt)) {
                historyStack.push(
                        new ActionRecord(ActionType.COMPLETE, o.getId(), "Sipariş #" + o.getId() + " teslim edildi."));
                showNotification("Sipariş tamamlandı!", true);
                refreshData();
            } else {
                showNotification("Hata oluştu veya sipariş iptal edildi.", false);
                refreshData();
            }
        });
    }

    /**
     * Releases an order back to the pool (Cancel assignment).
     *
     * @param o The order to be released.
     */
    private void handleReleaseOrder(Order o) {
        if (showConfirm("İptal", "Siparişi havuza geri bırakmak istiyor musunuz?")) {
            if (OrderDAO.releaseOrderToPool(o.getId(), currentUser.getId())) {
                historyStack.push(new ActionRecord(ActionType.RELEASE, o.getId(), "Sipariş bırakıldı."));
                refreshData();
                showNotification("Sipariş havuza bırakıldı.", true);
            } else {
                showNotification("İptal edilemedi.", false);
                refreshData();
            }
        }
    }

    /**
     * Undoes the completion of a specific order.
     *
     * @param o The order to revert status for.
     */
    private void handleUndoSpecificOrder(Order o) {
        if (showConfirm("Geri Al", "Sipariş #" + o.getId() + " teslimat durumunu geri almak istiyor musunuz?")) {
            if (OrderDAO.undoCompleteOrder(o.getId(), currentUser.getId())) {
                showNotification("Sipariş geri alındı (Üzerimdeki Paketler'e taşındı).", true);
                refreshData();
            } else {
                showNotification("İşlem başarısız.", false);
            }
        }
    }

    /**
     * Displays a temporary notification on the UI.
     *
     * @param message   The message to display.
     * @param isSuccess True for green (success), false for red (error).
     */
    private void showNotification(String message, boolean isSuccess) {
        if (notificationLabel == null)
            return;
        Platform.runLater(() -> {
            notificationLabel.setText(message);
            notificationLabel.setStyle("-fx-background-color: " + (isSuccess ? "#2e7d32" : "#c62828") +
                    "; -fx-text-fill: white; -fx-padding: 10 15; -fx-background-radius: 5;");
            notificationLabel.setVisible(true);
            notificationLabel.setOpacity(1.0);

            FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.seconds(3), notificationLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(javafx.util.Duration.seconds(2));
            fadeOut.setOnFinished(e -> notificationLabel.setVisible(false));
            fadeOut.play();
        });
    }

    /**
     * Calculates and updates the statistics labels (Active orders, Earnings, Avg Speed).
     */
    private void updateStats() {
        if (currentUser == null || allOrders == null)
            return;
        long active = allOrders.stream().filter(this::isActiveMine).count();
        List<Order> myDone = allOrders.stream().filter(this::isDeliveredMine).collect(Collectors.toList());

        // Calculate earnings based on carrier commission, not total revenue
        double earnings = myDone.stream().mapToDouble(Order::getCarrierEarnings).sum();

        double avgMin = myDone.stream()
                .filter(o -> o.getOrderTime() != null && o.getDeliveryTime() != null)
                .mapToLong(o -> Duration
                        .between(o.getOrderTime().toLocalDateTime(), o.getDeliveryTime().toLocalDateTime()).toMinutes())
                .average().orElse(0.0);

        lblActiveOrders.setText("Üzerimde: " + active);
        lblTotalEarnings.setText("Kazanç: " + String.format("%.2f", earnings) + " TL");
        lblAvgSpeed.setText("Ort. Hız: " + String.format("%.0f", avgMin) + " dk");
    }

    /**
     * Checks if an order belongs to the 'Pool' (Unassigned).
     */
    private boolean isPool(Order o) {
        return STATUS_POOL.equals(o.getStatus()) && (o.getCarrierId() == null || o.getCarrierId() == 0);
    }

    /**
     * Checks if an order is currently assigned to the logged-in carrier.
     */
    private boolean isActiveMine(Order o) {
        return o.getCarrierId() != null && o.getCarrierId() == currentUser.getId() && STATUS_OUT.equals(o.getStatus());
    }

    /**
     * Checks if an order was delivered by the logged-in carrier.
     */
    private boolean isDeliveredMine(Order o) {
        return o.getCarrierId() != null && o.getCarrierId() == currentUser.getId()
                && STATUS_DELIVERED.equals(o.getStatus());
    }

    /**
     * Checks if a completed order matches the selected date filter (24h or 30 days).
     */
    private boolean checkDateFilter(Order o, LocalDateTime now) {
        if (o.getDeliveryTime() == null)
            return true;
        LocalDateTime dt = o.getDeliveryTime().toLocalDateTime();
        return completedFilterCombo.getValue().equals("Son 24 Saat") ? dt.isAfter(now.minusHours(24))
                : dt.isAfter(now.minusDays(30));
    }

    /**
     * Checks if an order matches the current search text (Name, Address, Neighborhood).
     */
    private boolean matchesSearch(Order o, String filterText) {
        if (filterText == null || filterText.isEmpty())
            return true;
        String lower = filterText.toLowerCase(java.util.Locale.forLanguageTag("tr-TR"));
        return safe(o.getCustomerName()).toLowerCase().contains(lower) ||
                safe(o.getDeliveryAddress()).toLowerCase().contains(lower) ||
                safe(o.getDeliveryNeighborhood()).toLowerCase().contains(lower);
    }

    /**
     * Null-safe string utility.
     */
    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    /**
     * Shows a confirmation alert dialog.
     * @return true if the user clicked OK, false otherwise.
     */
    private boolean showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Handles the logout process, returning the user to the login screen.
     */
    @FXML
    public void handleLogout() {
        try {
            Stage stage = (Stage) lblUsername.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            
            // Set window size for login screen
            Scene scene = new Scene(root, 1200, 900);
            
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setWidth(1200);
            stage.setHeight(900);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays a dialog showing the carrier's ratings and reviews.
     */
    @FXML
    private void handleViewRatings() {
        List<OrderDAO.CarrierRating> ratings = OrderDAO.getCarrierRatings(currentUser.getId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("⭐ Değerlendirmelerim");
        dialog.setHeaderText("Aldığınız müşteri değerlendirmeleri:");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        content.setPrefHeight(400);

        if (ratings.isEmpty()) {
            Label noRatingsLabel = new Label("Henüz değerlendirme almadınız.");
            noRatingsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            content.getChildren().add(noRatingsLabel);
        } else {
            // Show average rating
            double avgRating = ratings.stream().mapToInt(r -> r.rating).average().orElse(0.0);
            Label avgLabel = new Label(String.format("📊 Ortalama Puan: %.1f/5.0 (%d değerlendirme)", avgRating, ratings.size()));
            avgLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
            content.getChildren().add(avgLabel);

            // List of ratings
            ScrollPane scrollPane = new ScrollPane();
            VBox ratingsBox = new VBox(8);

            for (OrderDAO.CarrierRating rating : ratings) {
                HBox ratingRow = new HBox(10);
                ratingRow.setPadding(new Insets(10));
                ratingRow.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-border-color: #e0e0e0; -fx-border-radius: 5;");

                // Stars
                String stars = "★".repeat(rating.rating) + "☆".repeat(5 - rating.rating);
                Label starsLabel = new Label(stars);
                starsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ff9800;");

                // Info
                VBox infoBox = new VBox(2);
                Label customerLabel = new Label("Müşteri: " + rating.customerName);
                customerLabel.setStyle("-fx-font-weight: bold;");

                Label orderLabel = new Label("Sipariş #" + rating.orderId);
                orderLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

                Label dateLabel = new Label("Tarih: " + rating.createdAt.format(dtf));
                dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

                infoBox.getChildren().addAll(customerLabel, orderLabel, dateLabel);

                ratingRow.getChildren().addAll(starsLabel, infoBox);
                ratingsBox.getChildren().add(ratingRow);
            }

            scrollPane.setContent(ratingsBox);
            scrollPane.setFitToWidth(true);
            content.getChildren().add(scrollPane);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}