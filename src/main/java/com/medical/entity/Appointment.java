package com.medical.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Doctor doctor;

    @ManyToOne
    private User patient;

    private LocalDateTime appointmentDate;
    private String contactPhone;
    private String contactEmail;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }


    public void setAppointmentDate(LocalDateTime date) {
        if (date.isBefore(LocalDateTime.now())) {
            System.out.println("Error: Date is in the past!");
        }
        this.appointmentDate = date;
    }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
