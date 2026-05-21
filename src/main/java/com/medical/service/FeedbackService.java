package com.medical.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;

    public Feedback saveFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }

    public List<Feedback> getFeedbackByDoctor(com.medical.entity.Doctor doctor) {
        return feedbackRepository.findByDoctor(doctor);
    }

    public List<Feedback> getFeedbackByPatient(com.medical.entity.User patient) {
        return feedbackRepository.findByPatient(patient);
    }
}
