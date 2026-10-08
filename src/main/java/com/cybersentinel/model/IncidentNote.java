package com.cybersentinel.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "incident_notes")
@EntityListeners(AuditingEntityListener.class)
public class IncidentNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID incidentId;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID authorId;

    @Column(nullable = false)
    private String authorName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String note;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getIncidentId() { return incidentId; }
    public void setIncidentId(UUID incidentId) { this.incidentId = incidentId; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
