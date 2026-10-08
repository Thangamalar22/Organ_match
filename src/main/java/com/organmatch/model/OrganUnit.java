package com.organmatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single available organ unit from a donor (e.g. KIDNEY-1, KIDNEY-2, HEART-1).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganUnit {
    private String id;
    private OrganType organType;
    private Donor donor;
}
