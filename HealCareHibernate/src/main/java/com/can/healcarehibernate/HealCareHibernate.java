/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.can.healcarehibernate;

import com.can.pojo.Doctor;
import com.can.pojo.Role;
import com.can.pojo.User;
import com.can.repository.impl.DoctorRepositoryImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Giidavibe
 */
public class HealCareHibernate {

    public static void main(String[] args) {
//        DoctorRepositoryImpl s2 = new DoctorRepositoryImpl();
        DoctorRepositoryImpl repo = new DoctorRepositoryImpl();
        
//        Map<String, String> params = new HashMap<>();
//        
//        
//        // Kiểm tra dữ liệu trả về
//        s2.getDoctors(params).forEach(p -> System.out.printf("%d - %s - %s\n", 
//                p.getId(), p.getUser().getLastName(), p.getUser().getFirstName()));
        
        // 1. Test thêm bác sĩ mới
        User newUser = new User();
        newUser.setFirstName("Charlie");
        newUser.setLastName("Davis");
        newUser.setUsername("charliedavis4");
        newUser.setPassword("password123");
        newUser.setEmail("charlie4@example.com");
        newUser.setPhoneNumber("1234561555");
        newUser.setRole(Role.DOCTOR);

        Doctor newDoctor = new Doctor();
        newDoctor.setUser(newUser);
        newDoctor.setSpecialization("Orthopedics");
        newDoctor.setHospital("General Hospital");
        newDoctor.setLicenseNumber("LIC543555");
        newDoctor.setIsVerified(false);
        newDoctor.setBio("Orthopedic specialist");
        newDoctor.setExperienceYears(12);
        newDoctor.setRating(4.8f);

        newDoctor = repo.addDoctor(newDoctor);
        System.out.println("Added new doctor with ID: " + newDoctor.getId());
        System.out.println("User ID: " + newDoctor.getUser().getId());
        System.out.println("User Name: " + newDoctor.getUser().getFirstName() + " " + newDoctor.getUser().getLastName());
        System.out.println("User Email: " + newDoctor.getUser().getEmail());
        System.out.println("User Phone: " + newDoctor.getUser().getPhoneNumber());

        // 2. Test cập nhật bác sĩ
        newDoctor.setSpecialization("Neurology");
        newDoctor.setHospital("City Hospital");
        newDoctor.getUser().setFirstName("Charles");
        newDoctor.getUser().setEmail("charles.davis@example.com");

        newDoctor = repo.updateDoctor(newDoctor);
        System.out.println("Updated doctor ID: " + newDoctor.getId());
        System.out.println("Updated Specialization: " + newDoctor.getSpecialization());
        System.out.println("Updated Hospital: " + newDoctor.getHospital());
        System.out.println("Updated User Name: " + newDoctor.getUser().getFirstName() + " " + newDoctor.getUser().getLastName());
        System.out.println("Updated User Email: " + newDoctor.getUser().getEmail());
//        
        // 1. Test lấy tất cả bác sĩ
        List<Doctor> allDoctors = repo.getAllDoctors();
        System.out.println("All doctors: " + allDoctors.size());
        for (Doctor doctor : allDoctors) {
            System.out.println("Doctor ID: " + doctor.getId());
            System.out.println("Name: " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());
            System.out.println("Email: " + doctor.getUser().getEmail());
            System.out.println("Phone: " + doctor.getUser().getPhoneNumber());
            System.out.println("Specialization: " + doctor.getSpecialization());
            System.out.println("Hospital: " + doctor.getHospital());
            System.out.println("---");
        }

        // 2. Test lấy bác sĩ theo ID
        Doctor doctor = repo.getDoctorById(1); // Thay ID bằng ID thực tế trong DB
        if (doctor != null) {
            System.out.println("Doctor ID: " + doctor.getId());
            System.out.println("Name: " + doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName());
            System.out.println("Email: " + doctor.getUser().getEmail());
            System.out.println("Phone: " + doctor.getUser().getPhoneNumber());
            System.out.println("Specialization: " + doctor.getSpecialization());
            System.out.println("Hospital: " + doctor.getHospital());
        } else {
            System.out.println("Doctor not found");
        }

        // 3. Test lấy danh sách bác sĩ chưa xác nhận
        List<Doctor> unverifiedDoctors = repo.getUnverifiedDoctors(1);
        System.out.println("Unverified doctors: " + unverifiedDoctors.size());
        for (Doctor d : unverifiedDoctors) {
            System.out.println("Doctor ID: " + d.getId());
            System.out.println("Name: " + d.getUser().getFirstName() + " " + d.getUser().getLastName());
            System.out.println("Email: " + d.getUser().getEmail());
            System.out.println("Phone: " + d.getUser().getPhoneNumber());
            System.out.println("---");
        }
    }
}
