package com.medical.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {

    //Specific fields for patient
    private String bloodGroup;
    private String medicalHistory;
    private String emergencyContact;

    // Default Constructor
    public Patient() {
        super();
        this.setRole("ROLE_PATIENT");
    }

    //Constructor with fields
    public Patient(String username, String password, String fullName, String bloodGroup) {
        super(username, password, fullName); // Calls the User class constructors
        this.bloodGroup = bloodGroup;
        this.setRole("ROLE_PATIENT");
    }

    // Getters and Setters for Patient-specific fields
    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}