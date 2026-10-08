package com.organmatch.service;

import com.organmatch.model.Recipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO recording details of why a candidate recipient was eliminated during constraint evaluation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CandidateElimination {
    private Recipient recipient;
    private String constraintName;
    private String reason;
}
