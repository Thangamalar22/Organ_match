package com.organmatch.csp.constraints;

import com.organmatch.model.OrganType;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;

/**
 * Hard Unary Constraint: Donor/Recipient weight ratio compatibility.
 * Applied to HEART & LUNGS (ratio 0.8 - 1.2) and LIVER (ratio 0.7 - 1.5).
 * Skipped for KIDNEY and PANCREAS.
 */
public class SizeConstraint implements NamedUnaryConstraint {

    @Override
    public String name() {
        return "Donor-Recipient Size Compatibility";
    }

    @Override
    public boolean isSatisfied(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null) {
            return false;
        }
        OrganType organType = organUnit.getOrganType();
        if (organType == OrganType.KIDNEY || organType == OrganType.PANCREAS) {
            return true;
        }

        double donorWeight = organUnit.getDonor().getWeightKg();
        double recipientWeight = recipient.getWeightKg();
        if (recipientWeight <= 0) {
            return false;
        }

        double ratio = donorWeight / recipientWeight;

        if (organType == OrganType.HEART || organType == OrganType.LUNGS) {
            return ratio >= 0.8 && ratio <= 1.2;
        } else if (organType == OrganType.LIVER) {
            return ratio >= 0.7 && ratio <= 1.5;
        }

        return true;
    }

    @Override
    public String getExplanation(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null) {
            return "Invalid donor or recipient data";
        }
        double donorWeight = organUnit.getDonor().getWeightKg();
        double recipientWeight = recipient.getWeightKg();
        double ratio = recipientWeight > 0 ? donorWeight / recipientWeight : 0.0;
        return String.format("Donor weight (%.1f kg) to recipient weight (%.1f kg) ratio (%.2f) is outside allowed range for %s",
                donorWeight, recipientWeight, ratio, organUnit.getOrganType());
    }
}
