package com.medical.service;

public class CashPayment implements Payment {
    @Override
    public void processPayment(Double amount) {
        System.out.println("Processing cash payment of $" + amount);
        // Logic for cash payment
    }
}
