package com.medical.medicalappointmentsystemproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ✅ KEY FIX: scan ALL packages under com.medical
@SpringBootApplication(scanBasePackages = "com.medical")
public class MedicalAppointmentSystemProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                MedicalAppointmentSystemProjectApplication.class, args
        );
        System.out.println("\n");
        System.out.println("==========================================");
        System.out.println("  App Started!");
        System.out.println("  Visit: http://localhost:8080/appointments");
        System.out.println("==========================================");
        System.out.println("\n");
    }
}