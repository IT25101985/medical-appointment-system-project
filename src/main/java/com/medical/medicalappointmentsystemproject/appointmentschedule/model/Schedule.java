package com.medical.medicalappointmentsystemproject.appointmentschedule.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Doctor Info
    @NotNull(message = "Doctor ID is required")
    @Column(name = "doctor_id")
    private Long doctorId;

    @NotBlank(message = "Doctor name is required")
    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @Column(name = "specialization", length = 100)
    private String specialization;

    // Schedule Details
    @NotNull(message = "Schedule date is required")
    @FutureOrPresent(message = "Date must be today or future")
    @Column(name = "schedule_date")
    private LocalDate scheduleDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time")
    private LocalTime endTime;

    // Slot Management
    @Min(value = 1, message = "At least 1 slot required")
    @Max(value = 50, message = "Maximum 50 slots")
    @Column(name = "max_slots")
    private Integer maxSlots;

    @Column(name = "booked_slots")
    @Builder.Default
    private Integer bookedSlots = 0;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.AVAILABLE;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "notes", length = 500)
    private String notes;

    // Enum
    public enum ScheduleStatus {
        AVAILABLE,
        FULL,
        CANCELLED,
        COMPLETED
    }

    // Helper Methods
    public Integer getAvailableSlots() {
        if (maxSlots == null || bookedSlots == null) return 0;
        return maxSlots - bookedSlots;
    }

    public boolean hasAvailableSlots() {
        return bookedSlots < maxSlots && status == ScheduleStatus.AVAILABLE;
    }

    public void bookSlot() {
        if (hasAvailableSlots()) {
            this.bookedSlots++;
            if (this.bookedSlots >= this.maxSlots) {
                this.status = ScheduleStatus.FULL;
            }
        }
    }

    public void releaseSlot() {
        if (this.bookedSlots > 0) {
            this.bookedSlots--;
            if (this.status == ScheduleStatus.FULL) {
                this.status = ScheduleStatus.AVAILABLE;
            }
        }
    }

    public String getFormattedDate() {
        return scheduleDate != null ?
                scheduleDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
    }

    public String getFormattedTimeRange() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
        return (startTime != null ? startTime.format(fmt) : "") +
                " - " +
                (endTime != null ? endTime.format(fmt) : "");
    }
}