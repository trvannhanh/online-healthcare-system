/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.repositories;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import java.security.Principal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Giidavibe
 */
public interface AppointmentRepository {
    List<Appointment> getAppointments(Map<String, String> params) throws ParseException;
    List<Appointment> getAllAppointments() throws ParseException;
    Appointment getAppointmentById(int id);
    Appointment addAppointment(Appointment appointment);
    Appointment updateAppointment(Appointment appointment);
    void deleteAppointment(int id);
    List<Appointment> getAppointmentsByStatus(AppointmentStatus status, int page);
    List<Appointment> getAppointmentsByDoctor(int doctorId, int page);
    List<Appointment> getAppointmentsByPatient(int patientId, int page);
    Appointment cancelAppointment(int id);
    Appointment rescheduleAppointment(int id, LocalDateTime newDate);
    Appointment confirmAppointment(int id);
    List<Appointment> getAppointmentsCompleteByDateRange(Date fromDateStr, Date toDateStr) throws ParseException;
    int countDistinctPatientsByDoctorAndDateRange(int doctorId, Date fromDateStr, Date toDateStr) throws ParseException;
    int countDistinctPatientsByDoctorAndMonth(int doctorId, int year, int month) throws ParseException;
    int countDistinctPatientsByDoctorAndQuarter(int doctorId, int year, int quarter) throws ParseException;
    int countDistinctPatientsByDateRange(Date fromDateStr, Date toDateStr) throws ParseException;
    int countDistinctPatientsByQuarter(int year, int quarter) throws ParseException;
    int countDistinctPatientsByMonth(int year, int month) throws ParseException;
}
