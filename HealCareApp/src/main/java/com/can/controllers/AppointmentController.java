/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.Appointment;
import com.can.pojo.Doctor;
import com.can.pojo.Patient;
import com.can.services.AppointmentService;
import com.can.services.DoctorService;
import com.can.services.PatientService;
import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Giidavibe
 */
@Controller
public class AppointmentController {

    @Autowired
    private AppointmentService appService;
    @Autowired
    private DoctorService docService;
    @Autowired
    private PatientService patService;

    @GetMapping("/appointments")
    public String showAppointment(Model model, @RequestParam Map<String, String> params) throws ParseException {
        model.addAttribute("appointments", this.appService.getAppointments(params));
        model.addAttribute("doctors", this.docService.getAllDoctors());
        model.addAttribute("patients", this.patService.getPatients(null));
        return "appointments/appointments";
    }

    @GetMapping("/appointments/add")
    public String showAddForm(Model model) {
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("doctors", this.docService.getAllDoctors());
        model.addAttribute("patients", this.patService.getPatients(null));
        return "appointments/appointment_add";
    }

    @PostMapping("/appointments/add")
    public String addAppointment(@ModelAttribute("appointment") Appointment appointment) {
        this.appService.addAppointment(appointment); // gọi lại phương thức đã có
        return "redirect:/appointments"; // quay lại danh sách lịch hẹn
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Doctor.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                Doctor d = docService.getDoctorById(Integer.parseInt(text));
                setValue(d);
            }
        });

        binder.registerCustomEditor(Patient.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                Patient p = patService.getPatientById(Integer.valueOf(text));
                setValue(p);
            }
        });
    }
}
