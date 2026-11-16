package com.covoiturage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plateforme de Covoiturage Multi-Étapes API")
                        .version("1.0.0")
                        .description("API REST pour la gestion d'une plateforme de covoiturage avec trajets multi-arrêts intermédiaires, " +
                                "réservation de segments et tarification dynamique - Simplon Maghreb / YouCode.")
                        .contact(new Contact()
                                .name("YouCode Covoiturage Team")
                                .email("contact@covoiturage.ma"))
                        .license(new License().name("Apache 2.0")));
    }
}
