package com.medical.medicalappointmentsystemproject.appointmentschedule.repository;

import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByAppointmentRef(String ref);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(
            Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDesc(
            Long doctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(
            LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.patientId = :patientId " +
            "AND a.appointmentDate >= :today " +
            "AND a.status IN ('PENDING','CONFIRMED') " +
            "ORDER BY a.appointmentDate ASC, " +
            "a.appointmentTime ASC")
    List<Appointment> findUpcomingByPatient(
            @Param("patientId") Long patientId,
            @Param("today") LocalDate today);

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.doctorId = :doctorId " +
            "AND a.appointmentDate = CURRENT_DATE " +
            "ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayByDoctor(
            @Param("doctorId") Long doctorId);

    @Query("SELECT a FROM Appointment a WHERE " +
            "LOWER(a.patientName) LIKE " +
            "LOWER(CONCAT('%',:keyword,'%')) " +
            "OR a.patientPhone LIKE " +
            "CONCAT('%',:keyword,'%') " +
            "OR a.appointmentRef LIKE " +
            "CONCAT('%',:keyword,'%') " +
            "ORDER BY a.appointmentDate DESC")
    List<Appointment> searchAppointments(
            @Param("keyword") String keyword);

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.appointmentDate BETWEEN :start AND :end " +
            "ORDER BY a.appointmentDate ASC, " +
            "a.appointmentTime ASC")
    List<Appointment> findByDateRange(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.status = :status")
    Long countByStatus(
            @Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.appointmentDate = CURRENT_DATE")
    Long countTodayAppointments();

    @Query("SELECT COALESCE(MAX(a.tokenNumber),0)+1 " +
            "FROM Appointment a " +
            "WHERE a.schedule.id = :scheduleId " +
            "AND a.status != 'CANCELLED'")
    Integer getNextTokenNumber(
            @Param("scheduleId") Long scheduleId);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
            "a.patientId = :patientId " +
            "AND a.doctorId = :doctorId " +
            "AND a.appointmentDate = :date " +
            "AND a.status NOT IN ('CANCELLED')")
    boolean existsDuplicate(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date);
}