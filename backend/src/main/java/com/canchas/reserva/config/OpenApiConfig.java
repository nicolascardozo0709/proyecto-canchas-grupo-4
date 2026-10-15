package com.canchas.reserva.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components().addSecuritySchemes(securitySchemeName,
                new SecurityScheme()
                    .name(securitySchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Ingrese el token JWT obtenido del endpoint /api/auth/login")
            ))
            .info(new Info()
                .title("API REST - Plataforma de Reserva de Escenarios Deportivos")
                .version("1.0.0")
                .description("Documentación oficial de la API para la Entrega 2 de Desarrollo de Software (Pontificia Universidad Javeriana).\n\n"
                    + "Permite gestión de escenarios deportivos, franjas horarias de 60 minutos, control de concurrencia anti-overbooking y reservas.")
                .contact(new Contact()
                    .name("Grupo 4 - Desarrollo de Software")
                    .email("nicolas.cardozo0709@gmail.com")
                )
                .license(new License().name("Uso Académico - PUJ 2026-3"))
            );
    }
}
