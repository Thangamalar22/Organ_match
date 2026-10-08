package com.organmatch.controller;

import com.organmatch.dto.RecipientFormDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.entity.Role;
import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import com.organmatch.service.RecipientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Controller serving Recipient management UI pages and CRUD endpoint handling.
 */
@Controller
@RequestMapping("/recipients")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @GetMapping
    public String listRecipients(
            @RequestParam(required = false) OrganType organ,
            @RequestParam(required = false) BloodGroup bloodGroup,
            @RequestParam(required = false) City city,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            Principal principal) {

        Page<RecipientEntity> recipientsPage = recipientService.getRecipientsPage(
                organ, bloodGroup, city, search, page, size, principal);

        AppUser currentUser = recipientService.getCurrentUser(principal);

        model.addAttribute("recipientsPage", recipientsPage);
        model.addAttribute("recipients", recipientsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", recipientsPage.getTotalPages());
        model.addAttribute("totalElements", recipientsPage.getTotalElements());

        model.addAttribute("selectedOrgan", organ);
        model.addAttribute("selectedBloodGroup", bloodGroup);
        model.addAttribute("selectedCity", city);
        model.addAttribute("search", search);

        model.addAttribute("allOrgans", OrganType.values());
        model.addAttribute("allBloodGroups", BloodGroup.values());
        model.addAttribute("allCities", City.values());
        model.addAttribute("currentUserRole", currentUser.getRole().name());
        model.addAttribute("userHospitalName", currentUser.getHospitalName());

        return "recipients/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        AppUser currentUser = recipientService.getCurrentUser(principal);
        RecipientFormDTO form = RecipientFormDTO.builder()
                .crossmatchPositive(false)
                .medicallyFit(true)
                .urgency(5)
                .waitingDays(30)
                .age(40)
                .weightKg(65.0)
                .hla1(1).hla2(2).hla3(3).hla4(4).hla5(5).hla6(6)
                .hospitalName(currentUser.getRole() == Role.HOSPITAL ? currentUser.getHospitalName() : "")
                .build();

        populateFormModel(model, currentUser, form);
        return "recipients/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model, Principal principal) {
        AppUser currentUser = recipientService.getCurrentUser(principal);
        RecipientFormDTO form = recipientService.getRecipientFormById(id, principal);
        populateFormModel(model, currentUser, form);
        return "recipients/form";
    }

    @PostMapping("/save")
    public String saveRecipient(
            @Valid @ModelAttribute("recipientForm") RecipientFormDTO form,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            Principal principal) {

        AppUser currentUser = recipientService.getCurrentUser(principal);

        if (result.hasErrors()) {
            populateFormModel(model, currentUser, form);
            return "recipients/form";
        }

        recipientService.saveRecipient(form, principal);
        redirectAttributes.addFlashAttribute("success", "Recipient " + (form.getId() != null ? "updated" : "created") + " successfully!");
        return "redirect:/recipients";
    }

    @PostMapping("/{id}/delete")
    public String deleteRecipient(@PathVariable String id, RedirectAttributes redirectAttributes, Principal principal) {
        recipientService.deleteRecipient(id, principal);
        redirectAttributes.addFlashAttribute("success", "Recipient " + id + " deleted successfully!");
        return "redirect:/recipients";
    }

    private void populateFormModel(Model model, AppUser currentUser, RecipientFormDTO form) {
        model.addAttribute("recipientForm", form);
        model.addAttribute("allOrgans", OrganType.values());
        model.addAttribute("allBloodGroups", BloodGroup.values());
        model.addAttribute("allCities", City.values());
        model.addAttribute("isHospitalUser", currentUser.getRole() == Role.HOSPITAL);
        model.addAttribute("userHospitalName", currentUser.getHospitalName());
    }
}
