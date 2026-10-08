package com.cybersentinel.model;

import com.cybersentinel.model.enums.RiskSubjectType;
import com.cybersentinel.model.enums.Severity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskSubjectType subjectType;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID subjectId;

    @Column(nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private String factors;

    @Column(nullable = false)
    private Instant evaluatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public RiskSubjectType getSubjectType() { return subjectType; }
    public void setSubjectType(RiskSubjectType subjectType) { this.subjectType = subjectType; }
    public UUID getSubjectId() { return subjectId; }
    public void setSubjectId(UUID subjectId) { this.subjectId = subjectId; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getFactors() { return factors; }
    public void setFactors(String factors) { this.factors = factors; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
