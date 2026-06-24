package ru.starashchuk.currency.exchange.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.starashchuk.currency.exchange.util.DatabaseInitializer;
import ru.starashchuk.currency.exchange.util.DatabaseSource;

@Configuration
@ComponentScan("ru.starashchuk.currency.exchange")
@PropertySource("classpath:application.properties")
@EnableWebMvc
@RequiredArgsConstructor
public class SpringConfig implements WebMvcConfigurer {
    private final Environment environment;
    private static final int MIN_IDLE = 3;
    private static final int MAX_POOL_SIZE = 5;
    private static final String PENS_ALLOWED_FOR_REQUEST = "/**";
    private static final String ALLOWED_ORIGIN = "http://83.222.24.63/";

    @Bean(destroyMethod = "close")
    public HikariDataSource getHikariDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(environment.getProperty("db.driver"));
        hikariConfig.setJdbcUrl(environment.getProperty("db.url"));
        hikariConfig.setUsername(environment.getProperty("db.username"));
        hikariConfig.setPassword(environment.getProperty("db.password"));
        hikariConfig.setMinimumIdle(MIN_IDLE);
        hikariConfig.setMaximumPoolSize(MAX_POOL_SIZE);
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        return dataSource;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(PENS_ALLOWED_FOR_REQUEST)
                .allowedOrigins(ALLOWED_ORIGIN)
                .allowedMethods("GET", "POST", "OPTIONS", "PATCH");
    }

    @Bean
    public DatabaseInitializer databaseInitialization(DatabaseSource source) {
        return new DatabaseInitializer(source);
    }
}
