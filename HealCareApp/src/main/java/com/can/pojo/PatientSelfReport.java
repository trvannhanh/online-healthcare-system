package com.can.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

/**
 * 
 * @author DELL
 */
@Entity
@Table(name = "patient_self_reports")
public class PatientSelfReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false,  unique = true)
    private Patient patient;
    
    // Chiều cao (cm)
    @Column(name = "height")
    private Float height;
    
    // Cân nặng (kg)
    @Column(name = "weight")
    private Float weight;
    
    // Tiền sử bệnh bản thân
    @Column(name = "personal_medical_history", columnDefinition = "TEXT")
    private String personalMedicalHistory;
    
    // Tiền sử bệnh người thân
    @Column(name = "family_medical_history", columnDefinition = "TEXT")
    private String familyMedicalHistory;
    
    // Tiểu sử thai sản
    @Column(name = "pregnancy_history", columnDefinition = "TEXT")
    private String pregnancyHistory;

        // Nhóm máu (A, B, AB, O với RH+ hoặc RH-)
    @Column(name = "blood_type", length = 5)
    private String bloodType;
    
    // Dị ứng thuốc
    @Column(name = "medication_allergies", columnDefinition = "TEXT")
    private String medicationAllergies;
    
    // Thuốc đang sử dụng
    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;
    
    // Đang điều trị bệnh gì
    @Column(name = "current_treatments", columnDefinition = "TEXT")
    private String currentTreatments;
    
    // Ngày tạo báo cáo
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    // Constructor
    public PatientSelfReport() {
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public Float getHeight() {
        return height;
    }
    
    public void setHeight(Float height) {
        this.height = height;
    }
    
    public Float getWeight() {
        return weight;
    }
    
    public void setWeight(Float weight) {
        this.weight = weight;
    }
    
    public String getPersonalMedicalHistory() {
        return personalMedicalHistory;
    }
    
    public void setPersonalMedicalHistory(String personalMedicalHistory) {
        this.personalMedicalHistory = personalMedicalHistory;
    }
    
    public String getFamilyMedicalHistory() {
        return familyMedicalHistory;
    }
    
    public void setFamilyMedicalHistory(String familyMedicalHistory) {
        this.familyMedicalHistory = familyMedicalHistory;
    }
    
    public String getPregnancyHistory() {
        return pregnancyHistory;
    }
    
    public void setPregnancyHistory(String pregnancyHistory) {
        this.pregnancyHistory = pregnancyHistory;
    }

        public String getBloodType() {
        return bloodType;
    }
    
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }
    
    public String getMedicationAllergies() {
        return medicationAllergies;
    }
    
    public void setMedicationAllergies(String medicationAllergies) {
        this.medicationAllergies = medicationAllergies;
    }
    
    public String getCurrentMedications() {
        return currentMedications;
    }
    
    public void setCurrentMedications(String currentMedications) {
        this.currentMedications = currentMedications;
    }
    
    public String getCurrentTreatments() {
        return currentTreatments;
    }
    
    public void setCurrentTreatments(String currentTreatments) {
        this.currentTreatments = currentTreatments;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}