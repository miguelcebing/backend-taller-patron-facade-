package com.hotel.reservas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configurable por variable de entorno (imprescindible para Vercel -> Railway).
 * En produccion: CORS_ALLOWED_ORIGINS=https://tu-app.vercel.app
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] origenes;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PUT", "OPTIONS")
                .allowedHeaders("*");
        registry.addMapping("/actuator/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET");
    }
}
