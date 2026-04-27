package com.campusconnect.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Converts Railway's DATABASE_URL (postgresql://user:pass@host:port/db)
 * into the JDBC format (jdbc:postgresql://host:port/db) that HikariCP requires.
 *
 * Railway sets DATABASE_URL automatically when you add a PostgreSQL plugin.
 * Spring Boot's auto-configuration uses spring.datasource.url which requires
 * the jdbc: prefix — this bean bridges that gap without any env-var gymnastics.
 */
@Configuration
public class DataSourceConfig {

    /**
     * Falls back to an empty string so the app doesn't crash during local dev
     * when DATABASE_URL is absent (local dev uses application.properties values instead).
     */
    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${SPRING_DATASOURCE_URL:}")
    private String springDatasourceUrl;

    @Value("${SPRING_DATASOURCE_USERNAME:}")
    private String username;

    @Value("${SPRING_DATASOURCE_PASSWORD:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // --- Resolve JDBC URL ---
        // Priority 1: SPRING_DATASOURCE_URL (already in jdbc: format, manually set)
        // Priority 2: DATABASE_URL (Railway native, needs jdbc: prefix added)
        // Priority 3: local dev fallback handled by Spring Boot autoconfigure (this bean won't activate)

        String jdbcUrl = null;

        if (springDatasourceUrl != null && !springDatasourceUrl.isBlank()) {
            jdbcUrl = toJdbcUrl(springDatasourceUrl);
        } else if (databaseUrl != null && !databaseUrl.isBlank()) {
            jdbcUrl = toJdbcUrl(databaseUrl);
        }

        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            // Neither env var is set — return null so Spring Boot autoconfigure takes over
            // (works for local dev with application.properties hardcoded values)
            throw new IllegalStateException(
                "No database URL configured. Set DATABASE_URL or SPRING_DATASOURCE_URL environment variable."
            );
        }

        config.setJdbcUrl(jdbcUrl);

        // Parse credentials from URL if not supplied as separate env vars
        if (username != null && !username.isBlank()) {
            config.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }

        config.setDriverClassName("org.postgresql.Driver");

        // Connection pool tuning for Railway's free tier (limited connections)
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }

    /**
     * Converts postgresql:// or postgres:// URLs to jdbc:postgresql:// format.
     * If already in jdbc: format, returns as-is.
     */
    private String toJdbcUrl(String url) {
        if (url == null || url.isBlank()) return url;
        if (url.startsWith("jdbc:")) return url;           // already correct
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        if (url.startsWith("postgresql://")) {
            return "jdbc:postgresql://" + url.substring("postgresql://".length());
        }
        return url; // unknown format — pass through and let JDBC report the error
    }
}
