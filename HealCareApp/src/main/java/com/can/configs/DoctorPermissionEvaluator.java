/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.configs;

import com.can.pojo.User;
import com.can.services.DoctorService;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 *
 * @author Giidavibe
 */
@Component
public class DoctorPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private UserDetailsService userDetailService;

    @Autowired
    private DoctorService doctorService;

    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        User user = (User) userDetailService.loadUserByUsername(username); // Giả định UserDetailsService trả về User
        if (user == null || !user.getRole().name().equals("DOCTOR")) {
            return true; // Không phải bác sĩ, cho phép tiếp tục
        }

        // Kiểm tra trạng thái verified của bác sĩ
        return doctorService.isDoctorVerified(user.getId());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false; // Không sử dụng trong trường hợp này
    }
}
