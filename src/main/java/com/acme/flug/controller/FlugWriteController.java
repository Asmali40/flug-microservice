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

import com.acme.flug.controller.FlugDTO.OnCreate;
import com.acme.flug.service.FlugWriteService;
import com.acme.flug.service.VersionOutdatedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.groups.Default;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import static com.acme.flug.controller.Constants.API_PATH;
import static com.acme.flug.controller.Constants.ID_PATTERN;
import static com.acme.flug.controller.Constants.VERSION_3;
import static com.acme.flug.controller.Constants.VERSION_3_EXAMPLE;
import static com.acme.flug.controller.Constants.X_VERSION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

/// REST-Controller für das Neuanlegen, Aktualisieren und Löschen von Flügen.
/// ![Klassendiagramm](../../../../../asciidoc/FlugWriteController.svg)
///
// Maven: ![Klassendiagramm](../../../../../../geerated-docks/FlugWriteController.svg)
@Controller
@RequestMapping(API_PATH)
@Validated
@SuppressWarnings({"ClassFanOutComplexity", "java:S1075", "MethodCount"})
class FlugWriteController {
    private static final String VERSIONSNUMMER_FEHLT = "Versionsnummer fehlt";

    private final FlugWriteService service;
    private final FlugMapper mapper;
    private final UriHelper uriHelper;
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit `package private` für _Constructor Injection_ bei _Spring_.
    ///
    /// @param service Injiziertes Service-Objekt.
    /// @param mapper Injiziertes Mapper-Objekt.
    /// @param uriHelper Injiziertes Helper-Objekt für URIs.
    FlugWriteController(final FlugWriteService service, final FlugMapper mapper, final UriHelper uriHelper) {
        this.service = service;
        this.mapper = mapper;
        this.uriHelper = uriHelper;
    }

    /// Einen neuen Flug anlegen.
    ///
    /// @param flugDTO Das Flugobjekt aus dem Request-Body.
    /// @param request Das Request-Objekt zum Erzeugen des Location-Headers.
    /// @return Response mit Statuscode 201 und Location-Header.
    /// @throws URISyntaxException falls die URI im Request-Objekt nicht korrekt wäre
    @PostMapping(consumes = APPLICATION_JSON_VALUE, version = VERSION_3)
    @Operation(summary = "Einen neuen Flug anlegen", tags = "Neuanlegen")
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_3_EXAMPLE)
    @ApiResponse(responseCode = "201", description = "Flug neu angelegt")
    @ApiResponse(responseCode = "400", description = "Syntaktisch ungültiges JSON")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte")
    @SuppressWarnings("TrailingComment")
    ResponseEntity<Void> post(
        @RequestBody @Validated({Default.class, OnCreate.class}) final FlugDTO flugDTO,
        final HttpServletRequest request
    ) throws URISyntaxException {
        getLogger().debug("post: flugDTO={}", flugDTO);
        final var flugInput = mapper.toFlug(flugDTO);
        final var flug = service.create(flugInput);
        final var baseUri = uriHelper.getBaseUri(request);
        final var location = URI.create(baseUri + "/" + flug.getId());
        return created(location).build();
    }

    /// Einen vorhandenen Flug überschreiben.
    ///
    /// @param id ID des zu aktualisierenden Flugs.
    /// @param flugDTO Neues Flugobjekt aus dem Request-Body.
    /// @param ifMatch Versionsnummer aus dem Header `If-Match`.
    /// @return Response mit Statuscode 204 und neuem ETag.
    @PutMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE, version = VERSION_3)
    @Operation(summary = "Einen Flug aktualisieren", tags = "Aktualisieren")
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_3_EXAMPLE)
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "404", description = "Flug nicht vorhanden")
    @ApiResponse(responseCode = "412", description = "Versionsnummer falsch")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte oder Email vorhanden")
    @ApiResponse(responseCode = "428", description = "Versionsnummer fehlt")
    ResponseEntity<Void> put(
        @PathVariable final UUID id,
        @RequestBody @Validated final FlugDTO flugDTO,
        @RequestHeader("If-Match") @Nullable final String ifMatch
    ) {
        getLogger().debug("put: id={}, flugDTO={}, ifMatch={}", id, flugDTO, ifMatch);
        final int version = getVersion(ifMatch);
        final var flugInput = mapper.toFlug(flugDTO);
        final var flug = service.update(flugInput, id, version);
        getLogger().debug("put: {}", flug);
        return noContent().eTag("\"" + flug.getVersion() + '"').build();
    }

    @SuppressWarnings({"MagicNumber", "RedundantSuppression"})
    private int getVersion(@Nullable final String versionStr) {
        getLogger().trace("getVersion: {}", versionStr);
        if (versionStr == null) {
            throw new VersionInvalidException(PRECONDITION_REQUIRED, VERSIONSNUMMER_FEHLT);
        }
        if (versionStr.length() < 3 ||
            versionStr.charAt(0) != '"' ||
            versionStr.charAt(versionStr.length() - 1) != '"') {
            throw new VersionInvalidException(PRECONDITION_FAILED, "Ungueltiges ETag " + versionStr);
        }

        final int version;
        try {
            version = Integer.parseInt(versionStr.substring(1, versionStr.length() - 1));
        } catch (final NumberFormatException ex) {
            throw new VersionInvalidException(PRECONDITION_FAILED, "Ungueltiges ETag " + versionStr, ex);
        }

        getLogger().trace("getVersion: version={}", version);
        return version;
    }

    /// Einen Flug anhand seiner ID löschen.
    ///
    /// @param id ID des zu löschenden Flugs.
    @DeleteMapping(path = "{id:" + ID_PATTERN + "}", version = VERSION_3)
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Einen Flug anhand der ID loeschen", tags = "Loeschen")
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_3_EXAMPLE)
    @ApiResponse(responseCode = "204", description = "Gelöscht")
    void deleteById(@PathVariable final UUID id) {
        getLogger().debug("deleteById: id={}", id);
        service.deleteById(id);
    }

    /// _Exception Handler_ für _Spring WebMvc_ falls Constraints bei _POST_- oder _PUT_-Requests verletzt sind.
    ///
    /// @param ex Exception vom Typ `MethodArgumentNotValidException`.
    /// @return ErrorResponse mit `ProblemDetail` gemäß _RFC 9457_.
    @ExceptionHandler
    ErrorResponse onConstraintViolations(final MethodArgumentNotValidException ex) {
        getLogger().debug("onConstraintViolations: {}", ex.getMessage());

        final var detailMessages = ex.getDetailMessageArguments();
        final var detail = detailMessages.length == 0 || detailMessages[1] == null
            ? "Constraint Violation"
            : ((String) detailMessages[1]).replace(", and ", ", ");
        return ErrorResponse.create(ex, UNPROCESSABLE_CONTENT, detail);
    }

    /// _ExceptionHandler_, falls bei einem _PUT_-Request die Version fehlerhaft ist.
    ///
    /// @param ex Exception vom Typ `VersionInvalidException`.
    /// @return ErrorResponse mit `ProblemDetail` gemäß _RFC 9457_.
    @ExceptionHandler
    ErrorResponse onVersionInvalid(final VersionInvalidException ex) {
        getLogger().debug("onVersionInvalid: {}", ex.getMessage());
        return ErrorResponse.create(ex, ex.getStatus(), ex.getMessage());
    }

    /// _ExceptionHandler_, falls bei einem _PUT_-Request die Version nicht aktuell existiert.
    ///
    /// @param ex Exception vom Typ `VersionOutdatedException`.
    /// @return ErrorResponse mit `ProblemDetail` gemäß _RFC 9457_.
    @ExceptionHandler
    ErrorResponse onVersionOutdated(final VersionOutdatedException ex) {
        getLogger().debug("onVersionOutdated: {}", ex.getMessage());
        return ErrorResponse.create(ex, PRECONDITION_FAILED, ex.getMessage());
    }

    /// _ExceptionHandler_, falls bei einem _POST_- oder _PUT_-Request der Request-Body syntaktisch falsch ist.
    ///
    /// @param ex Exception vom Typ `HttpMessageNotReadableException`.
    /// @return ErrorResponse mit `ProblemDetail` gemäß _RFC 9457_.
    @ExceptionHandler
    ErrorResponse onMessageNotReadable(final HttpMessageNotReadableException ex) {
        final var msg = ex.getMessage() == null ? "N/A" : ex.getMessage();
        getLogger().debug("onMessageNotReadable: {}", msg);
        return ErrorResponse.create(ex, BAD_REQUEST, msg);
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(FlugWriteController.class));
    }
}
