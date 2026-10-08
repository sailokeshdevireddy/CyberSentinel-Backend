package com.cybersentinel.repository;

import com.cybersentinel.model.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

import com.cybersentinel.model.enums.RiskSubjectType;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {
    Optional<RiskAssessment> findBySubjectTypeAndSubjectId(RiskSubjectType subjectType, UUID subjectId);
}
