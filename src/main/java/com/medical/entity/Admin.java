package com.medical.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User { // --- OOP Concept: Inheritance (Admin is a Subclass/Child of User) ---

    // --- OOP Concept: Encapsulation (Private field, hidden from direct external access) ---
    private String department;

    // 1. Default No-Argument Constructor (Required by JPA to instantiate entities)
    public Admin() {
        super(); // Invokes the default constructor of the Parent class (User)
        this.setRole("ROLE_ADMIN"); // Automatically assigns the Admin role upon object creation
    }

    // 2. Parameterized Constructor (Used to easily instantiate a new Admin object with data)
    public Admin(String username, String password, String fullName, String department) {
        super(username, password, fullName); // Passes common attributes up to the Parent (User) constructor
        this.department = department;
        this.setRole("ROLE_ADMIN");
    }

    // --- OOP Concept: Encapsulation (Public Getters & Setters to read/modify private fields safely) ---
    public String getDepartment() {
        return this.department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}