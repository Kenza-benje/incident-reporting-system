package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.InternalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalNoteRepository extends JpaRepository<InternalNote, Long> {

    List<InternalNote> findByIncidentOrderByCreatedAtDesc(Incident incident);
}