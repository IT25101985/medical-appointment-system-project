package com.medical.medicalappointmentsystemproject.appointmentschedule.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique Reference
    @Column(name = "appointment_ref", unique = true, length = 20)
    private String appointmentRef;

    // Patient Info
    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id")
    private Long patientId;

    @NotBlank(message = "Patient name is required")
    @Size(min = 2, max = 100)
    @Column(name = "patient_name", length = 100)
    private String patientName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+94|0)[0-9]{9}$",
            message = "Invalid Sri Lankan phone number")
    @Column(name = "patient_phone", length = 15)
    private String patientPhone;

    @Email(message = "Invalid email")
    @Column(name = "patient_email", length = 100)
    private String patientEmail;

    // Doctor Info
    @NotNull(message = "Doctor ID is required")
    @Column(name = "doctor_id")
    private Long doctorId;

    @NotBlank(message = "Doctor name is required")
    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @Column(name = "specialization", length = 100)
    private String specialization;

    // Schedule Link
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    // Appointment Details
    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be today or future")
    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @NotNull(message = "Time is required")
    @Column(name = "appointment_time")
    private LocalTime appointmentTime;

    @Column(name = "token_number")
    private Integer tokenNumber;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    // Additional
    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", length = 20)
    @Builder.Default
    private AppointmentType appointmentType = AppointmentType.IN_PERSON;

    // Timestamps
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum AppointmentStatus {
        PENDING,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_SHOW,
        RESCHEDULED
    }

    public enum AppointmentType {
        IN_PERSON,
        ONLINE,
        PHONE
    }

    // Lifecycle
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.appointmentRef == null) {
            this.appointmentRef = generateRef();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper Methods
    private String generateRef() {
        String datePart = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d",
                (int)(Math.random() * 10000));
        return "APT-" + datePart + "-" + randomPart;
    }

    public String getFormattedDate() {
        return appointmentDate != null ?
                appointmentDate.format(
                        DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
    }

    public String getFormattedTime() {
        return appointmentTime != null ?
                appointmentTime.format(
                        DateTimeFormatter.ofPattern("hh:mm a")) : "";
    }

    public String getStatusBadgeClass() {
        if (status == null) return "secondary";
        return switch (status) {
            case PENDING -> "warning";
            case CONFIRMED -> "info";
            case IN_PROGRESS -> "primary";
            case COMPLETED -> "success";
            case CANCELLED -> "danger";
            case NO_SHOW -> "dark";
            case RESCHEDULED -> "secondary";
        };
    }

    public boolean isCancellable() {
        return status == AppointmentStatus.PENDING ||
                status == AppointmentStatus.CONFIRMED;
    }

    public boolean isReschedulable() {
        return status == AppointmentStatus.PENDING ||
                status == AppointmentStatus.CONFIRMED;
    }
}