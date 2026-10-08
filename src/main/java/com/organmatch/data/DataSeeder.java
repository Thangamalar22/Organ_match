package com.organmatch.data;

import com.organmatch.entity.AppUser;
import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.entity.Role;
import com.organmatch.mapper.ModelEntityMapper;
import com.organmatch.model.Donor;
import com.organmatch.model.Recipient;
import com.organmatch.repository.AppUserRepository;
import com.organmatch.repository.DonorEntityRepository;
import com.organmatch.repository.RecipientEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Seeds initial users, recipients, and donors into the H2 database if empty.
 */
@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final RecipientEntityRepository recipientRepository;
    private final DonorEntityRepository donorRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataGenerator dataGenerator;
    private final ModelEntityMapper mapper;

    public DataSeeder(AppUserRepository userRepository,
                      RecipientEntityRepository recipientRepository,
                      DonorEntityRepository donorRepository,
                      PasswordEncoder passwordEncoder,
                      DataGenerator dataGenerator,
                      ModelEntityMapper mapper) {
        this.userRepository = userRepository;
        this.recipientRepository = recipientRepository;
        this.donorRepository = donorRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataGenerator = dataGenerator;
        this.mapper = mapper;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping DataSeeder.");
            return;
        }

        log.info("Seeding initial users and synthetic data into H2 database...");

        // 1. Seed System Users
        AppUser admin = AppUser.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .fullName("System Administrator")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        AppUser coordinator = AppUser.builder()
                .username("coord1")
                .passwordHash(passwordEncoder.encode("Coord@123"))
                .fullName("Transplant Coordinator 1")
                .role(Role.COORDINATOR)
                .enabled(true)
                .build();

        AppUser hospital1 = AppUser.builder()
                .username("hospital1")
                .passwordHash(passwordEncoder.encode("Hosp@123"))
                .fullName("Apollo Hospital Admin")
                .role(Role.HOSPITAL)
                .hospitalName("Apollo Chennai")
                .enabled(true)
                .build();

        AppUser hospital2 = AppUser.builder()
                .username("hospital2")
                .passwordHash(passwordEncoder.encode("Hosp@123"))
                .fullName("AIIMS Hospital Admin")
                .role(Role.HOSPITAL)
                .hospitalName("AIIMS Delhi")
                .enabled(true)
                .build();

        userRepository.saveAll(List.of(admin, coordinator, hospital1, hospital2));
        log.info("Seeded 4 default users (admin, coord1, hospital1, hospital2).");

        // 2. Seed 300 Recipients and 10 Donors using DataGenerator
        DataGenerator fixedDataGen = new DataGenerator(42L);
        List<Recipient> modelRecipients = fixedDataGen.generateRecipients(300);
        List<Donor> modelDonors = fixedDataGen.generateDonors(10);

        String[] hospitals = {"Apollo Chennai", "AIIMS Delhi"};
        String[] registeredByUsers = {"hospital1", "hospital2"};
        Random rng = new Random(42L);

        for (Recipient r : modelRecipients) {
            int idx = rng.nextInt(2);
            RecipientEntity entity = mapper.toEntity(r, hospitals[idx], registeredByUsers[idx]);
            recipientRepository.save(entity);
        }

        for (Donor d : modelDonors) {
            int idx = rng.nextInt(2);
            DonorEntity entity = mapper.toEntity(d, hospitals[idx], registeredByUsers[idx]);
            donorRepository.save(entity);
        }

        log.info("Seeded 300 synthetic recipients and 10 donors into H2 database.");
    }
}
