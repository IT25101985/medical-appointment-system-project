package com.medical.service;

import com.medical.entity.MedicalRecord;

/**
 * Interface demonstrating Abstraction.
 * It hides the complex logic of PDF generation.
 */
public interface PDFGenerator {
    byte[] generateMedicalRecordPDF(MedicalRecord record);
}
