package com.taskscheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows local dev servers (React/Vite on any localhost port) to call the
 * API during development. The Vite dev proxy makes this transparent for most
 * requests, but this matters when the UI is served from another origin.
 *
 * <p>Currently allow-listed: the project's usual Vite port (5173), any other
 * Vite/dev port in the 517x range (e.g. 5174 when 5173 is busy), 3000 (classic
 * React dev server), and the 127.0.0.1 equivalent of each. Credentials are
 * allowed because no cookie/session auth is used in development.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://localhost:5174",
                        "http://127.0.0.1:5174",
                        "http://localhost:3000",
                        "http://127.0.0.1:3000")
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}