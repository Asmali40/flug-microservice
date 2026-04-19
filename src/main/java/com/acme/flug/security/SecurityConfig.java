/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.flug.security;

import com.c4_soft.springaddons.security.oidc.starter.synchronised.resourceserver.ResourceServerExpressionInterceptUrlRegistryPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.restclient.autoconfigure.RestClientSsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import static com.acme.flug.config.DevConfig.DEV;
import static org.springframework.http.HttpMethod.GET;

/// Security-Konfiguration.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
public class SecurityConfig {
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit `package private` für `@Import()` bei _Spring_.
    SecurityConfig() {
    }

    /// Bean-Methode, um ein Objekt zum Interface KeycloakRepository zu erstellen.
    ///
    /// @param keycloakConfig Application-Properties für Keycloak
    /// @param restClientBuilder Injiziertes Objekt von RestClient.Builder
    /// @param ssl Injiziertes Objekt von RestClientSsl
    /// @return Objekt zum Interface KeycloakRepository
    @Bean
    KeycloakRepository keycloakRepository(
        final KeycloakConfig keycloakConfig,
        final RestClient.Builder restClientBuilder,
        final RestClientSsl ssl
    ) {
        getLogger().info("@Bean keycloakRepository");
        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(keycloakConfig.schema())
            .host(keycloakConfig.host())
            .port(keycloakConfig.port())
            .build();
        getLogger().debug("@Bean keycloakRepository: uriComponents={}", uriComponents);

        // BEACHTE: Propagierung der Traces ueber das Netz fuer Zipkin funktioniert nur mit RestClient.Builder
        // und NICHT mit eigenem Bean fuer RestClient
        // https://docs.spring.io/spring-boot/reference/actuator/tracing.html
        final var restClient = restClientBuilder
            .baseUrl(uriComponents.toUriString())
            .apply(ssl.fromBundle("microservice"))
            .build();
        final var clientAdapter = RestClientAdapter.create(restClient);
        final var proxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build();
        return proxyFactory.createClient(KeycloakRepository.class);
    }

    /// Bean, um URIs aus Bibliotheken für z.B. Swagger und Actuator einzuschränken
    ///
    /// @return `ResourceServerExpressionInterceptUrlRegistryPostProcessor` aus _spring-addons-starter-oidc_
    // https://stackoverflow.com/questions/77801381/spring-boot-keycloak-rbac#answer-77802128
    @Bean
    @Profile(DEV)
    ResourceServerExpressionInterceptUrlRegistryPostProcessor expressionInterceptUrlRegistryPostProcessor() {
        return registry -> registry
            .requestMatchers(GET, "/v3/api-docs", "/v3/api-docs.yaml", "/swagger-ui.html",
                "/actuator", "/actuator/**")
            .permitAll();
    }

    /// Bean-Methode für die Überprüfung, ob ein Passwort ein bekanntes ("gehacktes") Passwort ist.
    ///
    /// @return "Checker-Objekt" für die Überprüfung, ob ein Passwort ein bekanntes ("gehacktes") Passwort ist.
    @Bean
    CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(SecurityConfig.class));
    }
}
