/*
 * Copyright (C) 2024 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.flug.security;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/// Scope gemäß [OAuth 2.0](https://www.rfc-editor.org/rfc/rfc6749.html) (hier nur: `email profile`).
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
public enum ScopeType {
    /// Scope "email profile" für OAuth 2.0.
    EMAIL_PROFILE("email profile");

    private final String value;

    ScopeType(final String value) {
        this.value = value;
    }

    /// Einen enum-Wert als String mit dem internen Wert ausgeben.
    /// Dieser Wert wird durch Jackson in einem JSON-Datensatz verwendet.
    /// [Wiki-Seiten](https://github.com/FasterXML/jackson-databind/wiki)
    ///
    /// @return Interner Wert
    @JsonValue
    public String getValue() {
        return value;
    }

    /// Konvertierung eines Strings in einen Enum-Wert.
    ///
    /// @param value Der String, zu dem ein passender Enum-Wert ermittelt werden soll.
    /// @return Passender Enum-Wert oder null.
    @JsonCreator
    @Nullable
    public static ScopeType of(final String value) {
        return Stream.of(values())
            .filter(token -> token.value.equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}
