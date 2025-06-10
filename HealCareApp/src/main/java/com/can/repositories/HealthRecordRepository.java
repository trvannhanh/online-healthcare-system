/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories;
import com.can.pojo.HealthRecord;

/**
 *
 * @author DELL
 */
public interface HealthRecordRepository {
    HealthRecord createHealthRecord(HealthRecord healthRecord);
    HealthRecord getHealthRecordByAppointmentId(Integer appointmentId);
}
