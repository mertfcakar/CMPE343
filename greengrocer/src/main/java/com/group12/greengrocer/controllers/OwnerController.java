package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.ProductDAO;
import com.group12.greengrocer.models.Order;
import com.group12.greengrocer.models.Product;
import com.group12.greengrocer.models.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class OwnerController {
    
    private User currentUser;
    
    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    
    // İstatistikler
    @FXML private Label totalProductsLabel;
    @FXML private Label activeOrdersLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label activeCarriersLabel;
    
    // Tablolar
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private TableView<Product> productsTable;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableView<User> carriersTable;
    @FXML private TableView<?> couponsTable;
    
    // Arama
    @FXML private TextField productSearchField;
    @FXML private ComboBox<String> orderStatusFilter;
    
    // Ürün Tablosu Sütunları
    @FXML private TableColumn<Product, Integer> prodIdCol;
    @FXML private TableColumn<Product, String> prodNameCol;
    @FXML private TableColumn<Product, String> prodTypeCol;
    @FXML private TableColumn<Product, Double> prodPriceCol;
    @FXML private TableColumn<Product, Double> prodStockCol;
    @FXML private TableColumn<Product, Double> prodThresholdCol;

    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Owner: " + user.getUsername());
        loadDashboard();
        loadAllData();
    }
    
    @FXML
    private void initialize() {
        setupProductTable();
        // Diğer tablo kurulumları...
        if(orderStatusFilter != null)
            orderStatusFilter.getItems().addAll("All", "Pending", "Assigned", "Completed");
    }
    
    private void setupProductTable() {
        // Tablo kolonlarını bağla
        if (!productsTable.getColumns().isEmpty()) {
            productsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
            productsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
            productsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("type"));
            productsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("price"));
            productsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("stock"));
            productsTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("threshold"));
        }
    }

    private void loadDashboard() {
        List<Product> products = ProductDAO.getAllProducts();
        totalProductsLabel.setText(String.valueOf(products.size()));
        activeOrdersLabel.setText("5"); 
        totalRevenueLabel.setText("₺1250.00");
    }
    
    private void loadAllData() {
        List<Product> products = ProductDAO.getAllProducts();
        ObservableList<Product> prodList = FXCollections.observableArrayList(products);
        productsTable.setItems(prodList);
        statusLabel.setText("Data refreshed.");
    }
    
    // --- YENİ ÜRÜN EKLEME (RESİM SEÇMELİ) ---
    @FXML
    private void handleAddProduct() {
        // 1. Dialog Oluştur
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Yeni Ürün Ekle");
        dialog.setHeaderText("Ürün detaylarını giriniz ve resim seçiniz.");

        // Butonlar
        ButtonType saveButtonType = new ButtonType("Kaydet", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 2. Form Alanları
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Ürün Adı");
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("vegetable", "fruit");
        typeCombo.setValue("vegetable");

        TextField priceField = new TextField();
        priceField.setPromptText("Fiyat (TL)");

        TextField stockField = new TextField();
        stockField.setPromptText("Stok (kg)");

        TextField thresholdField = new TextField();
        thresholdField.setPromptText("Eşik Değer (kg)");

        // Resim Seçme Butonu
        Button selectImageBtn = new Button("Resim Seç...");
        Label imagePathLabel = new Label("Resim seçilmedi");
        
        // Resim dosyasını tutacak değişken (Wrapper class içinde çünkü lambda içinde kullanacağız)
        final File[] selectedImageFile = {null};

        selectImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Ürün Resmini Seç");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                selectedImageFile[0] = file;
                imagePathLabel.setText(file.getName());
                imagePathLabel.setStyle("-fx-text-fill: green;");
            }
        });

        grid.add(new Label("Ad:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Tip:"), 0, 1); grid.add(typeCombo, 1, 1);
        grid.add(new Label("Fiyat:"), 0, 2); grid.add(priceField, 1, 2);
        grid.add(new Label("Stok:"), 0, 3); grid.add(stockField, 1, 3);
        grid.add(new Label("Eşik:"), 0, 4); grid.add(thresholdField, 1, 4);
        grid.add(new Label("Resim:"), 0, 5); grid.add(selectImageBtn, 1, 5);
        grid.add(imagePathLabel, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // 3. Sonucu İşle
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText();
                    String type = typeCombo.getValue();
                    double price = Double.parseDouble(priceField.getText());
                    double stock = Double.parseDouble(stockField.getText());
                    double threshold = Double.parseDouble(thresholdField.getText());
                    
                    // DAO'ya gönder
                    boolean success = ProductDAO.addProduct(name, type, price, stock, threshold, selectedImageFile[0]);
                    
                    if (success) {
                        return new Product(); // Başarılı (Dummy obje döndür)
                    }
                } catch (Exception e) {
                    showAlert("Hata", "Lütfen değerleri doğru giriniz.\n" + e.getMessage());
                }
            }
            return null;
        });

        Optional<Product> result = dialog.showAndWait();
        if (result.isPresent()) {
            loadAllData(); // Tabloyu yenile
            showAlert("Başarılı", "Ürün başarıyla eklendi!");
        }
    }
    
    // --- ÜRÜN SİLME ---
    @FXML
    private void handleRemoveProduct() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen silinecek ürünü tablodan seçin.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, selected.getName() + " silinsin mi?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            if (ProductDAO.deleteProduct(selected.getId())) {
                loadAllData();
                statusLabel.setText("Product deleted.");
            } else {
                showAlert("Hata", "Silme işlemi başarısız.");
            }
        }
    }
    
    @FXML
    private void handleRefreshOrders() {
        loadAllData();
        statusLabel.setText("Data refreshed.");
    }
    
    @FXML
    private void handleLogout() {
        try {
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("GreenGrocer Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // Boş Handlerlar (Hata almamak için)
    @FXML private void handleUpdateProduct() {}
    @FXML private void handleSearchProducts() {}
    @FXML private void handleHireCarrier() {}
    @FXML private void handleFireCarrier() {}
    @FXML private void handleViewCarrierRatings() {}
    @FXML private void handleRefreshMessages() {}
    @FXML private void handleReplyMessage() {}
    @FXML private void handleDeleteMessage() {}
    @FXML private void handleAddCoupon() {}
    @FXML private void handleSaveLoyaltySettings() {}
    @FXML private void handleGenerateReport() {}
    @FXML private void handleExportReport() {}

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}