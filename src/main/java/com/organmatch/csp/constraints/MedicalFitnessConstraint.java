package com.organmatch.csp.constraints;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Hard Unary Constraint: Recipient must be medically fit for transplant surgery.
 */
public class MedicalFitnessConstraint implements NamedUnaryConstraint {

    @Override
    public String name() {
        return "Medical Fitness";
    }

    @Override
    public boolean isSatisfied(OrganUnit organUnit, Recipient recipient) {
        if (recipient == null) {
            return false;
        }
        return recipient.isMedicallyFit();
    }

    @Override
    public String getExplanation(OrganUnit organUnit, Recipient recipient) {
        return String.format("Recipient %s is currently not medically fit for surgery",
                recipient != null ? recipient.getName() : "N/A");
    }
}
