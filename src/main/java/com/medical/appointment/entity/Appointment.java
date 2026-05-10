package com.medical.appointment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

// ✅ Both @Entity and @Table are required
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Patient name is required")
    @Size(min = 2, max = 100,
            message = "Name must be 2-100 characters")
    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @NotBlank(message = "Doctor name is required")
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @NotNull(message = "Date is required")
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull(message = "Time is required")
    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;

    @NotBlank(message = "Specialization is required")
    @Column(name = "specialization")
    private String specialization;

    @Column(name = "status")
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Email(message = "Enter valid email")
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

    // ✅ OOP: ABSTRACTION
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

    // ✅ OOP: POLYMORPHISM
    @Override
    public String toString() {
        return "Appointment{" +
                "id="             + id              +
                ", patient='"     + patientName     + '\'' +
                ", doctor='"      + doctorName      + '\'' +
                ", date="         + appointmentDate +
                ", time="         + appointmentTime +
                ", status='"      + status          + '\'' +
                '}';
    }

    // ✅ OOP: ENCAPSULATION - Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String n) { this.patientName = n; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String n) { this.doctorName = n; }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }
    public void setAppointmentDate(LocalDate d) {
        this.appointmentDate = d;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }
    public void setAppointmentTime(LocalTime t) {
        this.appointmentTime = t;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String s) {
        this.specialization = s;
    }

    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }

    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String e) {
        this.patientEmail = e;
    }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String p) {
        this.patientPhone = p;
    }
}