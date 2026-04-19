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
package com.acme.flug.service;

import java.io.Serial;

/// Exception, falls die Versionsnummer eines Fluges nicht aktuell ist.
/// Wird bei Optimistic Locking verwendet (keine Lost Updates).
public class VersionOutdatedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /// Die veraltete Version.
    private final int version;

    /// Konstruktor.
    ///
    /// @param version Die veraltete Version
    public VersionOutdatedException(final int version) {
        super("Die Versionsnummer " + version + " ist veraltet.");
        this.version = version;
    }

    /// Veraltete Version ermitteln.
    ///
    /// @return Die veraltete Version
    public int getVersion() {
        return version;
    }

    @Override
    public String getMessage() {
        return super.getMessage() == null ? "Die Versionsnummer ist veraltet." : super.getMessage();
    }
}
