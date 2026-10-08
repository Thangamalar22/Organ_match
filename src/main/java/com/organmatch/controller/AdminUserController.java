package com.organmatch.controller;

import com.organmatch.dto.UserFormDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.Role;
import com.organmatch.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controller serving Admin user management pages, user creation, status toggle, password reset, and deletion.
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(Model model, Principal principal) {
        List<AppUser> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentUsername", principal != null ? principal.getName() : "");
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("userForm", new UserFormDTO());
        model.addAttribute("allRoles", Role.values());
        return "admin/users/form";
    }

    @PostMapping("/save")
    public String saveUser(
            @Valid @ModelAttribute("userForm") UserFormDTO form,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            Principal principal) {

        if (form.getUsername() != null && !form.getUsername().isBlank()) {
            try {
                userService.getUserByUsername(form.getUsername());
                result.rejectValue("username", "error.username", "Username '" + form.getUsername() + "' is already taken");
            } catch (IllegalArgumentException expected) {
                // Username is free
            }
        }

        if (form.getRole() == Role.HOSPITAL && (form.getHospitalName() == null || form.getHospitalName().isBlank())) {
            result.rejectValue("hospitalName", "error.hospitalName", "Hospital name is required for HOSPITAL role");
        }

        if (result.hasErrors()) {
            model.addAttribute("allRoles", Role.values());
            return "admin/users/form";
        }

        userService.createUser(form, principal != null ? principal.getName() : "admin");
        redirectAttributes.addFlashAttribute("success", "User '" + form.getUsername() + "' created successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes, Principal principal) {
        try {
            userService.toggleUserStatus(id, principal != null ? principal.getName() : "admin");
            redirectAttributes.addFlashAttribute("success", "User status updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Reset@123") String newPassword,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        try {
            userService.resetUserPassword(id, newPassword, principal != null ? principal.getName() : "admin");
            redirectAttributes.addFlashAttribute("success", "Password for user reset successfully to: " + newPassword);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes, Principal principal) {
        try {
            userService.deleteUser(id, principal != null ? principal.getName() : "admin");
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
