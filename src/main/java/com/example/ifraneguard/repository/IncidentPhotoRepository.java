package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.IncidentPhoto;
import com.example.ifraneguard.enums.PhotoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentPhotoRepository extends JpaRepository<IncidentPhoto, Long> {

    Optional<IncidentPhoto> findByFileHash(String fileHash);

    boolean existsByFileHash(String fileHash);

    List<IncidentPhoto> findByIncidentOrderByUploadedAtDesc(Incident incident);

    List<IncidentPhoto> findByIncidentAndPhotoTypeOrderByUploadedAtDesc(
            Incident incident,
            PhotoType photoType
    );
}