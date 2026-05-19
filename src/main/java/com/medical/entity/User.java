package com.medical.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
// --- JPA Inheritance: Combines all subclasses (Admin, Doctor, Patient) into a single database table ---
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// --- JPA Concept: Defines the name of the column that identifies the type of user account ---
@DiscriminatorColumn(name = "user_type")
public class User { // --- OOP Concept: Base Class / Parent Class for child entities ---

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- OOP Concept: Encapsulation (All fields are private to prevent direct tampering) ---
    private String username;
    private String password;
    private String fullName;
    private String role; // Stores authorization roles: ROLE_ADMIN, ROLE_DOCTOR, ROLE_PATIENT
    private String email;
    private String phoneNo;
    private String address;

    @Column(columnDefinition = "LONGTEXT") // Allows storing large string data like Base64 images
    private String profileImage;

    // 1. Default No-Argument Constructor (Mandatory for Hibernate framework)
    public User() {}

    // 2. Parameterized Constructor (Used by child classes via 'super()' to set core values)
    public User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    // --- OOP Concept: Encapsulation (Public Accessors to read/write private fields safely) ---
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return this.fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return this.role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return this.phoneNo;
    }
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return this.profileImage;
    }
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}