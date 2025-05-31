/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.can.controllers;

import com.can.pojo.User;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Giidavibe
 */

@Controller
public class AdminController {
    @Autowired
    private UserService userService;

    @GetMapping("/admins/add")
    public String addAdminForm(Model model) {
        model.addAttribute("admin", new User());
        return "admins/add_admin";
    }

    @PostMapping("/admins/add")
    public String addAdmin(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("email") String email,
            @RequestParam("identityNumber") String identityNumber,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("avatar") MultipartFile avatar,
            Model model) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("firstName", firstName);
            params.put("lastName", lastName);
            params.put("phoneNumber", phoneNumber);
            params.put("email", email);
            params.put("identityNumber", identityNumber);
            params.put("username", username);
            params.put("password", password);
            params.put("role", "ADMIN");

            userService.addUser(params, avatar);
            return "redirect:/admins";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("admin", new User());
            return "admins/add_admin";
        }
    }

    @GetMapping("/admins")
    public String admins(Model model, @RequestParam Map<String, String> params) {
        int page = Math.max(1, Integer.parseInt(params.getOrDefault("page", "1"))) - 1;
        List<User> admins = userService.getUsersByRole("ADMIN"); // Sử dụng getUsersByRole
        long totalAdmins = admins.size(); // Tổng số admin, có thể tối ưu bằng count riêng nếu cần
        int totalPages = (int) Math.ceil((double) totalAdmins / 10);

        model.addAttribute("admins", admins);
        model.addAttribute("currentPage", page + 1); // Chuyển về 1-based index cho giao diện
        model.addAttribute("totalPages", totalPages);
        return "admins/admins";
    }
    
    @PostMapping("/admins/delete/{id}")
    public String deleteAdmin(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            if (userService.deleteUser(id)) {
                redirectAttributes.addFlashAttribute("success", "Xóa quản trị viên thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy quản trị viên để xóa.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa quản trị viên: " + e.getMessage());
        }
        return "redirect:admins/admins";
    }
}
