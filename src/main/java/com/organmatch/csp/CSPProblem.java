package com.organmatch.csp;

import com.organmatch.model.Recipient;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container class representing a CSP problem instance holding variables, initial domains,
 * unary node constraints, and global search constraints.
 */
@Getter
public class CSPProblem {
    private final List<Variable> variables;
    private final Map<Variable, Domain> domains;
    private final List<UnaryConstraint> unaryConstraints;
    private final List<GlobalConstraint> globalConstraints;

    public CSPProblem() {
        this.variables = new ArrayList<>();
        this.domains = new HashMap<>();
        this.unaryConstraints = new ArrayList<>();
        this.globalConstraints = new ArrayList<>();
    }

    public void addVariable(Variable var, List<Recipient> candidateRecipients) {
        variables.add(var);
        domains.put(var, new Domain(candidateRecipients));
    }

    public void addUnaryConstraint(UnaryConstraint constraint) {
        unaryConstraints.add(constraint);
    }

    public void addGlobalConstraint(GlobalConstraint constraint) {
        globalConstraints.add(constraint);
    }
}
