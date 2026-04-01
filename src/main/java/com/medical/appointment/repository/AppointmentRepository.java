package com.medical.appointment.repository;

import com.medical.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByStatus(String status);

    List<Appointment> findByPatientEmail(String email);

    List<Appointment> findByDoctorName(String doctorName);

    List<Appointment> findBySpecialization(String specialization);

    List<Appointment> findByAppointmentDate(LocalDate date);

    List<Appointment> findByDoctorNameAndAppointmentDate(
            String doctorName, LocalDate date);

    List<Appointment> findByAppointmentDateAfterOrderByAppointmentDateAsc(
            LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = ?1")
    long countByStatus(String status);

    // ===== Direct UPDATE queries =====
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    int updateStatusById(
            @Param("id") Long id,
            @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET " +
            "a.appointmentDate = :date, " +
            "a.appointmentTime = :time, " +
            "a.status = 'CONFIRMED' " +
            "WHERE a.id = :id")
    int updateDateTimeById(
            @Param("id") Long id,
            @Param("date") LocalDate date,
            @Param("time") java.time.LocalTime time);
}