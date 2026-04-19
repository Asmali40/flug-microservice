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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.flug.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import static com.acme.flug.security.AuthController.AUTH_PATH;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/// Controller für Abfragen zu Security.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
@RestController
@RequestMapping(AUTH_PATH)
@Tag(name = "Authentifizierung API")
@SuppressWarnings({"ClassFanOutComplexity", "java:S1075"})
class AuthController {
    /// Pfad für Authentifizierung.
    static final String AUTH_PATH = "/auth";

    private final KeycloakRepository keycloakRepository;
    private final KeycloakConfig keycloakConfig;
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit `package private` für _Constructor Injection_ bei _Spring_.
    ///
    /// @param keycloakRepository Repository für einen _HTTP Client_ mit _Spring_ für die REST-Schnittstelle von
    /// _Keycloak_.
    /// @param keycloakConfig Property-Objekt zu _Keycloak_.
    AuthController(final KeycloakRepository keycloakRepository, final KeycloakConfig keycloakConfig) {
        this.keycloakRepository = keycloakRepository;
        this.keycloakConfig = keycloakConfig;
    }

    /// Unmittelbarer Aufruf nach dem Konstruktor, um die Properties für _Keycloak_ zu protokollieren.
    @EventListener(ApplicationReadyEvent.class)
    void logInit() {
        getLogger().info("Keycloak keycloakConfig = {}", keycloakConfig);
    }

    /// Bearer-Token zu Benutzername und Passwort durch einen POST-Request ermitteln.
    ///
    /// @param loginDto DTO-Objekt mit Benutzername und Passwort.
    /// @return Bearer-Token mit Statuscode `200` bei gültigen Login-Daten, sonst Statuscode `401`.
    @PostMapping(path = "/token", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Token zu Benutzername und Passwort", tags = "Auth")
    @ApiResponse(responseCode = "200", description = "Token erhalten")
    @ApiResponse(responseCode = "401", description = "Fehler bei Username oder Passwort")
    TokenDTO token(@RequestBody final LoginDTO loginDto) {
        getLogger().debug("token: loginDto={}", loginDto);
        final var loginData = "username=" + loginDto.username() + "&password=" + loginDto.password() +
            "&grant_type=password&client_id=" + keycloakConfig.clientId() + "&client_secret=" +
            keycloakConfig.clientSecret();
        getLogger().trace("loginData: {}", loginData);

        final var tokenDTO = keycloakRepository.token(loginData, APPLICATION_FORM_URLENCODED_VALUE);
        getLogger().debug("token: tokenDTO={}", tokenDTO);
        return tokenDTO;
    }

    /// Einen JWT gemäß _OAuth 2_ durch einen GET-Request dekodieren.
    ///
    /// @param jwt JWT.
    /// @return Map für den JSON-Datensatz zum dekodierten JWT.
    @GetMapping("/me")
    @RolleAdminOrUser
    @Operation(summary = "JWT bei OAuth 2 abfragen", tags = "Auth")
    @ApiResponse(responseCode = "200", description = "Eingeloggt")
    @ApiResponse(responseCode = "401", description = "Fehler bei Username oder Passwort")
    Map<String, Object> me(@AuthenticationPrincipal final Jwt jwt) {
        getLogger().debug("me: jwt={}", jwt);
        return Map.of(
            "subject", jwt.getSubject(),
            "claims", jwt.getClaims()
        );
    }

    /// _ExceptionHandler_, falls bei einer _Query_ oder _Mutation_ ein Flug nicht gefunden wird.
    ///
    /// @param ex Exception, falls ein Login mit ungültigen Daten versucht wird.
    @ExceptionHandler
    @ResponseStatus(UNAUTHORIZED)
    void onUnauthorized(@SuppressWarnings("unused") final HttpClientErrorException.Unauthorized ex) {
        // keine Verarbeitung fuer den leeren Response-Body
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(AuthController.class));
    }
}
