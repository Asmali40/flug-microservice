/*
 * Copyright (C) 2024 - present Juergen Zimmermann, Hochschule Karlsruhe
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/// Spring-Konfiguration für einen _REST_-Client für _Keycloak_.
///
/// @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
/// @param schema http oder https (für den Keycloak-Server)
/// @param host Rechnername des Keycloak-Servers
/// @param port Port des Keycloak-Servers
/// @param issuerUri URI als Praefix fuer ".../.well-known/openid-configuration"
/// @param clientId Client-ID im Keycloak-Server
/// @param clientSecret Client-Secret gemäß der Client-Konfiguration in Keycloak
@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakConfig(
    @DefaultValue("https")
    String schema,

    @DefaultValue("keycloak")
    String host,

    @DefaultValue("8443")
    int port,

    @DefaultValue("https://keycloak:8443/realms/spring")
    String issuerUri,

    @DefaultValue("spring-client")
    String clientId,

    String clientSecret
) {
}
