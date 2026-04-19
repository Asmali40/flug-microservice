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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.flug.controller;

import com.acme.flug.service.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

/// Handler für allgemeine Exceptions.
@RestControllerAdvice
class GlobalExceptionHandler {

    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit package private für _Spring_.
    GlobalExceptionHandler() {
    }

    /// _ExceptionHandler_, falls bei einem _GET_- oder _PUT_-Request ein Flug nicht gefunden wird.
    ///
    /// @param ex Exception vom Typ NotFoundException.
    @ExceptionHandler
    ResponseEntity<ProblemDetail> onNotFound(final NotFoundException ex) {
        getLogger().debug("onNotFound: {}", ex.getMessage());

        final var problem = ProblemDetail.forStatusAndDetail(
            NOT_FOUND,
            ex.getMessage()
        );

        return ResponseEntity
            .status(NOT_FOUND)
            .contentType(APPLICATION_PROBLEM_JSON)
            .body(problem);
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(GlobalExceptionHandler.class));
    }
}

