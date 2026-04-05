package com.medical.appointment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Doctor name is required")
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @NotNull(message = "Available date is required")
    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time")
    private LocalTime endTime;

    @Min(value = 1, message = "Max patients must be at least 1")
    @Column(name = "max_patients")
    private int maxPatients;

    @Column(name = "current_patients")
    private int currentPatients;

    @Column(name = "is_available")
    private boolean isAvailable;

    @Column(name = "specialization")
    private String specialization;

    // ✅ OOP: Default Constructor
    public Schedule() {
        this.isAvailable     = true;
        this.currentPatients = 0;
    }

    // ✅ OOP: Parameterized Constructor
    public Schedule(String doctorName, LocalDate availableDate,
                    LocalTime startTime, LocalTime endTime,
                    int maxPatients, String specialization) {
        this.doctorName      = doctorName;
        this.availableDate   = availableDate;
        this.startTime       = startTime;
        this.endTime         = endTime;
        this.maxPatients     = maxPatients;
        this.specialization  = specialization;
        this.isAvailable     = true;
        this.currentPatients = 0;
    }

    // ✅ OOP: ABSTRACTION - Business Methods
    public boolean hasAvailableSlots() {
        return currentPatients < maxPatients && isAvailable;
    }

    public int getRemainingSlots() {
        return maxPatients - currentPatients;
    }

    public double getOccupancyPercentage() {
        if (maxPatients == 0) return 0;
        return ((double) currentPatients / maxPatients) * 100;
    }

    public void bookSlot() {
        if (hasAvailableSlots()) {
            currentPatients++;
            if (currentPatients >= maxPatients) {
                isAvailable = false;
            }
        }
    }

    public void cancelSlot() {
        if (currentPatients > 0) {
            currentPatients--;
            isAvailable = true;
        }
    }

    // ✅ OOP: POLYMORPHISM - toString
    @Override
    public String toString() {
        return "Schedule{" +
                "id="             + id              +
                ", doctorName='"  + doctorName      + '\'' +
                ", date="         + availableDate   +
                ", slots="        + currentPatients +
                "/"               + maxPatients     +
                ", available="    + isAvailable     +
                '}';
    }

    // ✅ OOP: ENCAPSULATION - Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate availableDate) {
        this.availableDate = availableDate;
    }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getMaxPatients() { return maxPatients; }
    public void setMaxPatients(int maxPatients) {
        this.maxPatients = maxPatients;
    }

    public int getCurrentPatients() { return currentPatients; }
    public void setCurrentPatients(int currentPatients) {
        this.currentPatients = currentPatients;
    }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}