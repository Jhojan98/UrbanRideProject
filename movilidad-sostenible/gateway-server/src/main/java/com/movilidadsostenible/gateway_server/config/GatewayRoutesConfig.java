package com.movilidadsostenible.gateway_server.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("usuario-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://usuario-service")
                )
                .route("bicis-service", r -> r
                        .path("/api/bicis/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://bicis-service")
                )
                .route("viaje-service", r -> r
                        .path("/api/travel/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://viaje-service")
                )
                .route("email-service", r -> r
                        .path("/api/email/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://email-service")
                )
                .route("estaciones-service", r -> r
                        .path("/api/stations/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://estaciones-service")
                )
                .route("ciudad-service", r -> r
                        .path("/api/cities/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://ciudad-service")
                )
                .build();
    }
}
