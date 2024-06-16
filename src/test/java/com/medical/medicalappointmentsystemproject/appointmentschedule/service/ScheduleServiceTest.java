package com.medical.medicalappointmentsystemproject.appointmentschedule.service;

import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule.ScheduleStatus;
import com.medical.medicalappointmentsystemproject.appointmentschedule.repository.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    void createSchedule_setsDefaultsAndSaves() {
        Schedule input = Schedule.builder()
                .doctorId(1L)
                .doctorName("Dr. Brown")
                .scheduleDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .maxSlots(5)
                .build();

        Schedule saved = Schedule.builder()
                .id(10L)
                .doctorId(1L)
                .doctorName("Dr. Brown")
                .scheduleDate(input.getScheduleDate())
                .startTime(input.getStartTime())
                .endTime(input.getEndTime())
                .maxSlots(5)
                .bookedSlots(0)
                .status(ScheduleStatus.AVAILABLE)
                .build();

        when(scheduleRepository.save(any(Schedule.class))).thenReturn(saved);

        Schedule result = scheduleService.createSchedule(input);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(ScheduleStatus.AVAILABLE, result.getStatus());
        assertEquals(0, result.getBookedSlots());
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void createSchedule_throwsWhenEndTimeNotAfterStartTime() {
        Schedule input = Schedule.builder()
                .doctorId(1L)
                .doctorName("Dr. Brown")
                .scheduleDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 0))
                .maxSlots(5)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> scheduleService.createSchedule(input));

        assertEquals("End time must be after start time", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void createSchedule_throwsWhenScheduleDateInPast() {
        Schedule input = Schedule.builder()
                .doctorId(1L)
                .doctorName("Dr. Brown")
                .scheduleDate(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .maxSlots(5)
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> scheduleService.createSchedule(input));

        assertEquals("Schedule date cannot be in the past", exception.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void updateSchedule_setsStatusFullWhenFullyBooked() {
        Schedule existing = Schedule.builder()
                .id(5L)
                .doctorId(1L)
                .doctorName("Dr. White")
                .scheduleDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .maxSlots(2)
                .bookedSlots(2)
                .status(ScheduleStatus.AVAILABLE)
                .build();

        Schedule updated = Schedule.builder()
                .doctorId(2L)
                .doctorName("Dr. Green")
                .specialization("Cardiology")
                .scheduleDate(LocalDate.now().plusDays(3))
                .startTime(LocalTime.of(15, 0))
                .endTime(LocalTime.of(16, 0))
                .maxSlots(2)
                .location("Room 2")
                .notes("Updated notes")
                .build();

        when(scheduleRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule result = scheduleService.updateSchedule(5L, updated);

        assertEquals(5L, result.getId());
        assertEquals("Dr. Green", result.getDoctorName());
        assertEquals(ScheduleStatus.FULL, result.getStatus());
        verify(scheduleRepository).findById(5L);
        verify(scheduleRepository).save(existing);
    }

    @Test
    void deleteSchedule_throwsWhenBookedSlotsExist() {
        Schedule existing = Schedule.builder()
                .id(7L)
                .bookedSlots(1)
                .status(ScheduleStatus.AVAILABLE)
                .build();

        when(scheduleRepository.findById(7L)).thenReturn(Optional.of(existing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> scheduleService.deleteSchedule(7L));

        assertEquals("Cannot delete schedule with existing bookings", exception.getMessage());
        verify(scheduleRepository, never()).delete(any());
    }

    @Test
    void bookSlot_updatesScheduleAndSaves() {
        Schedule existing = Schedule.builder()
                .id(9L)
                .maxSlots(3)
                .bookedSlots(1)
                .status(ScheduleStatus.AVAILABLE)
                .build();

        when(scheduleRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule result = scheduleService.bookSlot(9L);

        assertEquals(2, result.getBookedSlots());
        assertEquals(ScheduleStatus.AVAILABLE, result.getStatus());
        verify(scheduleRepository).save(existing);
    }

    @Test
    void releaseSlot_savesSchedule() {
        Schedule existing = Schedule.builder()
                .id(11L)
                .maxSlots(3)
                .bookedSlots(2)
                .status(ScheduleStatus.FULL)
                .build();

        when(scheduleRepository.findById(11L)).thenReturn(Optional.of(existing));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Schedule result = scheduleService.releaseSlot(11L);

        assertEquals(1, result.getBookedSlots());
        assertEquals(ScheduleStatus.AVAILABLE, result.getStatus());
        verify(scheduleRepository).save(existing);
    }
}