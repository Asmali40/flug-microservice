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
package com.acme.flug.config;

import com.acme.flug.security.RolleAdmin;
import java.util.Map;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.flug.config.DevConfig.DEV;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/// Eine Controller-Klasse, um beim Entwickeln, die (Test-) DB neu zu laden.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
@RestController
@RequestMapping("/dev")
@Profile(DEV)
class DbPopulateController {
    private final Flyway flyway;
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit `package private` für Constructor Injection bei _Spring_.
    ///
    /// @param flyway Injiziertes Objekt für die Integration mit _Flyway_.
    DbPopulateController(final Flyway flyway) {
        this.flyway = flyway;
    }

    /// Die (Test-) DB wird bei einem POST-Request neu geladen.
    ///
    /// @return Response mit Statuscode `200` und Body `{"db_populate": "ok"}`, falls keine Exception aufgetreten ist.
    @PostMapping(value = "db_populate", produces = APPLICATION_JSON_VALUE)
    @RolleAdmin
    Map<String, String> dbPopulate() {
        getLogger().warn("Die DB wird neu geladen");
        flyway.clean();
        flyway.migrate();
        getLogger().warn("Die DB wurde neu geladen");
        return Map.of("db_populate", "ok");
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(DbPopulateController.class));
    }
}
