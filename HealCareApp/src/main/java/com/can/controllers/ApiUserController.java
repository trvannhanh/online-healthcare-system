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
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

//    @PostMapping("/users")
//    public ResponseEntity<User> create(@RequestParam Map<String, String> params,
//            @RequestParam("avatar") MultipartFile avatar) {
//        User u = this.userService.addUser(params, avatar);
//
//        return new ResponseEntity<>(u, HttpStatus.CREATED);
//    }
    @PostMapping(path = "/users",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> create(@RequestParam Map<String, String> params,
            @RequestParam(value = "avatar") MultipartFile avatar) {
        return new ResponseEntity<>(this.userService.addUser(params, avatar), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User u) {

        if (this.userService.authenticate(u.getUsername(), u.getPassword())) {
            try {
                String token = JwtUtils.generateToken(u.getUsername());
                return ResponseEntity.ok().body(Collections.singletonMap("token", token));
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Lỗi khi tạo JWT");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai thông tin đăng nhập");
    }

    @RequestMapping("/secure/profile")
    @ResponseBody
    @CrossOrigin
    public ResponseEntity<User> getProfile(Principal principal) {
        return new ResponseEntity<>(this.userService.getUserByUsername(principal.getName()), HttpStatus.OK);
    }
}
