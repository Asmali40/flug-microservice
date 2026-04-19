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

import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.CLASS;

// https://gist.github.com/dariahervieux/49a644fb4a12c94558f87219169ed9f7

/// Annotation to put on Mapstruct mappers for generated classes to keep the annotation.
/// - `https://github.com/mapstruct/mapstruct/issues/1528`
/// - `https://github.com/mapstruct/mapstruct/issues/1574`
@Retention(CLASS)
public @interface ExcludeFromJacocoGeneratedReport {
}
