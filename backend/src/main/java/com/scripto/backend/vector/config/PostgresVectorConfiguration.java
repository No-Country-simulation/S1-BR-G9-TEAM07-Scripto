package com.scripto.backend.vector.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(
        name = "scripto.vector.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PostgresVectorConfiguration {

    @Bean(name = "postgresVectorDataSource")
    public DataSource postgresVectorDataSource(VectorProperties properties) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(properties.getMaximumPoolSize());
        config.setConnectionTimeout(properties.getConnectionTimeoutMs());
        config.setValidationTimeout(
                Math.min(properties.getConnectionTimeoutMs(), 2_000L)
        );
        config.setPoolName("scripto-pgvector");
        config.setInitializationFailTimeout(-1);

        return new HikariDataSource(config);
    }

    @Bean(name = "postgresVectorJdbcTemplate")
    public JdbcTemplate postgresVectorJdbcTemplate(
            @Qualifier("postgresVectorDataSource") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "postgresVectorTransactionTemplate")
    public TransactionTemplate postgresVectorTransactionTemplate(
            @Qualifier("postgresVectorDataSource") DataSource dataSource
    ) {
        return new TransactionTemplate(
                new DataSourceTransactionManager(dataSource)
        );
    }
}