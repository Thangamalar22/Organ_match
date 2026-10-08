package com.organmatch.service;

import com.organmatch.dto.DonorFormDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.Role;
import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.repository.DonorEntityRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Service managing Donor CRUD operations, role-based filtering, and hospital ownership enforcement.
 */
@Service
@Slf4j
public class DonorService {

    private final DonorEntityRepository donorRepository;
    private final AppUserRepository userRepository;

    public DonorService(DonorEntityRepository donorRepository, AppUserRepository userRepository) {
        this.donorRepository = donorRepository;
        this.userRepository = userRepository;
    }

    public AppUser getCurrentUser(Principal principal) {
        String username = principal != null ? principal.getName() : null;
        if (username == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                username = auth.getName();
            }
        }
        if (username == null) {
            throw new AccessDeniedException("User is not authenticated");
        }
        final String searchUsername = username;
        return userRepository.findByUsername(searchUsername)
                .orElseThrow(() -> new AccessDeniedException("User not found: " + searchUsername));
    }

    @Transactional(readOnly = true)
    public Page<DonorEntity> getDonorsPage(
            BloodGroup bloodGroup, City city, String search,
            int page, int size, Principal principal) {

        AppUser currentUser = getCurrentUser(principal);
        String hospitalFilter = (currentUser.getRole() == Role.HOSPITAL) ? currentUser.getHospitalName() : null;

        Specification<DonorEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hospitalFilter != null && !hospitalFilter.isBlank()) {
                predicates.add(cb.equal(root.get("hospitalName"), hospitalFilter));
            }
            if (bloodGroup != null) {
                predicates.add(cb.equal(root.get("bloodGroup"), bloodGroup));
            }
            if (city != null) {
                predicates.add(cb.equal(root.get("city"), city));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("id")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return donorRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public DonorEntity getDonorById(String id, Principal principal) {
        DonorEntity entity = donorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with ID: " + id));

        AppUser currentUser = getCurrentUser(principal);
        validateOwnershipOrAccess(entity, currentUser);

        return entity;
    }

    @Transactional(readOnly = true)
    public DonorFormDTO getDonorFormById(String id, Principal principal) {
        DonorEntity entity = getDonorById(id, principal);

        int kidneyCount = 0;
        List<OrganType> otherOrgans = new ArrayList<>();

        if (entity.getAvailableOrgans() != null) {
            for (OrganType organ : entity.getAvailableOrgans()) {
                if (organ == OrganType.KIDNEY) {
                    kidneyCount++;
                } else {
                    otherOrgans.add(organ);
                }
            }
        }

        DonorFormDTO form = DonorFormDTO.builder()
                .id(entity.getId())
                .bloodGroup(entity.getBloodGroup())
                .age(entity.getAge())
                .weightKg(entity.getWeightKg())
                .city(entity.getCity())
                .kidneyCount(kidneyCount)
                .otherOrgans(otherOrgans)
                .hospitalName(entity.getHospitalName())
                .build();
        form.setHlaArray(entity.getHlaArray());
        return form;
    }

    @Transactional
    public DonorEntity saveDonor(DonorFormDTO form, Principal principal) {
        AppUser currentUser = getCurrentUser(principal);

        if (currentUser.getRole() == Role.COORDINATOR) {
            throw new AccessDeniedException("Coordinators have read-only access.");
        }

        DonorEntity entity;
        if (form.getId() != null && !form.getId().isBlank()) {
            entity = donorRepository.findById(form.getId())
                    .orElse(new DonorEntity());
            if (entity.getId() != null) {
                validateOwnershipOrAccess(entity, currentUser);
            } else {
                entity.setId(form.getId());
            }
        } else {
            entity = new DonorEntity();
            entity.setId("D" + (donorRepository.count() + 100 + (int) (Math.random() * 900)));
        }

        entity.setBloodGroup(form.getBloodGroup());
        entity.setAge(form.getAge());
        entity.setWeightKg(form.getWeightKg());
        entity.setCity(form.getCity());
        entity.setHlaArray(form.getHlaArray());
        entity.setAvailableOrgans(form.buildAvailableOrgansList());

        if (currentUser.getRole() == Role.HOSPITAL) {
            entity.setHospitalName(currentUser.getHospitalName());
        } else if (form.getHospitalName() != null && !form.getHospitalName().isBlank()) {
            entity.setHospitalName(form.getHospitalName());
        } else if (entity.getHospitalName() == null) {
            entity.setHospitalName(currentUser.getHospitalName() != null ? currentUser.getHospitalName() : "General Hospital");
        }

        if (entity.getRegisteredBy() == null) {
            entity.setRegisteredBy(currentUser.getUsername());
        }

        return donorRepository.save(entity);
    }

    @Transactional
    public void deleteDonor(String id, Principal principal) {
        AppUser currentUser = getCurrentUser(principal);

        if (currentUser.getRole() == Role.COORDINATOR) {
            throw new AccessDeniedException("Coordinators have read-only access.");
        }

        DonorEntity entity = donorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found with ID: " + id));

        validateOwnershipOrAccess(entity, currentUser);
        donorRepository.delete(entity);
        log.info("DONOR DELETED: id={}, deletedBy={}", id, currentUser.getUsername());
    }

    private void validateOwnershipOrAccess(DonorEntity entity, AppUser user) {
        if (user.getRole() == Role.HOSPITAL) {
            if (user.getHospitalName() == null || !user.getHospitalName().equalsIgnoreCase(entity.getHospitalName())) {
                throw new AccessDeniedException("Access denied: You can only access records belonging to " + user.getHospitalName());
            }
        }
    }
}
