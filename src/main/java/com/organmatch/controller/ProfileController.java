package com.organmatch.controller;

import com.organmatch.dto.ChangePasswordDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Controller serving authenticated user profile view and password change requests.
 */
@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        AppUser user = userService.getUserByUsername(username);

        model.addAttribute("user", user);
        model.addAttribute("passwordForm", new ChangePasswordDTO());
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @Valid @ModelAttribute("passwordForm") ChangePasswordDTO form,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            Principal principal) {

        String username = principal != null ? principal.getName() : "admin";
        AppUser user = userService.getUserByUsername(username);

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "New password and confirmation do not match");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "profile";
        }

        try {
            userService.changePassword(username, form);
            redirectAttributes.addFlashAttribute("success", "Your password has been changed successfully!");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "profile";
        }
    }
}
