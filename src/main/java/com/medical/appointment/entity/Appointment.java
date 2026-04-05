package com.medical.appointment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Patient name is required")
    @Size(min = 2, max = 100,
            message = "Name must be between 2 and 100 characters")
    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @NotBlank(message = "Doctor name is required")
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @NotNull(message = "Appointment date is required")
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @NotBlank(message = "Specialization is required")
    @Column(name = "specialization")
    private String specialization;

    @Column(name = "status")
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Email(message = "Please enter a valid email")
    @Column(name = "patient_email")
    private String patientEmail;

    @Pattern(regexp = "^[0-9]{10}$",
            message = "Phone must be 10 digits")
    @Column(name = "patient_phone")
    private String patientPhone;

    // ✅ OOP: Default Constructor
    public Appointment() {
        this.status = "BOOKED";
    }

    // ✅ OOP: Parameterized Constructor
    public Appointment(String patientName, String doctorName,
                       LocalDate appointmentDate,
                       LocalTime appointmentTime,
                       String specialization) {
        this.patientName     = patientName;
        this.doctorName      = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.specialization  = specialization;
        this.status          = "BOOKED";
    }

    // ✅ OOP: ABSTRACTION - Business Methods
    public boolean isUpcoming() {
        return appointmentDate != null &&
                appointmentDate.isAfter(LocalDate.now());
    }

    public boolean isCancellable() {
        return "BOOKED".equals(status) ||
                "RESCHEDULED".equals(status);
    }

    public String getStatusBadgeColor() {
        if (status == null) return "secondary";
        switch (status) {
            case "BOOKED":      return "primary";
            case "COMPLETED":   return "success";
            case "CANCELLED":   return "danger";
            case "RESCHEDULED": return "warning";
            default:            return "secondary";
        }
    }

    // ✅ OOP: POLYMORPHISM - toString
    @Override
    public String toString() {
        return "Appointment{" +
                "id="                + id              +
                ", patientName='"    + patientName     + '\'' +
                ", doctorName='"     + doctorName      + '\'' +
                ", appointmentDate=" + appointmentDate +
                ", appointmentTime=" + appointmentTime +
                ", status='"         + status          + '\'' +
                '}';
    }

    // ✅ OOP: ENCAPSULATION - Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }
}