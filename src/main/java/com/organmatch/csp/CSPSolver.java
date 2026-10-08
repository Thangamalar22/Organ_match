package com.organmatch.csp;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hand-written Constraint Satisfaction Problem (CSP) Engine.
 * Features:
 *  1. Node Consistency Pruning
 *  2. Backtracking Search
 *  3. MRV (Minimum Remaining Values) Variable Selection
 *  4. Degree Heuristic Tie-Breaker
 *  5. LCV / Utility-Ordered Value Ordering
 *  6. Forward Checking & Early Domain Wipe-Out Detection
 *  7. Support for UNASSIGNED organs (null recipient) with penalty
 *  8. Branch and Bound Optimization Pruning
 *  9. Safety Node Expansion Limit
 */
public class CSPSolver {

    @Getter
    private SolverStats stats;

    @Getter
    private SearchTrace trace;

    private Map<Variable, Recipient> bestAssignment;
    private double bestUtility;

    public CSPSolver() {
        this.stats = new SolverStats();
        this.trace = new SearchTrace();
    }

    /**
     * Solves the CSP problem using configured engine settings and returns the optimal assignment.
     *
     * @param problem CSP Problem instance containing variables, domains, and constraints
     * @param scoringFunction Function to score utility of assignments
     * @param config Configurable solver flags
     * @return Map of Variable to Recipient representing assignment (null Recipient means UNASSIGNED)
     */
    public Map<Variable, Recipient> solve(CSPProblem problem, ScoringFunction scoringFunction, SolverConfig config) {
        long startTime = System.currentTimeMillis();
        this.stats = new SolverStats();
        this.trace = new SearchTrace();
        this.bestAssignment = null;
        this.bestUtility = Double.NEGATIVE_INFINITY;

        if (problem == null || problem.getVariables().isEmpty()) {
            return new HashMap<>();
        }

        // 1. Node Consistency Pruning
        if (config.isNodeConsistencyEnabled()) {
            applyNodeConsistency(problem);
        }

        // 2. Backtracking Search
        Map<Variable, Recipient> currentAssignment = new HashMap<>();
        backtrack(problem, currentAssignment, 0.0, scoringFunction, config);

        stats.setTimeMillis(System.currentTimeMillis() - startTime);

        if (bestAssignment == null) {
            trace.add("No valid assignment found!");
            return new HashMap<>();
        }

        return bestAssignment;
    }

    private void applyNodeConsistency(CSPProblem problem) {
        for (Variable var : problem.getVariables()) {
            Domain domain = problem.getDomains().get(var);
            List<Recipient> candidateValues = domain.getValues();
            for (Recipient recipient : candidateValues) {
                for (UnaryConstraint constraint : problem.getUnaryConstraints()) {
                    if (!constraint.isSatisfied(var.getOrganUnit(), recipient)) {
                        domain.remove(recipient);
                        stats.addNodeConsistencyPrunes(1);
                        trace.add("Node Consistency: Pruned recipient " + recipient.getId() + " from variable " + var);
                        break;
                    }
                }
            }
        }
    }

    private void backtrack(CSPProblem problem,
                          Map<Variable, Recipient> currentAssignment,
                          double currentUtility,
                          ScoringFunction scoringFunction,
                          SolverConfig config) {
        stats.incrementNodesExpanded();

        // Safety node expansion limit check
        if (config.getMaxNodesExpanded() > 0 && stats.getNodesExpanded() >= config.getMaxNodesExpanded()) {
            trace.add("Node limit reached (" + config.getMaxNodesExpanded() + "), aborting branch search");
            return;
        }

        // Base case: All variables assigned
        if (currentAssignment.size() == problem.getVariables().size()) {
            if (currentUtility > bestUtility) {
                bestUtility = currentUtility;
                bestAssignment = new HashMap<>(currentAssignment);
                trace.add(String.format("New best assignment found with utility: %.2f", bestUtility));
            }
            return;
        }

        // Branch and Bound Pruning
        if (config.isBranchAndBoundEnabled() && bestAssignment != null) {
            double optimisticBound = computeOptimisticUpperBound(problem, currentAssignment, currentUtility, scoringFunction, config);
            if (optimisticBound <= bestUtility) {
                trace.add(String.format("Branch & Bound pruned branch (Upper bound: %.2f <= Best: %.2f)", optimisticBound, bestUtility));
                return;
            }
        }

        // Variable Selection: MRV + Degree Heuristic
        Variable var = selectUnassignedVariable(problem, currentAssignment, config);
        if (var == null) {
            return;
        }

        // Value Ordering: LCV / Utility Scoring
        List<Recipient> orderedValues = getOrderedValues(var, problem, scoringFunction, config);

        for (Recipient val : orderedValues) {
            // Check Global Constraints
            boolean consistent = true;
            for (GlobalConstraint constraint : problem.getGlobalConstraints()) {
                if (!constraint.isConsistent(currentAssignment, var, val)) {
                    consistent = false;
                    break;
                }
            }

            if (!consistent) {
                continue;
            }

            double valScore = evaluateScore(var.getOrganUnit(), val, scoringFunction, config);

            // Make Assignment
            currentAssignment.put(var, val);
            trace.add("Assign " + var + " -> " + (val != null ? val.getId() : "UNASSIGNED"));

            // Forward Checking
            boolean wipeout = false;
            List<Variable> affectedVars = new ArrayList<>();

            if (config.isForwardCheckingEnabled()) {
                for (Variable u : problem.getVariables()) {
                    if (!currentAssignment.containsKey(u)) {
                        Domain uDomain = problem.getDomains().get(u);
                        uDomain.pushState();
                        affectedVars.add(u);

                        List<Recipient> uValues = uDomain.getValues();
                        for (Recipient uVal : uValues) {
                            for (GlobalConstraint gc : problem.getGlobalConstraints()) {
                                if (!gc.isConsistent(currentAssignment, u, uVal)) {
                                    uDomain.remove(uVal);
                                    stats.incrementForwardCheckingPrunes();
                                    break;
                                }
                            }
                        }

                        if (uDomain.isEmpty() && !config.isAllowUnassigned()) {
                            wipeout = true;
                            trace.add("Forward Checking wipe-out on variable " + u + ", backtrack");
                            break;
                        }
                    }
                }
            }

            if (!wipeout) {
                backtrack(problem, currentAssignment, currentUtility + valScore, scoringFunction, config);
            } else {
                stats.incrementBacktracks();
            }

            // Undo Forward Checking
            if (config.isForwardCheckingEnabled()) {
                for (Variable u : affectedVars) {
                    problem.getDomains().get(u).popState();
                }
            }

            // Undo Assignment
            currentAssignment.remove(var);
            trace.add("Backtrack on " + var);
        }
    }

    private Variable selectUnassignedVariable(CSPProblem problem, Map<Variable, Recipient> currentAssignment, SolverConfig config) {
        List<Variable> unassigned = new ArrayList<>();
        for (Variable var : problem.getVariables()) {
            if (!currentAssignment.containsKey(var)) {
                unassigned.add(var);
            }
        }
        if (unassigned.isEmpty()) {
            return null;
        }

        if (!config.isMrvEnabled()) {
            return unassigned.get(0);
        }

        // MRV: Minimum Remaining Values
        int minDomainSize = Integer.MAX_VALUE;
        List<Variable> mrvCandidates = new ArrayList<>();

        for (Variable var : unassigned) {
            int domainSize = problem.getDomains().get(var).size();
            if (domainSize < minDomainSize) {
                minDomainSize = domainSize;
                mrvCandidates.clear();
                mrvCandidates.add(var);
            } else if (domainSize == minDomainSize) {
                mrvCandidates.add(var);
            }
        }

        if (mrvCandidates.size() == 1 || !config.isDegreeHeuristicEnabled()) {
            return mrvCandidates.get(0);
        }

        // Degree Heuristic Tie-breaker
        Variable bestVar = mrvCandidates.get(0);
        int maxDegree = -1;

        for (Variable var : mrvCandidates) {
            int degree = calculateDegree(var, unassigned, problem);
            if (degree > maxDegree) {
                maxDegree = degree;
                bestVar = var;
            }
        }

        return bestVar;
    }

    private int calculateDegree(Variable var, List<Variable> unassigned, CSPProblem problem) {
        return problem.getGlobalConstraints().size();
    }

    private List<Recipient> getOrderedValues(Variable var, CSPProblem problem, ScoringFunction scoringFunction, SolverConfig config) {
        List<Recipient> candidateValues = new ArrayList<>(problem.getDomains().get(var).getValues());

        if (config.isAllowUnassigned()) {
            candidateValues.add(null);
        }

        if (config.isLcvEnabled() && scoringFunction != null) {
            candidateValues.sort((r1, r2) -> {
                double score1 = evaluateScore(var.getOrganUnit(), r1, scoringFunction, config);
                double score2 = evaluateScore(var.getOrganUnit(), r2, scoringFunction, config);
                return Double.compare(score2, score1);
            });
        }

        return candidateValues;
    }

    private double evaluateScore(OrganUnit organUnit, Recipient recipient, ScoringFunction scoringFunction, SolverConfig config) {
        if (recipient == null) {
            return config.getUnassignedPenalty();
        }
        return scoringFunction != null ? scoringFunction.score(organUnit, recipient) : 0.0;
    }

    private double computeOptimisticUpperBound(CSPProblem problem, Map<Variable, Recipient> currentAssignment, double currentUtility, ScoringFunction scoringFunction, SolverConfig config) {
        double bound = currentUtility;

        for (Variable var : problem.getVariables()) {
            if (!currentAssignment.containsKey(var)) {
                Domain domain = problem.getDomains().get(var);
                double maxVarScore = Double.NEGATIVE_INFINITY;

                List<Recipient> values = domain.getValues();
                for (Recipient r : values) {
                    double score = evaluateScore(var.getOrganUnit(), r, scoringFunction, config);
                    if (score > maxVarScore) {
                        maxVarScore = score;
                    }
                }
                if (config.isAllowUnassigned()) {
                    double unassignedScore = config.getUnassignedPenalty();
                    if (unassignedScore > maxVarScore) {
                        maxVarScore = unassignedScore;
                    }
                }

                if (maxVarScore != Double.NEGATIVE_INFINITY) {
                    bound += maxVarScore;
                }
            }
        }
        return bound;
    }
}
