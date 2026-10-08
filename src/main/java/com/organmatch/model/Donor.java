package com.organmatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Domain entity representing an organ donor.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Donor {
    private String id;
    private BloodGroup bloodGroup;
    private int age;
    private double weightKg;
    private City city;
    private int[] hla; // 6 HLA antigens
    private List<OrganType> availableOrgans;
}
