package com.medical.service;

import com.medical.entity.Appointment;
import com.medical.entity.User;
import java.util.List;

public interface AppointmentService {
    void saveAppointment(Appointment appointment);
    List<Appointment> getAppointmentsForPatient(User patient);
    List<Appointment> getAllAppointments();
}