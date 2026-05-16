# 🏥 HealthCarePlus - Medical Appointment Management System

A professional, feature-rich healthcare management portal built with Spring Boot, designed to streamline clinical operations for admins, doctors, and patients.

---

## 🚀 Key Features

- **Modern UI**: Glassmorphic dark-mode interface using Tailwind CSS.
- **Role-Based Access**: Specialized dashboards for Admins, Doctors, and Patients.
- **Appointment Management**: Real-time booking, rescheduling, and status tracking.
- **Clinical Records**: Digital prescriptions and medical history management with PDF generation.
- **Financial Module**: Automated invoice generation and payment tracking.
- **Feedback System**: Patient reviews and doctor rating analytics.

---

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3, Spring Security, Spring Data JPA
- **Database**: MySQL
- **Frontend**: Thymeleaf, Tailwind CSS, FontAwesome
- **Utilities**: PDFBox (for records), Twilio / JavaMail (for notifications)

---

## 👥 Module Distribution & Team

| Member | Module Name | Backend Classes (Java) | Frontend Templates (HTML) |
| :--- | :--- | :--- | :--- |
| **Member 1** | User & Admin Management | `User`, `Admin`, `UserService`, `AdminController`, `AuthController`, `UserRepository`, `AdminRepository`, `SecurityConfig`, `DataInitializer` | `login.html`, `register.html`, `forgot-password.html`, `admin/users.html`, `admin/dashboard.html`, `index.html` |
| **Member 2** | Doctor & Specialization | `Doctor`, `Specialization`, `DoctorService`, `DoctorProfileController`, `DoctorRestController`, `DoctorRepository`, `Cardiology`, `Pediatrics` | `admin/doctor-management.html`, `doctor/dashboard.html` |
| **Member 3** | Patient Profile Management | `Patient`, `PatientService`, `PatientProfileController`, `PatientRepository` | `patient/profile.html`, `doctor/patient-list.html` |
| **Member 4** | Appointment & Schedule | `Appointment`, `Schedule`, `AppointmentService`, `PatientBookingController`, `DoctorScheduleController`, `AppointmentRepository`, `ScheduleRepository` | `patient/book-appointment.html`, `patient/history.html`, `admin/appointments.html`, `doctor/view-appointments.html` |
| **Member 5** | Clinical Records & Prescriptions | `MedicalRecord`, `Prescription`, `MedicalRecordService`, `MedicalRecordRepository`, `PrescriptionRepository`, `PDFGenerator`, `PdfService` | `doctor/records.html`, `doctor/write-prescription.html`, `doctor/edit-record.html`, `patient/view-record.html` |
| **Member 6** | Payments, Invoices & Feedback | `Invoice`, `Feedback`, `InvoiceService`, `FeedbackService`, `PaymentController`, `InvoiceRepository`, `FeedbackRepository`, `CardPayment`, `CashPayment`, `Payment`, `RatingService` | `admin/invoice-management.html`, `admin/review-management.html`, `patient/payment-success.html` |

---

## ⚙️ Setup Instructions

1. **Database Setup**
   - Create a MySQL database named: `medical_db`

2. **Configuration**
   - Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/medical_db
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD
