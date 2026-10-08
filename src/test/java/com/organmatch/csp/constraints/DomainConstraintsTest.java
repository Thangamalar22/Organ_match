package com.organmatch.csp.constraints;

import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.Donor;
import com.organmatch.model.OrganType;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import com.organmatch.util.TravelTimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DomainConstraintsTest {

    private TravelTimeService travelTimeService;

    @BeforeEach
    public void setUp() {
        travelTimeService = new TravelTimeService();
    }

    @Test
    public void testBloodGroupConstraint() {
        BloodGroupConstraint constraint = new BloodGroupConstraint();

        Donor donorO = Donor.builder().bloodGroup(BloodGroup.O).build();
        Donor donorA = Donor.builder().bloodGroup(BloodGroup.A).build();
        Donor donorAB = Donor.builder().bloodGroup(BloodGroup.AB).build();

        Recipient recipientA = Recipient.builder().bloodGroup(BloodGroup.A).build();
        Recipient recipientO = Recipient.builder().bloodGroup(BloodGroup.O).build();
        Recipient recipientAB = Recipient.builder().bloodGroup(BloodGroup.AB).build();

        OrganUnit unitO = OrganUnit.builder().donor(donorO).build();
        OrganUnit unitA = OrganUnit.builder().donor(donorA).build();
        OrganUnit unitAB = OrganUnit.builder().donor(donorAB).build();

        // O can donate to A
        assertTrue(constraint.isSatisfied(unitO, recipientA));
        // A cannot donate to O
        assertFalse(constraint.isSatisfied(unitA, recipientO));
        // AB cannot donate to A
        assertFalse(constraint.isSatisfied(unitAB, recipientA));
        // AB can donate to AB
        assertTrue(constraint.isSatisfied(unitAB, recipientAB));
    }

    @Test
    public void testSizeConstraint() {
        SizeConstraint constraint = new SizeConstraint();

        Donor donor60kg = Donor.builder().weightKg(60.0).build();
        Donor donor100kg = Donor.builder().weightKg(100.0).build();
        Donor donor70kg = Donor.builder().weightKg(70.0).build();

        Recipient recipient60kg = Recipient.builder().weightKg(60.0).build();
        Recipient recipient50kg = Recipient.builder().weightKg(50.0).build();
        Recipient recipient100kg = Recipient.builder().weightKg(100.0).build();

        OrganUnit heart1to1 = OrganUnit.builder().organType(OrganType.HEART).donor(donor60kg).build();
        OrganUnit heart2to1 = OrganUnit.builder().organType(OrganType.HEART).donor(donor100kg).build();
        OrganUnit liver07to1 = OrganUnit.builder().organType(OrganType.LIVER).donor(donor70kg).build();
        OrganUnit kidneyBigRatio = OrganUnit.builder().organType(OrganType.KIDNEY).donor(donor100kg).build();

        // Ratio 1.0 for HEART -> true
        assertTrue(constraint.isSatisfied(heart1to1, recipient60kg));
        // Ratio 2.0 for HEART -> false
        assertFalse(constraint.isSatisfied(heart2to1, recipient50kg));
        // Ratio 0.7 for LIVER -> true
        assertTrue(constraint.isSatisfied(liver07to1, recipient100kg));
        // KIDNEY ignores weight ratio -> true
        assertTrue(constraint.isSatisfied(kidneyBigRatio, recipient50kg));
    }

    @Test
    public void testIschemiaTimeConstraint() {
        IschemiaTimeConstraint constraint = new IschemiaTimeConstraint(travelTimeService);

        Donor donorMumbai = Donor.builder().city(City.MUMBAI).build();
        Donor donorDelhi = Donor.builder().city(City.DELHI).build();

        Recipient recipientMumbai = Recipient.builder().city(City.MUMBAI).build();
        Recipient recipientKochi = Recipient.builder().city(City.KOCHI).build();

        // Heart max ischemia = 5.0h
        OrganUnit heartMumbai = OrganUnit.builder().organType(OrganType.HEART).donor(donorMumbai).build();
        OrganUnit heartDelhi = OrganUnit.builder().organType(OrganType.HEART).donor(donorDelhi).build();
        // Kidney max ischemia = 30.0h
        OrganUnit kidneyDelhi = OrganUnit.builder().organType(OrganType.KIDNEY).donor(donorDelhi).build();

        // Same city MUMBAI -> MUMBAI: 0.5h transit + 1.5h prep = 2.0h <= 5.0h -> true
        assertTrue(constraint.isSatisfied(heartMumbai, recipientMumbai));

        // Long distance DELHI -> KOCHI: 5.0h transit + 1.5h prep = 6.5h > 5.0h -> false
        assertFalse(constraint.isSatisfied(heartDelhi, recipientKochi));

        // Long distance DELHI -> KOCHI for KIDNEY: 6.5h <= 30.0h -> true
        assertTrue(constraint.isSatisfied(kidneyDelhi, recipientKochi));
    }

    @Test
    public void testHlaConstraint() {
        HlaConstraint constraint = new HlaConstraint();

        Donor donorHla = Donor.builder().hla(new int[]{1, 2, 3, 4, 5, 6}).build();

        Recipient recipient3Match = Recipient.builder().hla(new int[]{1, 2, 3, 9, 9, 9}).build();
        Recipient recipient1Match = Recipient.builder().hla(new int[]{1, 9, 9, 9, 9, 9}).build();

        OrganUnit kidney = OrganUnit.builder().organType(OrganType.KIDNEY).donor(donorHla).build();
        OrganUnit heart = OrganUnit.builder().organType(OrganType.HEART).donor(donorHla).build();

        // Kidney with 3 HLA matches -> true
        assertTrue(constraint.isSatisfied(kidney, recipient3Match));
        // Kidney with 1 HLA match -> false
        assertFalse(constraint.isSatisfied(kidney, recipient1Match));
        // Heart with 1 HLA match -> true (HLA constraint ignored for non-kidney)
        assertTrue(constraint.isSatisfied(heart, recipient1Match));
    }
}
