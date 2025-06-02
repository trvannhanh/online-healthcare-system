/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Doctor;
import com.can.pojo.NotificationType;
import com.can.pojo.Notifications;
import com.can.pojo.Patient;
import com.can.services.AppointmentService;
import com.can.services.NotificationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.can.pojo.User;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 *
 * @author Giidavibe
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiAppointmentController {

    @Autowired
    private AppointmentService appService;

    @PostMapping("/secure/appointments")
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment, Principal principal) {
        try {
            Appointment newAppointment = appService.addAppointment(appointment, principal.getName());
            return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PatchMapping("/secure/appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable("id") int id, Principal principal) {
        try {
            Appointment updatedAppointment = appService.cancelAppointment(id, principal.getName());
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/secure/appointments/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(@PathVariable("id") int id, @RequestBody Map<String, String> body,
            Principal principal) {
        try {
            String newDateStr = body.get("newDateTime");
            Date newDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(newDateStr);
            Appointment updatedAppointment = appService.rescheduleAppointment(id, newDate, principal.getName());
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/appointments/filter")
    public ResponseEntity<List<Appointment>> getAppointmentsWithFilters(
            @RequestParam Map<String, String> params) {
        try {
            List<Appointment> appointments = appService.getAppointmentsWithFilters(params);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("secure/doctors/{doctorId}/available-slots")
    public ResponseEntity<?> getAvailableSlots(
            @PathVariable("doctorId") int doctorId,
            @RequestParam("date") String date,
            Principal principal) {
        try {
            List<String> availableSlots = appService.getAvailableSlots(doctorId, date);
            return new ResponseEntity<>(availableSlots, HttpStatus.OK);
        } catch (ParseException e) {
            return new ResponseEntity<>("Định dạng ngày không hợp lệ", HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/appointments/{appointmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable(value = "appointmentId") int id) {
        this.appService.deleteAppointment(id);
    }

    

    @PutMapping("/appointments/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        try {
            appointment.setId(id.intValue());
            Appointment updatedAppointment = appService.updateAppointment(appointment);
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            } else if (e.getMessage().contains("less than 24 hours")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/secure/appointments/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable("id") int id, Principal principal) {
        try {
            Appointment updatedAppointment = appService.confirmAppointment(id, principal.getName());
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/appointments/detail/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable("id") int id) {
        try {
            Appointment appointment = appService.getAppointmentById(id);
            if (appointment != null) {
                return new ResponseEntity<>(appointment, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
