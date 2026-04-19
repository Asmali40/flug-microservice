/*
 * Copyright (C) 2025 - present Juergen Zimmermann, Hochschule Karlsruhe
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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/// ValueObject für die Daten eines Passagiers.
///
/// @param vorname Der Vorname.
/// @param nachname Der Nachname.
/// @param geburtsdatum Das Geburtsdatum.
/// @param sitzplatz Sitzplatzbezeichnung im Flugzeug.
public record PassagierDTO(

    @NotNull
    @Pattern(regexp = NAME_PATTERN)
    String vorname,

    @NotNull
    @Pattern(regexp = NAME_PATTERN)
    String nachname,

    @Past
    LocalDate geburtsdatum,

    @NotNull
    @Pattern(regexp = SITZPLATZ_PATTERN)
    String sitzplatz

) {

    /// Muster für Namen.
    public static final String NAME_PATTERN =
        "[A-ZÄÖÜ][a-zA-Zäöüß\\-]{1,}";

    /// Muster für Sitzplatzangaben (z. B. 12A)
    public static final String SITZPLATZ_PATTERN =
        "[0-9]{1,2}[A-F]";
}
