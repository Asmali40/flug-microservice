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
package com.acme.flug.controller;

import com.acme.flug.service.FlugService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.flug.controller.Constants.API_PATH;
import static com.acme.flug.controller.Constants.ID_PATTERN;
import static com.acme.flug.controller.Constants.VERSION_3;
import static com.acme.flug.controller.Constants.VERSION_3_EXAMPLE;
import static com.acme.flug.controller.Constants.X_VERSION;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

/// Eine _Controller_-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
/// Methoden der Klasse abgebildet werden.
/// ![Klassendiagramm](../../../../../asciidoc/FlugController.svg)
///
// Maven: [Klassendiagramm](../../../../../../generated-docs/FlugController.svg)
@RestController
@RequestMapping(API_PATH)
@OpenAPIDefinition(info = @Info(title = "Flug API", version = VERSION_3))
@SuppressWarnings({"java:S1075", "ClassFanOutComplexity"})
class FlugController {

    private static final String DEFAULT_PAGE = "0";

    private static final String DEFAULT_PAGE_SIZE = "10";

    private static final String SUCHEN_TAG = "Suchen";

    private final FlugService service;
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit `package private` für _Constructor Injection_ bei _Spring_.
    ///
    /// @param service Injiziertes Objekt von `FlugService`.
    FlugController(final FlugService service) {
        this.service = service;
    }

    /// Suche anhand der Flug-ID als Pfad-Parameter
    ///
    /// @param id ID des zu suchenden Flugs
    /// @return Ein Response mit einem Dummy-User und Statuscode 200.
    @GetMapping(path = "{id:" + ID_PATTERN + "}", produces = APPLICATION_JSON_VALUE, version = VERSION_3)
    @Operation(summary = "Suche Flug anhand der ID")
    @ApiResponse(responseCode = "200", description = "Flug gefunden")
    @ApiResponse(responseCode = "304", description = "Nicht geändert")
    @ApiResponse(responseCode = "404", description = "Flug nicht gefunden")
    ResponseEntity<Object> getById(
        @PathVariable final UUID id,
        @RequestHeader("If-None-Match") @Nullable final String ifNoneMatch
    ) {
        getLogger().debug("getById: id={}, ifNoneMatch={}", id, ifNoneMatch);

        final var flug = service.findByIdMitFlugzeug(id);
        final var versionStr = "\"" + flug.getVersion() + '"';

        if (versionStr.equals(ifNoneMatch)) {
            getLogger().debug("getById: NOT_MODIFIED");
            return status(NOT_MODIFIED).build();
        }

        return ok()
            .eTag(versionStr)
            .body(FlugOhnePassagiere.ofMitPassagieren(flug));
    }

    /// Suche von Flügen über Query-Parameter.
    ///
    /// @param queryparam Query-Parameter als Map.
    /// @param page Seitennummerierung mit Spring Data.
    /// @param size Anzahl Einträge je Seite.
    /// @return Ein Response mit dem Statuscode 200 und den gefundenen Flug als Page oder Statuscode 404.
    @GetMapping(produces = APPLICATION_JSON_VALUE, version = VERSION_3)
    @Operation(summary = "Suche Flüge mit Query-Parametern", tags = SUCHEN_TAG)
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_3_EXAMPLE)
    @ApiResponse(responseCode = "200", description = "Page mit den Fluegen")
    @ApiResponse(responseCode = "404", description = "Keine Fluege gefunden")
    PagedModel<FlugOhnePassagiere> get(
        @RequestParam final MultiValueMap<String, String> queryparam,
        @RequestParam(defaultValue = DEFAULT_PAGE) final int page,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size
    ) {
        getLogger().debug("get: queryparam={}, page={}, size={}", queryparam, page, size);
        queryparam.remove("page");
        queryparam.remove("size");
        getLogger().trace("get: queryparam={}", queryparam);
        final var pageable = PageRequest.of(page, size);
        final var flugPage = service.find(queryparam, pageable);
        getLogger().debug("get: {}, {}", flugPage, flugPage.getContent());
        final var mappedPage = flugPage.map(FlugOhnePassagiere::of);
        return new PagedModel<>(mappedPage);
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(FlugController.class));
    }
}
