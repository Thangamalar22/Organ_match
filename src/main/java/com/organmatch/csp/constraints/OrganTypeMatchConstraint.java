package com.organmatch.csp.constraints;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Hard Unary Constraint: Recipient's needed organ must match the donor organ type.
 */
public class OrganTypeMatchConstraint implements NamedUnaryConstraint {

    @Override
    public String name() {
        return "Organ Type Match";
    }

    @Override
    public boolean isSatisfied(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || recipient == null) {
            return false;
        }
        return organUnit.getOrganType() == recipient.getNeededOrgan();
    }

    @Override
    public String getExplanation(OrganUnit organUnit, Recipient recipient) {
        return String.format("Recipient requires %s but available organ unit is %s",
                recipient != null ? recipient.getNeededOrgan() : "N/A",
                organUnit != null ? organUnit.getOrganType() : "N/A");
    }
}
