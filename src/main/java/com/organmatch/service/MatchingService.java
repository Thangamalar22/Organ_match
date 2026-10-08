package com.organmatch.service;

import com.organmatch.csp.CSPProblem;
import com.organmatch.csp.CSPSolver;
import com.organmatch.csp.SearchTrace;
import com.organmatch.csp.SolverConfig;
import com.organmatch.csp.SolverStats;
import com.organmatch.csp.Variable;
import com.organmatch.csp.constraints.BloodGroupConstraint;
import com.organmatch.csp.constraints.CrossmatchConstraint;
import com.organmatch.csp.constraints.HlaConstraint;
import com.organmatch.csp.constraints.IschemiaTimeConstraint;
import com.organmatch.csp.constraints.MedicalFitnessConstraint;
import com.organmatch.csp.constraints.NamedGlobalConstraint;
import com.organmatch.csp.constraints.NamedUnaryConstraint;
import com.organmatch.csp.constraints.OneOrganPerRecipientConstraint;
import com.organmatch.csp.constraints.OrganTypeMatchConstraint;
import com.organmatch.csp.constraints.PediatricConstraint;
import com.organmatch.csp.constraints.SizeConstraint;
import com.organmatch.data.DataGenerator;
import com.organmatch.model.Donor;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import com.organmatch.util.TravelTimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service managing organ donor-recipient matching allocations using CSP optimization and Greedy baseline comparison.
 */
@Service
@Slf4j
public class MatchingService {

    private final DataGenerator dataGenerator;
    private final UtilityScorer utilityScorer;
    private final TravelTimeService travelTimeService;

    public MatchingService(DataGenerator dataGenerator, UtilityScorer utilityScorer, TravelTimeService travelTimeService) {
        this.dataGenerator = dataGenerator;
        this.utilityScorer = utilityScorer;
        this.travelTimeService = travelTimeService;
    }

    /**
     * Executes CSP optimization algorithm for allocating donor organs to recipient candidates.
     *
     * @param donor Donor providing organ units
     * @param recipients Candidate recipients pool
     * @param config CSP Solver configuration
     * @return MatchResult containing optimal assignments, total utility, and trace metrics
     */
    public MatchResult runCSP(Donor donor, List<Recipient> recipients, SolverConfig config) {
        long startTime = System.currentTimeMillis();
        List<OrganUnit> organUnits = dataGenerator.expandDonor(donor);

        List<NamedUnaryConstraint> unaryConstraints = createUnaryConstraints();
        NamedGlobalConstraint globalConstraint = new OneOrganPerRecipientConstraint();

        CSPProblem problem = new CSPProblem();
        unaryConstraints.forEach(problem::addUnaryConstraint);
        problem.addGlobalConstraint(globalConstraint);

        List<Variable> variables = new ArrayList<>();
        for (OrganUnit unit : organUnits) {
            Variable var = new Variable(unit);
            variables.add(var);

            // Domain setup: Filter recipients needing this organ type
            List<Recipient> candidates = recipients.stream()
                    .filter(r -> r.getNeededOrgan() == unit.getOrganType())
                    .collect(Collectors.toList());

            problem.addVariable(var, candidates);
        }

        CSPSolver solver = new CSPSolver();
        Map<Variable, Recipient> assignments = solver.solve(problem, utilityScorer, config);

        long elapsedTime = System.currentTimeMillis() - startTime;
        if (solver.getStats() != null) {
            solver.getStats().setTimeMillis(elapsedTime);
        }

        double totalUtility = calculateTotalUtility(assignments);
        List<OrganUnit> unassignedOrgans = findUnassignedOrgans(organUnits, assignments);
        Map<OrganUnit, OrganMatchExplanation> explanations = buildExplanations(organUnits, assignments, recipients, unaryConstraints, globalConstraint);

        return MatchResult.builder()
                .algorithmName("CSP Solver")
                .donor(donor)
                .assignments(assignments)
                .unassignedOrgans(unassignedOrgans)
                .totalUtility(Math.round(totalUtility * 1000.0) / 1000.0)
                .stats(solver.getStats())
                .trace(solver.getTrace())
                .perOrganExplanations(explanations)
                .build();
    }

    /**
     * Executes Greedy baseline allocation (allocates highest-urgency feasible recipient per organ).
     *
     * @param donor Donor providing organ units
     * @param recipients Candidate recipients pool
     * @return MatchResult containing greedy assignments and metrics
     */
    public MatchResult runGreedy(Donor donor, List<Recipient> recipients) {
        long startTime = System.currentTimeMillis();
        List<OrganUnit> organUnits = dataGenerator.expandDonor(donor);
        List<NamedUnaryConstraint> unaryConstraints = createUnaryConstraints();
        NamedGlobalConstraint globalConstraint = new OneOrganPerRecipientConstraint();

        Map<Variable, Recipient> assignments = new HashMap<>();
        Set<Recipient> allocatedRecipients = new HashSet<>();
        SearchTrace trace = new SearchTrace();
        SolverStats stats = new SolverStats();

        // Sort candidates by urgency (descending), then waiting days (descending)
        List<Recipient> sortedRecipients = recipients.stream()
                .sorted(Comparator.comparingInt(Recipient::getUrgency).reversed()
                        .thenComparingInt(Recipient::getWaitingDays).reversed())
                .collect(Collectors.toList());

        for (OrganUnit unit : organUnits) {
            Variable var = new Variable(unit);
            stats.incrementNodesExpanded();
            Recipient chosen = null;

            for (Recipient candidate : sortedRecipients) {
                if (candidate.getNeededOrgan() != unit.getOrganType()) {
                    continue;
                }
                if (allocatedRecipients.contains(candidate)) {
                    continue;
                }

                // Check Unary Constraints
                boolean unaryOk = true;
                for (NamedUnaryConstraint uc : unaryConstraints) {
                    if (!uc.isSatisfied(unit, candidate)) {
                        unaryOk = false;
                        break;
                    }
                }
                if (!unaryOk) {
                    continue;
                }

                // Check Global Constraint
                if (!globalConstraint.isConsistent(assignments, var, candidate)) {
                    continue;
                }

                // Match found
                chosen = candidate;
                allocatedRecipients.add(candidate);
                assignments.put(var, candidate);
                trace.add("Greedy Assign " + var + " -> " + candidate.getId() + " (" + candidate.getName() + ")");
                break;
            }

            if (chosen == null) {
                assignments.put(var, null);
                trace.add("Greedy UNASSIGNED " + var);
            }
        }

        stats.setTimeMillis(System.currentTimeMillis() - startTime);

        double totalUtility = calculateTotalUtility(assignments);
        List<OrganUnit> unassignedOrgans = findUnassignedOrgans(organUnits, assignments);
        Map<OrganUnit, OrganMatchExplanation> explanations = buildExplanations(organUnits, assignments, recipients, unaryConstraints, globalConstraint);

        return MatchResult.builder()
                .algorithmName("Greedy Baseline")
                .donor(donor)
                .assignments(assignments)
                .unassignedOrgans(unassignedOrgans)
                .totalUtility(Math.round(totalUtility * 1000.0) / 1000.0)
                .stats(stats)
                .trace(trace)
                .perOrganExplanations(explanations)
                .build();
    }

    /**
     * Runs both CSP and Greedy algorithms on the same dataset and returns side-by-side comparison.
     *
     * @param donor Donor instance
     * @param recipients Recipients pool
     * @param config CSP Solver config
     * @return ComparisonResult DTO
     */
    public ComparisonResult compare(Donor donor, List<Recipient> recipients, SolverConfig config) {
        MatchResult csp = runCSP(donor, recipients, config);
        MatchResult greedy = runGreedy(donor, recipients);

        double diff = csp.getTotalUtility() - greedy.getTotalUtility();
        double percentage = greedy.getTotalUtility() > 0 ? (diff / greedy.getTotalUtility()) * 100.0 : 0.0;
        long timeDiff = (csp.getStats() != null ? csp.getStats().getTimeMillis() : 0)
                - (greedy.getStats() != null ? greedy.getStats().getTimeMillis() : 0);

        return ComparisonResult.builder()
                .cspResult(csp)
                .greedyResult(greedy)
                .utilityDifference(Math.round(diff * 1000.0) / 1000.0)
                .utilityPercentageGain(Math.round(percentage * 100.0) / 100.0)
                .timeDifferenceMillis(timeDiff)
                .build();
    }

    // --- Helper Methods ---

    private List<NamedUnaryConstraint> createUnaryConstraints() {
        return List.of(
                new OrganTypeMatchConstraint(),
                new BloodGroupConstraint(),
                new MedicalFitnessConstraint(),
                new SizeConstraint(),
                new IschemiaTimeConstraint(travelTimeService),
                new CrossmatchConstraint(),
                new HlaConstraint(),
                new PediatricConstraint()
        );
    }

    private double calculateTotalUtility(Map<Variable, Recipient> assignments) {
        if (assignments == null) return 0.0;
        double sum = 0.0;
        for (Map.Entry<Variable, Recipient> entry : assignments.entrySet()) {
            if (entry.getValue() != null && entry.getKey() != null) {
                sum += utilityScorer.score(entry.getKey().getOrganUnit(), entry.getValue());
            }
        }
        return sum;
    }

    private List<OrganUnit> findUnassignedOrgans(List<OrganUnit> allUnits, Map<Variable, Recipient> assignments) {
        List<OrganUnit> unassigned = new ArrayList<>();
        for (OrganUnit unit : allUnits) {
            boolean assigned = false;
            for (Map.Entry<Variable, Recipient> entry : assignments.entrySet()) {
                if (entry.getKey().getOrganUnit().getId().equals(unit.getId()) && entry.getValue() != null) {
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                unassigned.add(unit);
            }
        }
        return unassigned;
    }

    private Map<OrganUnit, OrganMatchExplanation> buildExplanations(
            List<OrganUnit> organUnits,
            Map<Variable, Recipient> assignments,
            List<Recipient> allRecipients,
            List<NamedUnaryConstraint> unaryConstraints,
            NamedGlobalConstraint globalConstraint) {

        Map<OrganUnit, OrganMatchExplanation> explanations = new HashMap<>();

        for (OrganUnit unit : organUnits) {
            Variable var = new Variable(unit);
            Recipient chosen = assignments.get(var);
            double utility = chosen != null ? utilityScorer.score(unit, chosen) : 0.0;
            Map<String, Double> breakdown = utilityScorer.getScoreBreakdown(unit, chosen);

            List<CandidateElimination> eliminations = new ArrayList<>();
            List<Recipient> candidatesForOrgan = allRecipients.stream()
                    .filter(r -> r.getNeededOrgan() == unit.getOrganType())
                    .collect(Collectors.toList());

            for (Recipient candidate : candidatesForOrgan) {
                if (chosen != null && candidate.getId().equals(chosen.getId())) {
                    continue;
                }
                if (eliminations.size() >= 5) {
                    break;
                }

                // Evaluate Unary constraints
                boolean rejected = false;
                for (NamedUnaryConstraint uc : unaryConstraints) {
                    if (!uc.isSatisfied(unit, candidate)) {
                        eliminations.add(CandidateElimination.builder()
                                .recipient(candidate)
                                .constraintName(uc.name())
                                .reason(uc.getExplanation(unit, candidate))
                                .build());
                        rejected = true;
                        break;
                    }
                }

                if (!rejected && !globalConstraint.isConsistent(assignments, var, candidate)) {
                    eliminations.add(CandidateElimination.builder()
                            .recipient(candidate)
                            .constraintName(globalConstraint.name())
                            .reason(globalConstraint.getExplanation(assignments, var, candidate))
                            .build());
                }
            }

            explanations.put(unit, OrganMatchExplanation.builder()
                    .organUnit(unit)
                    .chosenRecipient(chosen)
                    .utilityScore(Math.round(utility * 1000.0) / 1000.0)
                    .scoreBreakdown(breakdown)
                    .topRejectedCandidates(eliminations)
                    .build());
        }

        return explanations;
    }
}
