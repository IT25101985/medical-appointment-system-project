package com.medical.appointment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

// ✅ Both annotations required
@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Doctor name is required")
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @NotNull(message = "Date is required")
    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time")
    private LocalTime endTime;

    @Min(value = 1, message = "Min 1 patient")
    @Column(name = "max_patients")
    private int maxPatients;

    @Column(name = "current_patients")
    private int currentPatients;

    // ✅ IMPORTANT: field name is "isAvailable"
    // Getter must be "isAvailable()" - Spring uses this for
    // findByIsAvailableTrue() query method
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

    // ✅ OOP: POLYMORPHISM
    @Override
    public String toString() {
        return "Schedule{" +
                "id="           + id              +
                ", doctor='"    + doctorName      + '\'' +
                ", date="       + availableDate   +
                ", slots="      + currentPatients +
                "/"             + maxPatients     +
                ", available="  + isAvailable     +
                '}';
    }

    // ✅ OOP: ENCAPSULATION - Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String n) { this.doctorName = n; }

    public LocalDate getAvailableDate() { return availableDate; }
    public void setAvailableDate(LocalDate d) {
        this.availableDate = d;
    }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime t) { this.startTime = t; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime t) { this.endTime = t; }

    public int getMaxPatients() { return maxPatients; }
    public void setMaxPatients(int n) { this.maxPatients = n; }

    public int getCurrentPatients() { return currentPatients; }
    public void setCurrentPatients(int n) {
        this.currentPatients = n;
    }

    // ✅ IMPORTANT: boolean getter is "isAvailable" not "getIsAvailable"
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean a) { this.isAvailable = a; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String s) {
        this.specialization = s;
    }
}