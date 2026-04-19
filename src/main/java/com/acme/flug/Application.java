/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
/// This program is free software: you can redistribute it and/or modify
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

package com.acme.flug;

import com.acme.flug.config.DevConfig;
import com.acme.flug.config.WebConfig;
import com.acme.flug.security.SecurityConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import static com.acme.flug.Banner.TEXT;

/// Klasse mit der main-Methode für die Anwendung auf Basis von Spring Boot.
/// ![Komponentendiagramm](../../../../asciidoc/Komponenten.svg)
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
// Maven: [Komponentendiagramm](../../../../../generated-docs/Komponenten.svg)
@SpringBootApplication(proxyBeanMethods = false)
@Import({SecurityConfig.class, WebConfig.class, DevConfig.class})
@ConfigurationPropertiesScan
@EnableJpaRepositories
@EnableMethodSecurity
@EnableAsync
@SuppressWarnings({"ImplicitSubclassInspection", "ClassUnconnectedToPackage"})
public final class Application {
    private Application() {
    }

    /// Hauptprogramm, um den Microservice zu starten.
    ///
    /// @param args Evtl. zusätzliche Argumente für den Start des Microservice
    public static void main(final String... args) {
        new SpringApplicationBuilder(Application.class)
            .banner((_, _, out) -> out.println(TEXT))
            .run(args);
    }
}
