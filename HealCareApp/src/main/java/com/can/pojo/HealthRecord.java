/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 *
 * @author Giidavibe
 */
@Entity
@Table(name = "health_records")
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "examination_results", columnDefinition = "TEXT")
    private String examinationResults;

    @Column(name = "disease_type")
    private String diseaseType;
    
    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;
    // Getters and Setters

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the medicalHistory
     */
    public String getMedicalHistory() {
        return medicalHistory;
    }

    /**
     * @param medicalHistory the medicalHistory to set
     */
    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    /**
     * @return the examinationResults
     */
    public String getExaminationResults() {
        return examinationResults;
    }

    /**
     * @param examinationResults the examinationResults to set
     */
    public void setExaminationResults(String examinationResults) {
        this.examinationResults = examinationResults;
    }

    /**
     * @return the diseaseType
     */
     public String getDiseaseType() {
        return diseaseType;
    }
    
    /**
     * @param diseaseType the diseaseType to set
     */
    public void setDiseaseType(String diseaseType) {
        this.diseaseType = diseaseType;
    }
    
    /**
     * @return the appointment
     */
    public Appointment getAppointment() {
        return appointment;
    }
    
    /**
     * @param appointment the appointment to set
     */
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }
}



