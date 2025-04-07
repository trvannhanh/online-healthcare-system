/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Doctor;
import com.can.repositories.DoctorRepository;
import com.can.services.DoctorService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */
@Service
public class DoctorServiceImpl implements DoctorService{
    @Autowired
    private DoctorRepository docRepo;
    
    @Override
    public List<Doctor> getDoctors(Map<String, String> params) {
        return this.docRepo.getDoctors(params);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return this.docRepo.getAllDoctors();
    }
    
}
