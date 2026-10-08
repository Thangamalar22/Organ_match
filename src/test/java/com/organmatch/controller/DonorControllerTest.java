package com.organmatch.controller;

import com.organmatch.entity.AppUser;
import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.Role;
import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.repository.DonorEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
public class DonorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DonorEntityRepository donorRepository;

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

        DonorEntity d1 = DonorEntity.builder()
                .id("DTEST-1")
                .bloodGroup(BloodGroup.O)
                .age(40)
                .weightKg(70.0)
                .city(City.CHENNAI)
                .hlaString("1,2,3,4,5,6")
                .availableOrgans(List.of(OrganType.KIDNEY, OrganType.KIDNEY, OrganType.LIVER))
                .hospitalName("Apollo Chennai")
                .registeredBy("hospital1")
                .build();
        donorRepository.save(d1);

        DonorEntity d2 = DonorEntity.builder()
                .id("DTEST-2")
                .bloodGroup(BloodGroup.B)
                .age(55)
                .weightKg(80.0)
                .city(City.DELHI)
                .hlaString("2,3,4,5,6,7")
                .availableOrgans(List.of(OrganType.HEART, OrganType.LUNGS))
                .hospitalName("AIIMS Delhi")
                .registeredBy("hospital2")
                .build();
        donorRepository.save(d2);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminViewListAndCreateDonor() throws Exception {
        mockMvc.perform(get("/donors"))
                .andExpect(status().isOk())
                .andExpect(view().name("donors/list"))
                .andExpect(model().attributeExists("donorsPage"));

        mockMvc.perform(post("/donors/save")
                        .with(csrf())
                        .param("bloodGroup", "AB")
                        .param("age", "35")
                        .param("weightKg", "65.0")
                        .param("city", "BENGALURU")
                        .param("kidneyCount", "2")
                        .param("otherOrgans", "LIVER")
                        .param("otherOrgans", "HEART")
                        .param("hla1", "1").param("hla2", "2").param("hla3", "3")
                        .param("hla4", "4").param("hla5", "5").param("hla6", "6")
                        .param("hospitalName", "Apollo Chennai"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donors"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "hospital1", roles = {"HOSPITAL"})
    public void testHospitalOwnershipValidationOnDonorSaveAndDelete() throws Exception {
        mockMvc.perform(get("/donors"))
                .andExpect(status().isOk())
                .andExpect(view().name("donors/list"));

        // Attempting to edit hospital2 donor returns 403 Access Denied
        mockMvc.perform(get("/donors/DTEST-2/edit"))
                .andExpect(status().isForbidden());

        // Attempting to delete hospital2 donor returns 403 Access Denied
        mockMvc.perform(post("/donors/DTEST-2/delete").with(csrf()))
                .andExpect(status().isForbidden());

        // Deleting own hospital donor succeeds
        mockMvc.perform(post("/donors/DTEST-1/delete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donors"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testDonorFormValidationErrors() throws Exception {
        // Submitting invalid kidney count (>2) and age (>100) triggers validation errors
        mockMvc.perform(post("/donors/save")
                        .with(csrf())
                        .param("age", "120")
                        .param("kidneyCount", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("donors/form"))
                .andExpect(model().hasErrors());
    }
}
