package com.medical.appointment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    // ✅ OOP ENCAPSULATION - all private fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Patient name is required")
    @Size(min = 2, max = 100,
            message = "Name must be between 2 and 100 characters")
    @Column(name = "patient_name", nullable = false, length = 100)
    private String patientName;

    @NotBlank(message = "Doctor name is required")
    @Column(name = "doctor_name", nullable = false, length = 100)
    private String doctorName;

    @NotNull(message = "Appointment date is required")
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @NotBlank(message = "Specialization is required")
    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Email(message = "Please enter a valid email address")
    @Column(name = "patient_email", length = 150)
    private String patientEmail;

    @Pattern(regexp = "^[0-9]{10}$",
            message = "Phone must be exactly 10 digits")
    @Column(name = "patient_phone", length = 15)
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
        this.patientName    = patientName;
        this.doctorName     = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.specialization  = specialization;
        this.status          = "BOOKED";
    }

    // ✅ OOP: ABSTRACTION - Business Logic
    public boolean isUpcoming() {
        return this.appointmentDate != null &&
                this.appointmentDate.isAfter(LocalDate.now());
    }

    public boolean isCancellable() {
        return "BOOKED".equals(this.status) ||
                "RESCHEDULED".equals(this.status);
    }

    public String getStatusBadgeColor() {
        return switch (this.status) {
            case "BOOKED"       -> "primary";
            case "COMPLETED"    -> "success";
            case "CANCELLED"    -> "danger";
            case "RESCHEDULED"  -> "warning";
            default             -> "secondary";
        };
    }

    // ✅ OOP: POLYMORPHISM - Override toString
    @Override
    public String toString() {
        return "Appointment{" +
                "id="               + id              +
                ", patientName='"   + patientName     + '\'' +
                ", doctorName='"    + doctorName      + '\'' +
                ", date="           + appointmentDate +
                ", time="           + appointmentTime +
                ", specialization='"+ specialization  + '\'' +
                ", status='"        + status          + '\'' +
                '}';
    }

    // ✅ OOP: ENCAPSULATION - Getters and Setters
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