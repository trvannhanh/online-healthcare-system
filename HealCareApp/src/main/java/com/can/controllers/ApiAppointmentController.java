/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.pojo.AppointmentStatus;
import com.can.pojo.Notifications;
import com.can.services.AppointmentService;
import com.can.services.NotificationService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.can.pojo.User;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.text.ParseException;
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
public class ApiAppointmentController {

    @Autowired
    private AppointmentService appService;

    @Autowired
    private NotificationService notiService;

    @PostMapping("/appointments")
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        try {
            Appointment newAppointment = appService.addAppointment(appointment);
            Notifications notification = new Notifications();
            notification.setMessage("Bạn có lịch hẹn mới với bệnh nhân: " 
                                    + newAppointment.getPatient().getUser().getFullName()
                                    + " vào lúc " 
                                    + new SimpleDateFormat("HH:mm dd/MM/yyyy").format(newAppointment.getAppointmentDate()));
            notification.setUser(newAppointment.getDoctor().getUser()); // Người nhận thông báo là bác sĩ
            notification.setSentAt(new Date()); // Thời gian gửi thông báo
            notiService.addNotification(notification);
            return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/appointments/{appointmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable(value = "appointmentId") int id) {
        this.appService.deleteAppointment(id);
    }

//    @PatchMapping("/appointments/{appointmentId}/cancel")
//    public ResponseEntity<Appointment> cancelAppointment(@PathVariable(value = "appointmentId") int id) {
//
//        Appointment newAppointment = appService.cancelAppointment(id);
//        return new ResponseEntity<>(newAppointment, HttpStatus.OK);
////        try {
////            appointment.setId(id.intValue());
////            Appointment updatedAppointment = appService.updateAppointment(appointment);
////            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
////        } catch (RuntimeException e) {
////            if (e.getMessage().contains("not found")) {
////                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
////            } else if (e.getMessage().contains("less than 24 hours")) {
////                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
////            }
////            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
////        }
//    }
    
    
    @PatchMapping("/secure/appointments/{id}/cancel")
    @CrossOrigin
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
    @CrossOrigin
    public ResponseEntity<?> rescheduleAppointment(@PathVariable("id") int id ,@RequestBody Map<String, String> body, Principal principal) {
        try {
            String newDateStr = body.get("newDateTime");
            Appointment updatedAppointment = appService.rescheduleAppointment(id, newDateStr, principal.getName());
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi hệ thống: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    
    

    @PutMapping("/secure/appointments/{id}/confirm")
    @CrossOrigin
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

    @GetMapping("/appointments/confirm")
    public RedirectView confirmAppointmentAndRedirect(@RequestParam("id") int appointmentId) {
        Appointment appointment = appService.getAppointmentById(appointmentId);

        if (appointment != null && appointment.getStatus() == AppointmentStatus.PENDING) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appService.updateAppointment(appointment);
        }

        return new RedirectView("/appointment/appointment-confirmation-success.html");
    }

//    @GetMapping
//    public ResponseEntity<List<Appointment>> getAppointments(@RequestParam Map<String, String> params) {
//        try {
//            List<Appointment> appointments = appService.getAppointments(params);
//            return ResponseEntity.ok(appointments);
//        } catch (ParseException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    // Lấy tất cả lịch hẹn
//    @GetMapping("/all")
//    public ResponseEntity<List<Appointment>> getAllAppointments() {
//        try {
//            List<Appointment> appointments = appService.getAllAppointments();
//            return ResponseEntity.ok(appointments);
//        } catch (ParseException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    // Lấy lịch hẹn theo ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Appointment> getAppointmentById(@PathVariable int id) {
//        Appointment appointment = appService.getAppointmentById(id);
//        return appointment != null ? ResponseEntity.ok(appointment) : ResponseEntity.notFound().build();
//    }
//
//    // Thêm lịch hẹn mới
//    @PostMapping
//    public ResponseEntity<Appointment> addAppointment(@RequestBody Appointment appointment) {
//        try {
//            Appointment added = appService.addAppointment(appointment);
//            return ResponseEntity.ok(added);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
//    
//    
//    // Cập nhật lịch hẹn
//    @PutMapping("/{id}")
//    public ResponseEntity<Appointment> updateAppointment(@PathVariable int id, @RequestBody Appointment appointment) {
//        try {
//            appointment.setId(id);
//            Appointment updated = appService.updateAppointment(appointment);
//            return ResponseEntity.ok(updated);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(null);
//        }
//    }
}
