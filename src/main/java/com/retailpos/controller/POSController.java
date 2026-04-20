package com.retailpos.controller;

import com.retailpos.MainApp;
import com.retailpos.dao.ProductDAO;
import com.retailpos.dao.SaleDAO;
import com.retailpos.factory.PaymentFactory;
import com.retailpos.model.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class POSController {
    @FXML private TextField searchField, barcodeField, cashReceivedField;
    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> colProduct, colBarcode;
    @FXML private TableColumn<SaleItem, Integer> colQty;
    @FXML private TableColumn<SaleItem, Double> colPrice, colSubtotal;
    @FXML private TableColumn<SaleItem, Void> colAction;
    @FXML private FlowPane productGrid;
    @FXML private Label subtotalLabel, taxLabel, totalLabel, changeLabel, transactionLabel;
    @FXML private Label statusLabel, dateTimeLabel;
    @FXML private ToggleGroup paymentToggle;
    @FXML private RadioButton cashRadio, upiRadio, cardRadio;
    @FXML private VBox cashBox;
    @FXML private ScrollPane productScroll;

    private final ProductDAO productDAO = ProductDAO.getInstance();
    private final SaleDAO saleDAO = new SaleDAO();
    private final ObservableList<SaleItem> cartItems = FXCollections.observableArrayList();
    private Sale currentSale = new Sale();

    @FXML
    public void initialize() {
        setupTable();
        setupPaymentToggle();
        loadProducts("");
        updateDateTime();
        updateTotals();

        searchField.textProperty().addListener((obs, o, n) -> loadProducts(n));
        barcodeField.setOnAction(e -> scanBarcode());
    }

    private void setupTable() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("productBarcode"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        // Format price columns
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("₹%.2f", v));
            }
        });
        colSubtotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("₹%.2f", v));
            }
        });

        // Action column with +/- and delete buttons
        colAction.setCellFactory(tc -> new TableCell<>() {
            final Button minusBtn = new Button("−");
            final Button plusBtn = new Button("+");
            final Button delBtn = new Button("🗑");
            final HBox box = new HBox(4, minusBtn, plusBtn, delBtn);

            {
                minusBtn.getStyleClass().add("qty-btn");
                plusBtn.getStyleClass().add("qty-btn");
                delBtn.getStyleClass().addAll("qty-btn", "delete-btn");
                box.setPadding(new Insets(2));

                minusBtn.setOnAction(e -> {
                    SaleItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                    } else {
                        removeFromCart(item);
                        return;
                    }
                    cartItems.set(getIndex(), item);
                    updateTotals();
                });
                plusBtn.setOnAction(e -> {
                    SaleItem item = getTableView().getItems().get(getIndex());
                    if (productDAO.checkAvailability(item.getProduct().getId(), item.getQuantity() + 1)) {
                        item.setQuantity(item.getQuantity() + 1);
                        cartItems.set(getIndex(), item);
                        updateTotals();
                    } else {
                        showStatus("Insufficient stock!", true);
                    }
                });
                delBtn.setOnAction(e -> removeFromCart(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        cartTable.setItems(cartItems);
        cartTable.setPlaceholder(new Label("Cart is empty. Scan or click a product to add."));
    }

    private void setupPaymentToggle() {
        paymentToggle = new ToggleGroup();
        cashRadio.setToggleGroup(paymentToggle);
        upiRadio.setToggleGroup(paymentToggle);
        cardRadio.setToggleGroup(paymentToggle);
        cashRadio.setSelected(true);

        paymentToggle.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean isCash = n == cashRadio;
            cashBox.setVisible(isCash);
            cashBox.setManaged(isCash);
        });
    }

    private void loadProducts(String query) {
        productGrid.getChildren().clear();
        List<Product> products = query.isEmpty()
            ? productDAO.getAllProducts()
            : productDAO.searchProducts(query);

        for (Product p : products) {
            VBox card = createProductCard(p);
            productGrid.getChildren().add(card);
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(4);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(150);
        card.setPrefHeight(110);
        card.setPadding(new Insets(10));

        Label nameLabel = new Label(p.getName());
        nameLabel.getStyleClass().add("product-card-name");
        nameLabel.setWrapText(true);

        Label catLabel = new Label(p.getCategory());
        catLabel.getStyleClass().add("product-card-cat");

        Label priceLabel = new Label(String.format("₹%.2f", p.getPrice()));
        priceLabel.getStyleClass().add("product-card-price");

        Label stockLabel = new Label("Stock: " + p.getStockQty());
        stockLabel.getStyleClass().add("product-card-stock");
        if (p.getStockQty() <= 5) stockLabel.getStyleClass().add("low-stock");

        card.getChildren().addAll(nameLabel, catLabel, priceLabel, stockLabel);

        card.setOnMouseClicked(e -> addToCart(p));
        if (p.getStockQty() == 0) {
            card.setDisable(true);
            card.setOpacity(0.4);
        }
        return card;
    }

    @FXML
    private void scanBarcode() {
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) return;
        Product p = productDAO.findByBarcode(barcode);
        if (p != null) {
            addToCart(p);
            showStatus("Added: " + p.getName(), false);
        } else {
            showStatus("Product not found: " + barcode, true);
        }
        barcodeField.clear();
    }

    private void addToCart(Product p) {
        if (p.getStockQty() <= 0) { showStatus("Out of stock: " + p.getName(), true); return; }
        for (SaleItem item : cartItems) {
            if (item.getProduct().getId() == p.getId()) {
                if (!productDAO.checkAvailability(p.getId(), item.getQuantity() + 1)) {
                    showStatus("Max stock reached for: " + p.getName(), true); return;
                }
                item.setQuantity(item.getQuantity() + 1);
                cartTable.refresh();
                updateTotals();
                showStatus("Updated quantity: " + p.getName(), false);
                return;
            }
        }
        cartItems.add(new SaleItem(p, 1));
        updateTotals();
        showStatus("Added: " + p.getName(), false);
    }

    private void removeFromCart(SaleItem item) {
        cartItems.remove(item);
        currentSale.removeItem(item);
        updateTotals();
    }

    @FXML
    private void clearCart() {
        if (cartItems.isEmpty()) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Clear all items from cart?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            cartItems.clear();
            currentSale = new Sale();
            updateTotals();
        }
    }

    @FXML
    private void processPayment() {
        if (cartItems.isEmpty()) { showStatus("Cart is empty!", true); return; }

        currentSale = new Sale();
        currentSale.setCashierId(MainApp.getCurrentUser().getId());
        cartItems.forEach(currentSale::addItem);
        currentSale.recalculate();

        String method;
        double cashReceived = 0;
        double total = currentSale.getTotalAmount();

        if (cashRadio.isSelected()) {
            method = "CASH";
            String cashText = cashReceivedField.getText().trim();
            if (cashText.isEmpty()) { showStatus("Enter cash received amount!", true); return; }
            try {
                cashReceived = Double.parseDouble(cashText);
                if (cashReceived < total) { showStatus("Insufficient cash!", true); return; }
            } catch (NumberFormatException e) { showStatus("Invalid cash amount!", true); return; }
        } else if (upiRadio.isSelected()) {
            method = "UPI";
        } else {
            method = "CARD";
        }

        Payment payment = PaymentFactory.createPayment(method, total, cashReceived);
        boolean success = payment.processPayment();

        if (success) {
            currentSale.setPaymentMethod(payment.getPaymentMethod());
            currentSale.setPaymentStatus("COMPLETED");
            int saleId = saleDAO.saveSale(currentSale);

            if (saleId > 0) {
                currentSale.setId(saleId);
                String receiptText = generateReceiptText(currentSale, payment);
                showReceiptDialog(receiptText);
                cartItems.clear();
                cashReceivedField.clear();
                currentSale = new Sale();
                updateTotals();
                loadProducts(searchField.getText());
                showStatus("Sale #" + saleId + " completed successfully!", false);
                transactionLabel.setText("Last: Sale #" + saleId);
            } else {
                showStatus("Failed to save sale. Try again.", true);
            }
        } else {
            showStatus("Payment failed. Please retry.", true);
        }
    }

    private String generateReceiptText(Sale sale, Payment payment) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════╗\n");
        sb.append("║       RETAIL POS SYSTEM      ║\n");
        sb.append("╚══════════════════════════════╝\n");
        sb.append("Sale ID   : #").append(sale.getId()).append("\n");
        sb.append("Date      : ").append(sale.getSaleDate()).append("\n");
        sb.append("Cashier   : ").append(MainApp.getCurrentUser().getName()).append("\n");
        sb.append("──────────────────────────────\n");
        sb.append(String.format("%-18s %4s %8s\n", "ITEM", "QTY", "AMOUNT"));
        sb.append("──────────────────────────────\n");
        for (SaleItem item : sale.getItems()) {
            sb.append(String.format("%-18s %4d ₹%7.2f\n",
                item.getProductName().length() > 17 ? item.getProductName().substring(0, 17) : item.getProductName(),
                item.getQuantity(), item.getSubtotal()));
        }
        sb.append("──────────────────────────────\n");
        sb.append(String.format("%-18s      ₹%7.2f\n", "Subtotal:", sale.getSubtotal()));
        sb.append(String.format("%-18s      ₹%7.2f\n", "Tax (GST):", sale.getTaxAmount()));
        sb.append(String.format("%-18s      ₹%7.2f\n", "TOTAL:", sale.getTotalAmount()));
        sb.append("──────────────────────────────\n");
        sb.append("Payment   : ").append(payment.getPaymentMethod()).append("\n");
        if (payment instanceof CashPayment cp) {
            sb.append(String.format("Cash Rcvd  : ₹%.2f\n", cp.getCashReceived()));
            sb.append(String.format("Change     : ₹%.2f\n", cp.getChange()));
        } else if (payment instanceof OnlinePayment op) {
            sb.append("Txn ID    : ").append(op.getTransactionId()).append("\n");
        }
        sb.append("──────────────────────────────\n");
        sb.append("     Thank you! Visit again!   \n");
        sb.append("══════════════════════════════\n");
        return sb.toString();
    }

    private void showReceiptDialog(String receiptText) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Payment Receipt");
        dialog.setHeaderText("Sale Completed Successfully ✓");
        TextArea ta = new TextArea(receiptText);
        ta.setEditable(false);
        ta.setFont(javafx.scene.text.Font.font("Monospaced", 13));
        ta.setPrefRowCount(25);
        ta.setPrefColumnCount(35);
        dialog.getDialogPane().setContent(ta);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.showAndWait();
    }

    private void updateTotals() {
        currentSale = new Sale();
        cartItems.forEach(currentSale::addItem);
        subtotalLabel.setText(String.format("₹%.2f", currentSale.getSubtotal()));
        taxLabel.setText(String.format("₹%.2f", currentSale.getTaxAmount()));
        totalLabel.setText(String.format("₹%.2f", currentSale.getTotalAmount()));

        // Update change
        try {
            double cash = Double.parseDouble(cashReceivedField.getText().trim());
            double change = cash - currentSale.getTotalAmount();
            changeLabel.setText(change >= 0 ? String.format("₹%.2f", change) : "—");
        } catch (NumberFormatException e) { changeLabel.setText("—"); }
    }

    @FXML private void updateChange() { updateTotals(); }

    private void updateDateTime() {
        dateTimeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm")));
    }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-error", "status-ok");
        statusLabel.getStyleClass().add(error ? "status-error" : "status-ok");
    }
}
