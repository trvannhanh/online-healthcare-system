/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.services.PatientService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Giidavibe
 */
@Controller
public class PatientController {
    @Autowired
    private PatientService patService;
    
    @GetMapping("/patients")
    public String doctors(Model model, @RequestParam Map<String, String> params){
        model.addAttribute("patients", this.patService.getPatients(params));
        return "patients/patients";
    }
}
