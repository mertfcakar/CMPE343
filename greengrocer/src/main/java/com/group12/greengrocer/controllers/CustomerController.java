package com.group12.greengrocer.controllers;

import com.group12.greengrocer.database.MessageDAO;
import com.group12.greengrocer.database.OrderDAO;
import com.group12.greengrocer.database.ProductDAO;
import com.group12.greengrocer.database.UserDAO;
import com.group12.greengrocer.models.Message;
import com.group12.greengrocer.models.Order;
import com.group12.greengrocer.models.Product;
import com.group12.greengrocer.models.User;
import com.group12.greengrocer.utils.ShoppingCart;

import javafx.animation.FadeTransition;
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
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// PDF için
import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;

// FileChooser için
import javafx.stage.FileChooser;

public class CustomerController {

    private User currentUser;
    private List<Product> allProducts;

    // ANA EKRAN
    @FXML
    private BorderPane mainContent;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label cartItemsLabel;
    @FXML
    private FlowPane vegetablesFlowPane;
    @FXML
    private FlowPane fruitsFlowPane;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortComboBox; // YENİ: Sıralama Kutusu

    // OVERLAYS
    @FXML
    private StackPane overlayContainer;
    @FXML
    private VBox ordersOverlay;
    @FXML
    private VBox profileOverlay;
    @FXML
    private HBox chatOverlay; // YENİ: Chat artık HBox (Sol Menu + Sağ İçerik)
    @FXML
    private VBox orderDetailOverlay;

    // SİPARİŞ TABLOSU
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> colId;
    @FXML
    private TableColumn<Order, String> colDate;
    @FXML
    private TableColumn<Order, Double> colTotal;
    @FXML
    private TableColumn<Order, String> colStatus;
    @FXML
    private TableColumn<Order, String> colItems;
    @FXML
    private TableColumn<Order, Void> colAction;

    // DETAY
    @FXML
    private VBox orderDetailContainer;
    @FXML
    private Label detailOrderIdLabel;

    // PROFİL
    @FXML
    private TextField editAddressField;
    @FXML
    private TextField editEmailField;
    @FXML
    private TextField editPhoneField;
    @FXML
    private PasswordField editPasswordField;

    // CHAT (GELİŞMİŞ)
    @FXML
    private ListView<String> chatTopicsList; // Konu Başlıkları
    @FXML
    private VBox chatMessagesBox;
    @FXML
    private TextField chatInput;
    @FXML
    private ScrollPane chatScroll;
    @FXML
    private Label chatCurrentTopicLabel;

    private String currentChatSubject = "Genel Destek"; // Varsayılan Konu

    public void initData(User user) {
        this.currentUser = user;
        usernameLabel.setText("Merhaba, " + user.getUsername());
        ShoppingCart.getInstance().setCurrentUser(user);

        // Sıralama Seçeneklerini Yükle
        if (sortComboBox != null) {
            sortComboBox.getItems().addAll("Varsayılan (A-Z)", "İsim (Z-A)", "Fiyat (Artan)", "Fiyat (Azalan)");
            sortComboBox.setValue("Varsayılan (A-Z)");
            sortComboBox.setOnAction(e -> handleSortProducts());
        }

        loadProducts();
        updateCartLabel();
        closeAllOverlays();

        // Chat konusunu seçince mesajları yükle
        if (chatTopicsList != null) {
            chatTopicsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    currentChatSubject = newVal;
                    loadChatMessages(newVal);
                }
            });
        }
    }

    // --- ÜRÜN YÖNETİMİ VE SIRALAMA ---
    private void loadProducts() {
        allProducts = ProductDAO.getAllProducts(); // Varsayılan A-Z gelir
        displayProducts(allProducts);
    }

    private void handleSortProducts() {
        String sortType = sortComboBox.getValue();
        if (sortType == null || allProducts == null)
            return;

        List<Product> sortedList = switch (sortType) {
            case "İsim (Z-A)" -> allProducts.stream().sorted(Comparator.comparing(Product::getName).reversed())
                    .collect(Collectors.toList());
            case "Fiyat (Artan)" -> allProducts.stream().sorted(Comparator.comparingDouble(Product::getCurrentPrice))
                    .collect(Collectors.toList());
            case "Fiyat (Azalan)" ->
                allProducts.stream().sorted(Comparator.comparingDouble(Product::getCurrentPrice).reversed())
                        .collect(Collectors.toList());
            default -> allProducts.stream().sorted(Comparator.comparing(Product::getName)).collect(Collectors.toList());
        };
        displayProducts(sortedList);
    }

    private void displayProducts(List<Product> products) {
        if (vegetablesFlowPane != null)
            vegetablesFlowPane.getChildren().clear();
        if (fruitsFlowPane != null)
            fruitsFlowPane.getChildren().clear();

        for (Product p : products) {
            VBox card = createProductCard(p);
            if ("vegetable".equalsIgnoreCase(p.getType()))
                vegetablesFlowPane.getChildren().add(card);
            else if ("fruit".equalsIgnoreCase(p.getType()))
                fruitsFlowPane.getChildren().add(card);
        }
    }

    // --- ÜRÜN KARTI ---
    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setPrefHeight(290);

        ImageView imgView = new ImageView();
        imgView.setFitHeight(110);
        imgView.setFitWidth(150);
        imgView.setPreserveRatio(true);
        if (p.getImage() != null) {
            try {
                imgView.setImage(new Image(new ByteArrayInputStream(p.getImage())));
            } catch (Exception e) {
            }
        }

        Label stockLbl = new Label(
                p.getStock() <= 0 ? "TÜKENDİ" : (p.getStock() <= p.getThreshold() ? "AZ KALDI" : "STOKTA"));
        stockLbl.setStyle("-fx-background-color: "
                + (p.getStock() <= 0 ? "#c62828" : (p.getStock() <= p.getThreshold() ? "#f57c00" : "#4caf50"))
                + "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 5; -fx-font-size: 10px; -fx-font-weight: bold;");

                // Stock miktarı göster
                Label stockAmountLbl = new Label();
                if (p.getStock() <= p.getThreshold()) {
                    stockAmountLbl.setText(String.format("Stok: %.1f kg", p.getStock()));
                    stockAmountLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " 
                    + (p.getStock() <= 0 ? "#c62828" : "#f57c00") + "; -fx-font-weight: bold;");
                }

                Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        Label priceLbl = new Label(String.format("%.2f TL / kg", p.getCurrentPrice()));
        priceLbl.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold; -fx-font-size: 14px;");
        if (p.getStock() <= p.getThreshold() && p.getStock() > 0)
            priceLbl.setText(priceLbl.getText() + " (🔥 x2 Fiyat)");

        Spinner<Double> spinner = new Spinner<>(0.5, 20.0, 1.0, 0.5);
        spinner.setEditable(true);
        spinner.setPrefWidth(70);
        Button addBtn = new Button("Ekle");
        addBtn.setStyle(
                "-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        if (p.getStock() <= 0) {
            addBtn.setDisable(true);
            spinner.setDisable(true);
        }

        addBtn.setOnAction(e -> {
            if (spinner.getValue() > p.getStock()) {
                showAlert("Hata", "Stok yetersiz!");
                return;
            }
            ShoppingCart.getInstance().addItem(p, spinner.getValue());
            updateCartLabel();
            addBtn.setText("✔");
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> addBtn.setText("Ekle"));
                }
            }, 1000);
        });

        HBox actions = new HBox(10, spinner, addBtn);
        actions.setAlignment(Pos.CENTER);
        card.getChildren().addAll(stockLbl, imgView, nameLbl, priceLbl);
        
        // Stock miktarı varsa ekle
        if (!stockAmountLbl.getText().isEmpty()) {
            card.getChildren().add(stockAmountLbl);
        }
        
        card.getChildren().addAll(new Separator(), actions);
        return card;
    }

    private void updateCartLabel() {
        if (cartItemsLabel != null)
            cartItemsLabel.setText(ShoppingCart.getInstance().getItemCount() + " ürün");
    }

    // --- OVERLAY YÖNETİMİ (SORUN ÇÖZÜLDÜ) ---
    @FXML
    public void closeAllOverlays() {
        if (overlayContainer != null)
            overlayContainer.setVisible(false);
        if (mainContent != null)
            mainContent.setEffect(null);

        // Hepsini tek tek gizle ki bir sonraki açılışta çakışma olmasın
        if (ordersOverlay != null)
            ordersOverlay.setVisible(false);
        if (profileOverlay != null)
            profileOverlay.setVisible(false);
        if (chatOverlay != null)
            chatOverlay.setVisible(false);
        if (orderDetailOverlay != null)
            orderDetailOverlay.setVisible(false);
    }

    private void openOverlay(Parent overlay) {
        closeAllOverlays(); // Önce diğerlerini kapat
        overlayContainer.setVisible(true);
        mainContent.setEffect(new BoxBlur(10, 10, 3));
        overlay.setVisible(true);

        overlay.setOpacity(0);
        overlay.setScaleX(0.95);
        overlay.setScaleY(0.95);
        FadeTransition ft = new FadeTransition(Duration.millis(250), overlay);
        ft.setToValue(1);
        ft.play();
    }

    // --- SİPARİŞLER ---
    @FXML
    private void handleViewOrders() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colItems.setCellValueFactory(cell -> new SimpleStringProperty(
                String.join(", ", OrderDAO.getOrderItemsAsText(cell.getValue().getId()))));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRate = new Button("⭐");
            private final Button btnDetail = new Button("📄 Detay");
            private final Button btnHelp = new Button("❓"); // Destek Butonu
            private final Button btnCancel = new Button("❌ İptal"); // İptal Butonu
            private final HBox pane = new HBox(5, btnDetail, btnHelp, btnRate);

            {
                btnRate.setStyle(
                        "-fx-background-color: #ff9800; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 10px;");
                btnDetail.setStyle(
                        "-fx-background-color: #2196f3; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 10px;");
                btnHelp.setStyle(
                        "-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 10px;");
                btnCancel.setStyle(
                        "-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 10px;");
                pane.setAlignment(Pos.CENTER);

                btnRate.setOnAction(e -> CustomerController.this.handleRateOrder(getTableView().getItems().get(getIndex())));
                btnDetail.setOnAction(e -> CustomerController.this.showOrderDetails(getTableView().getItems().get(getIndex())));
                btnCancel.setOnAction(e -> CustomerController.this.handleCancelOrder(getTableView().getItems().get(getIndex())));

                // Sipariş için destek başlatma
                btnHelp.setOnAction(e -> {
                    Order o = getTableView().getItems().get(getIndex());
                    CustomerController.this.openChatWithTopic("Sipariş #" + o.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else {
                    Order order = getTableView().getItems().get(getIndex());
                    if ("completed".equalsIgnoreCase(order.getStatus())) {
                        pane.getChildren().setAll(btnDetail, btnHelp, btnRate);
                    } else if ("pending".equalsIgnoreCase(order.getStatus()) && canCancelOrder(order)) {
                        pane.getChildren().setAll(btnDetail, btnCancel, btnHelp);
                    } else {
                        pane.getChildren().setAll(btnDetail, btnHelp);
                    }
                    setGraphic(pane);
                }
            }
        });

        ordersTable.setItems(FXCollections.observableArrayList(OrderDAO.getOrdersByUserId(currentUser.getId())));
        openOverlay(ordersOverlay);
    }

    private void showOrderDetails(Order order) {
        detailOrderIdLabel.setText("Sipariş #" + order.getId() + " Detayları");
        orderDetailContainer.getChildren().clear();

        List<OrderDAO.OrderDetail> details = OrderDAO.getOrderDetailsWithImages(order.getId());

        for (OrderDAO.OrderDetail item : details) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            // Arka planı hafif gri yaptık, çerçeve ekledik
            row.setStyle(
                    "-fx-background-color: #f9f9f9; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #e0e0e0;");

            // 1. Resim
            ImageView imgView = new ImageView();
            imgView.setFitHeight(50);
            imgView.setFitWidth(50);
            imgView.setPreserveRatio(true);

            if (item.image != null && item.image.length > 0) {
                try {
                    imgView.setImage(new Image(new ByteArrayInputStream(item.image)));
                } catch (Exception e) {
                }
            }

            StackPane imgContainer = new StackPane(imgView);
            imgContainer.setPrefSize(50, 50);
            imgContainer.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5;");

            // 2. Bilgiler (RENK DÜZELTMESİ BURADA)
            VBox info = new VBox(3);
            Label nameLbl = new Label(item.name);
            // YAZI RENGİNİ SİYAH (#333) YAPTIK
            nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");

            Label qtyLbl = new Label(String.format("%.2f kg x %.2f TL", item.quantity, item.unitPrice));
            // YAZI RENGİNİ KOYU GRİ (#666) YAPTIK
            qtyLbl.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

            info.getChildren().addAll(nameLbl, qtyLbl);

            // 3. Fiyat
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label priceLbl = new Label(String.format("%.2f TL", item.totalPrice));
            priceLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size: 14px;");

            row.getChildren().addAll(imgContainer, info, spacer, priceLbl);
            orderDetailContainer.getChildren().add(row);
        }

        // PDF Butonu - Sadece tamamlanmış siparişler için
        if ("completed".equalsIgnoreCase(order.getStatus())) {
            HBox pdfBox = new HBox(10);
            pdfBox.setAlignment(Pos.CENTER);
            pdfBox.setPadding(new Insets(10, 0, 0, 0));

            Button pdfBtn = new Button("📄 PDF Faturayı İndir");
            pdfBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
            pdfBtn.setOnAction(e -> {
                byte[] pdfBytes = OrderDAO.getInvoicePDF(order.getId());
                if (pdfBytes != null && pdfBytes.length > 0) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("PDF Faturayı Kaydet");
                    fileChooser.setInitialFileName("fatura_" + order.getId() + ".pdf");
                    fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                    );
                    
                    // Geçerli stage'i bul
                    Stage stage = (Stage) pdfBtn.getScene().getWindow();
                    java.io.File selectedFile = fileChooser.showSaveDialog(stage);
                    
                    if (selectedFile != null) {
                        try {
                            Files.write(selectedFile.toPath(), pdfBytes);
                            showAlert("Başarılı", "PDF başarıyla kaydedildi: " + selectedFile.getAbsolutePath());
                            
                            // Kaydedilen PDF'i aç
                            Desktop.getDesktop().open(selectedFile);
                        } catch (Exception ex) {
                            showAlert("Hata", "PDF kaydedilemedi: " + ex.getMessage());
                        }
                    }
                } else {
                    showAlert("Hata", "PDF fatura bulunamadı.");
                }
            });

            pdfBox.getChildren().add(pdfBtn);
            orderDetailContainer.getChildren().add(pdfBox);
        }

        openOverlay(orderDetailOverlay);
    }

    // --- GELİŞMİŞ CHAT SİSTEMİ (KONULU) ---
    @FXML
    private void handleOpenChat() {
        refreshChatTopics();
        if (chatTopicsList.getItems().isEmpty()) {
            chatTopicsList.getItems().add("Genel Destek");
        }
        chatTopicsList.getSelectionModel().selectFirst();
        openOverlay(chatOverlay);
    }

    // Siparişlerim'den tıklandığında direkt o konuyu açar
    private void openChatWithTopic(String topic) {
        refreshChatTopics();
        if (!chatTopicsList.getItems().contains(topic)) {
            chatTopicsList.getItems().add(0, topic); // Listeye ekle
        }
        chatTopicsList.getSelectionModel().select(topic);
        openOverlay(chatOverlay);
    }

    private void refreshChatTopics() {
        int ownerId = UserDAO.getOwnerId();
        // Tüm mesajları çekip tarihlerine göre gruplayacağız
        List<Message> allMsgs = MessageDAO.getConversation(currentUser.getId(), ownerId);

        chatTopicsList.getItems().clear();

        if (allMsgs.isEmpty()) {
            chatTopicsList.getItems().add("Genel Destek");
        } else {
            // Mesajları Konuya Göre Grupla
            Map<String, List<Message>> grouped = allMsgs.stream()
                    .collect(Collectors.groupingBy(Message::getSubject));

            for (Map.Entry<String, List<Message>> entry : grouped.entrySet()) {
                String subject = entry.getKey();
                List<Message> msgs = entry.getValue();

                // O konudaki en son mesajın tarihini bul
                String lastDate = "";
                if (!msgs.isEmpty()) {
                    Message lastMsg = msgs.get(msgs.size() - 1);
                    // Tarihi sadece Gün.Ay Saat:Dakika olarak al (YYYY-MM-DD HH:MM:SS -> kesiyoruz)
                    String fullDate = lastMsg.getCreatedAt().toString();
                    if (fullDate.length() > 16)
                        lastDate = fullDate.substring(5, 16);
                }

                // Listeye ekle: "Konu Başlığı (01-01 14:30)"
                chatTopicsList.getItems().add(subject + " (" + lastDate + ")");
            }
        }
    }

    private void loadChatMessages(String selection) {
        if (selection == null)
            return;

        // Seçilen satırdan tarihi temizle "Konu (Tarih)" -> "Konu"
        String subject = selection;
        if (selection.contains(" (")) {
            subject = selection.substring(0, selection.lastIndexOf(" ("));
        }

        chatCurrentTopicLabel.setText(subject);
        currentChatSubject = subject; // Yeni mesaj atarken kullanılacak konu

        chatMessagesBox.getChildren().clear();
        int ownerId = UserDAO.getOwnerId();
        List<Message> msgs = MessageDAO.getConversation(currentUser.getId(), ownerId);

        for (Message m : msgs) {
            if (m.getSubject().equalsIgnoreCase(subject)) {
                addMessageBubble(m.getContent(), m.getSenderId() == currentUser.getId());
            }
        }

        // Scroll'u en aşağı kaydır (biraz gecikmeli ki layout otursun)
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> chatScroll.setVvalue(1.0));
            }
        }, 100);
    }

    @FXML
    private void sendMessage() {
        String txt = chatInput.getText().trim();
        if (txt.isEmpty())
            return;
        int ownerId = UserDAO.getOwnerId();

        // Mesajı seçili konu başlığı (Subject) ile gönderiyoruz
        if (MessageDAO.sendMessage(currentUser.getId(), ownerId, currentChatSubject, txt)) {
            addMessageBubble(txt, true);
            chatInput.clear();
            chatScroll.setVvalue(1.0);
        }
    }

    private void addMessageBubble(String text, boolean isMe) {
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setMaxWidth(350); // Genişlik arttırıldı
        lbl.setPadding(new Insets(10, 15, 10, 15));
        
        // Font ayarı
        lbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        HBox box = new HBox();
        
        if (isMe) {
            // BENİM MESAJIM (SAĞ) - Açık Yeşil (#dcf8c6)
            lbl.setStyle("-fx-background-color: #dcf8c6; " +
                         "-fx-background-radius: 15 15 0 15; " + // Sağ alt köşe sivri
                         "-fx-text-fill: black; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");
            box.setAlignment(Pos.CENTER_RIGHT);
            box.getChildren().add(lbl);
        } else {
            // KARŞI TARAFIN MESAJI (SOL) - Beyaz (#ffffff)
            lbl.setStyle("-fx-background-color: #ffffff; " +
                         "-fx-background-radius: 15 15 15 0; " + // Sol alt köşe sivri
                         "-fx-text-fill: black; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");
            box.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(lbl);
        }
        
        // Balonlar arasına biraz boşluk bırak
        box.setPadding(new Insets(0, 0, 5, 0));
        chatMessagesBox.getChildren().add(box);
        
        // Otomatik aşağı kaydır
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override public void run() { 
                javafx.application.Platform.runLater(() -> chatScroll.setVvalue(1.0)); 
            } 
        }, 100);
    }

    @FXML
    private void handleNewTicket() {
        // 1. Kullanıcının Siparişlerini Çek
        List<Order> myOrders = OrderDAO.getOrdersByUserId(currentUser.getId());
        
        // 2. Seçenek Listesi (Genel Destek ARTIK YOK)
        java.util.List<String> choices = new java.util.ArrayList<>();
        
        if (myOrders.isEmpty()) {
            showAlert("Bilgi", "Henüz bir siparişiniz yok. Lütfen 'Genel Destek' bölümünü kullanın.");
            return;
        }

        for (Order o : myOrders) {
            choices.add("Sipariş #" + o.getId() + " (" + o.getOrderTime().toString().substring(0, 10) + ")");
        }

        // 3. Seçim Diyaloğu
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Yeni Destek Talebi");
        dialog.setHeaderText("Hangi siparişinizle ilgili sorun yaşıyorsunuz?");
        dialog.setContentText("Sipariş Seçiniz:");

        dialog.showAndWait().ifPresent(selectedSubject -> {
            if (!selectedSubject.trim().isEmpty()) {
                currentChatSubject = selectedSubject;
                
                // Listeyi yenile
                refreshChatTopics();
                
                // Eğer konu listede yoksa ekle (Tarihsiz olarak ekle, refresh düzeltecek)
                String listItem = selectedSubject; 
                boolean exists = false;
                
                // Listede var mı kontrol et (Tarihli formatla eşleşiyor mu?)
                for(String s : chatTopicsList.getItems()) {
                    if(s.startsWith(selectedSubject)) {
                        chatTopicsList.getSelectionModel().select(s);
                        exists = true;
                        break;
                    }
                }
                
                if (!exists) {
                    chatTopicsList.getItems().add(0, listItem);
                    chatTopicsList.getSelectionModel().select(listItem);
                }
                
                chatMessagesBox.getChildren().clear(); 
                chatCurrentTopicLabel.setText(currentChatSubject);
                // Chat penceresini aç
                openOverlay(chatOverlay);
            }
        });
    }

    @FXML
    private void handleDeleteChat() {
        if (currentChatSubject == null || currentChatSubject.isEmpty()) return;

        // Genel Destek sohbeti silinemez
        if ("Genel Destek".equals(currentChatSubject)) {
            showAlert("Bilgi", "Genel Destek sohbeti silinemez. Bu, genel sorularınız için ayrılmıştır.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sohbeti Sil");
        alert.setHeaderText("Bu konuya ait tüm mesajlar silinecek!");
        alert.setContentText("Konu: " + currentChatSubject + "\nOnaylıyor musunuz?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            boolean success = MessageDAO.deleteChatTopic(currentUser.getId(), currentChatSubject);
            if (success) {
                chatMessagesBox.getChildren().clear();
                refreshChatTopics(); // Listeden kaldırır
                showAlert("Başarılı", "Sohbet geçmişi silindi.");
                
                // Eğer liste boş kaldıysa Genel Destek ekle
                if (chatTopicsList.getItems().isEmpty()) {
                    chatTopicsList.getItems().add("Genel Destek");
                    chatTopicsList.getSelectionModel().selectFirst();
                } else {
                    chatTopicsList.getSelectionModel().selectFirst();
                }
            } else {
                showAlert("Hata", "Silme işlemi başarısız veya zaten boş.");
            }
        }
    }

    // --- DİĞER FONKSİYONLAR ---
    @FXML
    private void handleEditProfile() {
        editAddressField.setText(currentUser.getAddress());
        editEmailField.setText(currentUser.getEmail());
        editPhoneField.setText(currentUser.getPhoneNumber());
        openOverlay(profileOverlay);
    }

    @FXML
    private void saveProfile() {
        if (UserDAO.updateUserProfile(currentUser.getId(), editAddressField.getText(), editEmailField.getText(),
                editPhoneField.getText(),
                editPasswordField.getText().isEmpty() ? currentUser.getPassword() : editPasswordField.getText())) {
            showAlert("Başarılı", "Profil güncellendi.");
            closeAllOverlays();
        } else
            showAlert("Hata", "Güncelleme başarısız.");
    }

    private boolean canCancelOrder(Order order) {
        if (order.getOrderTime() == null) return false;
        LocalDateTime orderTime = order.getOrderTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        return orderTime.isAfter(now.minusHours(1)); // 1 saat içinde
    }

    private void handleCancelOrder(Order order) {
        if (!canCancelOrder(order)) {
            showAlert("Hata", "Sipariş artık iptal edilemez (1 saat geçti).");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sipariş İptali");
        alert.setHeaderText("Sipariş #" + order.getId() + " iptal edilsin mi?");
        alert.setContentText("Stoklar geri yüklenecek.");
        if (alert.showAndWait().get() == ButtonType.OK) {
            if (OrderDAO.cancelOrder(order.getId())) {
                showAlert("Başarılı", "Sipariş iptal edildi.");
                handleViewOrders(); // Listeyi yenile
            } else {
                showAlert("Hata", "İptal başarısız.");
            }
        }
    }

    private void handleRateOrder(Order order) {
        List<String> ratings = List.of("1", "2", "3", "4", "5");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("5", ratings);
        dialog.setTitle("Kuryeyi Değerlendir");
        dialog.setHeaderText("Sipariş #" + order.getId() + " için kuryeyi değerlendirin");
        dialog.setContentText("Puan (1-5):");
        dialog.showAndWait().ifPresent(rating -> {
            int rate = Integer.parseInt(rating);
            if (OrderDAO.rateOrder(order.getId(), rate)) {
                showAlert("Başarılı", "Değerlendirme kaydedildi.");
            } else {
                showAlert("Hata", "Değerlendirme başarısız.");
            }
        });
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().toLowerCase();
        displayProducts(q.isEmpty() ? allProducts
                : allProducts.stream().filter(p -> p.getName().toLowerCase().contains(q)).collect(Collectors.toList()));
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        displayProducts(allProducts);
    }

    @FXML
    private void handleViewCart() {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/shopping_cart.fxml"));
            Parent r = l.load();
            Stage s = new Stage();
            s.setTitle("Sepetim");
            s.setScene(new Scene(r));
            s.show();
            s.setOnHidden(e -> updateCartLabel());
        } catch (Exception e) {
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // Mevcut pencereyi al
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            
            // Login sayfasını yükle
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            
            // Login sahnesini 1280x800 boyutuyla oluştur
            Scene scene = new Scene(root, 1200, 900);
            
            stage.setScene(scene);
            stage.setMaximized(false); // Login ekranı pencere modunda olsun (ama büyük)
            stage.setWidth(1200);
            stage.setHeight(900);
            stage.centerOnScreen(); // Ortala
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setContentText(c);
        a.showAndWait();
    }
}