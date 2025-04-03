/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.can.healcarehibernate;

import com.can.repository.impl.DoctorRepositoryImpl;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Giidavibe
 */
public class HealCareHibernate {

    public static void main(String[] args) {
        DoctorRepositoryImpl s2 = new DoctorRepositoryImpl();
        
        Map<String, String> params = new HashMap<>();
        
        
        // Kiểm tra dữ liệu trả về
        s2.getDoctors(params).forEach(p -> System.out.printf("%d - %s - %s\n", 
                p.getId(), p.getUser().getLastName(), p.getUser().getFirstName()));
    }
}
