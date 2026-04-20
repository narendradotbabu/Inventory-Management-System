package com.retailpos.controller;

import com.retailpos.dao.ProductDAO;
import com.retailpos.dao.ReturnDAO;
import com.retailpos.dao.SaleDAO;
import com.retailpos.model.Product;
import com.retailpos.model.ReturnItem;
import com.retailpos.model.Sale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReturnsController {
    @FXML private TextField saleIdField;
    @FXML private TextField productIdField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> returnTypeCombo;
    @FXML private TextField refundAmountField;
    @FXML private Label statusLabel;
    @FXML private TableView<ReturnItem> returnsTable;
    @FXML private TableColumn<ReturnItem, Integer> colId;
    @FXML private TableColumn<ReturnItem, Integer> colSaleId;
    @FXML private TableColumn<ReturnItem, String> colProduct;
    @FXML private TableColumn<ReturnItem, Integer> colQty;
    @FXML private TableColumn<ReturnItem, String> colType;
    @FXML private TableColumn<ReturnItem, Double> colRefund;
    @FXML private TableColumn<ReturnItem, String> colDate;
    @FXML private TableColumn<ReturnItem, String> colStatus;

    private final ReturnDAO returnDAO = new ReturnDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final ProductDAO productDAO = ProductDAO.getInstance();
    private final ObservableList<ReturnItem> returns = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        returnTypeCombo.getItems().addAll("REFUND", "EXCHANGE");
        returnTypeCombo.setValue("REFUND");
        loadReturns();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSaleId.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colType.setCellValueFactory(new PropertyValueFactory<>("returnType"));
        colRefund.setCellValueFactory(new PropertyValueFactory<>("refundAmount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRefund.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("Rs. %.2f", value));
            }
        });
        returnsTable.setItems(returns);
    }

    private void loadReturns() {
        returns.setAll(returnDAO.getAllReturns());
    }

    @FXML
    private void handleProcessReturn() {
        try {
            int saleId = Integer.parseInt(saleIdField.getText().trim());
            int productId = Integer.parseInt(productIdField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                showStatus("Quantity must be greater than zero.", true);
                return;
            }

            Sale sale = saleDAO.getSaleById(saleId);
            if (sale == null) {
                showStatus("Sale ID not found.", true);
                return;
            }

            Product product = productDAO.findById(productId);
            if (product == null) {
                showStatus("Product ID not found.", true);
                return;
            }

            int soldQty = saleDAO.getSoldQuantityForProduct(saleId, productId);
            if (soldQty <= 0) {
                showStatus("That product was not part of the selected sale.", true);
                return;
            }

            int alreadyReturned = returnDAO.getReturnedQuantityForProduct(saleId, productId);
            int remainingQty = soldQty - alreadyReturned;
            if (quantity > remainingQty) {
                showStatus("Only " + remainingQty + " item(s) can still be returned for this sale.", true);
                return;
            }

            String returnType = returnTypeCombo.getValue();
            if (returnType == null || returnType.isBlank()) {
                showStatus("Select a return type.", true);
                return;
            }

            double refundAmount;
            String refundText = refundAmountField.getText().trim();
            if (refundText.isEmpty()) {
                refundAmount = quantity * product.getPriceWithTax();
            } else {
                refundAmount = Double.parseDouble(refundText);
                if (refundAmount < 0) {
                    showStatus("Refund amount cannot be negative.", true);
                    return;
                }
            }

            ReturnItem item = new ReturnItem();
            item.setSaleId(saleId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setReturnType(returnType);
            item.setReturnDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            item.setRefundAmount(refundAmount);
            item.setStatus("APPROVED");

            if (returnDAO.saveReturn(item)) {
                clearForm();
                loadReturns();
                showStatus("Return processed for " + product.getName() + ".", false);
            } else {
                showStatus("Failed to save return.", true);
            }
        } catch (NumberFormatException e) {
            showStatus("Enter valid numeric values for sale, product, quantity, and refund.", true);
        }
    }

    private void clearForm() {
        saleIdField.clear();
        productIdField.clear();
        quantityField.clear();
        refundAmountField.clear();
        returnTypeCombo.setValue("REFUND");
    }

    private void showStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
