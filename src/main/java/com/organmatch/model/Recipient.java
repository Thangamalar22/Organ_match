package com.organmatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing an organ recipient candidate.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Recipient {
    private String id;
    private String name;
    private BloodGroup bloodGroup;
    private int age;
    private double weightKg;
    private City city;
    private OrganType neededOrgan;
    private int urgency; // 1 to 10
    private int waitingDays;
    private int[] hla; // 6 HLA antigens
    private boolean crossmatchPositive; // simulated crossmatch result
    private boolean medicallyFit;
}
