package com.organmatch.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a recent donor or recipient entry registered by a specific hospital.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HospitalRecentEntryDTO {
    private String type; // "Recipient" or "Donor"
    private String id;
    private String nameOrDetails;
    private String city;
    private String registeredBy;
    private LocalDateTime createdAt;
}
