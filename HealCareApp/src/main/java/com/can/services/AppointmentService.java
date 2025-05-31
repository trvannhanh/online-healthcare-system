/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.services;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Giidavibe
 */
public interface AppointmentService {
    List<Appointment> getAppointments(Map<String, String> params) throws ParseException;
    List<Appointment> getAllAppointments() throws ParseException;
    Appointment getAppointmentById(int id);
    Appointment addAppointment(Appointment appointment, String username);
    Appointment updateAppointment(Appointment appointment);
    void deleteAppointment(int id);
    List<Appointment> getAppointmentsByStatus(AppointmentStatus status, int page);
    List<Appointment> getAppointmentsByDoctor(int doctorId, int page);
    List<Appointment> getAppointmentsByPatient(int patientId, int page);
    Appointment cancelAppointment(int id, String username);
    Appointment rescheduleAppointment(int id, Date newDate, String username);
    Appointment confirmAppointment(int id, String username);
    List<Appointment> getAppointmentsWithFilters(Map<String, String> params);
    List<String> getAvailableSlots(int doctorId, String date) throws ParseException;
}
