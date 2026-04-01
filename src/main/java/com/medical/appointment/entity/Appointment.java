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
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    @Column(nullable = false)
    private String patientName;

    @NotBlank(message = "Patient email is required")
    @Email(message = "Please enter a valid email")
    @Column(nullable = false)
    private String patientEmail;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$",
            message = "Phone must be 10 digits")
    @Column(nullable = false)
    private String patientPhone;

    @NotBlank(message = "Doctor name is required")
    @Column(nullable = false)
    private String doctorName;

    @NotBlank(message = "Specialization is required")
    @Column(nullable = false)
    private String specialization;

    @NotNull(message = "Appointment date is required")
    @Column(nullable = false)
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    @Column(nullable = false)
    private LocalTime appointmentTime;

    @NotBlank(message = "Reason is required")
    @Size(min = 5, max = 500,
            message = "Reason must be 5-500 characters")
    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false)
    private String appointmentType = "IN_PERSON";

    @Column(length = 1000)
    private String notes;

    // ===== CONSTRUCTORS =====
    public Appointment() {}

    public Appointment(String patientName, String patientEmail,
                       String patientPhone, String doctorName,
                       String specialization,
                       LocalDate appointmentDate,
                       LocalTime appointmentTime, String reason) {
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.reason = reason;
        this.status = "PENDING";
        this.appointmentType = "IN_PERSON";
    }

    // ===== GETTERS AND SETTERS =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // ===== BUSINESS LOGIC =====
    public boolean isCancellable() {
        return "PENDING".equals(this.status)
                || "CONFIRMED".equals(this.status);
    }

    public boolean isUpcoming() {
        return this.appointmentDate != null
                && this.appointmentDate.isAfter(LocalDate.now())
                && !"CANCELLED".equals(this.status);
    }

    @Override
    public String toString() {
        return id + "," + patientName + "," + patientEmail + ","
                + patientPhone + "," + doctorName + ","
                + specialization + "," + appointmentDate + ","
                + appointmentTime + "," + reason + ","
                + status + "," + appointmentType;
    }
}