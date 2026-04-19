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

import com.acme.flug.entity.Flug;
import com.acme.flug.entity.FlugTyp;
import com.acme.flug.entity.Flugzeug;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/// ValueObject für einen gefundenen Flug ohne nicht-serialisierbare Beziehungen,
///
/// @param id ID des Flugs
/// @param version Version
/// @param typ Flugzeugtyp
/// @param startOrt Startort
/// @param zielOrt Zielort
/// @param abflugZeit Abflugzeit
/// @param ankunftZeit Ankunftszeit
/// @param username Username
/// @param flugzeug Flugzeug
/// @param passagiere Passagiere
@SuppressWarnings("RecordComponentNumber")
record FlugOhnePassagiere(
    UUID id,
    int version,
    FlugTyp typ,
    String startOrt,
    String zielOrt,
    LocalDateTime abflugZeit,
    LocalDateTime ankunftZeit,
    String username,
    FlugzeugDTO flugzeug,
    List<PassagierDTO> passagiere
) {

    /// DTO-Objekt ohne Passagiere aus einem Flug-Objekt erstellen.
    ///
    /// @param flug Flug-Objekt als Ausgangspunkt
    /// @return DTO-Objekt ohne Passagiere
    static FlugOhnePassagiere of(final Flug flug) {
        return of(flug, false);
    }

    private static FlugOhnePassagiere of(final Flug flug, final boolean includePassagiere) {
        final var passagiere = includePassagiere && flug.getPassagiere() != null
            ? flug.getPassagiere().stream().map(FlugOhnePassagiere::toPassagierDTO).toList()
            : List.<PassagierDTO>of();
        return new FlugOhnePassagiere(
            flug.getId(),
            flug.getVersion(),
            flug.getTyp(),
            flug.getStartOrt(),
            flug.getZielOrt(),
            flug.getAbflugZeit(),
            flug.getAnkunftZeit(),
            flug.getUsername(),
            toFlugzeugDTO(flug.getFlugzeug()),
            passagiere
        );
    }

    /// DTO-Objekt mit Passagieren aus einem Flug-Objekt erstellen.
    ///
    /// @param flug Flug-Objekt als Ausgangspunkt
    /// @return DTO-Objekt mit Passagieren
    static FlugOhnePassagiere ofMitPassagieren(final Flug flug) {
        return of(flug, true);
    }

    private static FlugzeugDTO toFlugzeugDTO(final Flugzeug flugzeug) {
        return new FlugzeugDTO(
            flugzeug.getHersteller(),
            flugzeug.getModell(),
            flugzeug.getSitzplaetze(),
            flugzeug.getReichweiteKm()
        );
    }

    private static PassagierDTO toPassagierDTO(final com.acme.flug.entity.Passagier passagier) {
        return new PassagierDTO(
            passagier.getVorname(),
            passagier.getNachname(),
            passagier.getGeburtsdatum(),
            passagier.getSitzplatz()
        );
    }
}
