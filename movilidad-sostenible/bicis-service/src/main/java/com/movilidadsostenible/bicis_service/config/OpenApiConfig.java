package com.movilidadsostenible.bicis_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Bicis Service",
                version = "v1",
                description = "Documentación OpenAPI para el microservicio de gestión de bicicletas",
                contact = @Contact(name = "Equipo Desarrollo", email = "dev@movilidad.com"),
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        servers = {
                @Server(url = "http://localhost:8002", description = "Local"),
                @Server(url = "http://localhost:8090/bicy", description = "Gateway")
        }
)
public class OpenApiConfig {
}

