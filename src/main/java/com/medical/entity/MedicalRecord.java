package com.medical.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User patient;

    private String diagnosis;
    private LocalDateTime recordDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getPatient() { return patient; }
    public void setPatient(User patient) { this.patient = patient; }
    
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String d) { this.diagnosis = d; }
    
    public LocalDateTime getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDateTime d) { this.recordDate = d; }
}
