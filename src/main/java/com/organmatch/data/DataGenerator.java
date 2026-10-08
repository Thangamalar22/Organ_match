package com.organmatch.data;

import com.organmatch.model.BloodGroup;
import com.organmatch.model.City;
import com.organmatch.model.Donor;
import com.organmatch.model.OrganType;
import com.organmatch.model.OrganUnit;
import com.organmatch.model.Recipient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Component for generating synthetic data for donors, recipients, and organ units.
 * Uses a seeded Random for reproducible test dataset generation.
 */
@Component
@Slf4j
public class DataGenerator {

    private final Random random;
    private final long seed;

    public DataGenerator(@Value("${app.seed:42}") long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * Generates a list of n synthetic recipient candidates with realistic distributions.
     *
     * @param n Number of recipients to generate
     * @return List of generated Recipient instances
     */
    public List<Recipient> generateRecipients(int n) {
        List<Recipient> recipients = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            String id = "R" + i;
            String name = "Recipient-" + i;
            BloodGroup bg = getRandomBloodGroup();
            int age = 5 + random.nextInt(66); // age 5 to 70
            double weight = calculateWeightForAge(age);
            City city = getRandomCity();
            OrganType organ = getRandomNeededOrgan();
            int urgency = 1 + random.nextInt(10); // urgency 1 to 10
            int waitingDays = 10 + random.nextInt(2491); // waiting days 10 to 2500
            int[] hla = generateHla();
            boolean crossmatchPositive = random.nextDouble() < 0.10; // ~10% positive
            boolean medicallyFit = random.nextDouble() < 0.95; // ~95% fit

            Recipient recipient = Recipient.builder()
                    .id(id)
                    .name(name)
                    .bloodGroup(bg)
                    .age(age)
                    .weightKg(Math.round(weight * 10.0) / 10.0)
                    .city(city)
                    .neededOrgan(organ)
                    .urgency(urgency)
                    .waitingDays(waitingDays)
                    .hla(hla)
                    .crossmatchPositive(crossmatchPositive)
                    .medicallyFit(medicallyFit)
                    .build();

            recipients.add(recipient);
        }
        return recipients;
    }

    /**
     * Generates a single brain-dead donor with random characteristics and available organs.
     *
     * @return Generated Donor instance
     */
    public Donor generateDonor() {
        return generateDonor("D1");
    }

    /**
     * Generates a single brain-dead donor with specified ID.
     *
     * @param id Specific ID for the donor
     * @return Generated Donor instance
     */
    public Donor generateDonor(String id) {
        BloodGroup bg = getRandomBloodGroup();
        int age = 18 + random.nextInt(43); // age 18 to 60
        double weight = 45.0 + random.nextDouble() * 50.0; // weight 45 to 95 kg
        City city = getRandomCity();
        int[] hla = generateHla();

        // Initial organ set for a brain-dead donor: 2 KIDNEY, 1 LIVER, 1 HEART, 1 LUNGS, 1 PANCREAS
        List<OrganType> baseOrgans = new ArrayList<>(List.of(
                OrganType.KIDNEY,
                OrganType.KIDNEY,
                OrganType.LIVER,
                OrganType.HEART,
                OrganType.LUNGS,
                OrganType.PANCREAS
        ));

        // Randomly drop 1 or 2 organs to simulate medically unusable organs
        int dropCount = 1 + random.nextInt(2);
        for (int k = 0; k < dropCount && !baseOrgans.isEmpty(); k++) {
            int removeIndex = random.nextInt(baseOrgans.size());
            baseOrgans.remove(removeIndex);
        }

        return Donor.builder()
                .id(id)
                .bloodGroup(bg)
                .age(age)
                .weightKg(Math.round(weight * 10.0) / 10.0)
                .city(city)
                .hla(hla)
                .availableOrgans(baseOrgans)
                .build();
    }

    /**
     * Generates a list of synthetic donors.
     *
     * @param count Number of donors
     * @return List of generated Donors
     */
    public List<Donor> generateDonors(int count) {
        List<Donor> donors = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            donors.add(generateDonor("D" + i));
        }
        return donors;
    }

    /**
     * Expands a Donor's available organs into individual OrganUnit entities.
     * Each organ unit receives a distinct ID (e.g., D1-KIDNEY-1).
     *
     * @param donor Donor to expand
     * @return List of OrganUnit instances
     */
    public List<OrganUnit> expandDonor(Donor donor) {
        if (donor == null || donor.getAvailableOrgans() == null) {
            return Collections.emptyList();
        }
        List<OrganUnit> units = new ArrayList<>();
        Map<OrganType, Integer> typeCounters = new HashMap<>();

        for (OrganType organType : donor.getAvailableOrgans()) {
            int count = typeCounters.getOrDefault(organType, 0) + 1;
            typeCounters.put(organType, count);

            String unitId = donor.getId() + "-" + organType.name() + "-" + count;
            units.add(OrganUnit.builder()
                    .id(unitId)
                    .organType(organType)
                    .donor(donor)
                    .build());
        }
        return units;
    }

    /**
     * Expands a list of donors into organ units.
     *
     * @param donors List of donors
     * @return List of all organ units
     */
    public List<OrganUnit> expandDonors(List<Donor> donors) {
        List<OrganUnit> allUnits = new ArrayList<>();
        if (donors != null) {
            for (Donor d : donors) {
                allUnits.addAll(expandDonor(d));
            }
        }
        return allUnits;
    }

    /**
     * Startup listener that logs a summary count of synthetic recipients by organ type.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        List<Recipient> sampleRecipients = generateRecipients(50);
        Map<OrganType, Long> counts = sampleRecipients.stream()
                .collect(Collectors.groupingBy(Recipient::getNeededOrgan, Collectors.counting()));

        log.info("=================================================");
        log.info("DataGenerator active with seed {}", seed);
        log.info("Sample Recipients breakdown (n={}):", sampleRecipients.size());
        for (OrganType organ : OrganType.values()) {
            log.info(" - {}: {} recipients", organ, counts.getOrDefault(organ, 0L));
        }
        log.info("=================================================");
    }

    // --- Helper Methods ---

    /**
     * Realistic Indian blood group distribution:
     * O ~37%, B ~32%, A ~22%, AB ~8%
     */
    private BloodGroup getRandomBloodGroup() {
        double r = random.nextDouble();
        if (r < 0.37) {
            return BloodGroup.O;
        } else if (r < 0.69) {
            return BloodGroup.B;
        } else if (r < 0.91) {
            return BloodGroup.A;
        } else {
            return BloodGroup.AB;
        }
    }

    /**
     * Organ demand distribution:
     * KIDNEY 60%, LIVER 20%, HEART 8%, LUNGS 6%, PANCREAS 6%
     */
    private OrganType getRandomNeededOrgan() {
        double r = random.nextDouble();
        if (r < 0.60) {
            return OrganType.KIDNEY;
        } else if (r < 0.80) {
            return OrganType.LIVER;
        } else if (r < 0.88) {
            return OrganType.HEART;
        } else if (r < 0.94) {
            return OrganType.LUNGS;
        } else {
            return OrganType.PANCREAS;
        }
    }

    private City getRandomCity() {
        City[] cities = City.values();
        return cities[random.nextInt(cities.length)];
    }

    private double calculateWeightForAge(int age) {
        if (age < 18) {
            return 15.0 + (age - 5) * 3.5 + (random.nextDouble() * 4.0 - 2.0);
        } else {
            return 45.0 + random.nextDouble() * 50.0;
        }
    }

    private int[] generateHla() {
        int[] hla = new int[6];
        for (int i = 0; i < 6; i++) {
            hla[i] = 1 + random.nextInt(10);
        }
        return hla;
    }
}
