package com.group12.greengrocer.controllers;

import java.io.File;
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
import javafx.scene.control.Separator;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.PieChart;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;

/**
 * Controller class for the Store Owner (Admin) Dashboard.
 * <p>
 * This class serves as the central management hub for the application. It provides comprehensive
 * control over the system, including:
 * <ul>
 * <li><b>Dashboard:</b> Real-time statistics and visual charts (Sales, Revenue, Performance).</li>
 * <li><b>Inventory Management:</b> Adding, updating, and removing products.</li>
 * <li><b>Order Management:</b> Monitoring all orders and filtering by status.</li>
 * <li><b>Personnel Management:</b> Hiring and firing carriers (delivery drivers).</li>
 * <li><b>Customer Support:</b> A chat interface to reply to customer tickets.</li>
 * <li><b>Settings:</b> Managing discount coupons and loyalty program parameters.</li>
 * <li><b>Reporting:</b> Generating financial/operational reports and exporting them to PDF.</li>
 * </ul>
 */
public class OwnerController {

    private User currentUser;

    @FXML
    private Label usernameLabel;
    @FXML
    private Label statusLabel;

    // --- DASHBOARD UI ELEMENTS ---
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

    // --- PRODUCT MANAGEMENT UI ---
    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TextField productSearchField;

    // --- ORDER MANAGEMENT UI ---
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private ComboBox<String> orderStatusFilter;
    @FXML
    private Label ordersCountLabel;

    // --- CARRIER MANAGEMENT UI ---
    @FXML
    private TableView<User> carriersTable;

    // --- MESSAGING SYSTEM UI ---
    @FXML
    private ListView<String> chatTopicsList; // List of active conversations
    @FXML
    private VBox chatMessagesBox; // Container for message bubbles
    @FXML
    private ScrollPane chatScroll; 
    @FXML
    private TextField chatInput; 
    @FXML
    private Label chatCurrentTopicLabel; 
    @FXML
    private Label chatCustomerNameLabel; 
    
    private String currentChatSubject = null;
    private int currentChatCustomerId = 0;

    // --- SETTINGS UI ---
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

    // --- REPORTING UI ---
    @FXML
    private ComboBox<String> reportTypeCombo;
    @FXML
    private VBox reportContentBox;

    // --- CHARTS ---
    @FXML
    private VBox mostSoldProductsChart;
    @FXML
    private VBox carrierPerformanceChart;
    @FXML
    private VBox orderIntensityHourChart;
    @FXML
    private VBox mostActiveCustomersChart;
    @FXML
    private VBox revenueByCategoryChart;

    private ObservableList<Product> masterProductList = FXCollections.observableArrayList();
    private ObservableList<Order> masterOrderList = FXCollections.observableArrayList();

    /**
     * Initializes the controller with the logged-in user's data.
     * Performs a security check to ensure the user has the 'OWNER' role.
     *
     * @param user The User object representing the logged-in admin.
     */
    public void initData(User user) {
        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            showAlert("Access Denied", "You are not authorized to access this panel.");
            return;
        }

        this.currentUser = user;
        usernameLabel.setText("Owner: " + user.getUsername());
        refreshAllData();
    }

    /**
     * Adds a listener to a TextField to prevent non-numeric input.
     *
     * @param field The TextField to restrict.
     */
    private void allowOnlyPositiveNumbers(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });
    }

    /**
     * Standard JavaFX initialize method.
     * Sets up table columns, filters, and listeners for the UI components.
     */
    @FXML
    private void initialize() {
        setupProductTable();
        setupOrderTable();
        setupCarrierTable();
        setupCouponTable(); 
        setupRecentOrdersTable(); 

        // Order Status Filters
        if (orderStatusFilter != null) {
            orderStatusFilter.getItems().addAll("All", "Pending", "Assigned", "Completed", "Cancelled");
            orderStatusFilter.getSelectionModel().selectFirst();
            orderStatusFilter.setOnAction(e -> filterOrders());
        }

        // Report Types
        if (reportTypeCombo != null) {
            reportTypeCombo.getItems().addAll("Product Revenue", "Carrier Performance", 
                "Revenue by Time (Daily)", "Revenue by Time (Weekly)", "Revenue by Time (Monthly)", 
                "Revenue by Amount Range");
            reportTypeCombo.getSelectionModel().selectFirst();
        }

        // Chat Topic Listener
        if (chatTopicsList != null) {
            chatTopicsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null)
                    loadChatMessages(newVal);
            });
        }
    }

    /**
     * Triggers a complete refresh of all data sections (Dashboard, Inventory, Orders, Carriers, etc.).
     */
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

    // --- DASHBOARD SECTION ---

    /**
     * Loads high-level statistics (Total Revenue, Active Orders, etc.) for the dashboard summary.
     * Populates the "Recent Orders" table with the latest transactions.
     */
    private void loadDashboardStats() {
        try {
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
        if (allOrders != null && !allOrders.isEmpty()) {
            if (allOrders.size() > 5)
                recent.addAll(allOrders.subList(0, 5));
            else
                recent.addAll(allOrders);
        }

        if (recentOrdersTable != null) {
            recentOrdersTable.setItems(recent);
            // Debugging logs
            System.out.println("Recent orders loaded: " + recent.size());
            if (!recent.isEmpty()) {
                System.out.println("First order: " + recent.get(0).getId() + " - " + recent.get(0).getCustomerName());
            }
        }
        
        loadDashboardCharts();
        } catch (Exception e) {
            showAlert("Error", "Failed to load dashboard stats: " + e.getMessage());
        }
    }

    /**
     * Fetches analytical data from the database and renders JavaFX Charts.
     * Includes:
     * <ul>
     * <li>Most Sold Products (Bar Chart)</li>
     * <li>Carrier Performance (Bar Chart)</li>
     * <li>Order Intensity by Hour (Bar Chart)</li>
     * <li>Most Active Customers (Bar Chart)</li>
     * <li>Revenue by Category (Pie Chart)</li>
     * </ul>
     */
    private void loadDashboardCharts() {
        try {
            // Most Sold Products Chart
            if (mostSoldProductsChart != null) {
                mostSoldProductsChart.getChildren().clear();
                Map<String, Double> data = OrderDAO.getMostSoldProducts(10);
                if (!data.isEmpty()) {
                    CategoryAxis xAxis = new CategoryAxis();
                    NumberAxis yAxis = new NumberAxis();
                    yAxis.setLabel("Quantity (kg)");
                    BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
                    chart.setTitle("Top 10 Most Sold Products");
                    chart.setLegendVisible(false);
                    chart.setPrefHeight(250);
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
                    chart.getData().add(series);
                    mostSoldProductsChart.getChildren().add(chart);
                } else {
                    Label noData = new Label("No data available");
                    noData.setStyle("-fx-text-fill: #666; -fx-padding: 20;");
                    mostSoldProductsChart.getChildren().add(noData);
                }
            }
            
            // Carrier Performance Chart
            if (carrierPerformanceChart != null) {
                carrierPerformanceChart.getChildren().clear();
                Map<String, Double> ratings = OrderDAO.getCarrierAverageRatings();
                Map<String, Integer> deliveries = OrderDAO.getCarrierPerformanceReport();
                
                if (!ratings.isEmpty() || !deliveries.isEmpty()) {
                    CategoryAxis xAxis = new CategoryAxis();
                    NumberAxis yAxis = new NumberAxis();
                    yAxis.setLabel("Rating / Deliveries");
                    BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
                    chart.setTitle("Carrier Performance");
                    chart.setPrefHeight(200);
                    
                    if (!ratings.isEmpty()) {
                        XYChart.Series<String, Number> ratingSeries = new XYChart.Series<>();
                        ratingSeries.setName("Avg Rating");
                        ratings.forEach((k, v) -> ratingSeries.getData().add(new XYChart.Data<>(k, v)));
                        chart.getData().add(ratingSeries);
                    }
                    
                    if (!deliveries.isEmpty()) {
                        XYChart.Series<String, Number> deliverySeries = new XYChart.Series<>();
                        deliverySeries.setName("Deliveries");
                        deliveries.forEach((k, v) -> deliverySeries.getData().add(new XYChart.Data<>(k, v.doubleValue())));
                        chart.getData().add(deliverySeries);
                    }
                    
                    carrierPerformanceChart.getChildren().add(chart);
                } else {
                    Label noData = new Label("No data available");
                    noData.setStyle("-fx-text-fill: #666; -fx-padding: 20;");
                    carrierPerformanceChart.getChildren().add(noData);
                }
            }
            
            // Order Intensity by Hour Chart
            if (orderIntensityHourChart != null) {
                orderIntensityHourChart.getChildren().clear();
                Map<String, Integer> data = OrderDAO.getOrderIntensityByHour();
                if (!data.isEmpty()) {
                    CategoryAxis xAxis = new CategoryAxis();
                    NumberAxis yAxis = new NumberAxis();
                    yAxis.setLabel("Order Count");
                    BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
                    chart.setTitle("Order Intensity by Hour");
                    chart.setLegendVisible(false);
                    chart.setPrefHeight(200);
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
                    chart.getData().add(series);
                    orderIntensityHourChart.getChildren().add(chart);
                } else {
                    Label noData = new Label("No data available");
                    noData.setStyle("-fx-text-fill: #666; -fx-padding: 20;");
                    orderIntensityHourChart.getChildren().add(noData);
                }
            }
            
            // Most Active Customers Chart
            if (mostActiveCustomersChart != null) {
                mostActiveCustomersChart.getChildren().clear();
                Map<String, Integer> data = OrderDAO.getMostActiveCustomers(10);
                if (!data.isEmpty()) {
                    CategoryAxis xAxis = new CategoryAxis();
                    NumberAxis yAxis = new NumberAxis();
                    yAxis.setLabel("Order Count");
                    BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
                    chart.setTitle("Top 10 Most Active Customers");
                    chart.setLegendVisible(false);
                    chart.setPrefHeight(200);
                    
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
                    chart.getData().add(series);
                    mostActiveCustomersChart.getChildren().add(chart);
                } else {
                    Label noData = new Label("No data available");
                    noData.setStyle("-fx-text-fill: #666; -fx-padding: 20;");
                    mostActiveCustomersChart.getChildren().add(noData);
                }
            }
            
            // Revenue by Category Chart
            if (revenueByCategoryChart != null) {
                revenueByCategoryChart.getChildren().clear();
                Map<String, Double> data = OrderDAO.getRevenueByCategory();
                if (!data.isEmpty()) {
                    PieChart chart = new PieChart();
                    chart.setTitle("Revenue by Category");
                    chart.setPrefHeight(200);
                    data.forEach((k, v) -> chart.getData().add(new PieChart.Data(
                        k.substring(0, 1).toUpperCase() + k.substring(1) + " (" + String.format("%.2f TL", v) + ")", v)));
                    revenueByCategoryChart.getChildren().add(chart);
                } else {
                    Label noData = new Label("No data available");
                    noData.setStyle("-fx-text-fill: #666; -fx-padding: 20;");
                    revenueByCategoryChart.getChildren().add(noData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Silently fail for charts - don't show alert for every chart
        }
    }

    // --- PRODUCTS SECTION ---

    /**
     * Configures the product table columns.
     * Applies custom CellFactories to color-code rows based on stock levels (Low Stock, Out of Stock).
     */
    private void setupProductTable() {
        if (productsTable != null && !productsTable.getColumns().isEmpty()) {
            productsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
            productsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
            productsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("type"));
            productsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("price"));
            productsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("stock"));
            productsTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("threshold"));
            
            // Coloring for Price column
            if (productsTable.getColumns().size() > 6) {
                TableColumn<Product, String> priceCol = (TableColumn<Product, String>) productsTable.getColumns().get(6);
                priceCol.setCellValueFactory(data -> {
                    Product p = data.getValue();
                    double currentPrice = p.getCurrentPrice();
                    return new SimpleStringProperty(String.format("%.2f TL", currentPrice));
                });
                priceCol.setCellFactory(column -> {
                    return new TableCell<Product, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                            } else {
                                setText(item);
                                Product p = getTableView().getItems().get(getIndex());
                                if (p.getStock() <= p.getThreshold()) {
                                    setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                                } else {
                                    setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;");
                                }
                            }
                        }
                    };
                });
            }
            
            // Status column with visual indicators
            if (productsTable.getColumns().size() > 7) {
                TableColumn<Product, String> statusCol = (TableColumn<Product, String>) productsTable.getColumns().get(7);
                statusCol.setCellValueFactory(data -> {
                    Product p = data.getValue();
                    if (p.getStock() <= p.getThreshold() && p.getStock() > 0) {
                        return new SimpleStringProperty("⚠️ Low Stock");
                    } else if (p.getStock() == 0) {
                        return new SimpleStringProperty("❌ Out of Stock");
                    } else {
                        return new SimpleStringProperty("✅ In Stock");
                    }
                });
                statusCol.setCellFactory(column -> {
                    return new TableCell<Product, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                            } else {
                                setText(item);
                                Product p = getTableView().getItems().get(getIndex());
                                if (p.getStock() <= p.getThreshold() && p.getStock() > 0) {
                                    setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f57c00; -fx-font-weight: bold;");
                                } else if (p.getStock() == 0) {
                                    setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                                } else {
                                    setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                                }
                            }
                        }
                    };
                });
            }
        }
    }

    /**
     * Reloads product data from the database into the master list.
     */
    private void loadProducts() {
        masterProductList.setAll(ProductDAO.getAllProducts());
        if (productsTable != null)
            productsTable.setItems(masterProductList);
    }

    /**
     * Filters the product table based on the search query (Name or Type).
     */
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

    /**
     * Opens a dialog to add a new product.
     * Validates inputs (negative numbers, empty fields) and saves the product to the database.
     */
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

        allowOnlyPositiveNumbers(priceField);
        allowOnlyPositiveNumbers(stockField);
        allowOnlyPositiveNumbers(thresholdField);

        Button imgBtn = new Button("Select Image");
        Label imgLabel = new Label("No file selected");
        final File[] selectedFile = { null };

        imgBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg")
            );
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
                            selectedFile[0]
                    );

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

    /**
     * Deletes the selected product from the database after confirmation.
     */
    @FXML
    private void handleRemoveProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a product to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Product");
        confirm.setContentText("Are you sure you want to delete " + selected.getName() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
        if (ProductDAO.deleteProduct(selected.getId())) {
            loadProducts();
            loadDashboardStats();
            showAlert("Success", "Product deleted.");
        } else {
            showAlert("Error", "Could not delete product.");
        }
                } catch (Exception e) {
                    showAlert("Error", "Error deleting product: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Opens a dialog to update an existing product.
     * Pre-fills the dialog with current product details.
     */
    @FXML
    private void handleUpdateProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a product to update.");
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Update Product");
        dialog.setHeaderText("Update product details for: " + selected.getName());

        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("vegetable", "fruit");
        typeCombo.setValue(selected.getType());
        TextField priceField = new TextField(String.valueOf(selected.getPrice()));
        TextField stockField = new TextField(String.valueOf(selected.getStock()));
        TextField thresholdField = new TextField(String.valueOf(selected.getThreshold()));

        Button imgBtn = new Button("Change Image");
        Label imgLabel = new Label("No change");
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
                    String name = nameField.getText().trim();
                    String type = typeCombo.getValue();
                    double price = Double.parseDouble(priceField.getText());
                    double stock = Double.parseDouble(stockField.getText());
                    double threshold = Double.parseDouble(thresholdField.getText());

                    // Validations
                    if (name.isEmpty()) {
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

                    // Check for duplicate name (excluding itself)
                    if (!name.equals(selected.getName()) || !type.equals(selected.getType())) {
                        if (ProductDAO.productExists(name, type)) {
                            showAlert("Error", "A product with this name and type already exists.");
                            return false;
                        }
                    }

                    boolean success = ProductDAO.updateProduct(
                            selected.getId(),
                            name,
                            type,
                            price,
                            stock,
                            threshold,
                            selectedFile[0]);

                    if (success) {
                loadProducts();
                        loadDashboardStats();
                        return true;
                    } else {
                        showAlert("Error", "Failed to update product.");
                        return false;
                    }

            } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter valid numeric values for price, stock, and threshold.");
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
            showAlert("Success", "Product updated successfully.");
        }
    }


    // --- ORDERS SECTION ---

    /**
     * Configures the main order table and color-codes rows based on order status.
     * Colors: Pending (Orange), Assigned (Blue), Completed (Green), Cancelled (Red).
     */
    private void setupOrderTable() {
        if (ordersTable != null && !ordersTable.getColumns().isEmpty()) {
            ordersTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
            ordersTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("customerName"));
            ordersTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("carrierId"));
            ordersTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("deliveryNeighborhood"));
            ordersTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("totalCost"));
            
            // Status column coloring
            if (ordersTable.getColumns().size() > 5) {
                TableColumn<Order, String> statusCol = (TableColumn<Order, String>) ordersTable.getColumns().get(5);
                statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
                statusCol.setCellFactory(column -> {
                    return new TableCell<Order, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                            } else {
                                setText(item);
                                String status = item.toLowerCase();
                                if (status.contains("pending")) {
                                    setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f57c00; -fx-font-weight: bold;");
                                } else if (status.contains("assigned")) {
                                    setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
                                } else if (status.contains("completed")) {
                                    setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                                } else if (status.contains("cancelled")) {
                                    setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                                } else {
                                    setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #666;");
                                }
                            }
                        }
                    };
                });
            }
            
            ordersTable.getColumns().get(6).setCellValueFactory(new PropertyValueFactory<>("orderTime"));
            ordersTable.getColumns().get(7).setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));
        }
    }

    /**
     * Sets up the "Recent Orders" table on the dashboard with abbreviated columns.
     */
    private void setupRecentOrdersTable() {
        if (recentOrdersTable == null) return;
        
        // If columns are missing, create them
        if (recentOrdersTable.getColumns().isEmpty()) {
            TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            idCol.setPrefWidth(80);
            
            TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
            customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            customerCol.setPrefWidth(150);
            
            TableColumn<Order, String> totalCol = new TableColumn<>("Total");
            totalCol.setCellValueFactory(data -> {
                double total = data.getValue().getTotalCost();
                return new SimpleStringProperty(String.format("%.2f TL", total));
            });
            totalCol.setPrefWidth(100);
            
            TableColumn<Order, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            statusCol.setCellFactory(column -> {
                return new TableCell<Order, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            String status = item.toLowerCase();
                            if (status.contains("pending")) {
                                setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f57c00; -fx-font-weight: bold;");
                            } else if (status.contains("assigned")) {
                                setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
                            } else if (status.contains("completed")) {
                                setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                            } else if (status.contains("cancelled")) {
                                setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #666;");
                            }
                        }
                    }
                };
            });
            statusCol.setPrefWidth(120);
            
            TableColumn<Order, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(data -> {
                java.sql.Timestamp time = data.getValue().getOrderTime();
                if (time != null) {
                    return new SimpleStringProperty(time.toString().substring(0, 16));
                }
                return new SimpleStringProperty("-");
            });
            dateCol.setPrefWidth(150);
            
            recentOrdersTable.getColumns().addAll(idCol, customerCol, totalCol, statusCol, dateCol);
        } else {
            // Re-bind if columns exist
            recentOrdersTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
            recentOrdersTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("customerName"));
            
            if (recentOrdersTable.getColumns().size() > 2) {
                TableColumn<Order, String> totalCol = (TableColumn<Order, String>) recentOrdersTable.getColumns().get(2);
                totalCol.setCellValueFactory(data -> {
                    double total = data.getValue().getTotalCost();
                    return new SimpleStringProperty(String.format("%.2f TL", total));
                });
            }
            
            if (recentOrdersTable.getColumns().size() > 3) {
                TableColumn<Order, String> statusCol = (TableColumn<Order, String>) recentOrdersTable.getColumns().get(3);
                statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
                statusCol.setCellFactory(column -> {
                    return new TableCell<Order, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                            } else {
                                setText(item);
                                String status = item.toLowerCase();
                                if (status.contains("pending")) {
                                    setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #f57c00; -fx-font-weight: bold;");
                                } else if (status.contains("assigned")) {
                                    setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
                                } else if (status.contains("completed")) {
                                    setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                                } else if (status.contains("cancelled")) {
                                    setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                                } else {
                                    setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #666;");
                                }
                            }
                        }
                    };
                });
            }
            
            if (recentOrdersTable.getColumns().size() > 4) {
                TableColumn<Order, String> dateCol = (TableColumn<Order, String>) recentOrdersTable.getColumns().get(4);
                dateCol.setCellValueFactory(data -> {
                    java.sql.Timestamp time = data.getValue().getOrderTime();
                    if (time != null) {
                        return new SimpleStringProperty(time.toString().substring(0, 16));
                    }
                    return new SimpleStringProperty("-");
                });
            }
        }
    }

    /**
     * Reloads all orders for the admin view.
     */
    private void loadOrders() {
        masterOrderList.setAll(OrderDAO.getAllOrdersForAdmin());
        filterOrders();
    }

    /**
     * Filters the order list based on the selected status in the dropdown.
     */
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

    // --- CARRIERS SECTION ---

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

    /**
     * Hires a new carrier (creates a user with 'CARRIER' role).
     */
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

    /**
     * Fires a carrier (Deletes the user).
     * Prevents deletion if the carrier has active/assigned orders.
     */
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

    /**
     * Views detailed ratings and performance metrics for a selected carrier.
     */
    @FXML
    private void handleViewCarrierRatings() {
        User selected = carriersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a carrier to view ratings.");
            return;
        }

        try {
            List<OrderDAO.CarrierRating> ratings = OrderDAO.getCarrierRatings(selected.getId());
            double avgRating = OrderDAO.getCarrierAverageRating(selected.getId());
        Map<String, Integer> performance = OrderDAO.getCarrierPerformanceReport();
        int completedDeliveries = performance.getOrDefault(selected.getUsername(), 0);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Carrier Ratings & Performance");
            dialog.setHeaderText("Detailed Performance: " + selected.getUsername());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));

            Label statsLabel = new Label();
            statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            statsLabel.setText(String.format("Average Rating: %.2f / 5.0\nCompleted Deliveries: %d", 
                avgRating, completedDeliveries));
            content.getChildren().add(statsLabel);

            if (!ratings.isEmpty()) {
                Label ratingsLabel = new Label("Recent Ratings:");
                ratingsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                content.getChildren().add(ratingsLabel);

                TableView<OrderDAO.CarrierRating> ratingsTable = new TableView<>();
                TableColumn<OrderDAO.CarrierRating, Integer> orderCol = new TableColumn<>("Order ID");
                orderCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().orderId).asObject());
                TableColumn<OrderDAO.CarrierRating, String> customerCol = new TableColumn<>("Customer");
                customerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().customerName));
                TableColumn<OrderDAO.CarrierRating, Integer> ratingCol = new TableColumn<>("Rating");
                ratingCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().rating).asObject());
                TableColumn<OrderDAO.CarrierRating, String> commentCol = new TableColumn<>("Comment");
                commentCol.setCellValueFactory(data -> new SimpleStringProperty(
                    data.getValue().comment != null ? data.getValue().comment : "-"));
                
                ratingsTable.getColumns().add(orderCol);
                ratingsTable.getColumns().add(customerCol);
                ratingsTable.getColumns().add(ratingCol);
                ratingsTable.getColumns().add(commentCol);
                ratingsTable.setItems(FXCollections.observableArrayList(ratings));
                ratingsTable.setPrefHeight(200);
                content.getChildren().add(ratingsTable);
            } else {
                Label noRatingsLabel = new Label("No ratings yet.");
                noRatingsLabel.setStyle("-fx-text-fill: #666;");
                content.getChildren().add(noRatingsLabel);
            }

            dialog.getDialogPane().setContent(content);
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert("Error", "Failed to load carrier ratings: " + e.getMessage());
        }
    }

    // --- MESSAGING SYSTEM ---

    /**
     * Loads all customer conversations and groups them by customer and subject in the list view.
     */
    private void loadMessages() {
        if (chatTopicsList == null) return;
        
        try {
            List<Message> allMsgs = MessageDAO.getAllMessages();
            chatTopicsList.getItems().clear();
            
            if (allMsgs.isEmpty()) {
                chatTopicsList.getItems().add("No messages yet");
                return;
            }
            
            // Group messages by "Customer - Subject"
            Map<String, List<Message>> grouped = allMsgs.stream()
                .collect(java.util.stream.Collectors.groupingBy(msg -> 
                    msg.getSenderName() + " - " + msg.getSubject()));
            
            for (Map.Entry<String, List<Message>> entry : grouped.entrySet()) {
                List<Message> msgs = entry.getValue();
                if (!msgs.isEmpty()) {
                    Message lastMsg = msgs.get(msgs.size() - 1);
                    String displayText = entry.getKey();
                    if (lastMsg.getCreatedAt() != null) {
                        String dateStr = lastMsg.getCreatedAt().toString().substring(0, 10);
                        displayText += " (" + dateStr + ")";
                    }
                    chatTopicsList.getItems().add(displayText);
                }
            }
            
            if (!chatTopicsList.getItems().isEmpty()) {
                chatTopicsList.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load messages: " + e.getMessage());
        }
    }

    /**
     * Loads the chat history for a selected topic.
     * Identifies the customer ID associated with the chat.
     *
     * @param selection The selected string from the topic list.
     */
    private void loadChatMessages(String selection) {
        if (selection == null || chatMessagesBox == null) return;
        
        try {
            String[] parts = selection.split(" - ");
            if (parts.length < 2) return;
            
            String customerName = parts[0];
            String subject = parts[1];
            if (subject.contains(" (")) {
                subject = subject.substring(0, subject.lastIndexOf(" ("));
            }
            
            currentChatSubject = subject;
            
            // Find Customer ID
            List<User> customers = UserDAO.getAllCustomers();
            for (User customer : customers) {
                if (customer.getUsername().equals(customerName)) {
                    currentChatCustomerId = customer.getId();
                    break;
                }
            }
            
            if (chatCurrentTopicLabel != null)
                chatCurrentTopicLabel.setText(subject);
            if (chatCustomerNameLabel != null)
                chatCustomerNameLabel.setText(customerName);
            
            chatMessagesBox.getChildren().clear();
            
            List<Message> msgs = MessageDAO.getConversation(currentChatCustomerId, currentUser.getId());
            for (Message m : msgs) {
                if (m.getSubject().equalsIgnoreCase(subject)) {
                    addMessageBubble(m.getContent(), m.getSenderId() == currentUser.getId(), 
                        m.getCreatedAt() != null ? m.getCreatedAt().toString() : "");
                }
            }
            
            // Auto scroll to bottom
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        if (chatScroll != null) chatScroll.setVvalue(1.0);
                    });
                }
            }, 100);
        } catch (Exception e) {
            showAlert("Error", "Failed to load chat: " + e.getMessage());
        }
    }

    /**
     * Adds a chat bubble to the message interface.
     *
     * @param text The message content.
     * @param isMe True if sent by admin (Blue), False if by customer (White).
     * @param timestamp The time of the message.
     */
    private void addMessageBubble(String text, boolean isMe, String timestamp) {
        if (chatMessagesBox == null) return;
        
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setMaxWidth(350);
        lbl.setPadding(new Insets(10, 15, 10, 15));
        lbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
        
        HBox box = new HBox();
        box.setPadding(new Insets(0, 0, 5, 0));
        
        if (isMe) {
            // OWNER MESSAGE (RIGHT) - Blue
            lbl.setStyle("-fx-background-color: #2196f3; " +
                         "-fx-background-radius: 15 15 0 15; " +
                         "-fx-text-fill: white; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1); " +
                         "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
            box.setAlignment(Pos.CENTER_RIGHT);
        } else {
            // CUSTOMER MESSAGE (LEFT) - White
            lbl.setStyle("-fx-background-color: #ffffff; " +
                         "-fx-background-radius: 15 15 15 0; " +
                         "-fx-text-fill: black; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1); " +
                         "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
            box.setAlignment(Pos.CENTER_LEFT);
        }
        
        box.getChildren().add(lbl);
        chatMessagesBox.getChildren().add(box);
        
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (chatScroll != null) chatScroll.setVvalue(1.0);
                });
            }
        }, 100);
    }

    @FXML
    private void handleRefreshMessages() {
        loadMessages();
    }

    @FXML
    private void handleSendMessage() {
        if (chatInput == null || currentChatSubject == null || currentChatCustomerId == 0) {
            showAlert("Warning", "Please select a conversation first.");
            return;
        }

        String txt = chatInput.getText().trim();
        if (txt.isEmpty()) return;
        
        try {
            if (MessageDAO.sendMessage(currentUser.getId(), currentChatCustomerId, currentChatSubject, txt)) {
                addMessageBubble(txt, true, LocalDateTime.now().toString());
                chatInput.clear();
                if (chatScroll != null) chatScroll.setVvalue(1.0);
            } else {
                showAlert("Error", "Failed to send message.");
            }
        } catch (Exception e) {
            showAlert("Error", "Error sending message: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteMessage() {
        if (currentChatSubject == null || currentChatCustomerId == 0) {
            showAlert("Warning", "Please select a conversation to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Conversation");
        confirm.setContentText("Are you sure you want to delete this conversation?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (MessageDAO.deleteChatTopic(currentChatCustomerId, currentChatSubject)) {
            loadMessages();
                        if (chatMessagesBox != null) chatMessagesBox.getChildren().clear();
                        if (chatCurrentTopicLabel != null) chatCurrentTopicLabel.setText("-");
                        if (chatCustomerNameLabel != null) chatCustomerNameLabel.setText("-");
                        showAlert("Success", "Conversation deleted.");
                    } else {
                        showAlert("Error", "Failed to delete conversation.");
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error deleting conversation: " + e.getMessage());
                }
            }
        });
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

    /**
     * Loads system settings like Loyalty Program parameters and Minimum Cart Value.
     */
    private void loadSettings() {
        try {
            Integer[] loyalty = SettingsDAO.getLoyaltySettings();
            if (minOrdersField != null) {
                if (loyalty[0] != null) {
            minOrdersField.setText(String.valueOf(loyalty[0]));
                } else {
                    minOrdersField.setText(""); 
                }
            }
        if (loyaltyDiscountField != null)
            loyaltyDiscountField.setText(String.valueOf(loyalty[1]));
            
            double minCartValue = SettingsDAO.getMinCartValue();
            if (minCartValueField != null)
                minCartValueField.setText(String.format("%.2f", minCartValue));
        } catch (Exception e) {
            showAlert("Error", "Failed to load settings: " + e.getMessage());
        }
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
        datePicker.setValue(java.time.LocalDate.now().plusMonths(1));

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
                    String code = codeField.getText().trim().toUpperCase();
                    double discount = Double.parseDouble(discField.getText());
                    double minPurchase = Double.parseDouble(minField.getText());
                    java.time.LocalDate validUntil = datePicker.getValue();

                    if (code.isEmpty()) {
                        showAlert("Error", "Coupon code cannot be empty.");
                        return false;
                    }
                    if (discount <= 0 || discount > 100) {
                        showAlert("Error", "Discount must be between 0 and 100.");
                        return false;
                    }
                    if (minPurchase < 0) {
                        showAlert("Error", "Minimum purchase cannot be negative.");
                        return false;
                    }
                    if (validUntil == null || validUntil.isBefore(java.time.LocalDate.now())) {
                        showAlert("Error", "Valid until date must be in the future.");
                        return false;
                    }

                    return SettingsDAO.addCoupon(code, discount, minPurchase, validUntil);
                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter valid numeric values.");
                    return false;
                } catch (Exception e) {
                    showAlert("Error", "Error adding coupon: " + e.getMessage());
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
    private void handleRemoveCoupon() {
        Coupon selected = couponsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a coupon to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Coupon");
        confirm.setContentText("Are you sure you want to delete coupon " + selected.getCode() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (SettingsDAO.deleteCoupon(selected.getId())) {
                        loadCoupons();
                        showAlert("Success", "Coupon deleted.");
                    } else {
                        showAlert("Error", "Could not delete coupon.");
                    }
                } catch (Exception e) {
                    showAlert("Error", "Error deleting coupon: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleUpdateCoupon() {
        Coupon selected = couponsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a coupon to update.");
            return;
        }

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Update Coupon");
        dialog.setHeaderText("Update coupon: " + selected.getCode());

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField(selected.getCode());
        TextField discField = new TextField(String.valueOf(selected.getDiscountPercentage()));
        TextField minField = new TextField(String.valueOf(selected.getMinPurchaseAmount()));
        DatePicker datePicker = new DatePicker(selected.getValidUntil().toLocalDateTime().toLocalDate());
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Inactive");
        statusCombo.setValue(selected.isActive() ? "Active" : "Inactive");

        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Discount (%):"), 0, 1);
        grid.add(discField, 1, 1);
        grid.add(new Label("Min Purchase:"), 0, 2);
        grid.add(minField, 1, 2);
        grid.add(new Label("Valid Until:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    String code = codeField.getText().trim().toUpperCase();
                    double discount = Double.parseDouble(discField.getText());
                    double minPurchase = Double.parseDouble(minField.getText());
                    java.time.LocalDate validUntil = datePicker.getValue();
                    boolean isActive = statusCombo.getValue().equals("Active");

                    if (code.isEmpty()) {
                        showAlert("Error", "Coupon code cannot be empty.");
                        return false;
                    }
                    if (discount <= 0 || discount > 100) {
                        showAlert("Error", "Discount must be between 0 and 100.");
                        return false;
                    }
                    if (minPurchase < 0) {
                        showAlert("Error", "Minimum purchase cannot be negative.");
                        return false;
                    }
                    if (validUntil == null) {
                        showAlert("Error", "Please select a valid date.");
                        return false;
                    }

                    return SettingsDAO.updateCoupon(selected.getId(), code, discount, minPurchase, validUntil, isActive);
                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter valid numeric values.");
                    return false;
                } catch (Exception e) {
                    showAlert("Error", "Error updating coupon: " + e.getMessage());
                    return false;
                }
            }
            return null;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            loadCoupons();
            showAlert("Success", "Coupon updated.");
        }
    }

    @FXML
    private void handleSaveLoyaltySettings() {
        try {
            Integer minOrders = null;
            String minOrdersText = minOrdersField.getText().trim();
            if (!minOrdersText.isEmpty()) {
                minOrders = Integer.parseInt(minOrdersText);
                if (minOrders < 0) {
                    showAlert("Error", "Minimum orders cannot be negative.");
                    return;
                }
            }
            
            double discount = Double.parseDouble(loyaltyDiscountField.getText());
            if (discount < 0 || discount > 100) {
                showAlert("Error", "Discount must be between 0 and 100.");
                return;
            }
            
            SettingsDAO.updateLoyaltySettings(minOrders, discount);
            showAlert("Success", "Loyalty settings updated.");
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers.");
        } catch (Exception e) {
            showAlert("Error", "Error updating loyalty settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveMinCartValue() {
        try {
            double minValue = Double.parseDouble(minCartValueField.getText());
            if (minValue < 0) {
                showAlert("Error", "Minimum cart value cannot be negative.");
                return;
            }
            if (SettingsDAO.updateMinCartValue(minValue)) {
                showAlert("Success", "Minimum cart value updated.");
            } else {
                showAlert("Error", "Failed to update minimum cart value.");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number.");
        } catch (Exception e) {
            showAlert("Error", "Error updating minimum cart value: " + e.getMessage());
        }
    }

    // --- REPORTING SECTION ---

    /**
     * Generates a report preview in the UI based on the selected report type.
     * Supported types: Revenue (Time/Amount), Product Revenue, Carrier Performance.
     */
    @FXML
    private void handleGenerateReport() {
        String type = reportTypeCombo.getValue();
        if (type == null) {
            showAlert("Warning", "Please select a report type.");
            return;
        }

        reportContentBox.getChildren().clear();

        try {
            // Header
            VBox reportContainer = new VBox(10);
            reportContainer.setStyle("-fx-background-color: white; -fx-padding: 30;");
            
            // Green Grocer Title
            Label headerLabel = new Label("GREEN GROCER");
            headerLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-alignment: center;");
            headerLabel.setAlignment(Pos.CENTER);
            headerLabel.setMaxWidth(Double.MAX_VALUE);
            
            // Report Name
            Label reportTitleLabel = new Label(type);
            reportTitleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-alignment: center;");
            reportTitleLabel.setAlignment(Pos.CENTER);
            reportTitleLabel.setMaxWidth(Double.MAX_VALUE);
            
            // Date
            Label dateLabel = new Label("Date: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-alignment: center;");
            dateLabel.setAlignment(Pos.CENTER);
            dateLabel.setMaxWidth(Double.MAX_VALUE);
            
            Separator separator = new Separator();
            
            reportContainer.getChildren().addAll(headerLabel, reportTitleLabel, dateLabel, separator);
            
            // Table
            TableView<ReportItem> table = new TableView<>();
            table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
            table.setStyle("-fx-background-color: white;");
            
            TableColumn<ReportItem, String> keyCol = new TableColumn<>("Item");
            keyCol.setCellValueFactory(new PropertyValueFactory<>("key"));
            keyCol.setStyle("-fx-font-weight: bold;");
            keyCol.setPrefWidth(400);
            
            TableColumn<ReportItem, String> valCol = new TableColumn<>("Value");
            valCol.setCellValueFactory(new PropertyValueFactory<>("value"));
            valCol.setPrefWidth(200);
            
            table.getColumns().add(keyCol);
            table.getColumns().add(valCol);
            
            ObservableList<ReportItem> data = FXCollections.observableArrayList();
            
            if (type.contains("Revenue by Time")) {
                String period = type.contains("Daily") ? "daily" : 
                               type.contains("Weekly") ? "weekly" : "monthly";
                Map<String, Double> reportData = OrderDAO.getRevenueByTimeReport(period);
                
                if (reportData.isEmpty()) {
                    Label noDataLabel = new Label("No data available for this period.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                    reportContainer.getChildren().add(noDataLabel);
                } else {
                    keyCol.setText("Period");
                    valCol.setText("Revenue (TL)");
                    reportData.forEach((k, v) -> data.add(new ReportItem(k, String.format("%.2f TL", v))));
                    table.setItems(data);
                    table.setPrefHeight(400);
                    reportContainer.getChildren().add(table);
                }
                
            } else if (type.equals("Revenue by Amount Range")) {
                Map<String, Double> reportData = OrderDAO.getRevenueByAmountRange();
                
                if (reportData.isEmpty()) {
                    Label noDataLabel = new Label("No data available.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                    reportContainer.getChildren().add(noDataLabel);
                } else {
                    keyCol.setText("Amount Range");
                    valCol.setText("Revenue (TL)");
                    reportData.forEach((k, v) -> data.add(new ReportItem(k, String.format("%.2f TL", v))));
                    table.setItems(data);
                    table.setPrefHeight(400);
                    reportContainer.getChildren().add(table);
                }
                
            } else if (type.equals("Product Revenue")) {
                Map<String, Double> map = OrderDAO.getRevenueByProductReport();
                
                if (map.isEmpty()) {
                    Label noDataLabel = new Label("No product revenue data available.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                    reportContainer.getChildren().add(noDataLabel);
                } else {
                    keyCol.setText("Product Name");
                    valCol.setText("Total Revenue (TL)");
                    map.forEach((k, v) -> data.add(new ReportItem(k, String.format("%.2f TL", v))));
                    table.setItems(data);
                    table.setPrefHeight(400);
                    reportContainer.getChildren().add(table);
                }
                
            } else if (type.equals("Carrier Performance")) {
                Map<String, Integer> map = OrderDAO.getCarrierPerformanceReport();
                
                if (map.isEmpty()) {
                    Label noDataLabel = new Label("No carrier performance data available.");
                    noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20;");
                    reportContainer.getChildren().add(noDataLabel);
                } else {
                    keyCol.setText("Carrier Username");
                    valCol.setText("Completed Deliveries");
                    map.forEach((k, v) -> data.add(new ReportItem(k, String.valueOf(v))));
                    table.setItems(data);
                    table.setPrefHeight(400);
                    reportContainer.getChildren().add(table);
                }
            }
            
            reportContentBox.getChildren().add(reportContainer);
        } catch (Exception e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportReport() {
        if (reportTypeCombo.getValue() == null) {
            showAlert("Warning", "Please generate a report first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as PDF");
        fileChooser.setInitialFileName("Report_" + System.currentTimeMillis() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveReportToPDF(file);
        }
    }

    /**
     * Exports the generated report data to a PDF file using iTextPDF library.
     * Includes formatting for headers, dates, and data tables.
     *
     * @param file The destination file.
     */
    private void saveReportToPDF(File file) {
        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
            document.open();

            String reportType = reportTypeCombo.getValue();
            
            // Header
            com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 32, com.itextpdf.text.BaseColor.GREEN);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("GREEN GROCER", titleFont);
            title.setAlignment(com.itextpdf.text.Paragraph.ALIGN_CENTER);
            document.add(title);
            
            document.add(new com.itextpdf.text.Paragraph(" "));
            
            // Report Title
            com.itextpdf.text.Font reportTitleFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA_BOLD, 20, com.itextpdf.text.BaseColor.BLACK);
            com.itextpdf.text.Paragraph reportTitle = new com.itextpdf.text.Paragraph(reportType, reportTitleFont);
            reportTitle.setAlignment(com.itextpdf.text.Paragraph.ALIGN_CENTER);
            document.add(reportTitle);
            
            // Date
            com.itextpdf.text.Font dateFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 12, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Paragraph date = new com.itextpdf.text.Paragraph(
                "Date: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), 
                dateFont);
            date.setAlignment(com.itextpdf.text.Paragraph.ALIGN_CENTER);
            document.add(date);
            
            document.add(new com.itextpdf.text.Paragraph(" "));
            document.add(new com.itextpdf.text.Paragraph("--------------------------------"));
            document.add(new com.itextpdf.text.Paragraph(" "));

            // Get Table Data
            VBox box = (VBox) reportContentBox;
            List<ReportItem> reportItems = new java.util.ArrayList<>();
            
            if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof VBox) {
                VBox reportContainer = (VBox) box.getChildren().get(0);
                for (javafx.scene.Node node : reportContainer.getChildren()) {
                    if (node instanceof TableView) {
                        @SuppressWarnings("unchecked")
                        TableView<ReportItem> table = (TableView<ReportItem>) node;
                        reportItems.addAll(table.getItems());
                        break;
                    }
                }
            }
            
            // Fallback: If table is not in UI, fetch data again
            if (reportItems.isEmpty()) {
                String type = reportType;
                if (type.contains("Revenue by Time")) {
                    String period = type.contains("Daily") ? "daily" : 
                                   type.contains("Weekly") ? "weekly" : "monthly";
                    Map<String, Double> data = OrderDAO.getRevenueByTimeReport(period);
                    data.forEach((k, v) -> reportItems.add(new ReportItem(k, String.format("%.2f TL", v))));
                } else if (type.equals("Revenue by Amount Range")) {
                    Map<String, Double> data = OrderDAO.getRevenueByAmountRange();
                    data.forEach((k, v) -> reportItems.add(new ReportItem(k, String.format("%.2f TL", v))));
                } else if (type.equals("Product Revenue")) {
                    Map<String, Double> map = OrderDAO.getRevenueByProductReport();
                    map.forEach((k, v) -> reportItems.add(new ReportItem(k, String.format("%.2f TL", v))));
                } else if (type.equals("Carrier Performance")) {
                    Map<String, Integer> map = OrderDAO.getCarrierPerformanceReport();
                    map.forEach((k, v) -> reportItems.add(new ReportItem(k, String.valueOf(v))));
                }
            }
            
            if (reportItems.isEmpty()) {
                com.itextpdf.text.Paragraph noData = new com.itextpdf.text.Paragraph("No data available for this report.");
                noData.setAlignment(com.itextpdf.text.Paragraph.ALIGN_CENTER);
                document.add(noData);
            } else {
                // PDF Table
                com.itextpdf.text.pdf.PdfPTable pdfTable = new com.itextpdf.text.pdf.PdfPTable(2);
                pdfTable.setWidthPercentage(100);
                pdfTable.setWidths(new float[]{3, 2});
                
                // Headers
                com.itextpdf.text.Font headerFont = com.itextpdf.text.FontFactory.getFont(
                    com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12, com.itextpdf.text.BaseColor.WHITE);
                com.itextpdf.text.pdf.PdfPCell headerCell1 = new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase("Item", headerFont));
                headerCell1.setBackgroundColor(com.itextpdf.text.BaseColor.DARK_GRAY);
                pdfTable.addCell(headerCell1);
                
                com.itextpdf.text.pdf.PdfPCell headerCell2 = new com.itextpdf.text.pdf.PdfPCell(
                    new com.itextpdf.text.Phrase("Value", headerFont));
                headerCell2.setBackgroundColor(com.itextpdf.text.BaseColor.DARK_GRAY);
                pdfTable.addCell(headerCell2);
                
                // Data Rows
                com.itextpdf.text.Font dataFont = com.itextpdf.text.FontFactory.getFont(
                    com.itextpdf.text.FontFactory.HELVETICA, 11, com.itextpdf.text.BaseColor.BLACK);
                boolean alternate = false;
                for (ReportItem item : reportItems) {
                    com.itextpdf.text.BaseColor rowColor = alternate ? 
                        com.itextpdf.text.BaseColor.WHITE : 
                        new com.itextpdf.text.BaseColor(245, 245, 245);
                    alternate = !alternate;
                    
                    com.itextpdf.text.pdf.PdfPCell cell1 = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Phrase(item.getKey(), dataFont));
                    cell1.setBackgroundColor(rowColor);
                    pdfTable.addCell(cell1);
                    
                    com.itextpdf.text.pdf.PdfPCell cell2 = new com.itextpdf.text.pdf.PdfPCell(
                        new com.itextpdf.text.Phrase(item.getValue(), dataFont));
                    cell2.setBackgroundColor(rowColor);
                    pdfTable.addCell(cell2);
                }
                
                document.add(pdfTable);
            }

            document.add(new com.itextpdf.text.Paragraph(" "));
            document.add(new com.itextpdf.text.Paragraph("--------------------------------"));
            
            // Footer
            com.itextpdf.text.Font footerFont = com.itextpdf.text.FontFactory.getFont(
                com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GRAY);
            com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph(
                "This report was generated electronically.", footerFont);
            footer.setAlignment(com.itextpdf.text.Paragraph.ALIGN_CENTER);
            document.add(footer);

            document.close();
            showAlert("Success", "Report saved as PDF to: " + file.getAbsolutePath());

        } catch (Exception e) {
            showAlert("Error", "Could not save PDF file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logs the admin out and returns to the login screen.
     */
    @FXML 
    private void handleLogout() {
        try {
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            
            // 1280x800 resolution
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

    /**
     * Helper inner class for report generation.
     * Represents a single Key-Value pair row in the report table.
     */
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