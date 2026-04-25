package com.example.ifraneguard.Model;

import com.example.ifraneguard.enums.PhotoType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One incident can have many photos
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    // Who uploaded the photo: citizen/officer/admin
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhotoType photoType;

    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private String fileType;
    private Long fileSize;

    @Column(unique = true)
    private String fileHash;

    private Boolean gpsMetadataPresent;
    private Boolean blurry;
    private Boolean emptyImage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}