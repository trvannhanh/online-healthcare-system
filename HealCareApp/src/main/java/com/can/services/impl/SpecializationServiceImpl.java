/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Specialization;
import com.can.repositories.SpecializationRepository;
import com.can.services.SpecializationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Giidavibe
 */

@Service
public class SpecializationServiceImpl implements SpecializationService{
    
    @Autowired
    private SpecializationRepository specRepo;
    
    @Override
    public Specialization getSpecializationById(int id) {
        return this.specRepo.getSpecializationById(id);
    }

    @Override
    public List<Specialization> getAllSpecializations() {
        return this.specRepo.getAllSpecializations();
    }

    @Override
    public Specialization getSpecializationByName(String name) {
        return this.specRepo.getSpecializationByName(name);
    }

    @Override
    public Specialization addSpecialization(Specialization specialization) {
        return this.specRepo.addSpecialization(specialization);
    }

    @Override
    public boolean updateSpecialization(Specialization specialization) {
        return this.specRepo.updateSpecialization(specialization);
    }

    @Override
    public boolean deleteSpecialization(int id) {
        return this.specRepo.deleteSpecialization(id);
    }
    
}
