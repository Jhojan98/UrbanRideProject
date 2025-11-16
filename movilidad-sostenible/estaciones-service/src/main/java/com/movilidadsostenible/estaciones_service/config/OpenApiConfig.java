package com.movilidadsostenible.estaciones_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Estaciones Service",
                version = "v1",
                description = "Documentación OpenAPI para el microservicio de estaciones",
                contact = @Contact(name = "Equipo Estaciones", email = "estaciones@movilidad.com"),
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        servers = {
                @Server(url = "http://localhost:8005", description = "Local")
        }
)
public class OpenApiConfig {}

