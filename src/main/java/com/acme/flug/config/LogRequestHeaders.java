/*
 * Copyright (C) 2025 - present Efe Yüksel, Hochschule Karlsruhe
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
package com.acme.flug.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/// WebFilter zur Protokollierung des Request-Headers.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
sealed interface LogRequestHeaders permits DevConfig {
    /// WebFilter zur Protokollierung des Request-Headers.
    ///
    /// @return `CommonsRequestLoggingFilter`, der den Request-Header protokolliert.
    @Bean
    default CommonsRequestLoggingFilter logRequestFilter() {
        // https://www.baeldung.com/spring-http-logging
        // https://stackoverflow.com/questions/33744875...
        // .../spring-boot-how-to-log-all-requests-and-responses-with-exceptions-in-single-pl
        // https://github.com/zalando/logbook
        final var filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(true);
        return filter;
    }
}
