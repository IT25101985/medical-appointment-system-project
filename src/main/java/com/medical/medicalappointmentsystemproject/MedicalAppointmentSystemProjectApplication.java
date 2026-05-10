package com.medical.medicalappointmentsystemproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
        scanBasePackages = {
                "com.medical.appointment",
                "com.medical.medicalappointmentsystemproject"
        }
)
@EntityScan(
        basePackages = {
                "com.medical.appointment.entity",
                "com.medical.medicalappointmentsystemproject.patient.model"
        }
)
@EnableJpaRepositories(
        basePackages = {
                "com.medical.appointment.repository"
        }
)
public class MedicalAppointmentSystemProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                MedicalAppointmentSystemProjectApplication.class,
                args
        );
        System.out.println("\n");
        System.out.println("=========================================");
        System.out.println("   Medical Appointment System Started!   ");
        System.out.println("   http://localhost:8080/appointments     ");
        System.out.println("=========================================");
        System.out.println("\n");
    }
}