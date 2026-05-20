package com.medical.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.medical.entity.Appointment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService implements PDFGenerator {

    @Override
    public byte[] generateMedicalRecordPDF(com.medical.entity.MedicalRecord record) {
        // Simple logic hiding the complexity of PDF generation
        return "PDF Content for Medical Record".getBytes();
    }

    public ByteArrayInputStream generateAppointmentSummary(Appointment appointment) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Medical Appointment Summary", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Add Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 2});

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

            addTableRow(table, "Appointment ID", "#" + appointment.getId());
            addTableRow(table, "Patient Name", appointment.getPatient().getFullName());
            addTableRow(table, "Doctor Name", appointment.getDoctor().getName());
            addTableRow(table, "Department", appointment.getDoctor().getSpecialization());
            addTableRow(table, "Date & Time", appointment.getAppointmentDate().format(formatter));
            addTableRow(table, "Status", appointment.getStatus());

            document.add(table);
            document.close();

        } catch (DocumentException ex) {
            ex.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTableRow(PdfPTable table, String header, String content) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        PdfPCell hCell = new PdfPCell(new Phrase(header, headerFont));
        hCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        hCell.setPadding(8);

        PdfPCell cCell = new PdfPCell(new Phrase(content, contentFont));
        cCell.setPadding(8);

        table.addCell(hCell);
        table.addCell(cCell);
    }
}
