package com.movilidadsostenible.viaje_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Viaje Service",
                version = "v1",
                description = "Documentación OpenAPI para el microservicio de gestión de viajes",
                contact = @Contact(name = "Equipo Viajes", email = "viajes@movilidad.com"),
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        servers = {
                @Server(url = "http://localhost:8003", description = "Local"),
                @Server(url = "http://gateway-server:8090", description = "Gateway interno")
        }
)
public class OpenApiConfig {
    // Espacio para futuras configuraciones (seguridad, grupos, etc.)
}

