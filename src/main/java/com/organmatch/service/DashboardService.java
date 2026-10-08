package com.organmatch.service;

import com.organmatch.entity.AppUser;
import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.MatchRecord;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.entity.Role;
import com.organmatch.model.OrganType;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.repository.DonorEntityRepository;
import com.organmatch.repository.MatchRecordRepository;
import com.organmatch.repository.RecipientEntityRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for fetching and aggregating role-specific dashboard metrics.
 */
@Service
public class DashboardService {

    private final AppUserRepository userRepository;
    private final RecipientEntityRepository recipientRepository;
    private final DonorEntityRepository donorRepository;
    private final MatchRecordRepository matchRecordRepository;

    public DashboardService(AppUserRepository userRepository,
                            RecipientEntityRepository recipientRepository,
                            DonorEntityRepository donorRepository,
                            MatchRecordRepository matchRecordRepository) {
        this.userRepository = userRepository;
        this.recipientRepository = recipientRepository;
        this.donorRepository = donorRepository;
        this.matchRecordRepository = matchRecordRepository;
    }

    /**
     * Aggregates dashboard statistics for the logged-in user based on their role.
     *
     * @param username Authenticated username
     * @return DashboardDTO containing metrics and recent records
     */
    public DashboardDTO getDashboardData(String username) {
        AppUser user = userRepository.findByUsername(username).orElse(null);
        Role role = user != null ? user.getRole() : Role.ADMIN;

        DashboardDTO.DashboardDTOBuilder builder = DashboardDTO.builder();

        boolean isAdmin = role == Role.ADMIN;
        boolean isCoordinator = role == Role.COORDINATOR;
        boolean isHospital = role == Role.HOSPITAL;

        builder.isAdmin(isAdmin)
               .isAdminOrCoordinator(isAdmin || isCoordinator)
               .isHospital(isHospital);

        if (isAdmin || isCoordinator) {
            long totalRecipients = recipientRepository.count();
            long totalDonors = donorRepository.count();
            long totalMatches = matchRecordRepository.count();

            builder.totalRecipients(totalRecipients)
                   .totalDonors(totalDonors)
                   .totalMatchesRun(totalMatches);

            Map<OrganType, Long> organCounts = new HashMap<>();
            for (OrganType type : OrganType.values()) {
                organCounts.put(type, 0L);
            }
            List<RecipientEntity> allRecipients = recipientRepository.findAll();
            for (RecipientEntity r : allRecipients) {
                if (r.getNeededOrgan() != null) {
                    organCounts.put(r.getNeededOrgan(), organCounts.getOrDefault(r.getNeededOrgan(), 0L) + 1);
                }
            }
            builder.recipientCountByOrgan(organCounts);

            List<MatchRecord> allRecords = matchRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "runAt"));
            List<MatchRunSummaryDTO> recentRuns = allRecords.stream()
                    .limit(5)
                    .map(m -> MatchRunSummaryDTO.builder()
                            .id(m.getId())
                            .donorId(m.getDonorId())
                            .runBy(m.getRunBy())
                            .runAt(m.getRunAt())
                            .algorithm(m.getAlgorithm())
                            .totalUtility(Math.round(m.getTotalUtility() * 1000.0) / 1000.0)
                            .organsAssigned(m.getOrgansAssigned())
                            .organsUnassigned(m.getOrgansUnassigned())
                            .timeMillis(m.getTimeMillis())
                            .build())
                    .collect(Collectors.toList());
            builder.recentMatchRuns(recentRuns);

            double avgCsp = allRecords.stream()
                    .filter(m -> m.getAlgorithm() != null && (m.getAlgorithm().contains("CSP") || m.getAlgorithm().equalsIgnoreCase("CSP")))
                    .mapToDouble(MatchRecord::getTotalUtility)
                    .average().orElse(0.0);

            double avgGreedy = allRecords.stream()
                    .filter(m -> m.getAlgorithm() != null && (m.getAlgorithm().contains("Greedy") || m.getAlgorithm().equalsIgnoreCase("GREEDY")))
                    .mapToDouble(MatchRecord::getTotalUtility)
                    .average().orElse(0.0);

            builder.avgCspUtility(Math.round(avgCsp * 1000.0) / 1000.0)
                   .avgGreedyUtility(Math.round(avgGreedy * 1000.0) / 1000.0);
        }

        if (isAdmin) {
            Map<String, Long> userRoleCounts = new HashMap<>();
            List<AppUser> allUsers = userRepository.findAll();
            for (Role r : Role.values()) {
                userRoleCounts.put(r.name(), allUsers.stream().filter(u -> u.getRole() == r).count());
            }
            builder.userCountByRole(userRoleCounts);
        }

        if (isHospital) {
            String hospitalName = user != null && user.getHospitalName() != null ? user.getHospitalName() : "Hospital Portal";
            builder.hospitalName(hospitalName);

            List<RecipientEntity> hospitalRecipients = recipientRepository.findByHospitalName(hospitalName);
            List<DonorEntity> hospitalDonors = donorRepository.findByHospitalName(hospitalName);

            builder.hospitalRecipientCount(hospitalRecipients.size())
                   .hospitalDonorCount(hospitalDonors.size());

            List<HospitalRecentEntryDTO> recentEntries = new ArrayList<>();
            for (RecipientEntity r : hospitalRecipients) {
                recentEntries.add(HospitalRecentEntryDTO.builder()
                        .type("Recipient")
                        .id(r.getId())
                        .nameOrDetails(r.getName() + " (" + r.getNeededOrgan() + ")")
                        .city(r.getCity() != null ? r.getCity().name() : "-")
                        .registeredBy(r.getRegisteredBy())
                        .createdAt(r.getCreatedAt())
                        .build());
            }
            for (DonorEntity d : hospitalDonors) {
                recentEntries.add(HospitalRecentEntryDTO.builder()
                        .type("Donor")
                        .id(d.getId())
                        .nameOrDetails("Age " + d.getAge() + ", BG: " + d.getBloodGroup())
                        .city(d.getCity() != null ? d.getCity().name() : "-")
                        .registeredBy(d.getRegisteredBy())
                        .createdAt(d.getCreatedAt())
                        .build());
            }

            recentEntries.sort(Comparator.comparing(HospitalRecentEntryDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
            List<HospitalRecentEntryDTO> top5 = recentEntries.stream().limit(5).collect(Collectors.toList());
            builder.hospitalRecentEntries(top5);
        }

        return builder.build();
    }
}
