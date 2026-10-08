package com.organmatch.repository;

import com.organmatch.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRecordRepository extends JpaRepository<MatchRecord, Long>, JpaSpecificationExecutor<MatchRecord> {
    List<MatchRecord> findByRunBy(String runBy);
    List<MatchRecord> findByDonorId(String donorId);
}
