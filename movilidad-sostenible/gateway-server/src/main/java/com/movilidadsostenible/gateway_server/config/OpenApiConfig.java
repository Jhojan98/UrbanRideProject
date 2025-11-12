package com.movilidadsostenible.gateway_server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "UrbanRide API Gateway",
                version = "v1",
                description = "Documentación centralizada de APIs para el ecosistema UrbanRide",
                contact = @Contact(name = "Equipo UrbanRide", email = "soporte@urbanride.local"),
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        servers = {
                @Server(url = "http://localhost:8090", description = "local")
        }
)
public class OpenApiConfig {
}

