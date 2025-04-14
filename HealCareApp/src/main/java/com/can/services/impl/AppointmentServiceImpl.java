/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.repositories.AppointmentRepository;
import com.can.services.AppointmentService;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */
@Service
public class AppointmentServiceImpl implements AppointmentService{
    
    @Autowired
    private AppointmentRepository appRepo;

    @Override
    public List<Appointment> getAppointments(Map<String, String> params) throws ParseException {
        return this.appRepo.getAppointments(params);
    }

    @Override
    public List<Appointment> getAllAppointments() throws ParseException {
        return this.appRepo.getAllAppointments();
    }

    @Override
    public Appointment getAppointmentById(int id) {
        return this.appRepo.getAppointmentById(id);
    }

    @Override
    public Appointment addAppointment(Appointment appointment) {
        return this.appRepo.addAppointment(appointment);
    }

    @Override
    public Appointment updateAppointment(Appointment appointment) {
        return this.appRepo.updateAppointment(appointment);
    }

    @Override
    public void deleteAppointment(int id) {
        this.appRepo.deleteAppointment(id);
    }

    @Override
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status, int page) {
        return this.appRepo.getAppointmentsByStatus(status, page);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(int doctorId, int page) {
        return this.appRepo.getAppointmentsByDoctor(doctorId, page);
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(int patientId, int page) {
        return this.appRepo.getAppointmentsByPatient(patientId, page);
    }
    
    
    
}
