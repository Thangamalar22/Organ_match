package com.organmatch.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExperimentServiceTest {

    @Autowired
    private ExperimentService experimentService;

    @Test
    public void testRunExperiment() {
        ExperimentSummary summary = experimentService.runExperiment();

        assertNotNull(summary);
        assertEquals(50, summary.getDonorCount());
        assertTrue(summary.getRecipientCount() > 0);
        assertEquals(4, summary.getConfigResults().size());
        assertTrue(summary.getCspAvgUtility() >= summary.getGreedyAvgUtility());

        String csv = experimentService.generateCsv(summary);
        assertNotNull(csv);
        assertTrue(csv.contains("Plain Backtracking"));
        assertTrue(csv.contains("Greedy Baseline"));
    }
}
