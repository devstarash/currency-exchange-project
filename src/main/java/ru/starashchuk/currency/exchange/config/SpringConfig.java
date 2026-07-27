package ru.starashchuk.currency.exchange.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan("ru.starashchuk.currency.exchange")
@PropertySource("classpath:application.properties")
@EnableWebMvc
@RequiredArgsConstructor
public class SpringConfig implements WebMvcConfigurer {
    private static final String PENS_ALLOWED_FOR_REQUEST = "/**";
    private static final String ALLOWED_ORIGIN = "http://83.222.24.63/";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(PENS_ALLOWED_FOR_REQUEST)
                .allowedOrigins(ALLOWED_ORIGIN)
                .allowedMethods("GET", "POST", "OPTIONS", "PATCH");
    }
}
