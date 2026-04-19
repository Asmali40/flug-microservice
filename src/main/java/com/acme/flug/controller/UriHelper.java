/*
 * Copyright (C) 2025 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.flug.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import static com.acme.flug.controller.Constants.API_PATH;

/// Hilfsklasse um URIs für den Location-Header zu ermitteln.
@Component
class UriHelper {

    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String X_FORWARDED_HOST = "x-forwarded-host";
    private static final String X_FORWARDED_PREFIX = "x-forwarded-prefix";
    private static final String FLUG_PREFIX = "/fluege";

    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor package-private für Spring.
    UriHelper() {

    }

    /// Basis-URI ermitteln (ohne Query-Parameter).
    ///
    /// @param request Servlet-Request
    /// @return Die Basis-URI als String
    URI getBaseUri(final HttpServletRequest request) {
        final var forwardedHost = request.getHeader(X_FORWARDED_HOST);
        if (forwardedHost != null) {
            // Forwarding durch Ingress oder API-Gateway
            return getBaseUriForwarded(request, forwardedHost);
        }

        // KEIN Forwarding von einem API-Gateway
        // URI aus Schema, Host, Port und Pfad
        final var uriComponents = ServletUriComponentsBuilder.fromRequestUri(request).build();
        final var baseUri = uriComponents.getScheme() + "://" +
            uriComponents.getHost() + ':' + uriComponents.getPort() +
            '/' + API_PATH;

        getLogger().debug("getBaseUri (ohne Forwarding): baseUir={}", baseUri);
        return URI.create(baseUri);
    }

    private URI getBaseUriForwarded(final HttpServletRequest request, final String forwardedHost) {
        // x-forwarded-host = Hostname des API-Gateways

        // "http" oder "https"
        final var forwardedProto = request.getHeader(X_FORWARDED_PROTO);
        if (forwardedProto == null) {
            throw new IllegalStateException("Kein '" + X_FORWARDED_PROTO + "' im Header");
        }

        var forwardedPrefix = request.getHeader(X_FORWARDED_PREFIX);
        // x-forwarded-prefix: null bei Kubernetes Ingress COntroller bzw. "/fluege" bei Spring Cloud Gateway
        if (forwardedPrefix == null) {
            getLogger().trace("getBaseUriForwarded: Kein '{}' im Header", X_FORWARDED_PREFIX);
            forwardedPrefix = FLUG_PREFIX;
        }

        final var baseUri = forwardedProto + "://" + forwardedHost + forwardedPrefix + '/' + API_PATH;
        getLogger().debug("getBaseUriForwarded: {}", baseUri);
        return URI.create(baseUri);
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(UriHelper.class));
    }
}
