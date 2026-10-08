package com.cybersentinel.service;

import com.cybersentinel.dto.common.HealthResponse;
import com.cybersentinel.exception.ApiException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthService.class);

    private final JdbcTemplate jdbcTemplate;
    private final String applicationName;

    public HealthService(JdbcTemplate jdbcTemplate,
                         @Value("${spring.application.name}") String applicationName) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationName = applicationName;
    }

    /** Confirms the app is up and reports which MySQL schema it is really connected to. */
    public HealthResponse check() {
        String schema;
        try {
            schema = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        } catch (DataAccessException ex) {
            log.error("Database health check failed", ex);
            throw ApiException.serviceUnavailable("Database is unreachable");
        }
        return new HealthResponse("UP", applicationName, schema, Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
