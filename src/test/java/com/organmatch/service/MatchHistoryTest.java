package com.organmatch.service;

import com.organmatch.controller.MatchRequestDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.MatchRecord;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.entity.Role;
import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.repository.DonorEntityRepository;
import com.organmatch.repository.MatchRecordRepository;
import com.organmatch.repository.RecipientEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MatchHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRunnerService matchRunnerService;

    @Autowired
    private MatchRecordRepository matchRecordRepository;

    @Autowired
    private DonorEntityRepository donorRepository;

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

        DonorEntity d = DonorEntity.builder()
                .id("D-HIST-1")
                .bloodGroup(BloodGroup.O)
                .age(45)
                .weightKg(70.0)
                .city(City.CHENNAI)
                .hlaString("1,2,3,4,5,6")
                .availableOrgans(List.of(OrganType.KIDNEY, OrganType.LIVER))
                .hospitalName("Apollo Chennai")
                .registeredBy("admin")
                .build();
        donorRepository.save(d);

        RecipientEntity r = RecipientEntity.builder()
                .id("R-HIST-1")
                .name("Recipient Test")
                .bloodGroup(BloodGroup.O)
                .age(40)
                .weightKg(65.0)
                .city(City.CHENNAI)
                .neededOrgan(OrganType.KIDNEY)
                .urgency(8)
                .waitingDays(100)
                .hlaString("1,2,3,4,5,6")
                .hospitalName("Apollo Chennai")
                .registeredBy("admin")
                .medicallyFit(true)
                .build();
        recipientRepository.save(r);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testMatchingExecutionSavesMatchRecord() throws Exception {
        long initialCount = matchRecordRepository.count();

        mockMvc.perform(post("/api/match")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"donorId\":\"D-HIST-1\",\"algorithm\":\"CSP\",\"nodeConsistencyEnabled\":true,\"mrvEnabled\":true}"))
                .andExpect(status().isOk());

        assertEquals(initialCount + 1, matchRecordRepository.count());

        MatchRecord latest = matchRecordRepository.findAll().stream()
                .filter(m -> "D-HIST-1".equals(m.getDonorId()))
                .findFirst().orElse(null);

        assertNotNull(latest);
        assertEquals("admin", latest.getRunBy());
        assertEquals("CSP", latest.getAlgorithm());
        assertNotNull(latest.getResultJson());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testHistoryPageAndDetailView() throws Exception {
        matchRunnerService.runAndSaveMatching("D-HIST-1", null, "GREEDY", null);

        MatchRecord record = matchRecordRepository.findAll().get(0);

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("history/list"))
                .andExpect(model().attributeExists("historyPage"));

        mockMvc.perform(get("/history/" + record.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("history/detail"))
                .andExpect(model().attributeExists("record"))
                .andExpect(model().attributeExists("resultJson"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testHistoryCsvExport() throws Exception {
        mockMvc.perform(get("/history/export-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=match_history.csv"));
    }
}
