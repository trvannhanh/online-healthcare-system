/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.Hospital;
import com.can.services.HospitalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Giidavibe
 */

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiHospitalController {
    @Autowired
    private HospitalService hosService;
    
    @GetMapping("/hospitals")
    public ResponseEntity<List<Hospital>> list() {
        return new ResponseEntity<>(this.hosService.getAllHospitals(), HttpStatus.OK);
    }
}
