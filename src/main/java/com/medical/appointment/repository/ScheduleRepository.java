package com.medical.appointment.repository;

import com.medical.appointment.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    List<Schedule> findByDoctorName(String doctorName);

    List<Schedule> findByAvailableDate(LocalDate date);

    List<Schedule> findByIsAvailableTrue();

    List<Schedule> findBySpecialization(String specialization);

    List<Schedule> findByAvailableDateGreaterThanEqual(
            LocalDate date);

    @Query("SELECT s FROM Schedule s " +
            "WHERE s.isAvailable = true " +
            "AND s.availableDate >= :today " +
            "ORDER BY s.availableDate ASC")
    List<Schedule> findAllAvailable(
            @Param("today") LocalDate today);
}