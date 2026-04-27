package com.campusconnect.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Converts Railway's DATABASE_URL (postgresql://user:pass@host:port/db)
 * into the JDBC format (jdbc:postgresql://host:port/db) that HikariCP requires.
 *
 * This bean only activates when DATABASE_URL or SPRING_DATASOURCE_URL is present
 * in the environment. For local dev, Spring Boot's own autoconfigure kicks in
 * using the spring.datasource.* properties in application-local.properties.
 */
@Configuration
public class DataSourceConfig {

    @Value("${DATABASE_URL:NONE}")
    private String databaseUrl;

    @Value("${SPRING_DATASOURCE_URL:NONE}")
    private String springDatasourceUrl;

    @Value("${SPRING_DATASOURCE_USERNAME:}")
    private String username;

    @Value("${SPRING_DATASOURCE_PASSWORD:}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        // --- Resolve JDBC URL ---
        // Priority 1: SPRING_DATASOURCE_URL (manually set, may or may not have jdbc: prefix)
        // Priority 2: DATABASE_URL (set automatically by Railway PostgreSQL plugin)

        String rawUrl = null;

        if (!"NONE".equals(springDatasourceUrl) && !springDatasourceUrl.isBlank()) {
            rawUrl = springDatasourceUrl;
        } else if (!"NONE".equals(databaseUrl) && !databaseUrl.isBlank()) {
            rawUrl = databaseUrl;
        }

        if (rawUrl == null) {
            throw new IllegalStateException(
                "[CampusConnect] No database URL configured. " +
                "Set DATABASE_URL (Railway auto-sets this) or SPRING_DATASOURCE_URL."
            );
        }

        String jdbcUrl = toJdbcUrl(rawUrl);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.postgresql.Driver");

        // Parse credentials from the URL string itself if not set as separate vars
        if (!username.isBlank()) {
            config.setUsername(username);
        }
        if (!password.isBlank()) {
            config.setPassword(password);
        }

        // Conservative pool settings for Railway free tier (limited connections)
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);   // 30s
        config.setIdleTimeout(600000);        // 10min
        config.setMaxLifetime(1800000);       // 30min

        System.out.println("[CampusConnect] DataSource configured → " +
            jdbcUrl.replaceAll(":[^:@]+@", ":****@")); // mask password in logs

        return new HikariDataSource(config);
    }

    /**
     * Ensures the URL always starts with jdbc:postgresql://
     * Handles:
     *   postgresql://...  → jdbc:postgresql://...
     *   postgres://...    → jdbc:postgresql://...
     *   jdbc:postgresql://... → unchanged
     */
    private String toJdbcUrl(String url) {
        if (url == null || url.isBlank()) return url;
        if (url.startsWith("jdbc:")) return url;
        if (url.startsWith("postgres://"))   return "jdbc:postgresql://" + url.substring("postgres://".length());
        if (url.startsWith("postgresql://")) return "jdbc:postgresql://" + url.substring("postgresql://".length());
        return url; // unknown format — pass through
    }
}
