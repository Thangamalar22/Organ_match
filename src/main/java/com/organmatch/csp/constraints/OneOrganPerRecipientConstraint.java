package com.organmatch.csp.constraints;

import com.organmatch.csp.Variable;
import com.organmatch.model.Recipient;

import java.util.Map;

/**
 * Hard Global Constraint: A recipient can receive at most one organ in a single allocation run.
 */
public class OneOrganPerRecipientConstraint implements NamedGlobalConstraint {

    @Override
    public String name() {
        return "Single Organ Per Recipient";
    }

    @Override
    public boolean isConsistent(Map<Variable, Recipient> partialAssignment, Variable variable, Recipient recipient) {
        if (recipient == null) {
            return true; // UNASSIGNED organ does not conflict with any recipient
        }
        for (Map.Entry<Variable, Recipient> entry : partialAssignment.entrySet()) {
            if (!entry.getKey().equals(variable) && recipient.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getExplanation(Map<Variable, Recipient> partialAssignment, Variable variable, Recipient recipient) {
        return String.format("Recipient %s has already been allocated an organ in this run",
                recipient != null ? recipient.getName() : "N/A");
    }
}
