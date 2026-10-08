package com.organmatch.entity;

import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity for persistent organ donors.
 */
@Entity
@Table(name = "donors")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DonorEntity {

    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    private int age;
    private double weightKg;

    @Enumerated(EnumType.STRING)
    private City city;

    @Column(name = "hla_antigens")
    private String hlaString;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<OrganType> availableOrgans;

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
