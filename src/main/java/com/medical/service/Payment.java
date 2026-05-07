package com.medical.service;

/**
 * Interface demonstrating Polymorphism.
 * Different payment methods (Card, Cash) will implement this.
 */
public interface Payment {
    void processPayment(Double amount);
}
