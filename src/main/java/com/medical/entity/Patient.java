package com.medical.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {
    private String bloodGroup;
    private String bloodPressure;
    private String heartRate;
    private String emergencyContact;

    public Patient() {
        super();
        this.setRole("ROLE_PATIENT");
    }

    public Patient(String username, String password, String fullName, String bloodGroup) {
        super(username, password, fullName);
        this.bloodGroup = bloodGroup;
        this.setRole("ROLE_PATIENT");
    }

}