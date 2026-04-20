package com.retailpos.model;

public class Product {
    private int id;
    private String barcode, name, category;
    private double price, taxRate;
    private int stockQty;

    public Product() {}
    public Product(int id, String barcode, String name, String category, double price, int stockQty, double taxRate) {
        this.id = id; this.barcode = barcode; this.name = name; this.category = category;
        this.price = price; this.stockQty = stockQty; this.taxRate = taxRate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    public int getStockQty() { return stockQty; }
    public void setStockQty(int stockQty) { this.stockQty = stockQty; }
    public double getPriceWithTax() { return price * (1 + taxRate); }
    public String getTaxRatePercent() { return String.format("%.0f%%", taxRate * 100); }

    @Override public String toString() { return name; }
}
