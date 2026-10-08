package com.organmatch.service;

import com.organmatch.csp.SolverConfig;
import com.organmatch.data.DataGenerator;
import com.organmatch.model.Donor;
import com.organmatch.model.Recipient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.organmatch.entity.RecipientEntity;
import com.organmatch.mapper.ModelEntityMapper;
import com.organmatch.repository.RecipientEntityRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for running automated experiments comparing CSP solver configurations (A, B, C, D)
 * and comparing CSP against Greedy baseline over 50 random donors and 500 recipients.
 */
@Service
@Slf4j
public class ExperimentService {

    private final MatchingService matchingService;
    private final RecipientEntityRepository recipientRepository;
    private final ModelEntityMapper modelEntityMapper;
    private static final long EXPERIMENT_SEED = 12345L;

    public ExperimentService(MatchingService matchingService,
                              RecipientEntityRepository recipientRepository,
                              ModelEntityMapper modelEntityMapper) {
        this.matchingService = matchingService;
        this.recipientRepository = recipientRepository;
        this.modelEntityMapper = modelEntityMapper;
    }

    /**
     * Executes automated benchmark suite across 50 donors and stored recipients.
     *
     * @return ExperimentSummary containing benchmark statistics
     */
    public ExperimentSummary runExperiment() {
        long startTime = System.currentTimeMillis();
        DataGenerator expDataGen = new DataGenerator(EXPERIMENT_SEED);

        List<RecipientEntity> recipientEntities = recipientRepository.findAll();
        List<Recipient> recipients;
        if (recipientEntities.isEmpty()) {
            recipients = expDataGen.generateRecipients(500);
            List<RecipientEntity> entitiesToSave = modelEntityMapper.toRecipientEntityList(recipients, "Apollo Chennai", "admin");
            recipientRepository.saveAll(entitiesToSave);
        } else {
            recipients = modelEntityMapper.toRecipientModelList(recipientEntities);
        }

        int recipientCount = recipients.size();
        int donorCount = 50;

        List<Donor> donors = expDataGen.generateDonors(donorCount);

        // Configuration A: Plain Backtracking
        SolverConfig configA = SolverConfig.builder()
                .nodeConsistencyEnabled(false)
                .mrvEnabled(false)
                .degreeHeuristicEnabled(false)
                .lcvEnabled(false)
                .forwardCheckingEnabled(false)
                .branchAndBoundEnabled(false)
                .allowUnassigned(true)
                .maxNodesExpanded(50000) // Node limit safety
                .build();

        // Configuration B: Backtracking + Node Consistency
        SolverConfig configB = SolverConfig.builder()
                .nodeConsistencyEnabled(true)
                .mrvEnabled(false)
                .degreeHeuristicEnabled(false)
                .lcvEnabled(false)
                .forwardCheckingEnabled(false)
                .branchAndBoundEnabled(false)
                .allowUnassigned(true)
                .maxNodesExpanded(50000)
                .build();

        // Configuration C: B + MRV + LCV
        SolverConfig configC = SolverConfig.builder()
                .nodeConsistencyEnabled(true)
                .mrvEnabled(true)
                .degreeHeuristicEnabled(true)
                .lcvEnabled(true)
                .forwardCheckingEnabled(false)
                .branchAndBoundEnabled(false)
                .allowUnassigned(true)
                .maxNodesExpanded(50000)
                .build();

        // Configuration D: Full CSP (C + Forward Checking + Branch & Bound)
        SolverConfig configD = SolverConfig.builder()
                .nodeConsistencyEnabled(true)
                .mrvEnabled(true)
                .degreeHeuristicEnabled(true)
                .lcvEnabled(true)
                .forwardCheckingEnabled(true)
                .branchAndBoundEnabled(true)
                .allowUnassigned(true)
                .maxNodesExpanded(50000)
                .build();

        double[] nodesA = new double[donorCount];
        double[] backtracksA = new double[donorCount];
        double[] timeA = new double[donorCount];
        double[] utilA = new double[donorCount];
        double[] assignedA = new double[donorCount];

        double[] nodesB = new double[donorCount];
        double[] backtracksB = new double[donorCount];
        double[] timeB = new double[donorCount];
        double[] utilB = new double[donorCount];
        double[] assignedB = new double[donorCount];

        double[] nodesC = new double[donorCount];
        double[] backtracksC = new double[donorCount];
        double[] timeC = new double[donorCount];
        double[] utilC = new double[donorCount];
        double[] assignedC = new double[donorCount];

        double[] nodesD = new double[donorCount];
        double[] backtracksD = new double[donorCount];
        double[] timeD = new double[donorCount];
        double[] utilD = new double[donorCount];
        double[] assignedD = new double[donorCount];

        double[] utilGreedy = new double[donorCount];
        double[] assignedGreedy = new double[donorCount];

        for (int i = 0; i < donorCount; i++) {
            Donor donor = donors.get(i);

            // Run Config A
            MatchResult resA = matchingService.runCSP(donor, recipients, configA);
            nodesA[i] = resA.getStats().getNodesExpanded();
            backtracksA[i] = resA.getStats().getBacktracks();
            timeA[i] = resA.getStats().getTimeMillis();
            utilA[i] = resA.getTotalUtility();
            assignedA[i] = countAssignedOrgans(resA);

            // Run Config B
            MatchResult resB = matchingService.runCSP(donor, recipients, configB);
            nodesB[i] = resB.getStats().getNodesExpanded();
            backtracksB[i] = resB.getStats().getBacktracks();
            timeB[i] = resB.getStats().getTimeMillis();
            utilB[i] = resB.getTotalUtility();
            assignedB[i] = countAssignedOrgans(resB);

            // Run Config C
            MatchResult resC = matchingService.runCSP(donor, recipients, configC);
            nodesC[i] = resC.getStats().getNodesExpanded();
            backtracksC[i] = resC.getStats().getBacktracks();
            timeC[i] = resC.getStats().getTimeMillis();
            utilC[i] = resC.getTotalUtility();
            assignedC[i] = countAssignedOrgans(resC);

            // Run Config D
            MatchResult resD = matchingService.runCSP(donor, recipients, configD);
            nodesD[i] = resD.getStats().getNodesExpanded();
            backtracksD[i] = resD.getStats().getBacktracks();
            timeD[i] = resD.getStats().getTimeMillis();
            utilD[i] = resD.getTotalUtility();
            assignedD[i] = countAssignedOrgans(resD);

            // Run Greedy Baseline
            MatchResult resG = matchingService.runGreedy(donor, recipients);
            utilGreedy[i] = resG.getTotalUtility();
            assignedGreedy[i] = countAssignedOrgans(resG);
        }

        List<ExperimentConfigResult> configResults = new ArrayList<>();
        configResults.add(buildConfigResult("A) Plain Backtracking", nodesA, backtracksA, timeA, utilA, assignedA));
        configResults.add(buildConfigResult("B) Backtracking + Node Consistency", nodesB, backtracksB, timeB, utilB, assignedB));
        configResults.add(buildConfigResult("C) B + MRV + LCV", nodesC, backtracksC, timeC, utilC, assignedC));
        configResults.add(buildConfigResult("D) C + Forward Checking + Branch & Bound", nodesD, backtracksD, timeD, utilD, assignedD));

        double avgUtilD = average(utilD);
        double avgUtilG = average(utilGreedy);
        double avgAssignedD = average(assignedD);
        double avgAssignedG = average(assignedGreedy);
        double gain = avgUtilG > 0 ? ((avgUtilD - avgUtilG) / avgUtilG) * 100.0 : 0.0;

        return ExperimentSummary.builder()
                .donorCount(donorCount)
                .recipientCount(recipientCount)
                .seed(EXPERIMENT_SEED)
                .configResults(configResults)
                .cspAvgUtility(Math.round(avgUtilD * 1000.0) / 1000.0)
                .greedyAvgUtility(Math.round(avgUtilG * 1000.0) / 1000.0)
                .cspAvgAssignedOrgans(Math.round(avgAssignedD * 10.0) / 10.0)
                .greedyAvgAssignedOrgans(Math.round(avgAssignedG * 10.0) / 10.0)
                .utilityGainPercentage(Math.round(gain * 100.0) / 100.0)
                .totalRunTimeMillis(System.currentTimeMillis() - startTime)
                .build();
    }

    /**
     * Generates a CSV string representation of the experiment summary.
     *
     * @param summary ExperimentSummary
     * @return Formatted CSV string
     */
    public String generateCsv(ExperimentSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration,Avg Nodes Expanded,Avg Backtracks,Avg Time (ms),Avg Total Utility,Avg Organs Assigned\n");
        for (ExperimentConfigResult r : summary.getConfigResults()) {
            sb.append(String.format("\"%s\",%.1f,%.1f,%.1f,%.3f,%.1f\n",
                    r.getConfigName(), r.getAvgNodesExpanded(), r.getAvgBacktracks(), r.getAvgTimeMillis(), r.getAvgTotalUtility(), r.getAvgOrgansAssigned()));
        }
        sb.append("\n");
        sb.append("Algorithm,Avg Total Utility,Avg Organs Assigned\n");
        sb.append(String.format("\"CSP Solver (Config D)\",%.3f,%.1f\n", summary.getCspAvgUtility(), summary.getCspAvgAssignedOrgans()));
        sb.append(String.format("\"Greedy Baseline\",%.3f,%.1f\n", summary.getGreedyAvgUtility(), summary.getGreedyAvgAssignedOrgans()));
        sb.append(String.format("\"Utility Gain (%%)\",%.2f%%\n", summary.getUtilityGainPercentage()));
        return sb.toString();
    }

    private int countAssignedOrgans(MatchResult result) {
        if (result == null || result.getAssignments() == null) return 0;
        int count = 0;
        for (var entry : result.getAssignments().entrySet()) {
            if (entry.getValue() != null) {
                count++;
            }
        }
        return count;
    }

    private ExperimentConfigResult buildConfigResult(String name, double[] nodes, double[] backtracks, double[] times, double[] utils, double[] assigned) {
        return ExperimentConfigResult.builder()
                .configName(name)
                .avgNodesExpanded(Math.round(average(nodes) * 10.0) / 10.0)
                .avgBacktracks(Math.round(average(backtracks) * 10.0) / 10.0)
                .avgTimeMillis(Math.round(average(times) * 10.0) / 10.0)
                .avgTotalUtility(Math.round(average(utils) * 1000.0) / 1000.0)
                .avgOrgansAssigned(Math.round(average(assigned) * 10.0) / 10.0)
                .build();
    }

    private double average(double[] arr) {
        if (arr == null || arr.length == 0) return 0.0;
        double sum = 0;
        for (double v : arr) sum += v;
        return sum / arr.length;
    }
}
