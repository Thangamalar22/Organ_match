package com.organmatch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity recording matching run audit history and full JSON result payloads.
 */
@Entity
@Table(name = "match_records")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String donorId;
    private String runBy;
    private LocalDateTime runAt;

    private String algorithm;
    private String configFlags;

    private double totalUtility;
    private int organsAssigned;
    private int organsUnassigned;

    private long nodesExpanded;
    private long backtracks;
    private long timeMillis;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String resultJson;

    @PrePersist
    public void onCreate() {
        if (runAt == null) {
            runAt = LocalDateTime.now();
        }
    }
}
