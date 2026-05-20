package com.medical.repository;

<<<<<<<< HEAD:src/main/java/com/medical/repository/PrescriptionRepository.java
import com.medical.entity.Prescription;
========
import com.medical.entity.Admin;
>>>>>>>> dev:src/main/java/com/medical/repository/AdminRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
<<<<<<<< HEAD:src/main/java/com/medical/repository/PrescriptionRepository.java
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
========
public interface AdminRepository extends JpaRepository<Admin, Long> {
>>>>>>>> dev:src/main/java/com/medical/repository/AdminRepository.java
}
