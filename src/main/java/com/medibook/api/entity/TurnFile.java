package com.medibook.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "turn_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TurnFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "turn_id", nullable = false, unique = true)
    private UUID turnId;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_id", insertable = false, updatable = false)
    private TurnAssigned turn;
}
