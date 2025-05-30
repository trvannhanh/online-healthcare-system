/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.repositories;
import com.can.pojo.HealthRecord;

import java.util.List;
import java.util.Map;
/**
 *
 * @author DELL
 */
public interface HealthRecordRepository {
    HealthRecord createHealthRecord(HealthRecord healthRecord);
    HealthRecord getHealthRecordByAppointmentId(Integer appointmentId);
}
