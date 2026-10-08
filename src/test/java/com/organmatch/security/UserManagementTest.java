package com.organmatch.security;

import com.organmatch.dto.ChangePasswordDTO;
import com.organmatch.dto.UserFormDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.Role;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserManagementTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(AppUser.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .fullName("System Admin")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());
        }
        if (!userRepository.existsByUsername("hospital1")) {
            userRepository.save(AppUser.builder()
                    .username("hospital1")
                    .passwordHash(passwordEncoder.encode("Hosp@123"))
                    .fullName("Apollo Coordinator")
                    .role(Role.HOSPITAL)
                    .hospitalName("Apollo Chennai")
                    .enabled(true)
                    .build());
        }
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminUserCreationValidationAndBCrypt() throws Exception {
        UserFormDTO dto = UserFormDTO.builder()
                .username("newhospital")
                .fullName("New Hospital User")
                .role(Role.HOSPITAL)
                .hospitalName("Fortis Hospital")
                .password("Password@123")
                .build();

        AppUser created = userService.createUser(dto, "admin");
        assertNotNull(created.getId());
        assertTrue(passwordEncoder.matches("Password@123", created.getPasswordHash()));

        // Duplicate username throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto, "admin"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminCannotDisableOrDeleteSelf() throws Exception {
        AppUser adminUser = userService.getUserByUsername("admin");

        assertThrows(IllegalArgumentException.class, () -> userService.toggleUserStatus(adminUser.getId(), "admin"));
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(adminUser.getId(), "admin"));
    }

    @Test
    @WithMockUser(username = "hospital1", roles = {"HOSPITAL"})
    public void testProfilePasswordChange() throws Exception {
        ChangePasswordDTO dto = ChangePasswordDTO.builder()
                .currentPassword("Hosp@123")
                .newPassword("NewSecret@123")
                .confirmPassword("NewSecret@123")
                .build();

        userService.changePassword("hospital1", dto);

        AppUser updated = userService.getUserByUsername("hospital1");
        assertTrue(passwordEncoder.matches("NewSecret@123", updated.getPasswordHash()));

        // Invalid current password fails
        ChangePasswordDTO invalidCurrent = ChangePasswordDTO.builder()
                .currentPassword("WrongPassword")
                .newPassword("AnotherNew@123")
                .confirmPassword("AnotherNew@123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("hospital1", invalidCurrent));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminUserManagementEndpoints() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"))
                .andExpect(model().attributeExists("users"));

        mockMvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/form"));

        mockMvc.perform(post("/admin/users/save")
                        .with(csrf())
                        .param("username", "webuser")
                        .param("fullName", "Web Created User")
                        .param("role", "COORDINATOR")
                        .param("password", "Secret12345"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"));
    }
}
