package com.medical.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller to show how Polymorphism works in Payment.
 */
@Controller
public class PaymentController {

    @GetMapping("/pay")
    @ResponseBody
    public String processPayment(@RequestParam("type") String type, @RequestParam("amount") Double amount) {
        // Polymorphism: Using the interface
        Payment paymentMethod;

        if (type.equalsIgnoreCase("card")) {
            paymentMethod = new CardPayment();
        } else {
            paymentMethod = new CashPayment();
        }

        // The same processPayment method works for both types!
        paymentMethod.processPayment(amount);

        return "Successfully processed " + type + " payment of $" + amount + ". Check console for logs!";
    }
}
