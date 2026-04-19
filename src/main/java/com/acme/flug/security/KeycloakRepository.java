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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.flug.security;

import java.util.Map;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/// Repository für einen _HTTP-Client_ von _Spring_ für _Keycloak_.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
@HttpExchange
public interface KeycloakRepository {
    /// GET-Request, um von _Keycloak_ die Konfigurationsdaten abzufragen.
    ///
    /// @return Die Konfigurationsdaten als Map
    @GetExchange("/realms/spring/.well-known/openid-configuration")
    Map<String, Object> openidConfiguration();

    /// POST-Request, um von Keycloak einen JSON-Datensatz mit Access-Token, Refresh-Token und Ablaufdauer zu erhalten.
    ///
    /// @param loginData als String für den Request-Body zum Content-Type application/x-www-form-urlencoded
    /// @param contentType Content-Type "application/x-www-form-urlencoded"
    /// @return JWT nach erfolgreichem Einloggen; HttpClientErrorException.Unauthorized für den Statuscode 401
    @PostExchange("/realms/spring/protocol/openid-connect/token")
    TokenDTO token(
        @RequestBody String loginData,
        @RequestHeader(CONTENT_TYPE) String contentType
    );
}
