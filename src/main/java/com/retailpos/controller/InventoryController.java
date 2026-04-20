package com.retailpos.controller;

import com.retailpos.MainApp;
import com.retailpos.dao.ProductDAO;
import com.retailpos.model.Product;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.util.Optional;

public class InventoryController {
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colBarcode, colName, colCategory, colTax;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TextField searchField;
    @FXML private Label statusLabel, totalProductsLabel, lowStockLabel;
    @FXML private Button addBtn, editBtn, deleteBtn;

    private final ProductDAO productDAO = ProductDAO.getInstance();
    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadProducts();

        if (!MainApp.getCurrentUser().isAdmin()) {
            addBtn.setDisable(true);
            editBtn.setDisable(true);
            deleteBtn.setDisable(true);
        }
        searchField.textProperty().addListener((obs, o, n) -> filterProducts(n));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQty"));
        colTax.setCellValueFactory(new PropertyValueFactory<>("taxRatePercent"));

        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("₹%.2f", v));
            }
        });

        colStock.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v.toString());
                setStyle(v <= 5 ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                         v <= 20 ? "-fx-text-fill: #f39c12;" : "-fx-text-fill: #27ae60;");
            }
        });

        productTable.setItems(productList);
    }

    private void loadProducts() {
        productList.setAll(productDAO.getAllProducts());
        updateStats();
    }

    private void filterProducts(String query) {
        if (query.isEmpty()) { loadProducts(); return; }
        productList.setAll(productDAO.searchProducts(query));
    }

    private void updateStats() {
        totalProductsLabel.setText("Total: " + productList.size());
        long low = productList.stream().filter(p -> p.getStockQty() <= 5).count();
        lowStockLabel.setText("Low Stock: " + low);
        if (low > 0) lowStockLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }

    @FXML
    private void handleAdd() {
        showProductDialog(null).ifPresent(p -> {
            if (productDAO.addProduct(p)) { loadProducts(); showStatus("Product added successfully!", false); }
            else showStatus("Failed to add product (barcode may exist).", true);
        });
    }

    @FXML
    private void handleEdit() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a product to edit.", true); return; }
        showProductDialog(selected).ifPresent(p -> {
            if (productDAO.updateProduct(p)) { loadProducts(); showStatus("Product updated!", false); }
            else showStatus("Failed to update product.", true);
        });
    }

    @FXML
    private void handleDelete() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a product to delete.", true); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete product: " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (productDAO.deleteProduct(selected.getId())) { loadProducts(); showStatus("Product deleted.", false); }
            else showStatus("Cannot delete product (may be in active sales).", true);
        }
    }

    @FXML private void handleRefresh() { loadProducts(); showStatus("Refreshed!", false); }

    private Optional<Product> showProductDialog(Product existing) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add New Product" : "Edit Product");
        dialog.setHeaderText(existing == null ? "Enter product details:" : "Edit product: " + existing.getName());
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField barcodeF = new TextField(existing != null ? existing.getBarcode() : "");
        TextField nameF = new TextField(existing != null ? existing.getName() : "");
        TextField categoryF = new TextField(existing != null ? existing.getCategory() : "");
        TextField priceF = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "");
        TextField stockF = new TextField(existing != null ? String.valueOf(existing.getStockQty()) : "0");
        TextField taxF = new TextField(existing != null ? String.valueOf(existing.getTaxRate() * 100) : "18");

        grid.add(new Label("Barcode:"), 0, 0); grid.add(barcodeF, 1, 0);
        grid.add(new Label("Name:"), 0, 1); grid.add(nameF, 1, 1);
        grid.add(new Label("Category:"), 0, 2); grid.add(categoryF, 1, 2);
        grid.add(new Label("Price (₹):"), 0, 3); grid.add(priceF, 1, 3);
        grid.add(new Label("Stock Qty:"), 0, 4); grid.add(stockF, 1, 4);
        grid.add(new Label("Tax Rate (%):"), 0, 5); grid.add(taxF, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                Product p = existing != null ? existing : new Product();
                p.setBarcode(barcodeF.getText().trim());
                p.setName(nameF.getText().trim());
                p.setCategory(categoryF.getText().trim());
                p.setPrice(Double.parseDouble(priceF.getText().trim()));
                p.setStockQty(Integer.parseInt(stockF.getText().trim()));
                p.setTaxRate(Double.parseDouble(taxF.getText().trim()) / 100.0);
                return p;
            } catch (Exception e) { return null; }
        });

        return dialog.showAndWait();
    }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
