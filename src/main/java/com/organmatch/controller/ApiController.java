package com.organmatch.controller;

import com.organmatch.csp.SolverConfig;
import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.model.Donor;
import com.organmatch.model.Recipient;
import com.organmatch.service.ExperimentService;
import com.organmatch.service.ExperimentSummary;
import com.organmatch.service.MatchRunnerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller exposing API endpoints for recipient querying, donor generation, matching execution,
 * and benchmark experiment execution/export.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final MatchRunnerService matchRunnerService;
    private final ExperimentService experimentService;

    public ApiController(MatchRunnerService matchRunnerService, ExperimentService experimentService) {
        this.matchRunnerService = matchRunnerService;
        this.experimentService = experimentService;
    }

    @GetMapping("/recipients")
    public List<RecipientEntity> getRecipients() {
        return matchRunnerService.getAllStoredRecipients();
    }

    @GetMapping("/donor")
    public DonorEntity getDonor() {
        List<DonorEntity> donors = matchRunnerService.getAllStoredDonors();
        return donors.isEmpty() ? null : donors.get(0);
    }

    @PostMapping("/donor/random")
    public DonorEntity generateRandomDonor(Principal principal) {
        return matchRunnerService.generateAndSaveRandomDonor(principal);
    }

    @PostMapping("/match")
    public Object match(@RequestBody MatchRequestDTO request, Principal principal) {
        SolverConfig config = SolverConfig.builder()
                .nodeConsistencyEnabled(request.isNodeConsistencyEnabled())
                .mrvEnabled(request.isMrvEnabled())
                .degreeHeuristicEnabled(request.isDegreeHeuristicEnabled())
                .lcvEnabled(request.isLcvEnabled())
                .forwardCheckingEnabled(request.isForwardCheckingEnabled())
                .allowUnassigned(request.isAllowUnassigned())
                .branchAndBoundEnabled(request.isBranchAndBoundEnabled())
                .build();

        return matchRunnerService.runAndSaveMatching(
                request.getDonorId(), config, request.getAlgorithm(), principal);
    }

    @GetMapping("/experiments")
    public ExperimentSummary runExperiments() {
        return experimentService.runExperiment();
    }

    @GetMapping("/experiments/csv")
    public ResponseEntity<String> downloadExperimentsCsv() {
        ExperimentSummary summary = experimentService.runExperiment();
        String csvData = experimentService.generateCsv(summary);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=experiment_results.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
