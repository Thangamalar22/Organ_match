package com.organmatch.controller;

import com.organmatch.dto.DonorFormDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.Role;
import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import com.organmatch.service.DonorService;
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
import java.util.List;

/**
 * Controller serving Donor management UI pages and CRUD endpoint handling.
 */
@Controller
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @GetMapping
    public String listDonors(
            @RequestParam(required = false) BloodGroup bloodGroup,
            @RequestParam(required = false) City city,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model,
            Principal principal) {

        Page<DonorEntity> donorsPage = donorService.getDonorsPage(
                bloodGroup, city, search, page, size, principal);

        AppUser currentUser = donorService.getCurrentUser(principal);

        model.addAttribute("donorsPage", donorsPage);
        model.addAttribute("donors", donorsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", donorsPage.getTotalPages());
        model.addAttribute("totalElements", donorsPage.getTotalElements());

        model.addAttribute("selectedBloodGroup", bloodGroup);
        model.addAttribute("selectedCity", city);
        model.addAttribute("search", search);

        model.addAttribute("allBloodGroups", BloodGroup.values());
        model.addAttribute("allCities", City.values());
        model.addAttribute("currentUserRole", currentUser.getRole().name());
        model.addAttribute("userHospitalName", currentUser.getHospitalName());

        return "donors/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        AppUser currentUser = donorService.getCurrentUser(principal);
        DonorFormDTO form = DonorFormDTO.builder()
                .age(45)
                .weightKg(70.0)
                .kidneyCount(2)
                .otherOrgans(List.of(OrganType.LIVER, OrganType.HEART))
                .hla1(1).hla2(2).hla3(3).hla4(4).hla5(5).hla6(6)
                .hospitalName(currentUser.getRole() == Role.HOSPITAL ? currentUser.getHospitalName() : "")
                .build();

        populateFormModel(model, currentUser, form);
        return "donors/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable String id, Model model, Principal principal) {
        AppUser currentUser = donorService.getCurrentUser(principal);
        DonorFormDTO form = donorService.getDonorFormById(id, principal);
        populateFormModel(model, currentUser, form);
        return "donors/form";
    }

    @PostMapping("/save")
    public String saveDonor(
            @Valid @ModelAttribute("donorForm") DonorFormDTO form,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            Principal principal) {

        AppUser currentUser = donorService.getCurrentUser(principal);

        if (result.hasErrors()) {
            populateFormModel(model, currentUser, form);
            return "donors/form";
        }

        donorService.saveDonor(form, principal);
        redirectAttributes.addFlashAttribute("success", "Donor " + (form.getId() != null ? "updated" : "created") + " successfully!");
        return "redirect:/donors";
    }

    @PostMapping("/{id}/delete")
    public String deleteDonor(@PathVariable String id, RedirectAttributes redirectAttributes, Principal principal) {
        donorService.deleteDonor(id, principal);
        redirectAttributes.addFlashAttribute("success", "Donor " + id + " deleted successfully!");
        return "redirect:/donors";
    }

    private void populateFormModel(Model model, AppUser currentUser, DonorFormDTO form) {
        model.addAttribute("donorForm", form);
        model.addAttribute("allBloodGroups", BloodGroup.values());
        model.addAttribute("allCities", City.values());
        model.addAttribute("nonKidneyOrgans", List.of(OrganType.HEART, OrganType.LIVER, OrganType.LUNGS, OrganType.PANCREAS));
        model.addAttribute("isHospitalUser", currentUser.getRole() == Role.HOSPITAL);
        model.addAttribute("userHospitalName", currentUser.getHospitalName());
    }
}
