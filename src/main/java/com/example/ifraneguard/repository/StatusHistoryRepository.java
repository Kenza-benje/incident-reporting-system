package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    List<StatusHistory> findByIncidentIdOrderByChangedAtAsc(Long incidentId);

}