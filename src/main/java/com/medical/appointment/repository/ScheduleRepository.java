package com.medical.appointment.repository;

import com.medical.appointment.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    List<Schedule> findByDoctorName(String doctorName);

    List<Schedule> findByAvailableDate(LocalDate date);

    List<Schedule> findByIsAvailableTrue();

    List<Schedule> findByDoctorNameAndAvailableDate(
            String doctorName, LocalDate date);

    List<Schedule> findBySpecialization(String specialization);

    List<Schedule> findByAvailableDateGreaterThanEqual(LocalDate date);

    // Find available schedules for a specific doctor
    @Query("SELECT s FROM Schedule s WHERE s.doctorName = :doctor " +
            "AND s.isAvailable = true " +
            "AND s.availableDate >= CURRENT_DATE " +
            "ORDER BY s.availableDate ASC")
    List<Schedule> findAvailableByDoctor(String doctor);
}