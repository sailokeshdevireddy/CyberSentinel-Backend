package com.cybersentinel.repository;

import com.cybersentinel.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaMigrationTest extends BaseIntegrationTest {

    @Test
    void contextLoadsAndSchemaValidated() {
        // If the context loads successfully, it means:
        // 1. Testcontainers started MySQL
        // 2. Flyway successfully ran the V1 migration script
        // 3. Hibernate successfully validated the schema (spring.jpa.hibernate.ddl-auto=validate)
        assertThat(mysql.isRunning()).isTrue();
    }
}
