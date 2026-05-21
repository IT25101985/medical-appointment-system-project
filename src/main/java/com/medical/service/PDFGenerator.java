package com.medical.service;

/**
 * Interface demonstrating Abstraction.
 * It hides the complex logic of PDF generation.
 */
public interface PDFGenerator {
    byte[] generateMedicalRecordPDF(MedicalRecord record);
}
