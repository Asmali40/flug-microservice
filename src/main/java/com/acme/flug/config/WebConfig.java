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

import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.StandardApiVersionDeprecationHandler;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/// Versionierung der Endpunkte mit [Semantic Versioning](https://semver.org)
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
@Configuration
public class WebConfig implements WebMvcConfigurer {
    /// Konstruktor mit _package private_ für _Spring_.
    WebConfig() {
        // leerer Rumpf
    }

    /// Deprecation von versionierten Endpunkten.
    /// @param configurer Injiziertes Objekt vom Typ ApiVersionConfigurer
    // https://docs.spring.io/spring-framework/reference/7.0/web/webmvc/mvc-config/api-version.html
    // https://docs.spring.io/spring-framework/reference/7.0/web/webmvc/mvc-controller/ann-requestmapping.html
    @Override
    public void configureApiVersioning(final ApiVersionConfigurer configurer) {
        // https://www.epochconverter.com
        // https://www.unixtimestamp.com
        final var zoneId = ZoneId.systemDefault();
        @SuppressWarnings("MagicNumber")
        final var deprecationDate = LocalDate.of(2025, 9, 1)
            .atStartOfDay(zoneId);

        // https://docs.spring.io/spring-framework/docs/7.0.0-M9/javadoc-api/org/springframework/web/accept/...
        // ...StandardApiVersionDeprecationHandler.VersionSpec.html
        final var deprecationHandler = new StandardApiVersionDeprecationHandler();
        deprecationHandler.configureVersion("0.0.1")
            .setDeprecationDate(deprecationDate);

        configurer.setDeprecationHandler(deprecationHandler);
    }
}
