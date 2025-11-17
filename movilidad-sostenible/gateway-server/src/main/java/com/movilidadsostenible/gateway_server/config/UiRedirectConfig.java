package com.movilidadsostenible.gateway_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UiRedirectConfig {

    @Bean
    public RouterFunction<ServerResponse> indexRedirect() {
        return route(GET("/"),
                req -> ServerResponse.status(HttpStatus.FOUND)
                        .header("Location", "/swagger-ui.html")
                        .build());
    }

    @Bean
    public RouterFunction<ServerResponse> swaggerUiResources() {
        // Redirigir /swagger-ui.html a /swagger-ui/index.html
        RouterFunction<ServerResponse> redirect = route(GET("/swagger-ui.html"),
                req -> ServerResponse.status(HttpStatus.FOUND)
                        .location(URI.create("/swagger-ui/index.html"))
                        .build());

        // Servir recursos estáticos de WebJars para Swagger UI
        RouterFunction<ServerResponse> webjars = RouterFunctions.resources(
                "/webjars/**", new ClassPathResource("META-INF/resources/webjars/"));

        // Servir la carpeta de swagger-ui bajo /swagger-ui/**
        RouterFunction<ServerResponse> swaggerUi = RouterFunctions.resources(
                "/swagger-ui/**", new ClassPathResource("META-INF/resources/webjars/swagger-ui/"));

        // Servir swagger-ui.html directamente si fuera solicitado como recurso
        RouterFunction<ServerResponse> swaggerHtml = RouterFunctions.resources(
                "/swagger-ui.html", new ClassPathResource("META-INF/resources/"));

        return redirect.and(webjars).and(swaggerUi).and(swaggerHtml);
    }
}
