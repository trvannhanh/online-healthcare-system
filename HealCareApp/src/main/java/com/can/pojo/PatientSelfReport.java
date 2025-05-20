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
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}