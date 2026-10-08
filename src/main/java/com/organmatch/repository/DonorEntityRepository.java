package com.organmatch.repository;

import com.organmatch.entity.DonorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorEntityRepository extends JpaRepository<DonorEntity, String>, JpaSpecificationExecutor<DonorEntity> {
    List<DonorEntity> findByHospitalName(String hospitalName);
}
