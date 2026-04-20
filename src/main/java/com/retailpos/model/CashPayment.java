package com.retailpos.model;

public class CashPayment extends Payment {
    private double cashReceived;
    private double change;

    public CashPayment(double amount, double cashReceived) {
        super(amount);
        this.cashReceived = cashReceived;
        this.change = cashReceived - amount;
    }

    @Override
    public boolean processPayment() {
        if (cashReceived >= amount) {
            status = "COMPLETED";
            return true;
        }
        status = "FAILED";
        return false;
    }

    @Override public String getPaymentMethod() { return "CASH"; }
    public double getCashReceived() { return cashReceived; }
    public double getChange() { return change; }
}
