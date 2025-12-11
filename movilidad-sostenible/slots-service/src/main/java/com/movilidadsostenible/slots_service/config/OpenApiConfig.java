package com.movilidadsostenible.slots_service.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI slotsServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Slots Service API")
                .description("API para la gestión de slots de estaciones")
                .version("v1")
                .contact(new Contact().name("Equipo Movilidad Sostenible"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .externalDocs(new ExternalDocumentation()
                .description("Repositorio del proyecto")
                .url("https://example.com/repo"));
    }
}

