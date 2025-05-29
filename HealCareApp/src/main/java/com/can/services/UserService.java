/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.services;

import com.can.pojo.User;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Giidavibe
 */
public interface UserService extends UserDetailsService{
    User getUserById(int id);
    List<User> getAllUsers();
    User getUserByUsername(String username);
    User addUser(Map<String, String> params, MultipartFile avatar);
    boolean updateUser(User user);
    boolean deleteUser(int id);
    boolean authenticate(String username, String password);
    List<User> getUsersByRole(String role);
    String updateAvatar(int id, MultipartFile avatar);
    
    public boolean isAccountLocked(String username);
    public void incrementFailedLoginAttempts(String username);
    public void resetFailedLoginAttempts(String username);
    
    boolean changePassword(String username, String password, String newPassword);

    String updateUserAvatar(String username, MultipartFile avatar);
    boolean changeUserPassword(String currentPassword, String newPassword);
}
