package com.organmatch.dto;

import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.OrganType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Recipient creation and update forms.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipientFormDTO {

    private String id;

    @NotBlank(message = "Recipient name is required")
    private String name;

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 100, message = "Age cannot exceed 100")
    private Integer age;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "2.0", message = "Weight must be at least 2.0 kg")
    @DecimalMax(value = "200.0", message = "Weight cannot exceed 200.0 kg")
    private Double weightKg;

    @NotNull(message = "City is required")
    private City city;

    @NotNull(message = "Needed organ is required")
    private OrganType neededOrgan;

    @NotNull(message = "Urgency score is required")
    @Min(value = 1, message = "Urgency must be between 1 and 10")
    @Max(value = 10, message = "Urgency must be between 1 and 10")
    private Integer urgency;

    @NotNull(message = "Waiting days is required")
    @Min(value = 0, message = "Waiting days cannot be negative")
    @Max(value = 2500, message = "Waiting days cannot exceed 2500")
    private Integer waitingDays;

    @NotNull(message = "HLA antigen 1 is required")
    @Min(value = 1, message = "1-10") @Max(value = 10, message = "1-10")
    private Integer hla1;

    @NotNull(message = "HLA antigen 2 is required")
    @Min(value = 1, message = "1-10") @Max(value = 10, message = "1-10")
    private Integer hla2;

    @NotNull(message = "HLA antigen 3 is required")
    @Min(value = 1, message = "1-10") @Max(value = 10, message = "1-10")
    private Integer hla3;

    @NotNull(message = "HLA antigen 4 is required")
    @Min(value = 1, message = "1-10") @Max(value = 10, message = "1-10")
    private Integer hla4;

    @NotNull(message = "HLA antigen 5 is required")
    @Min(value = 1, message = "1-10") @Max(value = 10, message = "1-10")
    private Integer hla5;

    @NotNull(message = "HLA antigen 6 is required")
    @Min(value = 1, message = "1-10") @Max(value = 10, message = "1-10")
    private Integer hla6;

    private boolean crossmatchPositive;

    @Builder.Default
    private boolean medicallyFit = true;

    private String hospitalName;

    public int[] getHlaArray() {
        return new int[]{
            hla1 != null ? hla1 : 1,
            hla2 != null ? hla2 : 1,
            hla3 != null ? hla3 : 1,
            hla4 != null ? hla4 : 1,
            hla5 != null ? hla5 : 1,
            hla6 != null ? hla6 : 1
        };
    }

    public void setHlaArray(int[] hla) {
        if (hla != null && hla.length >= 6) {
            this.hla1 = hla[0];
            this.hla2 = hla[1];
            this.hla3 = hla[2];
            this.hla4 = hla[3];
            this.hla5 = hla[4];
            this.hla6 = hla[5];
        }
    }
}
