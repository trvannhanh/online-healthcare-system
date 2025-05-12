/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.services.impl;

import com.can.pojo.Doctor;
import com.can.pojo.Hospital;
import com.can.pojo.Patient;
import com.can.pojo.Role;
import com.can.pojo.Specialization;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.can.pojo.User;
import com.can.repositories.DoctorRepository;
import com.can.repositories.HospitalRepository;
import com.can.repositories.PatientRepository;
import com.can.repositories.SpecializationRepository;
import com.can.repositories.UserRepository;
import com.can.services.UserService;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Giidavibe
 */
@Service("userDetailService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PatientRepository patRepo;

    @Autowired
    private DoctorRepository docRepo;

    @Autowired
    private SpecializationRepository specializationRepo;

    @Autowired
    private HospitalRepository hospitalRepo;

    @Autowired
    private BCryptPasswordEncoder passEncoder;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public User getUserById(int id) {
        return this.userRepo.getUserById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return this.userRepo.getAllUsers();
    }

    @Override
    public User getUserByUsername(String username) {
        return this.userRepo.getUserByUsername(username);
    }

    @Override
    public User addUser(Map<String, String> params, MultipartFile avatar) {

        // Kiểm tra dữ liệu đầu vào
        validateParams(params);

        // Kiểm tra avatar
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Avatar là bắt buộc");
        }
        //Tạo User
        User u = new User();
        u.setFirstName(params.get("firstName"));
        u.setLastName(params.get("lastName"));
        u.setPhoneNumber(params.get("phoneNumber"));
        u.setEmail(params.get("email"));
        try {
            u.setRole(Role.valueOf(params.get("role").toUpperCase()));
        } catch (Exception e) {
            System.out.println("Invalid role, defaulting to PATIENT" + e.getMessage());
            u.setRole(Role.PATIENT);
        }
        u.setUsername(params.get("username"));
        u.setPassword(this.passEncoder.encode(params.get("password")));

        if (!avatar.isEmpty()) {
            try {
                Map res = cloudinary.uploader().upload(avatar.getBytes(),
                        ObjectUtils.asMap("resource_type", "auto"));
                u.setAvatar(res.get("secure_url").toString());
            } catch (IOException ex) {
                Logger.getLogger(AppointmentServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        User savedUser = this.userRepo.addUser(u);
        System.out.println("User saved successfully: " + savedUser.getId());

        //Xử lý thông tin riêng dựa trên role
        String role = params.get("role").toUpperCase();
        if ("PATIENT".equalsIgnoreCase(role)) {
            Patient patient = new Patient();
            patient.setUser(savedUser);
            patient.setDateOfBirth(new Date(params.get("dateOfBirth")));
            patient.setInsuranceNumber(params.get("insuranceNumber"));
            this.patRepo.addPatient(patient);
            System.out.println("Patient saved successfully for user" + savedUser.getId());
        } else if ("DOCTOR".equalsIgnoreCase(role)) {
            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setLicenseNumber(params.get("licenseNumber"));
            // Xử lý Hospital
            String hospitalName = params.get("hospital");
            Hospital hospital = hospitalRepo.getHospitalByName(hospitalName);
            if (hospital == null) {
                hospital = new Hospital();
                hospital.setName(hospitalName);
                hospital = hospitalRepo.addHospital(hospital);
            }
            doctor.setHospital(hospital);
            
            String specializationName = params.get("specialization");
            Specialization specialization = specializationRepo.getSpecializationByName(specializationName);
            if (specialization == null) {
                specialization = new Specialization();
                specialization.setName(specializationName);
                specialization = specializationRepo.addSpecialization(specialization);
            }
            doctor.setSpecialization(specialization);
            this.docRepo.addDoctor(doctor);
        } else {
            throw new IllegalArgumentException("Role không hợp lệ: " + role);
        }

        return savedUser;

//        return this.userRepo.addUser(u);
    }

    private void validateParams(Map<String, String> params) {
        // Kiểm tra các trường bắt buộc
        if (!params.containsKey("username") || params.get("username").isBlank()) {
            throw new IllegalArgumentException("Username là bắt buộc");
        }
        if (!params.containsKey("email") || !params.get("email").matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        if (!params.containsKey("password") || params.get("password").isBlank()) {
            throw new IllegalArgumentException("Password là bắt buộc");
        }
        if (!params.containsKey("role") || params.get("role").isBlank()) {
            throw new IllegalArgumentException("Role là bắt buộc");
        }

        // Kiểm tra các trường bổ sung dựa trên role
        String role = params.get("role");
        if ("Patient".equalsIgnoreCase(role)) {
            if (!params.containsKey("insuranceNumber") || params.get("insuranceNumber").isBlank()) {
                throw new IllegalArgumentException("insuranceNumber là bắt buộc cho Patient");
            }
            if (!params.containsKey("dateOfBirth") || !isValidDate(params.get("dateOfBirth"))) {
                throw new IllegalArgumentException("dateOfBirth không hợp lệ cho Patient");
            }
        } else if ("Doctor".equalsIgnoreCase(role)) {
            if (!params.containsKey("specialization") || params.get("specialization").isBlank()) {
                throw new IllegalArgumentException("specialization là bắt buộc cho Doctor");
            }
            if (!params.containsKey("licenseNumber") || params.get("licenseNumber").isBlank()) {
                throw new IllegalArgumentException("licenseNumber là bắt buộc cho Doctor");
            }
            if (!params.containsKey("hospital") || params.get("hospital").isBlank()) {
                throw new IllegalArgumentException("hospital là bắt buộc cho Doctor");
            }
        } else {
            throw new IllegalArgumentException("Role không hợp lệ: " + role);
        }
    }

    private boolean isValidDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public boolean updateUser(User user) {
        return this.userRepo.updateUser(user);
    }

    @Override
    public boolean deleteUser(int id) {
        return this.userRepo.deleteUser(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = this.userRepo.getUserByUsername(username);
        if (u == null) {
            throw new UsernameNotFoundException("Invalid username!");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(), u.getPassword(), authorities);
    }

    @Override
    public boolean authenticate(String username, String password) {
        return this.userRepo.authenticate(username, password);
    }
}
