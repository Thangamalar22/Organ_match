package com.organmatch.controller;

import com.organmatch.service.DashboardDTO;
import com.organmatch.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Controller serving role-based dashboard views and root URL redirection.
 */
@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String indexRedirect() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        DashboardDTO dashboardData = dashboardService.getDashboardData(username);
        model.addAttribute("dashboard", dashboardData);
        return "dashboard";
    }
}
