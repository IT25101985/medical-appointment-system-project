package com.medical.appointment.repository;

import com.medical.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientNameContainingIgnoreCase(
            String patientName);

    List<Appointment> findByDoctorName(String doctorName);

    List<Appointment> findByStatus(String status);

    List<Appointment> findByAppointmentDate(LocalDate date);

    List<Appointment>
    findByAppointmentDateGreaterThanEqualOrderByAppointmentDateAsc(
            LocalDate date);

    List<Appointment> findBySpecialization(String specialization);

    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.appointmentDate = :today " +
            "ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodaysAppointments(
            @Param("today") LocalDate today);

    @Query("SELECT a FROM Appointment a WHERE " +
            "LOWER(a.patientName) LIKE " +
            "LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.patientEmail) LIKE " +
            "LOWER(CONCAT('%', :keyword, '%'))")
    List<Appointment> searchByKeyword(
            @Param("keyword") String keyword);
}