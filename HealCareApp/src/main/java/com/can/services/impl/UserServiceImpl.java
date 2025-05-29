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
import com.can.pojo.VerificationStatus;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;

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
        validateParams(params);

        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Ảnh đại diện là bắt buộc");
        }

        User u = new User();
        u.setFirstName(params.get("firstName"));
        u.setLastName(params.get("lastName"));
        u.setPhoneNumber(params.get("phoneNumber"));
        u.setEmail(params.get("email"));
        u.setIdentityNumber(params.get("identityNumber"));
        u.setRole(Role.valueOf(params.get("role") != null ? params.get("role").toUpperCase() : "PATIENT"));
        u.setUsername(params.get("username"));
        u.setPassword(passEncoder.encode(params.get("password")));
        u.setFailedLoginAttempts(0);
        u.setIsLocked(false);

        try {
            Map res = cloudinary.uploader().upload(avatar.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            u.setAvatar(res.get("secure_url").toString());
        } catch (IOException ex) {
            Logger.getLogger(UserServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException("Lỗi khi tải ảnh đại diện");
        }

        User savedUser = userRepo.addUser(u);

        String role = params.get("role") != null ? params.get("role").toUpperCase() : "PATIENT";
        if ("PATIENT".equals(role)) {
            Patient patient = new Patient();
            patient.setUser(savedUser);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                Date dob = sdf.parse(params.get("dateOfBirth"));
                patient.setDateOfBirth(dob);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Ngày sinh không hợp lệ, định dạng phải là yyyy-MM-dd");
            }
            patient.setInsuranceNumber(params.get("insuranceNumber"));
            patRepo.addPatient(patient);
        } else if ("DOCTOR".equals(role)) {
            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setLicenseNumber(params.get("licenseNumber"));
            doctor.setIsVerified(false);
            doctor.setVerificationStatus(VerificationStatus.PENDING);

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
            docRepo.addDoctor(doctor);
        }

        return savedUser;
    }

    private void validateParams(Map<String, String> params) {
        // Kiểm tra các trường bắt buộc
        if (params.get("firstName") == null || params.get("firstName").isBlank() || params.get("firstName").length() > 50) {
            throw new IllegalArgumentException("Họ không được để trống và tối đa 50 ký tự");
        }
        if (params.get("lastName") == null || params.get("lastName").isBlank() || params.get("lastName").length() > 50) {
            throw new IllegalArgumentException("Tên không được để trống và tối đa 50 ký tự");
        }
        if (params.get("username") == null || !params.get("username").matches("^[a-zA-Z0-9_]{3,30}$")) {
            throw new IllegalArgumentException("Tên đăng nhập phải từ 3-30 ký tự, chỉ chứa chữ, số và dấu gạch dưới");
        }
        if (params.get("password") == null || !params.get("password").matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,50}$")) {
            throw new IllegalArgumentException("Mật khẩu phải từ 8-50 ký tự, chứa chữ hoa, chữ thường, số và ký tự đặc biệt");
        }
        if (params.get("email") == null || !params.get("email").matches("^[A-Za-z0-9+_.-]+@(.+)$") || params.get("email").length() > 50) {
            throw new IllegalArgumentException("Email không hợp lệ hoặc vượt quá 50 ký tự");
        }
        if (params.get("phoneNumber") == null || !params.get("phoneNumber").matches("^\\d{10}$")) {
            throw new IllegalArgumentException("Số điện thoại phải là 10 chữ số");
        }
        if (params.get("identityNumber") != null && !params.get("identityNumber").matches("^\\d{12}$")) {
            throw new IllegalArgumentException("Số CMND/CCCD phải là 12 chữ số");
        }

        // Kiểm tra tính duy nhất
        if (userRepo.getUserByUsername(params.get("username")) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userRepo.findByEmail(params.get("email")) != null) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (userRepo.findByPhoneNumber(params.get("phoneNumber")) != null) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }
        if (params.get("identityNumber") != null && userRepo.findByIdentityNumber(params.get("identityNumber")) != null) {
            throw new IllegalArgumentException("Số CMND/CCCD đã tồn tại");
        }

        // Kiểm tra vai trò
        String role = params.get("role") != null ? params.get("role").toUpperCase() : "PATIENT";
        if (!"PATIENT".equals(role) && !"DOCTOR".equals(role)) {
            throw new IllegalArgumentException("Vai trò phải là PATIENT hoặc DOCTOR");
        }

        // Kiểm tra thông tin theo vai trò
        if ("PATIENT".equals(role)) {
            if (params.get("insuranceNumber") == null || !params.get("insuranceNumber").matches("^[A-Za-z0-9]{10,20}$")) {
                throw new IllegalArgumentException("Số bảo hiểm phải từ 10-20 ký tự, chỉ chứa chữ và số");
            }
            if (params.get("dateOfBirth") == null || !params.get("dateOfBirth").matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                throw new IllegalArgumentException("Ngày sinh phải có định dạng yyyy-MM-dd");
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                Date dob = sdf.parse(params.get("dateOfBirth"));
                if (dob.after(new Date())) {
                    throw new IllegalArgumentException("Ngày sinh không được là ngày trong tương lai");
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("Ngày sinh không hợp lệ, định dạng phải là yyyy-MM-dd");
            }
        } else if ("DOCTOR".equals(role)) {
            if (params.get("licenseNumber") == null || !params.get("licenseNumber").matches("^[A-Za-z0-9]{8,20}$")) {
                throw new IllegalArgumentException("Số giấy phép phải từ 8-20 ký tự, chỉ chứa chữ và số");
            }
            if (params.get("hospital") == null || params.get("hospital").isBlank()) {
                throw new IllegalArgumentException("Bệnh viện không được để trống");
            }
            if (params.get("specialization") == null || params.get("specialization").isBlank()) {
                throw new IllegalArgumentException("Chuyên khoa không được để trống");
            }
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
        User user = userRepo.getUserByUsername(username);
        if (user == null || user.getIsLocked()) {
            return false;
        }
        boolean isAuthenticated = this.userRepo.authenticate(username, password);
        if (isAuthenticated) {
            resetFailedLoginAttempts(username);
        }
        return isAuthenticated;
    }

    @Override
    public boolean isAccountLocked(String username) {
        User user = userRepo.getUserByUsername(username);
        return user != null && user.getIsLocked();
    }


    @Override
    public void incrementFailedLoginAttempts(String username) {
        User user = userRepo.getUserByUsername(username);
        if (user != null) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            user.setLastFailedLoginTime(new java.util.Date());
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                user.setIsLocked(true);
            }
            userRepo.updateUser(user);
        }
    }

    @Override
    public void resetFailedLoginAttempts(String username) {
        User user = userRepo.getUserByUsername(username);
        if (user != null) {
            user.setFailedLoginAttempts(0);
            user.setLastFailedLoginTime(null);
            user.setIsLocked(false);
            userRepo.updateUser(user);
        }
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return this.userRepo.getUsersByRole(role);
    }

    @Override
    public String updateAvatar(int id, MultipartFile avatar) {
        User u = this.userRepo.getUserById(id);
        if (u == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Avatar is required");
        }
        try {
            Map res = cloudinary.uploader().upload(avatar.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            u.setAvatar(res.get("secure_url").toString());
            this.userRepo.updateUser(u);
            return u.getAvatar();
        } catch (IOException ex) {
            Logger.getLogger(UserServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Error uploading avatar: " + ex.getMessage());
        }
    }

    @Override
    public boolean changePassword(String username, String password, String newPassword) {
        User u = this.userRepo.getUserByUsername(username);
        if (u == null) {
            throw new UsernameNotFoundException("Invalid username!");
        }
        if (!this.passEncoder.matches(password, u.getPassword())) {
            throw new IllegalArgumentException("Invalid password!");
        }
        u.setPassword(this.passEncoder.encode(newPassword));
        return this.userRepo.updateUser(u);
    }

    @Override
    public String updateUserAvatar(String username, MultipartFile avatar) {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        return this.updateAvatar(user.getId(), avatar);
    }

    @Override
    public boolean changeUserPassword(String currentPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return this.changePassword(username, currentPassword, newPassword);
    }
}
