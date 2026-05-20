package com.medical.repository;

import com.medical.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    java.util.Optional<Invoice> findByAppointment(com.medical.entity.Appointment appointment);
}
