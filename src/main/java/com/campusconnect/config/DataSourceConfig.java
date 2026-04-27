package com.campusconnect.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Resolves Railway's DATABASE_URL (postgresql://user:pass@host:port/db)
 * into JDBC components that HikariCP understands.
 *
 * Railway auto-sets DATABASE_URL when a PostgreSQL service is linked.
 * Spring Boot / JDBC requires jdbc:postgresql:// prefix + separate credentials.
 * This bean bridges that gap reliably.
 */
@Configuration
public class DataSourceConfig {

    // Railway PostgreSQL plugin sets DATABASE_URL automatically
    @Value("${DATABASE_URL:NONE}")
    private String databaseUrl;

    // Fallback: manually set JDBC-style URL (already has jdbc: prefix)
    @Value("${SPRING_DATASOURCE_URL:NONE}")
    private String springDatasourceUrl;

    // Optional overrides for username/password (separate from URL)
    @Value("${SPRING_DATASOURCE_USERNAME:NONE}")
    private String envUsername;

    @Value("${SPRING_DATASOURCE_PASSWORD:NONE}")
    private String envPassword;

    // Railway also exposes individual PG variables
    @Value("${PGHOST:NONE}")
    private String pgHost;

    @Value("${PGPORT:5432}")
    private String pgPort;

    @Value("${PGDATABASE:NONE}")
    private String pgDatabase;

    @Value("${PGUSER:NONE}")
    private String pgUser;

    @Value("${PGPASSWORD:NONE}")
    private String pgPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");

        // --- Strategy 1: Parse DATABASE_URL (most common on Railway) ---
        if (!"NONE".equals(databaseUrl)) {
            applyFromUrl(config, databaseUrl);
        }
        // --- Strategy 2: SPRING_DATASOURCE_URL (manually configured) ---
        else if (!"NONE".equals(springDatasourceUrl)) {
            applyFromUrl(config, springDatasourceUrl);
        }
        // --- Strategy 3: Individual PG* variables (Railway also exposes these) ---
        else if (!"NONE".equals(pgHost)) {
            String jdbcUrl = "jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase;
            config.setJdbcUrl(jdbcUrl);
            if (!"NONE".equals(pgUser))     config.setUsername(pgUser);
            if (!"NONE".equals(pgPassword)) config.setPassword(pgPassword);
            System.out.println("[CampusConnect] DataSource via PG* vars → " + jdbcUrl);
        }
        else {
            throw new IllegalStateException(
                "[CampusConnect] No database configured! " +
                "Set DATABASE_URL in Railway by linking a PostgreSQL service."
            );
        }

        // Override username/password if explicitly set as separate vars
        if (!"NONE".equals(envUsername)) config.setUsername(envUsername);
        if (!"NONE".equals(envPassword)) config.setPassword(envPassword);

        // Conservative pool for Railway free tier
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }

    /**
     * Parses any PostgreSQL URL format and populates HikariConfig:
     *   postgresql://user:pass@host:port/db
     *   postgres://user:pass@host:port/db
     *   jdbc:postgresql://user:pass@host:port/db
     *   jdbc:postgresql://host:port/db  (credentials as separate vars)
     */
    private void applyFromUrl(HikariConfig config, String rawUrl) {
        try {
            // Normalise to a URI we can parse
            String uriStr = rawUrl
                .replace("jdbc:postgresql://", "postgres://")
                .replace("jdbc:postgres://",   "postgres://")
                .replace("postgresql://",       "postgres://");

            URI uri = new URI(uriStr);

            String host = uri.getHost();
            int    port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String db   = uri.getPath().replaceFirst("^/", "");

            // Build clean JDBC URL (no credentials in URL — set separately below)
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            config.setJdbcUrl(jdbcUrl);

            // Extract credentials from URL if present
            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isEmpty()) {
                String[] parts = userInfo.split(":", 2);
                config.setUsername(parts[0]);
                if (parts.length > 1) config.setPassword(parts[1]);
            }

            System.out.println("[CampusConnect] DataSource → " + jdbcUrl);

        } catch (URISyntaxException e) {
            // Fallback: use URL as-is with jdbc: prefix added
            String jdbcUrl = toJdbcUrl(rawUrl);
            config.setJdbcUrl(jdbcUrl);
            System.err.println("[CampusConnect] URI parse failed, using raw URL: " + jdbcUrl);
        }
    }

    private String toJdbcUrl(String url) {
        if (url.startsWith("jdbc:"))        return url;
        if (url.startsWith("postgres://"))  return "jdbc:postgresql://" + url.substring("postgres://".length());
        if (url.startsWith("postgresql://"))return "jdbc:postgresql://" + url.substring("postgresql://".length());
        return url;
    }
}
