package com.organmatch.csp.constraints;

import com.organmatch.model.OrganType;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Hard Unary Constraint: Applies to KIDNEY. Recipient crossmatch test must not be positive.
 */
public class CrossmatchConstraint implements NamedUnaryConstraint {

    @Override
    public String name() {
        return "Immunological Crossmatch (Kidney)";
    }

    @Override
    public boolean isSatisfied(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || recipient == null) {
            return false;
        }
        if (organUnit.getOrganType() != OrganType.KIDNEY) {
            return true;
        }
        return !recipient.isCrossmatchPositive();
    }

    @Override
    public String getExplanation(OrganUnit organUnit, Recipient recipient) {
        return String.format("Positive crossmatch test for recipient %s indicates high risk of hyperacute rejection",
                recipient != null ? recipient.getName() : "N/A");
    }
}
