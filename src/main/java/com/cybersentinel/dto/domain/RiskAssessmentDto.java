package com.cybersentinel.dto.domain;

import com.cybersentinel.model.enums.RiskSubjectType;
import com.cybersentinel.model.enums.Severity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class RiskAssessmentDto {
    private UUID id;
    private RiskSubjectType subjectType;
    private UUID subjectId;
    private Integer riskScore;
    private Severity severity;
    private List<RiskFactorDto> factors;
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
    public List<RiskFactorDto> getFactors() { return factors; }
    public void setFactors(List<RiskFactorDto> factors) { this.factors = factors; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
