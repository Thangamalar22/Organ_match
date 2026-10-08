package com.organmatch.service;

import com.organmatch.dto.ChangePasswordDTO;
import com.organmatch.dto.UserFormDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.Role;
import com.organmatch.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing system user administration, password changes, and audit logging.
 */
@Service
@Slf4j
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional(readOnly = true)
    public AppUser getUserByUsername(String username) {
        String cleanUsername = username != null ? username.trim() : "";
        return userRepository.findByUsernameIgnoreCase(cleanUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public AppUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    @Transactional
    public AppUser createUser(UserFormDTO form, String adminUsername) {
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("Username '" + form.getUsername() + "' is already taken");
        }

        if (form.getRole() == Role.HOSPITAL && (form.getHospitalName() == null || form.getHospitalName().isBlank())) {
            throw new IllegalArgumentException("Hospital name is required for HOSPITAL role");
        }

        AppUser user = AppUser.builder()
                .username(form.getUsername().trim())
                .fullName(form.getFullName().trim())
                .role(form.getRole())
                .hospitalName(form.getRole() == Role.HOSPITAL ? form.getHospitalName().trim() : null)
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .enabled(true)
                .build();

        AppUser savedUser = userRepository.save(user);
        log.info("USER CREATED: username={}, role={}, hospital={}, createdBy={}",
                savedUser.getUsername(), savedUser.getRole(), savedUser.getHospitalName(), adminUsername);

        return savedUser;
    }

    @Transactional
    public void toggleUserStatus(Long userId, String adminUsername) {
        AppUser targetUser = getUserById(userId);

        if (targetUser.getUsername().equalsIgnoreCase(adminUsername)) {
            throw new IllegalArgumentException("An admin cannot disable their own account");
        }

        targetUser.setEnabled(!targetUser.isEnabled());
        userRepository.save(targetUser);

        log.info("USER STATUS TOGGLED: targetUser={}, newEnabledStatus={}, performedBy={}",
                targetUser.getUsername(), targetUser.isEnabled(), adminUsername);
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword, String adminUsername) {
        AppUser targetUser = getUserById(userId);

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        targetUser.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(targetUser);

        log.info("USER PASSWORD RESET: targetUser={}, performedBy={}",
                targetUser.getUsername(), adminUsername);
    }

    @Transactional
    public void deleteUser(Long userId, String adminUsername) {
        AppUser targetUser = getUserById(userId);

        if (targetUser.getUsername().equalsIgnoreCase(adminUsername)) {
            throw new IllegalArgumentException("An admin cannot delete their own account");
        }

        userRepository.delete(targetUser);
        log.info("USER DELETED: targetUser={}, performedBy={}",
                targetUser.getUsername(), adminUsername);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordDTO dto) {
        AppUser user = getUserByUsername(username);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        log.info("USER PASSWORD CHANGED: username={}", username);
    }
}
