/*
 * Copyright (C) 2025 - present Efe Yüksel, Hochschule Karlsruhe
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
package com.acme.flug.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.tomcat.TomcatWebServerFactory;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;

/// Factory für Tomcat als Servlet-Container, um auch `HTTP` bereitzustellen - zusätzlich zu `HTTPS`.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
sealed interface TomcatHttpConnector permits DevConfig {
    /// Port für HTTP (zusätzlich zu HTTPS).
    int HTTP_PORT = 8081;

    /// Protokoll-Ausgabe, wenn Kubernetes erkannt wird.
    ///
    /// @return Factory für Tomcat als Servlet-Container, um zusätzlich zu `HTTPS` und Port `8443` auch `HTTP` mit Port
    ///         `8080` bereitzustellen.
    @Bean
    default WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatHttpConnector() {
        return factory -> {
            final var connector = new Connector(TomcatWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setPort(HTTP_PORT);
            connector.setSecure(false);
            factory.addAdditionalConnectors(connector);
        };
    }
}
