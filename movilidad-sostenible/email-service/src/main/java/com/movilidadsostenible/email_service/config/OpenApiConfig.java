package com.movilidadsostenible.email_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Email Service",
                version = "v1",
                description = "Documentación OpenAPI para el microservicio de envío y verificación de correos",
                contact = @Contact(name = "Equipo Email", email = "email@movilidad.com"),
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        servers = {
                @Server(url = "http://localhost:8004", description = "Local"),
        }
)
public class OpenApiConfig {}

