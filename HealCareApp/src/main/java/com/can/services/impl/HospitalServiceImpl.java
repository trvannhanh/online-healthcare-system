/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Hospital;
import com.can.repositories.HospitalRepository;
import com.can.services.HospitalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */

@Service
public class HospitalServiceImpl implements HospitalService{

    @Autowired
    private HospitalRepository hosRepo;
    
    @Override
    public Hospital getHospitalById(int id) {
        return this.hosRepo.getHospitalById(id);
    }

    @Override
    public List<Hospital> getAllHospitals() {
        return this.hosRepo.getAllHospitals();
    }

    @Override
    public Hospital getHospitalByName(String name) {
        return this.hosRepo.getHospitalByName(name);
    }

    @Override
    public Hospital addHospital(Hospital hospital) {
        return this.hosRepo.addHospital(hospital);
    }

    @Override
    public boolean updateHospital(Hospital hospital) {
        return this.hosRepo.updateHospital(hospital);
    }

    @Override
    public boolean deleteHospital(int id) {
        return this.hosRepo.deleteHospital(id);
    }
    
}
