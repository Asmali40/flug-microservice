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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.flug.security;

import com.fasterxml.jackson.annotation.JsonProperty;

/// JSON-Datensatz von Keycloak nach dem Einloggen mit Benutzername und Passwort.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
/// @param accessToken Access-Token für OAuth2
/// @param expiresIn Anzahl Sekunden für die Gültigkeit des Access-Tokens
/// @param refreshExpiresIn Anzahl Sekunden für die Gültigkeit des Refresh-Tokens
/// @param refreshToken Refresh-Token für OAuth2
/// @param tokenType Bearer
/// @param notBeforePolicy Zeitstempel, bevor dem ein JWT nicht benutzt werden darf, um Missbrauch zu vermeiden
/// @param sessionState UUID für die Session innerhalb derer Access-Tokens und Refresh-Tokens angefordert werden können
/// @param scope Scope gemäß <a href="https://www.rfc-editor.org/rfc/rfc6749.html">OAuth 2.0</a> (hier: "email profile")
public record TokenDTO(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("expires_in")
    int expiresIn,
    @JsonProperty("refresh_expires_in")
    int refreshExpiresIn,
    @JsonProperty("refresh_token")
    String refreshToken,
    @JsonProperty("token_type")
    TokenType tokenType,
    @JsonProperty("not-before-policy")
    int notBeforePolicy,
    @JsonProperty("session_state")
    String sessionState,
    ScopeType scope
) {
}
