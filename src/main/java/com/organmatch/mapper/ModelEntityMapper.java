package com.organmatch.mapper;

import com.organmatch.entity.DonorEntity;
import com.organmatch.entity.RecipientEntity;
import com.organmatch.model.Donor;
import com.organmatch.model.Recipient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Component mapping between persistent JPA entities and domain model classes used by the CSP engine.
 */
@Component
public class ModelEntityMapper {

    public Recipient toModel(RecipientEntity entity) {
        if (entity == null) {
            return null;
        }
        return Recipient.builder()
                .id(entity.getId())
                .name(entity.getName())
                .bloodGroup(entity.getBloodGroup())
                .age(entity.getAge())
                .weightKg(entity.getWeightKg())
                .city(entity.getCity())
                .neededOrgan(entity.getNeededOrgan())
                .urgency(entity.getUrgency())
                .waitingDays(entity.getWaitingDays())
                .hla(entity.getHlaArray())
                .crossmatchPositive(entity.isCrossmatchPositive())
                .medicallyFit(entity.isMedicallyFit())
                .build();
    }

    public RecipientEntity toEntity(Recipient model, String hospitalName, String registeredBy) {
        if (model == null) {
            return null;
        }
        RecipientEntity entity = RecipientEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .bloodGroup(model.getBloodGroup())
                .age(model.getAge())
                .weightKg(model.getWeightKg())
                .city(model.getCity())
                .neededOrgan(model.getNeededOrgan())
                .urgency(model.getUrgency())
                .waitingDays(model.getWaitingDays())
                .crossmatchPositive(model.isCrossmatchPositive())
                .medicallyFit(model.isMedicallyFit())
                .hospitalName(hospitalName)
                .registeredBy(registeredBy)
                .build();
        entity.setHlaArray(model.getHla());
        return entity;
    }

    public Donor toModel(DonorEntity entity) {
        if (entity == null) {
            return null;
        }
        return Donor.builder()
                .id(entity.getId())
                .bloodGroup(entity.getBloodGroup())
                .age(entity.getAge())
                .weightKg(entity.getWeightKg())
                .city(entity.getCity())
                .hla(entity.getHlaArray())
                .availableOrgans(entity.getAvailableOrgans() != null ? new ArrayList<>(entity.getAvailableOrgans()) : new ArrayList<>())
                .build();
    }

    public DonorEntity toEntity(Donor model, String hospitalName, String registeredBy) {
        if (model == null) {
            return null;
        }
        DonorEntity entity = DonorEntity.builder()
                .id(model.getId())
                .bloodGroup(model.getBloodGroup())
                .age(model.getAge())
                .weightKg(model.getWeightKg())
                .city(model.getCity())
                .availableOrgans(model.getAvailableOrgans() != null ? new ArrayList<>(model.getAvailableOrgans()) : new ArrayList<>())
                .hospitalName(hospitalName)
                .registeredBy(registeredBy)
                .build();
        entity.setHlaArray(model.getHla());
        return entity;
    }

    public List<Recipient> toRecipientModelList(List<RecipientEntity> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    public List<RecipientEntity> toRecipientEntityList(List<Recipient> models, String hospitalName, String registeredBy) {
        if (models == null) return new ArrayList<>();
        return models.stream().map(m -> toEntity(m, hospitalName, registeredBy)).collect(Collectors.toList());
    }
}
