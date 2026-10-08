package com.organmatch.service;

import com.organmatch.dto.RecipientFormDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.entity.Role;
import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.repository.RecipientEntityRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Service managing Recipient CRUD operations, role-based filtering, and hospital ownership enforcement.
 */
@Service
@Slf4j
public class RecipientService {

    private final RecipientEntityRepository recipientRepository;
    private final AppUserRepository userRepository;

    public RecipientService(RecipientEntityRepository recipientRepository, AppUserRepository userRepository) {
        this.recipientRepository = recipientRepository;
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
    public Page<RecipientEntity> getRecipientsPage(
            OrganType organ, BloodGroup bloodGroup, City city, String search,
            int page, int size, Principal principal) {

        AppUser currentUser = getCurrentUser(principal);
        String hospitalFilter = (currentUser.getRole() == Role.HOSPITAL) ? currentUser.getHospitalName() : null;

        Specification<RecipientEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hospitalFilter != null && !hospitalFilter.isBlank()) {
                predicates.add(cb.equal(root.get("hospitalName"), hospitalFilter));
            }
            if (organ != null) {
                predicates.add(cb.equal(root.get("neededOrgan"), organ));
            }
            if (bloodGroup != null) {
                predicates.add(cb.equal(root.get("bloodGroup"), bloodGroup));
            }
            if (city != null) {
                predicates.add(cb.equal(root.get("city"), city));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate idLike = cb.like(cb.lower(root.get("id")), pattern);
                predicates.add(cb.or(nameLike, idLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return recipientRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public RecipientEntity getRecipientById(String id, Principal principal) {
        RecipientEntity entity = recipientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found with ID: " + id));

        AppUser currentUser = getCurrentUser(principal);
        validateOwnershipOrAccess(entity, currentUser);

        return entity;
    }

    @Transactional(readOnly = true)
    public RecipientFormDTO getRecipientFormById(String id, Principal principal) {
        RecipientEntity entity = getRecipientById(id, principal);
        RecipientFormDTO form = RecipientFormDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .bloodGroup(entity.getBloodGroup())
                .age(entity.getAge())
                .weightKg(entity.getWeightKg())
                .city(entity.getCity())
                .neededOrgan(entity.getNeededOrgan())
                .urgency(entity.getUrgency())
                .waitingDays(entity.getWaitingDays())
                .crossmatchPositive(entity.isCrossmatchPositive())
                .medicallyFit(entity.isMedicallyFit())
                .hospitalName(entity.getHospitalName())
                .build();
        form.setHlaArray(entity.getHlaArray());
        return form;
    }

    @Transactional
    public RecipientEntity saveRecipient(RecipientFormDTO form, Principal principal) {
        AppUser currentUser = getCurrentUser(principal);

        if (currentUser.getRole() == Role.COORDINATOR) {
            throw new AccessDeniedException("Coordinators have read-only access.");
        }

        RecipientEntity entity;
        if (form.getId() != null && !form.getId().isBlank()) {
            entity = recipientRepository.findById(form.getId())
                    .orElse(new RecipientEntity());
            if (entity.getId() != null) {
                validateOwnershipOrAccess(entity, currentUser);
            } else {
                entity.setId(form.getId());
            }
        } else {
            entity = new RecipientEntity();
            entity.setId("R" + (recipientRepository.count() + 1000 + (int) (Math.random() * 9000)));
        }

        entity.setName(form.getName());
        entity.setBloodGroup(form.getBloodGroup());
        entity.setAge(form.getAge());
        entity.setWeightKg(form.getWeightKg());
        entity.setCity(form.getCity());
        entity.setNeededOrgan(form.getNeededOrgan());
        entity.setUrgency(form.getUrgency());
        entity.setWaitingDays(form.getWaitingDays());
        entity.setCrossmatchPositive(form.isCrossmatchPositive());
        entity.setMedicallyFit(form.isMedicallyFit());
        entity.setHlaArray(form.getHlaArray());

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

        return recipientRepository.save(entity);
    }

    @Transactional
    public void deleteRecipient(String id, Principal principal) {
        AppUser currentUser = getCurrentUser(principal);

        if (currentUser.getRole() == Role.COORDINATOR) {
            throw new AccessDeniedException("Coordinators have read-only access.");
        }

        RecipientEntity entity = recipientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found with ID: " + id));

        validateOwnershipOrAccess(entity, currentUser);
        recipientRepository.delete(entity);
        log.info("RECIPIENT DELETED: id={}, deletedBy={}", id, currentUser.getUsername());
    }

    private void validateOwnershipOrAccess(RecipientEntity entity, AppUser user) {
        if (user.getRole() == Role.HOSPITAL) {
            if (user.getHospitalName() == null || !user.getHospitalName().equalsIgnoreCase(entity.getHospitalName())) {
                throw new AccessDeniedException("Access denied: You can only access records belonging to " + user.getHospitalName());
            }
        }
    }
}
