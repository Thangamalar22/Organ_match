package com.organmatch.controller;

import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.service.MatchRunnerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

/**
 * Controller serving the stored-data organ matching page (/match).
 */
@Controller
public class MatchController {

    private final MatchRunnerService matchRunnerService;

    public MatchController(MatchRunnerService matchRunnerService) {
        this.matchRunnerService = matchRunnerService;
    }

    @GetMapping("/match")
    public String showMatchingPage(Model model, Principal principal) {
        List<DonorEntity> donors = matchRunnerService.getAllStoredDonors();
        if (donors.isEmpty()) {
            donors = List.of(matchRunnerService.generateAndSaveRandomDonor(principal));
        }

        List<RecipientEntity> recipients = matchRunnerService.getAllStoredRecipients();

        model.addAttribute("donors", donors);
        model.addAttribute("selectedDonor", donors.get(0));
        model.addAttribute("recipientsCount", recipients.size());

        return "match";
    }
}
