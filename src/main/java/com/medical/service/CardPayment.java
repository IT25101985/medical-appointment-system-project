package com.medical.service;

public class CardPayment implements Payment {
    @Override
    public void processPayment(Double amount) {
        System.out.println("Processing card payment of $" + amount);
        // Logic for card payment
    }
}
