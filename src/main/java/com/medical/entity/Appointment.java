package com.medical.entity;

import java.time.LocalDateTime;

public class Appointment {

    private Long id;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime appointmentDate;
    private String symptoms;
    private String status;

    public Appointment() {
        this.status = "PENDING";
    }

    public Appointment(Patient patient, Doctor doctor, LocalDateTime appointmentDate, String symptoms) {
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.symptoms = symptoms;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}