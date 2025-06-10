/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.User;
import com.can.services.UserService;
import com.can.utils.JwtUtils;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Giidavibe
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiUserController {

    @Autowired
    private UserService userService;

    @PostMapping(path = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestParam Map<String, String> params, 
                                   @RequestParam(value = "avatar") MultipartFile avatar) {
        try {

            if (params.get("firstName") == null || params.get("firstName").isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Họ không được để trống");
            }
            if (params.get("lastName") == null || params.get("lastName").isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tên không được để trống");
            }
            if (params.get("username") == null || params.get("username").isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tên đăng nhập không được để trống");
            }
            if (params.get("password") == null || params.get("password").isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu không được để trống");
            }
            if (params.get("email") == null || params.get("email").isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email không được để trống");
            }
            if (params.get("phoneNumber") == null || params.get("phoneNumber").isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số điện thoại không được để trống");
            }
            if (avatar == null || avatar.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ảnh đại diện là bắt buộc");
            }

            User user = userService.addUser(params, avatar);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống, vui lòng thử lại sau");
        }
    }

    @PostMapping("/login")
    @CrossOrigin
    public ResponseEntity<?> login(@RequestBody User u) {
        try {
            if (userService.isAccountLocked(u.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Tài khoản đã bị khóa do quá nhiều lần đăng nhập thất bại. Vui lòng liên hệ quản trị viên.");
            }

            if (userService.authenticate(u.getUsername(), u.getPassword())) {
                String token = JwtUtils.generateToken(u.getUsername());
                return ResponseEntity.ok().body(Collections.singletonMap("token", token));
            } else {
                userService.incrementFailedLoginAttempts(u.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Tên đăng nhập hoặc mật khẩu không đúng");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống, vui lòng thử lại sau");
        }
        
    }

    @RequestMapping("/secure/profile")
    @ResponseBody
    @CrossOrigin
    public ResponseEntity<User> getProfile(Principal principal) {
        return new ResponseEntity<>(this.userService.getUserByUsername(principal.getName()), HttpStatus.OK);
    }

    @PostMapping("/secure/avatar")
    public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile avatar) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            if (avatar == null || avatar.isEmpty()) {
                return ResponseEntity.badRequest().body("Không có file avatar");
            }

            // Gọi service để lưu avatar
            String avatarUrl = userService.updateUserAvatar(username, avatar);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cập nhật avatar thành công");
            response.put("avatarUrl", avatarUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật avatar: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword) {

        try {
            boolean changed = userService.changeUserPassword(currentPassword, newPassword);
            if (changed) {
                return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu hiện tại không đúng");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đổi mật khẩu: " + e.getMessage());
        }
    }
}
