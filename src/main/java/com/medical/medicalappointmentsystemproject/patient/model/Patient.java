package com.medical.medicalappointmentsystemproject.patient.model;

public class Patient {
    private String patientId;
    private String name;
    private int age;
    private String gender;
    private String contactNumber;
    private String address;

    // Default Constructor
    public Patient() {
    }

    // Parameterized Constructor
    public Patient(String patientId, String name, int age, String gender, String contactNumber, String address) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}