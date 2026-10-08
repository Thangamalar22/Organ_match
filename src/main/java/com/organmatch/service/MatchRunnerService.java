package com.organmatch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.organmatch.csp.SolverConfig;
import com.organmatch.data.DataGenerator;
import com.organmatch.dto.DonorFormDTO;
import com.organmatch.entity.AppUser;
import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.MatchRecord;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.entity.Role;
import com.organmatch.mapper.ModelEntityMapper;
import com.organmatch.model.Donor;
import com.organmatch.model.Recipient;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.repository.DonorEntityRepository;
import com.organmatch.repository.MatchRecordRepository;
import com.organmatch.repository.RecipientEntityRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Orchestrating service executing matching algorithms against stored database entities,
 * mapping to domain models, and recording audit history in MatchRecord persistence.
 */
@Service
@Slf4j
public class MatchRunnerService {

    private final DonorEntityRepository donorRepository;
    private final RecipientEntityRepository recipientRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final AppUserRepository userRepository;
    private final ModelEntityMapper modelEntityMapper;
    private final MatchingService matchingService;
    private final DataGenerator dataGenerator;
    private final ObjectMapper objectMapper;

    private final Random random = new Random();

    public MatchRunnerService(DonorEntityRepository donorRepository,
                              RecipientEntityRepository recipientRepository,
                              MatchRecordRepository matchRecordRepository,
                              AppUserRepository userRepository,
                              ModelEntityMapper modelEntityMapper,
                              MatchingService matchingService,
                              DataGenerator dataGenerator,
                              ObjectMapper objectMapper) {
        this.donorRepository = donorRepository;
        this.recipientRepository = recipientRepository;
        this.matchRecordRepository = matchRecordRepository;
        this.userRepository = userRepository;
        this.modelEntityMapper = modelEntityMapper;
        this.matchingService = matchingService;
        this.dataGenerator = dataGenerator;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<DonorEntity> getAllStoredDonors() {
        return donorRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public List<RecipientEntity> getAllStoredRecipients() {
        return recipientRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional
    public DonorEntity generateAndSaveRandomDonor(Principal principal) {
        String username = principal != null ? principal.getName() : "admin";
        AppUser user = userRepository.findByUsername(username).orElse(null);
        String hospitalName = (user != null && user.getHospitalName() != null) ? user.getHospitalName() : "Apollo Chennai";

        String donorId = "D-" + (100 + random.nextInt(900));
        Donor modelDonor = dataGenerator.generateDonor(donorId);
        DonorEntity entity = modelEntityMapper.toEntity(modelDonor, hospitalName, username);

        return donorRepository.save(entity);
    }

    @Transactional
    public Object runAndSaveMatching(String donorId, SolverConfig config, String algorithm, Principal principal) {
        String username = principal != null ? principal.getName() : "admin";

        DonorEntity donorEntity;
        if (donorId != null && !donorId.isBlank()) {
            donorEntity = donorRepository.findById(donorId)
                    .orElseGet(() -> generateAndSaveRandomDonor(principal));
        } else {
            donorEntity = donorRepository.findAll().stream().findFirst()
                    .orElseGet(() -> generateAndSaveRandomDonor(principal));
        }

        Donor donorModel = modelEntityMapper.toModel(donorEntity);

        List<RecipientEntity> recipientEntities = recipientRepository.findAll();
        if (recipientEntities.isEmpty()) {
            List<Recipient> syntheticRecipients = dataGenerator.generateRecipients(300);
            recipientEntities = modelEntityMapper.toRecipientEntityList(syntheticRecipients, "Apollo Chennai", username);
            recipientRepository.saveAll(recipientEntities);
        }

        List<Recipient> recipientModels = modelEntityMapper.toRecipientModelList(recipientEntities);

        Object result;
        MatchRecord record = new MatchRecord();
        record.setDonorId(donorEntity.getId());
        record.setRunBy(username);
        record.setRunAt(LocalDateTime.now());
        record.setConfigFlags(buildConfigFlagsString(config, algorithm));

        if ("COMPARE".equalsIgnoreCase(algorithm)) {
            ComparisonResult compResult = matchingService.compare(donorModel, recipientModels, config);
            result = compResult;

            record.setAlgorithm("COMPARE");
            record.setTotalUtility(compResult.getCspResult().getTotalUtility());
            record.setOrgansAssigned(countAssigned(compResult.getCspResult()));
            record.setOrgansUnassigned(compResult.getCspResult().getUnassignedOrgans() != null ? compResult.getCspResult().getUnassignedOrgans().size() : 0);
            if (compResult.getCspResult().getStats() != null) {
                record.setNodesExpanded(compResult.getCspResult().getStats().getNodesExpanded());
                record.setBacktracks(compResult.getCspResult().getStats().getBacktracks());
                record.setTimeMillis(compResult.getCspResult().getStats().getTimeMillis());
            }
        } else if ("GREEDY".equalsIgnoreCase(algorithm)) {
            MatchResult greedyResult = matchingService.runGreedy(donorModel, recipientModels);
            result = greedyResult;

            record.setAlgorithm("GREEDY");
            record.setTotalUtility(greedyResult.getTotalUtility());
            record.setOrgansAssigned(countAssigned(greedyResult));
            record.setOrgansUnassigned(greedyResult.getUnassignedOrgans() != null ? greedyResult.getUnassignedOrgans().size() : 0);
            if (greedyResult.getStats() != null) {
                record.setTimeMillis(greedyResult.getStats().getTimeMillis());
            }
        } else {
            MatchResult cspResult = matchingService.runCSP(donorModel, recipientModels, config);
            result = cspResult;

            record.setAlgorithm("CSP");
            record.setTotalUtility(cspResult.getTotalUtility());
            record.setOrgansAssigned(countAssigned(cspResult));
            record.setOrgansUnassigned(cspResult.getUnassignedOrgans() != null ? cspResult.getUnassignedOrgans().size() : 0);
            if (cspResult.getStats() != null) {
                record.setNodesExpanded(cspResult.getStats().getNodesExpanded());
                record.setBacktracks(cspResult.getStats().getBacktracks());
                record.setTimeMillis(cspResult.getStats().getTimeMillis());
            }
        }

        try {
            record.setResultJson(objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize matching result to JSON", e);
            record.setResultJson("{}");
        }

        matchRecordRepository.save(record);
        log.info("MATCH RUN EXECUTED: algorithm={}, donorId={}, runBy={}, totalUtility={}",
                record.getAlgorithm(), record.getDonorId(), username, record.getTotalUtility());
        return result;
    }

    @Transactional(readOnly = true)
    public Page<MatchRecord> getHistoryPage(String runBy, String algorithm, int page, int size) {
        Specification<MatchRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (runBy != null && !runBy.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("runBy")), "%" + runBy.trim().toLowerCase() + "%"));
            }
            if (algorithm != null && !algorithm.isBlank() && !"ALL".equalsIgnoreCase(algorithm)) {
                predicates.add(cb.equal(cb.upper(root.get("algorithm")), algorithm.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "runAt"));
        return matchRecordRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public MatchRecord getHistoryRecordById(Long id) {
        return matchRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match record not found with ID: " + id));
    }

    public String generateHistoryCsv(List<MatchRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("Run ID,Donor ID,Run By,Date & Time,Algorithm,Flags,Total Utility,Organs Assigned,Organs Unassigned,Nodes Expanded,Backtracks,Time (ms)\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (MatchRecord r : records) {
            sb.append(String.format("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.3f,%d,%d,%d,%d,%d\n",
                    r.getId(),
                    r.getDonorId(),
                    r.getRunBy(),
                    r.getRunAt() != null ? r.getRunAt().format(formatter) : "-",
                    r.getAlgorithm(),
                    r.getConfigFlags() != null ? r.getConfigFlags().replace("\"", "'") : "-",
                    r.getTotalUtility(),
                    r.getOrgansAssigned(),
                    r.getOrgansUnassigned(),
                    r.getNodesExpanded(),
                    r.getBacktracks(),
                    r.getTimeMillis()));
        }

        return sb.toString();
    }

    private int countAssigned(MatchResult result) {
        if (result == null || result.getAssignments() == null) return 0;
        int count = 0;
        for (var entry : result.getAssignments().entrySet()) {
            if (entry.getValue() != null) {
                count++;
            }
        }
        return count;
    }

    private String buildConfigFlagsString(SolverConfig config, String algorithm) {
        if ("GREEDY".equalsIgnoreCase(algorithm)) {
            return "Greedy Priority Order";
        }
        List<String> flags = new ArrayList<>();
        if (config.isNodeConsistencyEnabled()) flags.add("Node Consistency");
        if (config.isMrvEnabled()) flags.add("MRV");
        if (config.isForwardCheckingEnabled()) flags.add("Forward Checking");
        if (config.isBranchAndBoundEnabled()) flags.add("Branch & Bound");
        if (config.isLcvEnabled()) flags.add("LCV");
        if (config.isAllowUnassigned()) flags.add("Allow Unassigned");
        return String.join(", ", flags);
    }
}
