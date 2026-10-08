package com.organmatch.service;

import com.organmatch.csp.SolverConfig;
import com.organmatch.data.DataGenerator;
import com.organmatch.model.Donor;
import com.organmatch.model.Recipient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MatchingServiceTest {

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private DataGenerator dataGenerator;

    @Test
    public void testCspUtilityGreaterThanOrEqualToGreedy() {
        List<Recipient> recipients = dataGenerator.generateRecipients(300);
        Donor donor = dataGenerator.generateDonor();

        SolverConfig config = SolverConfig.builder()
                .nodeConsistencyEnabled(true)
                .mrvEnabled(true)
                .lcvEnabled(true)
                .forwardCheckingEnabled(true)
                .allowUnassigned(true)
                .branchAndBoundEnabled(true)
                .build();

        ComparisonResult comparison = matchingService.compare(donor, recipients, config);

        assertNotNull(comparison);
        assertNotNull(comparison.getCspResult());
        assertNotNull(comparison.getGreedyResult());

        double cspUtility = comparison.getCspResult().getTotalUtility();
        double greedyUtility = comparison.getGreedyResult().getTotalUtility();

        System.out.println("--------------------------------------------------");
        System.out.println("Matching Service Benchmark Test (n=300 recipients):");
        System.out.println("CSP Total Utility:    " + cspUtility);
        System.out.println("Greedy Total Utility: " + greedyUtility);
        System.out.println("Utility Gain (%):     " + comparison.getUtilityPercentageGain() + "%");
        System.out.println("--------------------------------------------------");

        assertTrue(cspUtility >= greedyUtility,
                String.format("CSP utility (%.3f) should be >= Greedy utility (%.3f)", cspUtility, greedyUtility));
    }
}
