/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.repositories;

import com.can.pojo.Hospital;
import java.util.List;


/**
 *
 * @author Giidavibe
 */


public interface HospitalRepository {
    Hospital getHospitalById(int id);
    List<Hospital> getAllHospitals();
    Hospital getHospitalByName(String name);
    Hospital addHospital(Hospital hospital);
    boolean updateHospital(Hospital hospital);
    boolean deleteHospital(int id);
}
