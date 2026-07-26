package com.marquina.pet_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI petApiOpenAPI() {
        Info info = new Info()
                .title("Pet API")
                .version("1.0.0")
                .description("""
                        API REST que actúa como fachada sobre Petstore Swagger.

                        Expone un contrato propio reducido: los campos del sistema externo \
                        que no forman parte de la especificación no se propagan al cliente.

                        Los errores del sistema externo se traducen a 502 y 504 en lugar de 500, \
                        para distinguir un fallo aguas arriba de un fallo de este servicio.""")
                .contact(new Contact().name("Milagros Marquina"));

        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Entorno local");

        return new OpenAPI().info(info).servers(List.of(localServer));
    }
}
