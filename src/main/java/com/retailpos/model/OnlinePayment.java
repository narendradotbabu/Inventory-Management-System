package com.retailpos.model;

import java.util.UUID;

public class OnlinePayment extends Payment {
    private String transactionId;
    private String paymentMode; // UPI, CARD, NET_BANKING

    public OnlinePayment(double amount, String paymentMode) {
        super(amount);
        this.paymentMode = paymentMode;
        this.transactionId = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    @Override
    public boolean processPayment() {
        // Simulate online payment verification
        status = "COMPLETED";
        return true;
    }

    public boolean verifyPayment() { return "COMPLETED".equals(status); }

    @Override public String getPaymentMethod() { return "ONLINE_" + paymentMode; }
    public String getTransactionId() { return transactionId; }
    public String getPaymentMode() { return paymentMode; }
}
