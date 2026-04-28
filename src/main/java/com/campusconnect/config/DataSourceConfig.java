package com.campusconnect.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Robust DataSource configuration for CampusConnect.
 * Automatically handles local MySQL and Railway production environments.
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Convert raw PostgreSQL URL format to JDBC format if necessary.
        // Railway provides DATABASE_URL as postgresql://user:pass@host:port/db,
        // but HikariCP requires jdbc:postgresql://user:pass@host:port/db.
        String jdbcUrl = dbUrl.startsWith("postgresql://")
                ? "jdbc:" + dbUrl
                : dbUrl;

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);

        // Driver Detection
        if (jdbcUrl.contains("mysql")) {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else if (jdbcUrl.contains("postgresql")) {
            config.setDriverClassName("org.postgresql.Driver");
        }

        // Optimized Connection Pool Settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1200000);
        
        return new HikariDataSource(config);
    }
}
