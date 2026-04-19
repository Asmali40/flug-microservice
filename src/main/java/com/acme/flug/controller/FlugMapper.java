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

import com.acme.flug.entity.Flug;
import com.acme.flug.entity.Flugzeug;
import com.acme.flug.entity.Passagier;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import static org.mapstruct.NullValueMappingStrategy.RETURN_DEFAULT;

/// Mapper zwischen DTOs und Entities.
/// Siehe: build/generated/sources/annotationProcessor/java/main/.../FlugMapperImpl.java.
///
/// Wandelt FlugDTO → Flug, FlugzeugDTO → Flugzeug, PassagierDTO → Passagier.
@Mapper(nullValueIterableMappingStrategy = RETURN_DEFAULT, componentModel = "spring")
@AnnotateWith(ExcludeFromJacocoGeneratedReport.class)
interface FlugMapper {

    /// Ein DTO-Objekt von [FlugDTO] in ein Objekt für [Flug] konvertieren.
    ///
    /// @param dto DTO-Objekt für `FlugDTO` ohne ID
    /// @return Konvertiertes `Flug`-Objekt mit null als ID
    @Mapping(target = "id", ignore = true)
    Flug toFlug(FlugDTO dto);

    /// Ein DTO-Objekt von [FlugzeugDTO] in ein Objekt für [Flugzeug] konvertieren.
    ///
    /// @param dto DTO-Objekt für `FlugzeugDTO`
    /// @return Konvertiertes `Flugzeug`-Objekt
    Flugzeug toFlugzeug(FlugzeugDTO dto);

    /// Ein DTO-Objekt von [PassagierDTO] in ein Objekt für [Passagier] konvertieren.
    ///
    /// @param dto DTO-Objekt für `PassagierDTO`
    /// @return Konvertiertes `Passagier`-Objekt mit null als ID
    @Mapping(target = "id", ignore = true)
    Passagier toPassagier(PassagierDTO dto);
}
