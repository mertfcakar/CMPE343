package com.group12.greengrocer.controllers;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.group12.greengrocer.database.MessageDAO;
import com.group12.greengrocer.database.OrderDAO;
import com.group12.greengrocer.database.ProductDAO;
import com.group12.greengrocer.database.SettingsDAO;
import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.Coupon;
import com.group12.greengrocer.models.Message;
import com.group12.greengrocer.models.Order;
import com.group12.greengrocer.models.Product;
import com.group12.greengrocer.models.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class OwnerController {

    private User currentUser;

    @FXML
    private Label usernameLabel;
    @FXML
    private Label statusLabel;

    // --- DASHBOARD ---
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label activeOrdersLabel;
    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label activeCarriersLabel;
    @FXML
    private TableView<Order> recentOrdersTable;

    // --- PRODUCTS ---
    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TextField productSearchField;

    // --- ORDERS ---
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private ComboBox<String> orderStatusFilter;
    @FXML
    private Label ordersCountLabel;

    // --- CARRIERS ---
    @FXML
    private TableView<User> carriersTable;

    // --- MESSAGES ---
    @FXML
    private ListView<Message> messagesListView;
    @FXML
    private Label messageFromLabel;
    @FXML
    private Label messageSubjectLabel;
    @FXML
    private Label messageDateLabel;
    @FXML
    private TextArea messageContentArea;

    // --- SETTINGS (COUPONS & LOYALTY) ---
    @FXML
    private TableView<Coupon> couponsTable;
    @FXML
    private TextField minOrdersField;
    @FXML
    private TextField loyaltyDiscountField;
    @FXML
    private TextField minCartValueField;
    @FXML
    private TextField vatRateField;

    // --- REPORTS ---
    @FXML
    private ComboBox<String> reportTypeCombo;
    @FXML
    private VBox reportContentBox;

    private ObservableList<Product> masterProductList = FXCollections.observableArrayList();
    private ObservableList<Order> masterOrderList = FXCollections.observableArrayList();

    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Owner: " + user.getUsername());
        refreshAllData();
    }

    @FXML
    private void initialize() {
        setupProductTable();
        setupOrderTable();
        setupCarrierTable();
        setupCouponTable(); // Kupon tablosunu kur

        // Sipariş Filtreleri
        if (orderStatusFilter != null) {
            orderStatusFilter.getItems().addAll("All", "Pending", "Assigned", "Completed", "Cancelled");
            orderStatusFilter.getSelectionModel().selectFirst();
            orderStatusFilter.setOnAction(e -> filterOrders());
        }

        // Rapor Tipleri
        if (reportTypeCombo != null) {
            reportTypeCombo.getItems().addAll("Product Revenue", "Carrier Performance");
            reportTypeCombo.getSelectionModel().selectFirst();
        }

        // Mesaj Listesi Seçim Listener
        if (messagesListView != null) {
            messagesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null)
                    displayMessageDetails(newVal);
            });
        }
    }

    private void refreshAllData() {
        loadDashboardStats();
        loadProducts();
        loadOrders();
        loadCarriers();
        loadMessages();
        loadCoupons();
        loadSettings();
        statusLabel.setText("All data refreshed at " + LocalDateTime.now().toString().substring(11, 19));
    }

    // --- DASHBOARD ---
    private void loadDashboardStats() {
        int prodCount = ProductDAO.getAllProducts().size();
        int activeOrders = OrderDAO.getActiveOrderCount();
        double revenue = OrderDAO.getTotalRevenue();
        int carrierCount = UserDAO.getAllCarriers().size();

        if (totalProductsLabel != null)
            totalProductsLabel.setText(String.valueOf(prodCount));
        if (activeOrdersLabel != null)
            activeOrdersLabel.setText(String.valueOf(activeOrders));
        if (totalRevenueLabel != null)
            totalRevenueLabel.setText(String.format("₺%.2f", revenue));
        if (activeCarriersLabel != null)
            activeCarriersLabel.setText(String.valueOf(carrierCount));

        List<Order> allOrders = OrderDAO.getAllOrdersForAdmin();
        ObservableList<Order> recent = FXCollections.observableArrayList();
        if (allOrders.size() > 5)
            recent.addAll(allOrders.subList(0, 5));
        else
            recent.addAll(allOrders);

        if (recentOrdersTable != null) {
            setupRecentOrdersTable();
            recentOrdersTable.setItems(recent);
        }
    }

    // --- PRODUCTS ---
    private void setupProductTable() {
        if (productsTable != null && !productsTable.getColumns().isEmpty()) {
            productsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
            productsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
            productsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("type"));
            productsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("price"));
            productsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("stock"));
            productsTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("threshold"));
        }
    }

    private void loadProducts() {
        masterProductList.setAll(ProductDAO.getAllProducts());
        if (productsTable != null)
            productsTable.setItems(masterProductList);
    }

    @FXML
    private void handleSearchProducts() {
        String filter = productSearchField.getText().toLowerCase();
        if (filter.isEmpty()) {
            productsTable.setItems(masterProductList);
        } else {
            FilteredList<Product> filtered = new FilteredList<>(masterProductList,
                    p -> p.getName().toLowerCase().contains(filter) ||
                            p.getType().toLowerCase().contains(filter));
            productsTable.setItems(filtered);
        }
    }

    @FXML
    private void handleAddProduct() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter product details");

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("vegetable", "fruit");
        typeCombo.setValue("vegetable");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField stockField = new TextField();
        stockField.setPromptText("Stock");
        TextField thresholdField = new TextField();
        thresholdField.setPromptText("Threshold");

        Button imgBtn = new Button("Select Image");
        Label imgLabel = new Label("No file selected");
        final File[] selectedFile = { null };

        imgBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"));
            File f = fc.showOpenDialog(dialog.getOwner());
            if (f != null) {
                selectedFile[0] = f;
                imgLabel.setText(f.getName());
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Threshold:"), 0, 4);
        grid.add(thresholdField, 1, 4);
        grid.add(new Label("Image:"), 0, 5);
        grid.add(imgBtn, 1, 5);
        grid.add(imgLabel, 2, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                try {
                    String name = nameField.getText();
                    String type = typeCombo.getValue();

                    double price = Double.parseDouble(priceField.getText());
                    double stock = Double.parseDouble(stockField.getText());
                    double threshold = Double.parseDouble(thresholdField.getText());

                    if (name == null || name.trim().isEmpty()) {
                        showAlert("Error", "Product name cannot be empty.");
                        return false;
                    }

                    if (price <= 0) {
                        showAlert("Error", "Price must be greater than 0.");
                        return false;
                    }

                    if (stock < 0) {
                        showAlert("Error", "Stock cannot be negative.");
                        return false;
                    }

                    if (threshold <= 0) {
                        showAlert("Error", "Threshold must be greater than 0.");
                        return false;
                    }

                    if (type == null) {
                        showAlert("Error", "Please select a product type.");
                        return false;
                    }

                    if (ProductDAO.productExists(name, type)) {
                        showAlert("Error", "This product already exists.");
                        return false;
                    }

                    return ProductDAO.addProduct(
                            name,
                            type,
                            price,
                            stock,
                            threshold,
                            selectedFile[0]);

                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter valid numeric values.");
                    return false;
                } catch (Exception e) {
                    showAlert("Error", "Unexpected error: " + e.getMessage());
                    return false;
                }
            }
            return null;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            loadProducts();
            loadDashboardStats();
            showAlert("Success", "Product added successfully.");
        }
    }

    @FXML
    private void handleRemoveProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a product to delete.");
            return;
        }
        if (ProductDAO.deleteProduct(selected.getId())) {
            loadProducts();
            loadDashboardStats();
            showAlert("Success", "Product deleted.");
        } else {
            showAlert("Error", "Could not delete product.");
        }
    }

    @FXML
    private void handleUpdateProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getPrice()));
        dialog.setTitle("Update Price");
        dialog.setHeaderText("Update price for " + selected.getName());
        dialog.setContentText("New Price:");

        dialog.showAndWait().ifPresent(priceStr -> {
            try {
                double newPrice = Double.parseDouble(priceStr);
                ProductDAO.updateProductStockAndPrice(selected.getId(), newPrice, selected.getStock());
                loadProducts();
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid price format.");
            }
        });
    }

    // --- ORDERS ---
    private void setupOrderTable() {
        if (ordersTable != null && !ordersTable.getColumns().isEmpty()) {
            ordersTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
            ordersTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("customerName"));
            ordersTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("carrierId"));
            ordersTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("deliveryNeighborhood"));
            ordersTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("totalCost"));
            ordersTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("status"));
            ordersTable.getColumns().get(6).setCellValueFactory(new PropertyValueFactory<>("orderTime"));
            ordersTable.getColumns().get(7).setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));
        }
    }

    private void setupRecentOrdersTable() {
        if (recentOrdersTable != null && !recentOrdersTable.getColumns().isEmpty()) {
            recentOrdersTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
            recentOrdersTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("customerName"));
            recentOrdersTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("totalCost"));
            recentOrdersTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("status"));
            recentOrdersTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        }
    }

    private void loadOrders() {
        masterOrderList.setAll(OrderDAO.getAllOrdersForAdmin());
        filterOrders();
    }

    private void filterOrders() {
        if (ordersTable == null)
            return;
        String status = orderStatusFilter.getValue();
        if (status == null || status.equals("All")) {
            ordersTable.setItems(masterOrderList);
            if (ordersCountLabel != null)
                ordersCountLabel.setText("Total: " + masterOrderList.size());
        } else {
            FilteredList<Order> filtered = new FilteredList<>(masterOrderList,
                    o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase(status));
            ordersTable.setItems(filtered);
            if (ordersCountLabel != null)
                ordersCountLabel.setText("Total: " + filtered.size());
        }
    }

    @FXML
    private void handleRefreshOrders() {
        loadOrders();
        loadDashboardStats();
    }

    // --- CARRIERS ---
    private void setupCarrierTable() {
        if (carriersTable != null && !carriersTable.getColumns().isEmpty()) {
            carriersTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
            carriersTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("username"));
            carriersTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("contactDetails"));
            carriersTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("role"));
        }
    }

    private void loadCarriers() {
        ObservableList<User> carriers = FXCollections.observableArrayList(UserDAO.getAllCarriers());
        if (carriersTable != null)
            carriersTable.setItems(carriers);
    }

    @FXML
    private void handleHireCarrier() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Hire New Carrier");
        dialog.setHeaderText("Enter carrier credentials");

        ButtonType hireBtn = new ButtonType("Hire", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(hireBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField userField = new TextField();
        userField.setPromptText("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        // DEĞİŞİKLİK BURADA BAŞLIYOR: Tek contact yerine iki alan
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(userField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == hireBtn) {
                // UserDAO.addCarrier artık 4 parametre alıyor
                return UserDAO.addCarrier(
                        userField.getText(),
                        passField.getText(),
                        emailField.getText(),
                        phoneField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                loadCarriers();
                loadDashboardStats();
                showAlert("Success", "Carrier hired successfully!");
            } else {
                showAlert("Error", "Could not hire carrier. Username might be taken.");
            }
        });
    }

    @FXML
    private void handleFireCarrier() {
        User selected = carriersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a carrier to fire.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to fire " + selected.getUsername() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {

            if (OrderDAO.hasActiveOrders(selected.getId())) {
                showAlert("Error", "This carrier has active deliveries and cannot be removed.");
                return;
            }

            if (UserDAO.deleteUser(selected.getId())) {
                loadCarriers();
                loadDashboardStats();
                showAlert("Success", "Carrier fired.");
            } else {
                showAlert("Error", "Operation failed.");
            }
        }
    }

    // --- CARRIER RATINGS (NEW) ---
    @FXML
    private void handleViewCarrierRatings() {
        User selected = carriersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a carrier to view ratings.");
            return;
        }

        Map<String, Integer> performance = OrderDAO.getCarrierPerformanceReport();
        int completedDeliveries = performance.getOrDefault(selected.getUsername(), 0);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Carrier Performance");
        alert.setHeaderText("Performance for: " + selected.getUsername());
        alert.setContentText("Total Completed Deliveries: " + completedDeliveries
                + "\n\n(Customer rating system integration coming in v2.0)");
        alert.showAndWait();
    }

    // --- MESSAGES ---
    private void loadMessages() {
        if (messagesListView != null) {
            messagesListView.setItems(FXCollections.observableArrayList(MessageDAO.getAllMessages()));
        }
    }

    private void displayMessageDetails(Message msg) {
        if (messageFromLabel != null)
            messageFromLabel.setText(msg.getSenderName());
        if (messageSubjectLabel != null)
            messageSubjectLabel.setText(msg.getSubject());
        if (messageDateLabel != null)
            messageDateLabel.setText(msg.getCreatedAt().toString());
        if (messageContentArea != null)
            messageContentArea.setText(msg.getContent());
    }

    @FXML
    private void handleRefreshMessages() {
        loadMessages();
    }

    @FXML
    private void handleReplyMessage() {
        Message selected = messagesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Lütfen cevaplanacak bir mesaj seçin.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mesajı Cevapla");
        dialog.setHeaderText("Kime: " + selected.getSenderName());
        dialog.setContentText("Cevabınız:");

        dialog.showAndWait().ifPresent(replyText -> {
            if (replyText.trim().isEmpty()) return;
            
            boolean sent = MessageDAO.sendMessage(
                currentUser.getId(), 
                selected.getSenderId(), 
                selected.getSubject(), // "RE:" eklemeden, aynı konu ile gönder
                replyText
            );

            if (sent) {
                showAlert("Success", "Cevap gönderildi.");
                // Cevap gönderilince ticket durumu güncellenebilir (İsteğe bağlı)
                MessageDAO.updateTicketStatus(selected.getSubject(), "RESOLVED"); 
            } else {
                showAlert("Error", "Mesaj gönderilemedi.");
            }
        });
    }

    @FXML
    private void handleDeleteMessage() {
        Message selected = messagesListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        if (MessageDAO.deleteMessage(selected.getId())) {
            loadMessages();
            messageContentArea.clear();
            messageFromLabel.setText("-");
            messageSubjectLabel.setText("-");
            messageDateLabel.setText("-");
        }
    }

    // --- SETTINGS (COUPONS & LOYALTY) ---
    private void setupCouponTable() {
        if (couponsTable != null && !couponsTable.getColumns().isEmpty()) {
            couponsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
            couponsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("discountPercentage"));
            couponsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("minPurchaseAmount"));
            couponsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("validUntil"));
            couponsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("status"));
        }
    }

    private void loadCoupons() {
        if (couponsTable != null) {
            List<Coupon> coupons = SettingsDAO.getAllCoupons();
            couponsTable.setItems(FXCollections.observableArrayList(coupons));
        }
    }

    private void loadSettings() {
        int[] loyalty = SettingsDAO.getLoyaltySettings();
        if (minOrdersField != null)
            minOrdersField.setText(String.valueOf(loyalty[0]));
        if (loyaltyDiscountField != null)
            loyaltyDiscountField.setText(String.valueOf(loyalty[1]));
    }

    @FXML
    private void handleAddCoupon() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Add Coupon");
        dialog.setHeaderText("Create a new discount coupon");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.setPromptText("Code (e.g. SUMMER25)");
        TextField discField = new TextField();
        discField.setPromptText("Discount %");
        TextField minField = new TextField();
        minField.setPromptText("Min Purchase (TL)");
        DatePicker datePicker = new DatePicker();

        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Discount (%):"), 0, 1);
        grid.add(discField, 1, 1);
        grid.add(new Label("Min Purchase:"), 0, 2);
        grid.add(minField, 1, 2);
        grid.add(new Label("Valid Until:"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    return SettingsDAO.addCoupon(
                            codeField.getText(),
                            Double.parseDouble(discField.getText()),
                            Double.parseDouble(minField.getText()),
                            datePicker.getValue());
                } catch (Exception e) {
                    return false;
                }
            }
            return null;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            loadCoupons();
            showAlert("Success", "Coupon added.");
        } else if (result.isPresent() && !result.get()) {
            showAlert("Error", "Invalid input or code already exists.");
        }
    }

    @FXML
    private void handleSaveLoyaltySettings() {
        try {
            int minOrders = Integer.parseInt(minOrdersField.getText());
            double discount = Double.parseDouble(loyaltyDiscountField.getText());
            SettingsDAO.updateLoyaltySettings(minOrders, discount);
            showAlert("Success", "Loyalty settings updated.");
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers.");
        }
    }

    // --- REPORTS ---
    @FXML
    private void handleGenerateReport() {
        String type = reportTypeCombo.getValue();
        if (type == null)
            return;

        reportContentBox.getChildren().clear();

        TableView<ReportItem> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ReportItem, String> keyCol = new TableColumn<>("Category");
        keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<ReportItem, String> valCol = new TableColumn<>("Value");
        valCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        table.getColumns().addAll(keyCol, valCol);

        ObservableList<ReportItem> data = FXCollections.observableArrayList();

        if (type.equals("Product Revenue")) {
            keyCol.setText("Product Name");
            valCol.setText("Total Revenue (TL)");
            Map<String, Double> map = OrderDAO.getRevenueByProductReport();
            map.forEach((k, v) -> data.add(new ReportItem(k, String.format("%.2f TL", v))));

        } else if (type.equals("Carrier Performance")) {
            keyCol.setText("Carrier Username");
            valCol.setText("Completed Deliveries");
            Map<String, Integer> map = OrderDAO.getCarrierPerformanceReport();
            map.forEach((k, v) -> data.add(new ReportItem(k, String.valueOf(v))));
        }

        table.setItems(data);
        table.setPrefHeight(400);
        reportContentBox.getChildren().add(table);
    }

    @FXML
    private void handleExportReport() {
        if (reportTypeCombo.getValue() == null) {
            showAlert("Warning", "Please generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName("Report_" + System.currentTimeMillis() + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveReportToFile(file);
        }
    }

    private void saveReportToFile(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("GREEN GROCER - MANAGEMENT REPORT");
            writer.println("--------------------------------");
            writer.println("Report Type: " + reportTypeCombo.getValue());
            writer.println("Date: " + LocalDateTime.now());
            writer.println("--------------------------------");
            writer.println("");

            VBox box = (VBox) reportContentBox;
            if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof TableView) {
                TableView<ReportItem> table = (TableView<ReportItem>) box.getChildren().get(0);
                for (ReportItem item : table.getItems()) {
                    writer.println(String.format("%-30s : %s", item.getKey(), item.getValue()));
                }
            }

            writer.println("");
            writer.println("--------------------------------");
            writer.println("End of Report");

            showAlert("Success", "Report saved to: " + file.getAbsolutePath());

        } catch (Exception e) {
            showAlert("Error", "Could not save file: " + e.getMessage());
        }
    }

    // --- OTHER HANDLERS ---
    @FXML 
    private void handleLogout() {
        try {
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            
            // 1280x800 boyutunda aç
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class ReportItem {
        private String key;
        private String value;

        public ReportItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}