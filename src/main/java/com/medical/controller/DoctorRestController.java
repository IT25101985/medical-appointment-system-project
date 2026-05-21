package com.medical.controller;

import com.medical.entity.Appointment;
import com.medical.repository.AppointmentRepository;
import com.medical.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class DoctorRestController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @GetMapping("/api/doctors/{id}/slots")
    public List<String> getAvailableSlots(@PathVariable Long id, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        Optional<Doctor> doctorOpt = doctorService.getDoctorById(id);

        List<String> availableSlots = new ArrayList<>();
        if (!doctorOpt.isPresent()) {
            return availableSlots;
        }

        Doctor doctor = doctorOpt.get();

        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

        List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndAppointmentDateBetween(doctor, startOfDay, endOfDay);

        // Generate slots from 09:00 to 17:00, every 1 hour
        for (int hour = 9; hour < 17; hour++) {
            LocalDateTime slotTime = localDate.atTime(hour, 0);

            // Check if this specific time is already booked by another patient
            boolean isBooked = false;
            for (Appointment app : existingAppointments) {
                // If the times match AND the appointment is NOT cancelled
                if (app.getAppointmentDate().equals(slotTime)) {
                    if (!"CANCELLED".equals(app.getStatus())) {
                        isBooked = true;
                        break; // Stop checking further appointments
                    }
                }
            }

            if (!isBooked) {
                // Add to our available list.
                // We format it standardly: yyyy-MM-dd'T'HH:mm
                availableSlots.add(slotTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
        }

        return availableSlots;
    }
}
