package com.medical.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Appointment appointment;

    private Double amount;
    private String status; // PAID, UNPAID

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment a) { this.appointment = a; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double a) { this.amount = a; }
    
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
}
