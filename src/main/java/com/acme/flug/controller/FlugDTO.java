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

import com.acme.flug.entity.FlugTyp;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.validator.constraints.UniqueElements;
import org.jspecify.annotations.Nullable;

/// ValueObject für das Neuanlegen und Ändern eines Fluges.
/// Beim Lesen wird FlugModel verwendet.
///
/// @param startOrt Der Startort.
/// @param zielOrt Der Zielort.
/// @param abflugZeit Zeitpunkt des Abflugs.
/// @param ankunftZeit Zeitpunkt der Ankunft.
/// @param flugzeug Das zugehörige Flugzeug.
/// @param passagiere Liste der Passagiere.
@SuppressWarnings("RecordComponentNumber")
public record FlugDTO(

    @NotNull
    FlugTyp typ,

    @NotNull
    @Pattern(regexp = ORT_PATTERN)
    String startOrt,

    @NotNull
    @Pattern(regexp = ORT_PATTERN)
    String zielOrt,

    @NotNull(groups = OnCreate.class)
    @Future
    LocalDateTime abflugZeit,

    @NotNull(groups = OnCreate.class)
    @Future
    LocalDateTime ankunftZeit,

    @Valid
    @NotNull(groups = OnCreate.class)
    FlugzeugDTO flugzeug,

    @Nullable
    @UniqueElements
    List<@Valid PassagierDTO> passagiere

) {

    /// Muster für gültige Ortsnamen.
    public static final String ORT_PATTERN = "[A-Za-zÄÖÜäöüß\\-\\s]{2,}";

    /// Marker-Interface für zusätzliche Validierung beim Neuanlegen.
    public interface OnCreate { }
}
