package com.medical.controller;

import com.medical.entity.*;
import com.medical.service.AppointmentService;
import com.medical.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorScheduleController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private UserService userService;

    // 1. View Appointments (Doctor-oda schedule-ai mattum filter panni kaatta)
    @GetMapping("/appointments")
    public String viewAppointments(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();

            // Logged-in doctor-oda username-ku match aagura appointments mattum edukkirom
            List<Appointment> doctorAppointments = appointmentService.getAllAppointments().stream()
                    .filter(a -> a.getDoctor() != null &&
                            a.getDoctor().getUser() != null &&
                            username.equals(a.getDoctor().getUser().getUsername()))
                    .collect(Collectors.toList());

            model.addAttribute("appointments", doctorAppointments);
        } else {
            model.addAttribute("appointments", new ArrayList<>());
        }
        return "doctor/view-appointments";
    }

    // 2. Patient List (System-la irukkura patients list-ai kaatta)
    @GetMapping("/patients")
    public String viewPatients(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            List<User> patients = userService.getUsersByRole("ROLE_PATIENT");
            model.addAttribute("patients", patients);
        }
        return "doctor/patient-list";
    }

    // 3. Patient Records (Historical Data)
    @GetMapping("/records")
    public String patientRecords(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            // Fetch all appointments for this doctor to identify their patients
            List<Appointment> doctorAppointments = appointmentService.getAllAppointments().stream()
                    .filter(a -> a.getDoctor() != null &&
                            a.getDoctor().getUser() != null &&
                            username.equals(a.getDoctor().getUser().getUsername()))
                    .collect(Collectors.toList());

            // Extract unique patients
            List<User> patients = doctorAppointments.stream()
                    .map(Appointment::getPatient)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // Fetch all records for these patients (excluding archived ones)
            List<MedicalRecord> allRecords = new ArrayList<>();
            for (User p : patients) {
                List<MedicalRecord> patientRecords = medicalRecordService.getRecordsByPatient(p).stream()
                        .filter(r -> !r.isArchived())
                        .collect(Collectors.toList());
                allRecords.addAll(patientRecords);
            }

            model.addAttribute("records", allRecords);
            model.addAttribute("username", username);
        }
        return "doctor/records";
    }

    // 3. Consultations (Doctor-oda schedule timing-la nadakkura consultations)
    @GetMapping("/consultations")
    public String consultations(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "doctor/consultations";
    }

    // 4. Prescribe / Write Medical Record
    @GetMapping("/prescribe/{appointmentId}")
    public String prescribeForm(@PathVariable Long appointmentId, Model model, Principal principal) {
        Optional<Appointment> optApp = appointmentService.getAppointmentById(appointmentId);
        if (optApp.isPresent()) {
            model.addAttribute("appointment", optApp.get());
            model.addAttribute("medicalRecord", new MedicalRecord());
            model.addAttribute("prescription", new Prescription());
            return "doctor/write-prescription";
        }
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/prescribe/{appointmentId}")
    public String savePrescription(@PathVariable Long appointmentId,
                                   @RequestParam String diagnosis,
                                   @RequestParam String medication,
                                   @RequestParam String dosage,
                                   @RequestParam String duration,
                                   @RequestParam Double price) {
        Optional<Appointment> optApp = appointmentService.getAppointmentById(appointmentId);
        if (optApp.isPresent()) {
            Appointment app = optApp.get();

            // Create and save medical record
            MedicalRecord record = new MedicalRecord();
            record.setPatient(app.getPatient());
            record.setAppointment(app); // Linking record to appointment
            record.setDiagnosis(diagnosis);
            record.setRecordDate(LocalDateTime.now());
            medicalRecordService.saveMedicalRecord(record);

            // Create and save prescription
            Prescription prescription = new Prescription();
            prescription.setMedicalRecord(record);
            prescription.setMedication(medication);
            prescription.setDosage(dosage);
            prescription.setDuration(duration);
            prescription.setPrice(price); // Set medicine price
            medicalRecordService.savePrescription(prescription);

            // Mark appointment as COMPLETED
            app.setStatus("COMPLETED");
            appointmentService.saveAppointment(app);
        }
        return "redirect:/doctor/appointments?success";
    }

    @GetMapping("/record/{id}/edit")
    public String editRecordForm(@PathVariable Long id, Model model) {
        Optional<MedicalRecord> optRecord = medicalRecordService.getRecordById(id);
        if (optRecord.isPresent()) {
            model.addAttribute("medicalRecord", optRecord.get());
            return "doctor/edit-record";
        }
        return "redirect:/doctor/records";
    }

    @PostMapping("/record/{id}/update")
    public String updateRecord(@PathVariable Long id, @RequestParam String diagnosis) {
        Optional<MedicalRecord> optRecord = medicalRecordService.getRecordById(id);
        if (optRecord.isPresent()) {
            MedicalRecord record = optRecord.get();
            record.setDiagnosis(diagnosis);
            medicalRecordService.saveMedicalRecord(record);
        }
        return "redirect:/doctor/records?updated";
    }

    @PostMapping("/record/{id}/archive")
    public String archiveRecord(@PathVariable Long id) {
        Optional<MedicalRecord> optRecord = medicalRecordService.getRecordById(id);
        if (optRecord.isPresent()) {
            MedicalRecord record = optRecord.get();
            record.setArchived(true);
            medicalRecordService.saveMedicalRecord(record);
        }
        return "redirect:/doctor/records?archived";
    }
}