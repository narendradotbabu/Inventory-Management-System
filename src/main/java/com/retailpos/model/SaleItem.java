package com.retailpos.model;

public class SaleItem {
    private int id, saleId, quantity;
    private Product product;
    private double unitPrice, subtotal;

    public SaleItem() {}
    public SaleItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.subtotal = unitPrice * quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = unitPrice * quantity;
    }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getTaxAmount() { return subtotal * product.getTaxRate(); }
    public double getTotalWithTax() { return subtotal + getTaxAmount(); }
    public String getProductName() { return product != null ? product.getName() : ""; }
    public String getProductBarcode() { return product != null ? product.getBarcode() : ""; }

    public void recalculate() { this.subtotal = unitPrice * quantity; }
}
