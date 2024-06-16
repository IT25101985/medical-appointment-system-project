package com.medical.medicalappointmentsystemproject.appointmentschedule.repository;

import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByDoctorId(Long doctorId);

    List<Schedule> findByScheduleDate(LocalDate date);

    List<Schedule> findByStatus(ScheduleStatus status);

    @Query("SELECT s FROM Schedule s WHERE s.status = 'AVAILABLE' " +
            "AND s.scheduleDate >= :today " +
            "ORDER BY s.scheduleDate ASC, s.startTime ASC")
    List<Schedule> findAvailableSchedules(
            @Param("today") LocalDate today);

    List<Schedule> findByDoctorIdAndScheduleDate(
            Long doctorId, LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE " +
            "s.specialization = :spec " +
            "AND s.status = 'AVAILABLE' " +
            "AND s.scheduleDate >= :today " +
            "ORDER BY s.scheduleDate ASC")
    List<Schedule> findAvailableBySpecialization(
            @Param("spec") String specialization,
            @Param("today") LocalDate today);

    @Query("SELECT s FROM Schedule s WHERE " +
            "s.scheduleDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.scheduleDate ASC, s.startTime ASC")
    List<Schedule> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Schedule s WHERE " +
            "LOWER(s.doctorName) LIKE " +
            "LOWER(CONCAT('%', :name, '%'))")
    List<Schedule> searchByDoctorName(
            @Param("name") String doctorName);

    @Query("SELECT s FROM Schedule s WHERE " +
            "s.scheduleDate = CURRENT_DATE " +
            "ORDER BY s.startTime ASC")
    List<Schedule> findTodaySchedules();
}