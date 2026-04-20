package com.retailpos.model;

// Abstract Payment - Strategy Pattern base
public abstract class Payment {
    protected int paymentId;
    protected double amount;
    protected String status;

    public Payment(double amount) {
        this.amount = amount;
        this.status = "PENDING";
    }

    public abstract boolean processPayment();
    public abstract String getPaymentMethod();

    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
