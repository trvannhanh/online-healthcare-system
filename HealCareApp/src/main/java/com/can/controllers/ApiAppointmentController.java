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
public class ApiAppointmentController {

    @Autowired
    private AppointmentService appService;

    @Autowired
    private com.can.services.PatientService patientService;

    @Autowired
    private com.can.services.DoctorService doctorService;

    @GetMapping("/appointments/filter")
    @CrossOrigin
    public ResponseEntity<List<Appointment>> getAppointmentsWithFilters(
            @RequestParam Map<String, String> params) {
        try {
            List<Appointment> appointments = appService.getAppointmentsWithFilters(params);
            return new ResponseEntity<>(appointments, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Autowired
    private NotificationService notiService;

    @PostMapping("/appointments")
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
//         Appointment newAppointment = null;
        try {
// <<<<<<< chi
//             newAppointment = appService.addAppointment(appointment);

//             try {
//                 if (newAppointment.getPatient() != null && newAppointment.getDoctor() != null) {
//                     // Load đầy đủ thông tin bệnh nhân và bác sĩ từ repository
//                     Patient fullPatient = patientService.getPatientById(newAppointment.getPatient().getId());
//                     Doctor fullDoctor = doctorService.getDoctorById(newAppointment.getDoctor().getId());

//                     if (fullPatient != null && fullPatient.getUser() != null &&
//                             fullDoctor != null && fullDoctor.getUser() != null) {

//                         Notifications notification = new Notifications();
//                         notification.setMessage("Bạn có lịch hẹn mới với bệnh nhân: "
//                                 + fullPatient.getUser().getFullName()
//                                 + " vào lúc "
//                                 + new SimpleDateFormat("HH:mm dd/MM/yyyy").format(newAppointment.getAppointmentDate()));
//                         notification.setUser(fullDoctor.getUser());
//                         notification.setSentAt(new Date());
//                         notification.setType(NotificationType.APPOINTMENT);
//                         notiService.addNotification(notification);
//                     }
//                 }
//             } catch (Exception ex) {
//                 // Ghi log lỗi nhưng không ảnh hưởng đến response
//                 System.err.println("Không thể tạo thông báo: " + ex.getMessage());
//                 // Có thể log thêm vào file log nếu cần
//             }
// =======
            Appointment newAppointment = appService.addAppointment(appointment);

            return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
//             if (newAppointment != null) {
//                 return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
//             }
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
//             if (newAppointment != null) {
//                 return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
//             }
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/appointments/{appointmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable(value = "appointmentId") int id) {
        this.appService.deleteAppointment(id);
    }

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

}
