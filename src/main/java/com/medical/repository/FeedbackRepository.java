package com.medical.repository;

import com.medical.entity.Doctor;
import com.medical.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByDoctor(Doctor doctor);
    List<Feedback> findByPatient(com.medical.entity.User patient);
}
