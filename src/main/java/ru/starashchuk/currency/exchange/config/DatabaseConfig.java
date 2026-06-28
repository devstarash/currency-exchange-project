package ru.starashchuk.currency.exchange.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    private static final int MIN_IDLE = 3;
    private static final int MAX_POOL_SIZE = 5;
    @Value("${db.url}")
    private String databaseUrl;
    @Value("${db.driver}")
    private String databaseDriver;
    @Value("${db.username}")
    private String databaseUsername;
    @Value("${db.password}")
    private String databasePassword;

    @Bean(destroyMethod = "close")
    public HikariDataSource getHikariDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(databaseDriver);
        hikariConfig.setJdbcUrl(databaseUrl);
        hikariConfig.setUsername(databaseUsername);
        hikariConfig.setPassword(databasePassword);
        hikariConfig.setMinimumIdle(MIN_IDLE);
        hikariConfig.setMaximumPoolSize(MAX_POOL_SIZE);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        DataSource dataSource = getHikariDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate;
    }

}
