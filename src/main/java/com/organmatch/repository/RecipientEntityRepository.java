package com.organmatch.repository;

import com.organmatch.entity.RecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipientEntityRepository extends JpaRepository<RecipientEntity, String>, JpaSpecificationExecutor<RecipientEntity> {
    List<RecipientEntity> findByHospitalName(String hospitalName);
}
