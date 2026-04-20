package com.retailpos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id, cashierId;
    private String saleDate, paymentMethod, paymentStatus;
    private double subtotal, taxAmount, totalAmount;
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
        this.saleDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.paymentStatus = "COMPLETED";
    }

    public void addItem(SaleItem item) { items.add(item); recalculate(); }
    public void removeItem(SaleItem item) { items.remove(item); recalculate(); }
    public void clearItems() { items.clear(); recalculate(); }

    public void recalculate() {
        subtotal = items.stream().mapToDouble(SaleItem::getSubtotal).sum();
        taxAmount = items.stream().mapToDouble(SaleItem::getTaxAmount).sum();
        totalAmount = subtotal + taxAmount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCashierId() { return cashierId; }
    public void setCashierId(int cashierId) { this.cashierId = cashierId; }
    public String getSaleDate() { return saleDate; }
    public void setSaleDate(String saleDate) { this.saleDate = saleDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; recalculate(); }
}
