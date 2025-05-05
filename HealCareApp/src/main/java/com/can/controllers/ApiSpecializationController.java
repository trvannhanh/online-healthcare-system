/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.Hospital;
import com.can.pojo.Specialization;
import com.can.services.SpecializationService;
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
public class ApiSpecializationController {
    @Autowired
    private SpecializationService specService;
    
    @GetMapping("/specialization")
    public ResponseEntity<List<Specialization>> list() {
        return new ResponseEntity<>(this.specService.getAllSpecializations(), HttpStatus.OK);
    }
}
