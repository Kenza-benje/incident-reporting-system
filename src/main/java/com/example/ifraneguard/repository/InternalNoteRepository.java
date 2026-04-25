package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.InternalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternalNoteRepository extends JpaRepository<InternalNote, Long> {

    List<InternalNote> findByIncidentOrderByCreatedAtDesc(Incident incident);
}