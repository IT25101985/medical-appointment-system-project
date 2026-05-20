package com.medical.repository;

import com.medical.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    java.util.List<Doctor> findBySpecialization(String specialization);
    java.util.Optional<Doctor> findByUser(com.medical.entity.User user);
}
