package com.organmatch.controller;

import com.organmatch.entity.AppUser;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.entity.Role;
import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.repository.RecipientEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class RecipientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipientEntityRepository recipientRepository;

    @Autowired
    private AppUserRepository userRepository;

    @BeforeEach
    public void setUp() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(AppUser.builder()
                    .username("admin")
                    .passwordHash("hash")
                    .fullName("System Admin")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());
        }
        if (!userRepository.existsByUsername("hospital1")) {
            userRepository.save(AppUser.builder()
                    .username("hospital1")
                    .passwordHash("hash")
                    .fullName("Apollo Coordinator")
                    .role(Role.HOSPITAL)
                    .hospitalName("Apollo Chennai")
                    .enabled(true)
                    .build());
        }
        if (!userRepository.existsByUsername("hospital2")) {
            userRepository.save(AppUser.builder()
                    .username("hospital2")
                    .passwordHash("hash")
                    .fullName("AIIMS Coordinator")
                    .role(Role.HOSPITAL)
                    .hospitalName("AIIMS Delhi")
                    .enabled(true)
                    .build());
        }

        RecipientEntity r1 = RecipientEntity.builder()
                .id("RTEST-1")
                .name("Apollo Recipient")
                .bloodGroup(BloodGroup.O)
                .age(45)
                .weightKg(65.0)
                .city(City.CHENNAI)
                .neededOrgan(OrganType.KIDNEY)
                .urgency(7)
                .waitingDays(120)
                .hlaString("1,2,3,4,5,6")
                .hospitalName("Apollo Chennai")
                .registeredBy("hospital1")
                .medicallyFit(true)
                .build();
        recipientRepository.save(r1);

        RecipientEntity r2 = RecipientEntity.builder()
                .id("RTEST-2")
                .name("AIIMS Recipient")
                .bloodGroup(BloodGroup.A)
                .age(50)
                .weightKg(70.0)
                .city(City.DELHI)
                .neededOrgan(OrganType.LIVER)
                .urgency(8)
                .waitingDays(200)
                .hlaString("2,3,4,5,6,7")
                .hospitalName("AIIMS Delhi")
                .registeredBy("hospital2")
                .medicallyFit(true)
                .build();
        recipientRepository.save(r2);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminViewListAndCreateRecipient() throws Exception {
        mockMvc.perform(get("/recipients"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipients/list"))
                .andExpect(model().attributeExists("recipientsPage"));

        mockMvc.perform(post("/recipients/save")
                        .with(csrf())
                        .param("name", "New Admin Recipient")
                        .param("bloodGroup", "B")
                        .param("age", "30")
                        .param("weightKg", "60.0")
                        .param("city", "MUMBAI")
                        .param("neededOrgan", "HEART")
                        .param("urgency", "9")
                        .param("waitingDays", "50")
                        .param("hla1", "1").param("hla2", "2").param("hla3", "3")
                        .param("hla4", "4").param("hla5", "5").param("hla6", "6")
                        .param("hospitalName", "Apollo Chennai")
                        .param("medicallyFit", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipients"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "hospital1", roles = {"HOSPITAL"})
    public void testHospitalOwnershipValidationOnSaveAndDelete() throws Exception {
        mockMvc.perform(get("/recipients"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipients/list"));

        // Attempting to edit hospital2 recipient returns 403 Access Denied
        mockMvc.perform(get("/recipients/RTEST-2/edit"))
                .andExpect(status().isForbidden());

        // Attempting to delete hospital2 recipient returns 403 Access Denied
        mockMvc.perform(post("/recipients/RTEST-2/delete").with(csrf()))
                .andExpect(status().isForbidden());

        // Deleting own hospital recipient succeeds
        mockMvc.perform(post("/recipients/RTEST-1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recipients"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testFormValidationErrors() throws Exception {
        // Submitting invalid age (>100) and blank name triggers validation errors
        mockMvc.perform(post("/recipients/save")
                        .with(csrf())
                        .param("name", "")
                        .param("age", "150"))
                .andExpect(status().isOk())
                .andExpect(view().name("recipients/form"))
                .andExpect(model().hasErrors());
    }
}
