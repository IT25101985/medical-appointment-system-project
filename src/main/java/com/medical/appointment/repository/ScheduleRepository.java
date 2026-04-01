package com.medical.appointment.repository;

import com.medical.appointment.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    List<Schedule> findByDoctorName(String doctorName);

    List<Schedule> findByDayOfWeek(String dayOfWeek);

    List<Schedule> findByIsAvailableTrue();

    List<Schedule> findBySpecialization(String specialization);

    List<Schedule> findByDoctorNameAndDayOfWeek(
            String doctorName, String dayOfWeek);
}