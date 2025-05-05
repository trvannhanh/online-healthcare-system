/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.services;

import com.can.pojo.Specialization;
import java.util.List;

/**
 *
 * @author Giidavibe
 */
public interface SpecializationService {
    Specialization getSpecializationById(int id);
    List<Specialization> getAllSpecializations();
    Specialization getSpecializationByName(String name);
    Specialization addSpecialization(Specialization specialization);
    boolean updateSpecialization(Specialization specialization);
    boolean deleteSpecialization(int id);
}
