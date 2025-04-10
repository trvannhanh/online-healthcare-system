/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.can.healcarehibernate;

import com.can.pojo.User;
import com.can.repository.impl.AppointmentRepositoryImpl;
import com.can.repository.impl.DoctorRepositoryImpl;
import com.can.repository.impl.PatientRepositoryImpl;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Giidavibe
 */
public class HealCareHibernate {

    public static void main(String[] args) {
//        DoctorRepositoryImpl doctorRepo = new DoctorRepositoryImpl();
//        PatientRepositoryImpl patientRepo = new PatientRepositoryImpl();
//        AppointmentRepositoryImpl appointmentRepo = new AppointmentRepositoryImpl();
//
//        try {
//            // 1. Tạo bác sĩ
//            User doctorUser = new User();
//            doctorUser.setFirstName("John");
//            doctorUser.setLastName("Doe");
//            doctorUser.setUsername("johndoe_" + System.currentTimeMillis());
//            doctorUser.setPassword("password123");
//            doctorUser.setEmail("john_" + System.currentTimeMillis() + "@example.com");
//            doctorUser.setPhoneNumber("1234567890");
//            doctorUser.setGender(Gender.MALE);
//            doctorUser.setRole(Role.DOCTOR);
//            doctorUser.setIdentityNumber("123456789012");
//            doctorUser.setAvatar("avatar.jpg");
//
//            Doctor doctor = new Doctor();
//            doctor.setUser(doctorUser);
//            doctor.setSpecialization("Orthopedics");
//            doctor.setHospital("General Hospital");
//            doctor.setLicenseNumber("LIC54321");
//            doctor.setIsVerified(false);
//            doctor.setBio("Orthopedic specialist");
//            doctor.setExperienceYears(12);
//            doctor.setRating(4.8f);
//
//            doctor = doctorRepo.addDoctor(doctor);
//
//            // 2. Tạo bệnh nhân
//            User patientUser = new User();
//            patientUser.setFirstName("Jane");
//            patientUser.setLastName("Smith");
//            patientUser.setUsername("janesmith_" + System.currentTimeMillis());
//            patientUser.setPassword("password123");
//            patientUser.setEmail("jane_" + System.currentTimeMillis() + "@example.com");
//            patientUser.setPhoneNumber("0987654321");
//            patientUser.setGender(Gender.FEMALE);
//            patientUser.setRole(Role.PATIENT);
//            patientUser.setIdentityNumber("987654321098");
//            patientUser.setAvatar("avatar2.jpg");
//
//            Patient patient = new Patient();
//            patient.setUser(patientUser);
//            patient.setMedicalHistory("No allergies");
//            patient.setBloodType("O+");
//
//            patient = patientRepo.addPatient(patient);
//
//            // 3. Tạo lịch hẹn
//            Appointment appointment = new Appointment();
//            appointment.setPatient(patient);
//            appointment.setDoctor(doctor);
//            appointment.setAppointmentDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse("2025-04-15 10:00"));
//            appointment.setStatus(AppointmentStatus.PENDING);
//
//            appointment = appointmentRepo.addAppointment(appointment);
//            System.out.println("Added new appointment with ID: " + appointment.getId());
//            System.out.println("Doctor: " + appointment.getDoctor().getUser().getFirstName() + " " + appointment.getDoctor().getUser().getLastName());
//            System.out.println("Patient: " + appointment.getPatient().getUser().getFirstName() + " " + appointment.getPatient().getUser().getLastName());
//            System.out.println("Appointment Date: " + appointment.getAppointmentDate());
//            System.out.println("Status: " + appointment.getStatus());
//
//            // 4. Lấy danh sách lịch hẹn theo trạng thái
//            List<Appointment> pendingAppointments = appointmentRepo.getAppointmentsByStatus(AppointmentStatus.PENDING, 1);
//            System.out.println("Pending Appointments: " + pendingAppointments.size());
//            for (Appointment appt : pendingAppointments) {
//                System.out.println("Appointment ID: " + appt.getId());
//                System.out.println("Doctor: " + appt.getDoctor().getUser().getFirstName() + " " + appt.getDoctor().getUser().getLastName());
//                System.out.println("Patient: " + appt.getPatient().getUser().getFirstName() + " " + appt.getPatient().getUser().getLastName());
//                System.out.println("Appointment Date: " + appt.getAppointmentDate());
//                System.out.println("Status: " + appt.getStatus());
//                System.out.println("---");
//            }
//
//            // 5. Cập nhật lịch hẹn
//            appointment.setStatus(AppointmentStatus.CONFIRMED);
//            appointment = appointmentRepo.updateAppointment(appointment);
//            System.out.println("Updated appointment status to: " + appointment.getStatus());
//
//            // 6. Lấy danh sách lịch hẹn của bác sĩ
//            List<Appointment> doctorAppointments = appointmentRepo.getAppointmentsByDoctor(doctor.getId(), 1);
//            System.out.println("Doctor's Appointments: " + doctorAppointments.size());
//            for (Appointment appt : doctorAppointments) {
//                System.out.println("Appointment ID: " + appt.getId());
//                System.out.println("Patient: " + appt.getPatient().getUser().getFirstName() + " " + appt.getPatient().getUser().getLastName());
//                System.out.println("Appointment Date: " + appt.getAppointmentDate());
//                System.out.println("Status: " + appt.getStatus());
//                System.out.println("---");
//            }
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//        PatientRepositoryImpl repo = new PatientRepositoryImpl();
//
//        try {
//            // 1. Test thêm bệnh nhân mới
//            User newUser = new User();
//            newUser.setFirstName("Jane");
//            newUser.setLastName("Smith");
//            newUser.setUsername("janesmith_" + System.currentTimeMillis());
//            newUser.setPassword("password123");
//            newUser.setEmail("jane_" + System.currentTimeMillis() + "@example.com");
//            newUser.setPhoneNumber("0987654321");
//            newUser.setGender(Gender.FEMALE);
//            newUser.setRole(Role.PATIENT);
//            newUser.setIdentityNumber("987654321098");
//            newUser.setAvatar("avatar2.jpg");
//
//            Patient newPatient = new Patient();
//            newPatient.setUser(newUser);
//            newPatient.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").parse("1990-05-15"));
//            newPatient.setInsuranceNumber("INS12345");
//
//            newPatient = repo.addPatient(newPatient);
//            System.out.println("Added new patient with ID: " + newPatient.getId());
//            System.out.println("User ID: " + newPatient.getUser().getId());
//            System.out.println("User Name: " + newPatient.getUser().getFirstName() + " " + newPatient.getUser().getLastName());
//            System.out.println("User Email: " + newPatient.getUser().getEmail());
//            System.out.println("User Phone: " + newPatient.getUser().getPhoneNumber());
//            System.out.println("Date of Birth: " + newPatient.getDateOfBirth());
//            System.out.println("Insurance Number: " + newPatient.getInsuranceNumber());
//
//            // 2. Test lấy danh sách bệnh nhân
//            List<Patient> patients = repo.getAllPatients();
//            System.out.println("Total patients: " + patients.size());
//            for (Patient patient : patients) {
//                System.out.println("Patient ID: " + patient.getId());
//                System.out.println("Name: " + patient.getUser().getFirstName() + " " + patient.getUser().getLastName());
//                System.out.println("Date of Birth: " + patient.getDateOfBirth());
//                System.out.println("Insurance Number: " + patient.getInsuranceNumber());
//                System.out.println("---");
//            }
//
//            // 3. Test cập nhật bệnh nhân
//            newPatient.setInsuranceNumber("INS54321");
//            newPatient = repo.updatePatient(newPatient);
//            System.out.println("Updated patient insurance number to: " + newPatient.getInsuranceNumber());
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        }
        
    }
}
