package com.movilidadsostenible.gateway_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                    .pathMatchers("/eureka/**").permitAll()
                    .pathMatchers("/user/register").permitAll()
                    .pathMatchers("/user/login/**").permitAll()
                    .pathMatchers("/bicis/**").permitAll()
                    .pathMatchers("/ws/**").permitAll() // WebSocket endpoints
                    .anyExchange().permitAll() //authenticated() 
            ).oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Orígenes explícitos para REST endpoints
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:8080",
            "http://localhost:8081",
            "http://client-frontend",
            "http://client-frontend:80"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicar CORS solo a rutas que NO son WebSocket para evitar duplicación
        source.registerCorsConfiguration("/user/**", configuration);
        source.registerCorsConfiguration("/bicy/**", configuration);
        source.registerCorsConfiguration("/city/**", configuration);
        source.registerCorsConfiguration("/email/**", configuration);
        source.registerCorsConfiguration("/station/**", configuration);
        source.registerCorsConfiguration("/travel/**", configuration);
        // NO aplicar a /bicis/** ni /ws/** - el servicio maneja su propio CORS para WebSocket
        return source;
    }
}
