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


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/// ValueObject für die Daten eines Flugzeugs.
///
/// @param hersteller Der Hersteller.
/// @param modell Das Modell.
/// @param sitzplaetze Anzahl der Sitzplätze.
/// @param reichweiteKm Reichweite in Kilometern.
public record FlugzeugDTO(

    @NotNull
    @Pattern(regexp = HERSTELLER_PATTERN)
    String hersteller,

    @NotNull
    @Pattern(regexp = MODELL_PATTERN)
    String modell,

    @Positive
    int sitzplaetze,

    @Positive
    double reichweiteKm

) {

    /// Muster für gültige Hersteller.
    public static final String HERSTELLER_PATTERN = "[A-Za-zÄÖÜäöüß\\s\\-]{2,}";

    /// Muster für gültige Modellbezeichnungen.
    public static final String MODELL_PATTERN = "[A-Za-z0-9\\-\\s]{1,}";
}
