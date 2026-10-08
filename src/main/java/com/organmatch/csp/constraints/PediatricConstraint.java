package com.organmatch.csp.constraints;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Hard Unary Constraint: If donor is pediatric (age < 18), organ is reserved for pediatric recipient (age < 18).
 */
public class PediatricConstraint implements NamedUnaryConstraint {

    @Override
    public String name() {
        return "Pediatric Donor Matching";
    }

    @Override
    public boolean isSatisfied(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null) {
            return false;
        }
        boolean donorPediatric = organUnit.getDonor().getAge() < 18;
        boolean recipientPediatric = recipient.getAge() < 18;

        if (donorPediatric) {
            return recipientPediatric;
        }
        return true;
    }

    @Override
    public String getExplanation(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null) {
            return "Invalid age data";
        }
        return String.format("Pediatric donor (age %d) organs are reserved for pediatric recipients (recipient age is %d)",
                organUnit.getDonor().getAge(), recipient.getAge());
    }
}
