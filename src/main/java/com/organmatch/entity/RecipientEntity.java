package com.organmatch.entity;

import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for persistent organ recipient candidates.
 */
@Entity
@Table(name = "recipients")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipientEntity {

    @Id
    private String id;

    private String name;

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    private int age;
    private double weightKg;

    @Enumerated(EnumType.STRING)
    private City city;

    @Enumerated(EnumType.STRING)
    private OrganType neededOrgan;

    private int urgency;
    private int waitingDays;

    @Column(name = "hla_antigens")
    private String hlaString; // Store 6 HLA values as comma-separated string e.g. "1,2,3,4,5,6"

    private boolean crossmatchPositive;
    private boolean medicallyFit;

    private String hospitalName;
    private String registeredBy;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public int[] getHlaArray() {
        if (hlaString == null || hlaString.isEmpty()) {
            return new int[6];
        }
        String[] parts = hlaString.split(",");
        int[] hla = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                hla[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                hla[i] = 1;
            }
        }
        return hla;
    }

    public void setHlaArray(int[] hla) {
        if (hla == null) {
            this.hlaString = "";
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hla.length; i++) {
            sb.append(hla[i]);
            if (i < hla.length - 1) {
                sb.append(",");
            }
        }
        this.hlaString = sb.toString();
    }
}
