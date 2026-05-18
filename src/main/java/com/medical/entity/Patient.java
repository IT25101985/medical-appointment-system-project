package com.medical.entity;

import jakarta.persistence.Entity;

@Entity
public class Patient extends User {
    private String bloodGroup;
    private String bloodPressure;
    private String heartRate;
    private String emergencyContact;
}