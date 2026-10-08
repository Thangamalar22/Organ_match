package com.organmatch.csp;

import com.organmatch.model.Donor;
import com.organmatch.model.OrganType;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test using a toy problem to verify CSPSolver backtracking, node consistency,
 * LCV, forward checking, and domain wipe-out detection.
 */
public class CSPSolverTest {

    @Test
    public void testToyProblemBacktrackingAndNodeConsistency() {
        Donor dummyDonor = Donor.builder().id("D1").build();

        OrganUnit unit1 = OrganUnit.builder().id("KIDNEY-1").organType(OrganType.KIDNEY).donor(dummyDonor).build();
        OrganUnit unit2 = OrganUnit.builder().id("KIDNEY-2").organType(OrganType.KIDNEY).donor(dummyDonor).build();

        Variable v1 = new Variable(unit1);
        Variable v2 = new Variable(unit2);

        Recipient r1 = Recipient.builder().id("R1").name("Recipient 1").build();
        Recipient r2 = Recipient.builder().id("R2").name("Recipient 2").build();
        Recipient r3 = Recipient.builder().id("R3").name("Recipient 3").build();

        CSPProblem problem = new CSPProblem();
        problem.addVariable(v1, List.of(r1, r2, r3));
        problem.addVariable(v2, List.of(r1, r2, r3));

        // Unary Constraint: R3 cannot be assigned to V1
        problem.addUnaryConstraint((organ, recipient) -> !(organ.getId().equals("KIDNEY-1") && recipient.getId().equals("R3")));

        // Global Constraint: Recipient uniqueness (at most 1 organ per recipient)
        problem.addGlobalConstraint((partial, var, val) -> {
            if (val == null) {
                return true;
            }
            return !partial.containsValue(val);
        });

        // Scoring Function: R1=100, R2=80, R3=50
        ScoringFunction scoring = (organ, recipient) -> {
            if (recipient == null) return 0.0;
            return switch (recipient.getId()) {
                case "R1" -> 100.0;
                case "R2" -> 80.0;
                case "R3" -> 50.0;
                default -> 0.0;
            };
        };

        SolverConfig config = SolverConfig.builder()
                .nodeConsistencyEnabled(true)
                .forwardCheckingEnabled(true)
                .allowUnassigned(false)
                .build();

        CSPSolver solver = new CSPSolver();
        Map<Variable, Recipient> assignment = solver.solve(problem, scoring, config);

        assertNotNull(assignment);
        assertEquals(2, assignment.size());
        assertEquals(r1, assignment.get(v1));
        assertEquals(r2, assignment.get(v2));

        assertTrue(solver.getStats().getNodesExpanded() > 0);
        assertTrue(solver.getStats().getDomainsPrunedByNodeConsistency() > 0);
        assertFalse(solver.getTrace().getEntries().isEmpty());
    }

    @Test
    public void testForwardCheckingWipeoutDetection() {
        Donor dummyDonor = Donor.builder().id("D1").build();

        OrganUnit unit1 = OrganUnit.builder().id("HEART-1").organType(OrganType.HEART).donor(dummyDonor).build();
        OrganUnit unit2 = OrganUnit.builder().id("HEART-2").organType(OrganType.HEART).donor(dummyDonor).build();

        Variable v1 = new Variable(unit1);
        Variable v2 = new Variable(unit2);

        // Only 1 recipient available for 2 organs
        Recipient r1 = Recipient.builder().id("R1").name("Only Candidate").build();

        CSPProblem problem = new CSPProblem();
        problem.addVariable(v1, List.of(r1));
        problem.addVariable(v2, List.of(r1));

        // Recipient uniqueness constraint
        problem.addGlobalConstraint((partial, var, val) -> {
            if (val == null) return true;
            return !partial.containsValue(val);
        });

        ScoringFunction scoring = (organ, recipient) -> recipient != null ? 100.0 : 0.0;

        SolverConfig config = SolverConfig.builder()
                .forwardCheckingEnabled(true)
                .allowUnassigned(false) // Strict: must assign all organs
                .build();

        CSPSolver solver = new CSPSolver();
        Map<Variable, Recipient> assignment = solver.solve(problem, scoring, config);

        // Since allowUnassigned=false and only 1 recipient is available for 2 organs, problem is unsatisfiable
        assertTrue(assignment.isEmpty());
        assertTrue(solver.getStats().getForwardCheckingPrunes() > 0);
        assertTrue(solver.getTrace().getEntries().stream().anyMatch(e -> e.contains("wipe-out")));
    }
}
