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
 * Resolves Railway's database environment variables into a HikariCP DataSource.
 *
 * Resolution order (first match wins):
 *   1. SPRING_DATASOURCE_URL  – Railway-standard JDBC URL variable
 *   2. DATABASE_URL           – legacy Railway postgresql:// URL
 *   3. PGHOST / PGPORT / PGDATABASE / PGUSER / PGPASSWORD – individual PG vars
 *
 * @Value keys must use Spring's lowercase dot-notation so that relaxed binding
 * correctly maps env-var names (e.g. SPRING_DATASOURCE_URL → spring.datasource.url).
 */
@Configuration
public class DataSourceConfig {

    // Strategy 1 – Railway standard: SPRING_DATASOURCE_URL (JDBC URL)
    // @Value must use lowercase dot-notation for Spring relaxed binding to work
    @Value("${spring.datasource.url:NONE}")
    private String springDatasourceUrl;

    // Strategy 1 credentials (separate env vars)
    @Value("${spring.datasource.username:NONE}")
    private String envUsername;

    @Value("${spring.datasource.password:NONE}")
    private String envPassword;

    // Strategy 2 – Legacy Railway: DATABASE_URL (postgresql:// URL)
    @Value("${DATABASE_URL:NONE}")
    private String databaseUrl;

    // Strategy 3 – Individual PG* variables (also exposed by Railway)
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

        // --- Strategy 1: SPRING_DATASOURCE_URL (Railway standard JDBC URL) ---
        if (!"NONE".equals(springDatasourceUrl)) {
            applyFromUrl(config, springDatasourceUrl);
            System.out.println("[CampusConnect] DataSource via SPRING_DATASOURCE_URL");
        }
        // --- Strategy 2: Parse DATABASE_URL (legacy postgresql:// format) ---
        else if (!"NONE".equals(databaseUrl)) {
            applyFromUrl(config, databaseUrl);
            System.out.println("[CampusConnect] DataSource via DATABASE_URL");
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
                "Please set SPRING_DATASOURCE_URL (and optionally SPRING_DATASOURCE_USERNAME / " +
                "SPRING_DATASOURCE_PASSWORD) in Railway, or link a PostgreSQL service."
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
