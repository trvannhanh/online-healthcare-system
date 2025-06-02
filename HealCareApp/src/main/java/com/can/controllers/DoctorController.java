/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.Doctor;
import com.can.pojo.Hospital;
import com.can.pojo.Specialization;
import com.can.pojo.User;
import com.can.services.DoctorService;
import com.can.services.HospitalService;
import com.can.services.SpecializationService;
import com.can.services.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/**
 *
 * @author Giidavibe
 */
@Controller
public class DoctorController {

    @Autowired
    private DoctorService docService;

    @Autowired
    private UserService userService;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private SpecializationService specializationService;

    @GetMapping("/doctors")
    public String doctors(Model model, @RequestParam Map<String, String> params) {
        model.addAttribute("doctors", this.docService.getDoctors(params));
        return "doctors/doctors";
    }

    @GetMapping("/doctors/loadMore")
    @ResponseBody
    public List<Doctor> loadMoreDoctors(@RequestParam Map<String, String> params) {
        return docService.getDoctors(params);
    }

    @GetMapping("/doctors/add")
    public String addDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "doctors/add_doctors";
    }

    @PostMapping("/doctors/verify/{id}")
    public String verifyDoctor(@PathVariable("id") int id) {
        docService.verifyDoctor(id);
        return "redirect:/doctors";
    }

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable("id") int id) {
        docService.deleteDoctor(id);
        return "redirect:/doctors";
    }

    @GetMapping("/doctors/edit/{id}")
    public String editDoctorForm(@PathVariable("id") int id, Model model) {
        Doctor doctor = docService.getDoctorById(id);
        model.addAttribute("doctor", doctor);
        return "doctors/edit_doctor";
    }

    @PostMapping("/doctors/edit/{id}")
    public String editDoctor(
            @PathVariable("id") int id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email,
            @RequestParam(value = "licenseNumber", required = false) String licenseNumber,
            @RequestParam(value = "experienceYears", required = false) Integer experienceYears,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam("hospital") String hospitalName,
            @RequestParam("specialization") String specializationName,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            if (firstName == null || firstName.trim().isEmpty()
                    || lastName == null || lastName.trim().isEmpty()
                    || phoneNumber == null || phoneNumber.trim().isEmpty()
                    || email == null || email.trim().isEmpty()
                    || hospitalName == null || hospitalName.trim().isEmpty()
                    || specializationName == null || specializationName.trim().isEmpty()) {
                throw new IllegalArgumentException("Các trường bắt buộc (Họ, Tên, Số điện thoại, Email, Bệnh viện, Chuyên khoa) không được để trống.");
            }

            Doctor doctor = docService.getDoctorById(id);
            if (doctor == null) {
                throw new RuntimeException("Doctor with ID " + id + " not found");
            }

            User user = doctor.getUser();
            if (user == null) {
                throw new RuntimeException("User information is required for a Doctor");
            }
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            user.setEmail(email);

            if (licenseNumber != null) {
                doctor.setLicenseNumber(licenseNumber);
            }
            if (experienceYears != null) {
                doctor.setExperienceYears(experienceYears);
            }
            if (bio != null) {
                doctor.setBio(bio);
            }

            Hospital hospital = hospitalService.getHospitalByName(hospitalName);
            if (hospital == null) {
                hospital = new Hospital();
                hospital.setName(hospitalName);
                hospital = hospitalService.addHospital(hospital);
            }
            doctor.setHospital(hospital);

            Specialization specialization = specializationService.getSpecializationByName(specializationName);
            if (specialization == null) {
                specialization = new Specialization();
                specialization.setName(specializationName);
                specialization = specializationService.addSpecialization(specialization);
            }
            doctor.setSpecialization(specialization);

            if (avatar != null && !avatar.isEmpty()) {
                userService.updateAvatar(doctor.getId(), avatar);
            }

            docService.updateDoctor(doctor);
            redirectAttributes.addFlashAttribute("success", "Cập nhật bác sĩ thành công!");
            return "redirect:/doctors";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi cập nhật bác sĩ: " + e.getMessage());
            model.addAttribute("doctor", docService.getDoctorById(id));
            return "edit_doctor";
        }
    }

    @PostMapping("/doctors/add")
    public String addDoctor(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email,
            @RequestParam("licenseNumber") String licenseNumber,
            @RequestParam("experienceYears") int experienceYears,
            @RequestParam("bio") String bio,
            @RequestParam("hospital") String hospital,
            @RequestParam("specialization") String specialization,
            @RequestParam("avatar") MultipartFile avatar,
            Model model) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("firstName", firstName);
            params.put("lastName", lastName);
            params.put("username", username);
            params.put("password", password);
            params.put("phoneNumber", phoneNumber);
            params.put("email", email);
            params.put("role", "DOCTOR");
            params.put("licenseNumber", licenseNumber);
            params.put("hospital", hospital);
            params.put("specialization", specialization);

            userService.addUser(params, avatar);
            return "redirect:/doctors";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("doctor", new Doctor());
            return "doctors/add_doctors";
        }
    }
}
