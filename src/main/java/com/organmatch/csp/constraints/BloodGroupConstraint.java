package com.organmatch.csp.constraints;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Hard Unary Constraint: Donor blood group must be compatible with recipient blood group (ABO rules).
 */
public class BloodGroupConstraint implements NamedUnaryConstraint {

    @Override
    public String name() {
        return "ABO Blood Group Compatibility";
    }

    @Override
    public boolean isSatisfied(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null) {
            return false;
        }
        return organUnit.getDonor().getBloodGroup().canDonateTo(recipient.getBloodGroup());
    }

    @Override
    public String getExplanation(OrganUnit organUnit, Recipient recipient) {
        return String.format("Donor blood group (%s) is incompatible with recipient blood group (%s)",
                organUnit != null && organUnit.getDonor() != null ? organUnit.getDonor().getBloodGroup() : "N/A",
                recipient != null ? recipient.getBloodGroup() : "N/A");
    }
}
