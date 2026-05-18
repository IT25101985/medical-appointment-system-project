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

}