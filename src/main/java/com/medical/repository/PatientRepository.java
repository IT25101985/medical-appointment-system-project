package com.medical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medical.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
