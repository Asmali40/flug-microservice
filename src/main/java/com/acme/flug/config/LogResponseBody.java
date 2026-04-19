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

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/// [ResponseBodyAdvice] zur Protokollierung des Response-Bodys.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
@ControllerAdvice
@Profile("log-body")
@SuppressWarnings("NullableProblems")
public class LogResponseBody implements ResponseBodyAdvice<Object> {
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit _package private_ für _Spring_.
    LogResponseBody() {
    }

    @Override
    public boolean supports(
        final MethodParameter returnType,
        final Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(
        @Nullable final Object body,
        final MethodParameter returnType,
        final MediaType selectedContentType,
        @SuppressWarnings("MethodParameterNamingConvention")
        final Class<? extends HttpMessageConverter<?>> selectedConverterType,
        final ServerHttpRequest request,
        final ServerHttpResponse response
    ) {
        getLogger().trace("{}", body);
        return body;
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(LogResponseBody.class));
    }
}
