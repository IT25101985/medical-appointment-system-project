package com.medical.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    // Dummy Twilio Credentials
    public static final String ACCOUNT_SID = "AC_DUMMY_SID";
    public static final String AUTH_TOKEN = "DUMMY_AUTH_TOKEN";
    public static final String TWILIO_NUMBER = "+1234567890";

    public NotificationService() {
        // Initialize Twilio
        // Twilio.init(ACCOUNT_SID, AUTH_TOKEN); // Commented out to prevent crash with dummy credentials
    }

    public void sendBookingConfirmationEmail(String toEmail, String patientName, String dateStr) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@healthcareplus.com");
            message.setTo(toEmail);
            message.setSubject("Appointment Confirmation - HealthCare Plus");
            message.setText("Dear " + patientName + ",\n\nYour appointment has been successfully booked for " + dateStr + ".\n\nThank you for choosing HealthCare Plus.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendCancellationEmail(String toEmail, String patientName, String dateStr) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@healthcareplus.com");
            message.setTo(toEmail);
            message.setSubject("Appointment Cancelled - HealthCare Plus");
            message.setText("Dear " + patientName + ",\n\nWe are sorry to inform you that your appointment on " + dateStr + " has been cancelled.\n\nPlease contact us to reschedule.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }
    }

    public void sendSmsReminder(String toPhone, String patientName, String dateStr) {
        try {
            // Uncomment to use real Twilio integration
            /*
            Message message = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(TWILIO_NUMBER),
                    "Reminder: You have an appointment at HealthCare Plus on " + dateStr 
            ).create();
            System.out.println("Sent SMS with SID: " + message.getSid());
            */
            System.out.println("Mock SMS sent to " + toPhone + " for appointment on " + dateStr);
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
}
