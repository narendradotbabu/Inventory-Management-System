package com.retailpos.controller;

import com.retailpos.dao.ProductDAO;
import com.retailpos.dao.SaleDAO;
import com.retailpos.model.Sale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReportsController {
    @FXML private Label todayRevenueLabel;
    @FXML private Label todayTxnLabel;
    @FXML private Label totalProductsLabel;
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> colId;
    @FXML private TableColumn<Sale, String> colDate;
    @FXML private TableColumn<Sale, Double> colSubtotal;
    @FXML private TableColumn<Sale, Double> colTax;
    @FXML private TableColumn<Sale, Double> colTotal;
    @FXML private TableColumn<Sale, String> colPayment;
    @FXML private TableColumn<Sale, String> colStatus;

    private final SaleDAO saleDAO = new SaleDAO();
    private final ProductDAO productDAO = ProductDAO.getInstance();
    private final ObservableList<Sale> sales = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadReportData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colTax.setCellValueFactory(new PropertyValueFactory<>("taxAmount"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        setCurrencyColumn(colSubtotal);
        setCurrencyColumn(colTax);
        setCurrencyColumn(colTotal);

        salesTable.setItems(sales);
    }

    private void setCurrencyColumn(TableColumn<Sale, Double> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("Rs. %.2f", value));
            }
        });
    }

    private void loadReportData() {
        todayRevenueLabel.setText(String.format("Rs. %.2f", saleDAO.getTodayRevenue()));
        todayTxnLabel.setText(String.valueOf(saleDAO.getTodayTransactions()));
        totalProductsLabel.setText(String.valueOf(productDAO.getProductCount()));
        sales.setAll(saleDAO.getRecentSales(50));
    }

    @FXML
    private void handleRefresh() {
        loadReportData();
    }
}
