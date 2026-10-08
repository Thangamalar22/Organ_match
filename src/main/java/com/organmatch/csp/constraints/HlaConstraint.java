package com.organmatch.csp.constraints;

import com.organmatch.model.OrganType;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Hard Unary Constraint: Applies to KIDNEY. At least 3 of 6 HLA antigens must match by position.
 */
public class HlaConstraint implements NamedUnaryConstraint {

    public static final int MIN_HLA_MATCHES = 3;

    @Override
    public String name() {
        return "HLA Antigen Match (Kidney)";
    }

    @Override
    public boolean isSatisfied(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null) {
            return false;
        }
        if (organUnit.getOrganType() != OrganType.KIDNEY) {
            return true;
        }
        int matches = countHlaMatches(organUnit.getDonor().getHla(), recipient.getHla());
        return matches >= MIN_HLA_MATCHES;
    }

    @Override
    public String getExplanation(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null) {
            return "Invalid HLA data";
        }
        int matches = countHlaMatches(organUnit.getDonor().getHla(), recipient.getHla());
        return String.format("HLA match count (%d/6) is below minimum threshold (%d/6) required for kidney transplant",
                matches, MIN_HLA_MATCHES);
    }

    public int countHlaMatches(int[] donorHla, int[] recipientHla) {
        if (donorHla == null || recipientHla == null) {
            return 0;
        }
        int matches = 0;
        int len = Math.min(donorHla.length, recipientHla.length);
        for (int i = 0; i < len; i++) {
            if (donorHla[i] == recipientHla[i]) {
                matches++;
            }
        }
        return matches;
    }
}
