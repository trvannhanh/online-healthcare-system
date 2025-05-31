/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.can.repositories;

import com.can.pojo.User;
import java.util.List;

/**
 *
 * @author Giidavibe
 */
public interface UserRepository {
    User getUserById(int id);
    List<User> getAllUsers();
    User getUserByUsername(String username);
    User addUser(User u);
    boolean updateUser(User user);
    boolean deleteUser(int id);
    boolean authenticate(String username, String password);
    
    boolean isEmailExist(String email);
    String getEmailByUserId(int userId);
    List<String> getAllEmails();
    List<User> getUsersByRole(String role);
    public User findByEmail(String email);
    public User findByPhoneNumber(String phoneNumber);
    public User findByIdentityNumber(String identityNumber);
}
