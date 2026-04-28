package com.campusconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Dynamically configures the DataSource based on the JDBC URL.
 * - Local dev  → MySQL  (jdbc:mysql://...)
 * - Railway    → PostgreSQL (jdbc:postgresql://...)
 * Spring Boot JPA auto-detects the Hibernate dialect from this DataSource.
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
        // Railway sometimes provides the URL as postgresql:// — convert to jdbc:postgresql://
        String jdbcUrl = dbUrl;
        if (jdbcUrl.startsWith("postgresql://")) {
            jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgresql://".length());
        }

        String driverClass;
        if (jdbcUrl.contains("mysql")) {
            driverClass = "com.mysql.cj.jdbc.Driver";
        } else {
            driverClass = "org.postgresql.Driver";
        }

        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(dbUsername)
                .password(dbPassword)
                .driverClassName(driverClass)
                .build();
    }
}
