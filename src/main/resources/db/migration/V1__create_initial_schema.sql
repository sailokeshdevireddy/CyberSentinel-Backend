CREATE TABLE organizations (
    id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    id BINARY(16) NOT NULL PRIMARY KEY,
    organization_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    last_login_at DATETIME(6),
    CONSTRAINT fk_users_org FOREIGN KEY (organization_id) REFERENCES organizations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE applications (
    id BINARY(16) NOT NULL PRIMARY KEY,
    organization_id BINARY(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    api_key VARCHAR(255) NOT NULL UNIQUE,
    secret_masked VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    last_event_at DATETIME(6),
    CONSTRAINT fk_applications_org FOREIGN KEY (organization_id) REFERENCES organizations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE security_events (
    id BINARY(16) NOT NULL PRIMARY KEY,
    organization_id BINARY(16) NOT NULL,
    application_id BINARY(16) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    received_at DATETIME(6) NOT NULL,
    user_identifier VARCHAR(255),
    device VARCHAR(255),
    ip_address VARCHAR(45),
    location VARCHAR(255),
    metadata JSON,
    risk_score INT,
    severity VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_security_events_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_security_events_app FOREIGN KEY (application_id) REFERENCES applications(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_security_events_org_app_occurred ON security_events (organization_id, application_id, occurred_at);

CREATE TABLE risk_assessments (
    id BINARY(16) NOT NULL PRIMARY KEY,
    subject_type VARCHAR(50) NOT NULL,
    subject_id BINARY(16) NOT NULL,
    risk_score INT NOT NULL,
    severity VARCHAR(50) NOT NULL,
    factors JSON,
    evaluated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_risk_assessments_subject ON risk_assessments (subject_type, subject_id);

CREATE TABLE threats (
    id BINARY(16) NOT NULL PRIMARY KEY,
    organization_id BINARY(16) NOT NULL,
    application_id BINARY(16) NOT NULL,
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    risk_score INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_threats_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_threats_app FOREIGN KEY (application_id) REFERENCES applications(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE incidents (
    id BINARY(16) NOT NULL PRIMARY KEY,
    organization_id BINARY(16) NOT NULL,
    application_id BINARY(16) NOT NULL,
    title VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    risk_score INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    assigned_to_user_id BINARY(16),
    resolution TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_incidents_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_incidents_app FOREIGN KEY (application_id) REFERENCES applications(id),
    CONSTRAINT fk_incidents_assignee FOREIGN KEY (assigned_to_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE incident_notes (
    id BINARY(16) NOT NULL PRIMARY KEY,
    incident_id BINARY(16) NOT NULL,
    author_id BINARY(16) NOT NULL,
    author_name VARCHAR(255) NOT NULL,
    note TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_incident_notes_incident FOREIGN KEY (incident_id) REFERENCES incidents(id),
    CONSTRAINT fk_incident_notes_author FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE incident_timeline_entries (
    id BINARY(16) NOT NULL PRIMARY KEY,
    incident_id BINARY(16) NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    actor_type VARCHAR(50) NOT NULL,
    actor_name VARCHAR(255) NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_incident_timeline_incident FOREIGN KEY (incident_id) REFERENCES incidents(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id BINARY(16) NOT NULL PRIMARY KEY,
    organization_id BINARY(16) NOT NULL,
    actor_id BINARY(16),
    actor_name VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id BINARY(16) NOT NULL,
    result VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    occurred_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_audit_logs_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_logs_target ON audit_logs (target_type, target_id);

CREATE TABLE notifications (
    id BINARY(16) NOT NULL PRIMARY KEY,
    organization_id BINARY(16) NOT NULL,
    user_id BINARY(16),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(100) NOT NULL,
    related_entity_id BINARY(16),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_notifications_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE threat_related_events (
    threat_id BINARY(16) NOT NULL,
    event_id BINARY(16) NOT NULL,
    CONSTRAINT uq_threat_event UNIQUE (threat_id, event_id),
    CONSTRAINT fk_threat_events_threat FOREIGN KEY (threat_id) REFERENCES threats(id),
    CONSTRAINT fk_threat_events_event FOREIGN KEY (event_id) REFERENCES security_events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE incident_related_threats (
    incident_id BINARY(16) NOT NULL,
    threat_id BINARY(16) NOT NULL,
    CONSTRAINT uq_incident_threat UNIQUE (incident_id, threat_id),
    CONSTRAINT fk_incident_threats_incident FOREIGN KEY (incident_id) REFERENCES incidents(id),
    CONSTRAINT fk_incident_threats_threat FOREIGN KEY (threat_id) REFERENCES threats(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE incident_related_events (
    incident_id BINARY(16) NOT NULL,
    event_id BINARY(16) NOT NULL,
    CONSTRAINT uq_incident_event UNIQUE (incident_id, event_id),
    CONSTRAINT fk_incident_events_incident FOREIGN KEY (incident_id) REFERENCES incidents(id),
    CONSTRAINT fk_incident_events_event FOREIGN KEY (event_id) REFERENCES security_events(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
