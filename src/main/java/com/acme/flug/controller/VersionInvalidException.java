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

import java.io.Serial;
import org.springframework.http.HttpStatusCode;

/// Exception, falls die Versionsnummer im Request-Header bei `If-Match`
/// fehlt oder syntaktisch ungültig ist.
///
/// Wird im FlugWriteController verwendet, um "Lost Updates" zu verhindern.
class VersionInvalidException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7029483458732458901L;

    private final HttpStatusCode status;

    /// Konstruktor für die Verwendung im FlugWriteController.
    ///
    /// @param status HTTP-Statuscode.
    /// @param message Die eigentliche Fehlermeldung.
    VersionInvalidException(final HttpStatusCode status, final String message) {
        super(message);
        this.status = status;
    }

    /// Konstruktor für die Verwendung im FlugWriteController.
    ///
    /// @param status HTTP-Statuscode.
    /// @param message Die eigentliche Fehlermeldung.
    /// @param ex Verursachende Exception.
    VersionInvalidException(
        final HttpStatusCode status,
        final String message,
        final Exception ex
    ) {
        super(message, ex);
        this.status = status;
    }

    @Override
    public String getMessage() {
        return super.getMessage() == null ? "" : super.getMessage();
    }

    HttpStatusCode getStatus() {
        return status;
    }
}
