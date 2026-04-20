package com.retailpos.factory;

import com.retailpos.model.*;

// Factory Pattern for creating Payment objects
public class PaymentFactory {
    public static Payment createPayment(String type, double amount, Object... extra) {
        return switch (type.toUpperCase()) {
            case "CASH" -> {
                double cashReceived = extra.length > 0 ? (double) extra[0] : amount;
                yield new CashPayment(amount, cashReceived);
            }
            case "UPI", "CARD", "NET_BANKING" -> new OnlinePayment(amount, type.toUpperCase());
            default -> throw new IllegalArgumentException("Unknown payment type: " + type);
        };
    }
}
