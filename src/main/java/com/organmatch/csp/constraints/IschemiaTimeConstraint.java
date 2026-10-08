package com.organmatch.csp.constraints;

import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import com.organmatch.util.TravelTimeService;

/**
 * Hard Unary Constraint: Travel transport time plus 1.5 hours preparation buffer
 * must not exceed the organ's maximum cold ischemia limit.
 */
public class IschemiaTimeConstraint implements NamedUnaryConstraint {

    public static final double PREPARATION_BUFFER_HOURS = 1.5;
    private final TravelTimeService travelTimeService;

    public IschemiaTimeConstraint(TravelTimeService travelTimeService) {
        this.travelTimeService = travelTimeService;
    }

    @Override
    public String name() {
        return "Maximum Cold Ischemia Time";
    }

    @Override
    public boolean isSatisfied(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null || travelTimeService == null) {
            return false;
        }
        double transitHours = travelTimeService.hours(organUnit.getDonor().getCity(), recipient.getCity());
        double totalHours = transitHours + PREPARATION_BUFFER_HOURS;
        double maxAllowed = organUnit.getOrganType().getMaxIschemiaHours();
        return totalHours <= maxAllowed;
    }

    @Override
    public String getExplanation(OrganUnit organUnit, Recipient recipient) {
        if (organUnit == null || organUnit.getDonor() == null || recipient == null || travelTimeService == null) {
            return "Invalid travel or organ data";
        }
        double transitHours = travelTimeService.hours(organUnit.getDonor().getCity(), recipient.getCity());
        double totalHours = transitHours + PREPARATION_BUFFER_HOURS;
        double maxAllowed = organUnit.getOrganType().getMaxIschemiaHours();
        return String.format("Total transport time (%.1fh transit + %.1fh buffer = %.1fh) exceeds maximum allowed ischemia time (%.1fh) for %s",
                transitHours, PREPARATION_BUFFER_HOURS, totalHours, maxAllowed, organUnit.getOrganType());
    }
}
