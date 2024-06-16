package com.medical.medicalappointmentsystemproject.appointmentschedule.service;

import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment.AppointmentStatus;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule;
import com.medical.medicalappointmentsystemproject.appointmentschedule.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private com.medical.medicalappointmentsystemproject.appointmentschedule.repository.ScheduleRepository scheduleRepository;

    private ScheduleService scheduleService;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(scheduleRepository);
        appointmentService = new AppointmentService(appointmentRepository, scheduleService);
    }

    @Test
    void bookAppointment_throwsWhenDuplicateExists() {
        Appointment appointment = Appointment.builder()
                .patientId(10L)
                .doctorId(20L)
                .appointmentDate(LocalDate.now().plusDays(1))
                .patientName("John Doe")
                .build();

        when(appointmentRepository.existsDuplicate(
                10L, 20L, appointment.getAppointmentDate()))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.bookAppointment(appointment));

        assertTrue(exception.getMessage().contains("You already have an appointment"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void bookAppointment_booksScheduleAndSaves() {
        Schedule schedule = Schedule.builder()
                .id(3L)
                .maxSlots(5)
                .bookedSlots(1)
                .status(Schedule.ScheduleStatus.AVAILABLE)
                .build();
        Appointment appointment = Appointment.builder()
                .patientId(10L)
                .doctorId(20L)
                .patientName("John Doe")
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(11, 0))
                .schedule(schedule)
                .build();

        when(appointmentRepository.existsDuplicate(
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getAppointmentDate())).thenReturn(false);
        when(scheduleRepository.findById(3L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.getNextTokenNumber(3L)).thenReturn(5);
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.bookAppointment(appointment);

        assertEquals(schedule, result.getSchedule());
        assertEquals(5, result.getTokenNumber());
        assertEquals(AppointmentStatus.PENDING, result.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void rescheduleAppointment_throwsWhenNotReschedulable() {
        Appointment existing = Appointment.builder()
                .id(12L)
                .status(AppointmentStatus.COMPLETED)
                .build();

        when(appointmentRepository.findById(12L))
                .thenReturn(Optional.of(existing));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> appointmentService.rescheduleAppointment(
                        12L,
                        LocalDate.now().plusDays(2),
                        LocalTime.of(10, 0),
                        null));

        assertTrue(exception.getMessage().contains("Cannot reschedule"));
    }

    @Test
    void updateStatus_cancelsAndReleasesSlot() {
        Schedule schedule = Schedule.builder().id(33L).build();
        Appointment existing = Appointment.builder()
                .id(22L)
                .status(AppointmentStatus.CONFIRMED)
                .schedule(schedule)
                .build();

        when(appointmentRepository.findById(22L))
                .thenReturn(Optional.of(existing));
        when(scheduleRepository.findById(33L)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = appointmentService.updateStatus(
                22L, AppointmentStatus.CANCELLED);

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        verify(scheduleRepository).findById(33L);
        verify(scheduleRepository).save(any(Schedule.class));
        verify(appointmentRepository).save(existing);
    }
}