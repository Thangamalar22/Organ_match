package com.organmatch.controller;

import com.organmatch.service.ExperimentService;
import com.organmatch.service.ExperimentSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller mapping public and secured routes for login, matching runner, administration, and experiments.
 */
@Controller
public class WebController {

    private final ApiController apiController;
    private final ExperimentService experimentService;

    public WebController(ApiController apiController, ExperimentService experimentService) {
        this.apiController = apiController;
        this.experimentService = experimentService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/experiments")
    public String experiments(Model model) {
        ExperimentSummary summary = experimentService.runExperiment();
        model.addAttribute("summary", summary);
        return "experiments";
    }

}
