package com.organmatch.model;

import lombok.Getter;

/**
 * Enum representing organs available for donation and their maximum cold ischemia times.
 */
@Getter
public enum OrganType {
    KIDNEY(30.0),
    LIVER(12.0),
    HEART(5.0),
    LUNGS(7.0),
    PANCREAS(14.0);

    private final double maxIschemiaHours;

    OrganType(double maxIschemiaHours) {
        this.maxIschemiaHours = maxIschemiaHours;
    }
}
