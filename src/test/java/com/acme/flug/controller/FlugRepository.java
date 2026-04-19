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

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import static com.acme.flug.controller.TestConstants.VERSION_3;

@HttpExchange(accept = "application/json", contentType = "application/json")
@SuppressWarnings({"WriteTag", "PMD.AvoidDuplicateLiterals"})
interface FlugRepository {

    @GetExchange(url = "/{id}", version = VERSION_3)
    ResponseEntity<FlugOhnePassagiere> getById(
        @PathVariable("id") String id,
        @RequestHeader(HttpHeaders.IF_NONE_MATCH) String version,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );

    @GetExchange(url = "/{id}", version = VERSION_3)
    ResponseEntity<FlugOhnePassagiere> getByIdOhneVersion(
        @PathVariable("id") String id,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );

    @GetExchange(version = VERSION_3)
    FlugOhnePassagierListPage get(
        @RequestParam MultiValueMap<String, String> queryparam,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );

    @PostExchange(version = VERSION_3)
    ResponseEntity<Void> post(
        @RequestBody FlugDTO flugDTO,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );

    @PutExchange(url = "/{id}", version = VERSION_3)
    ResponseEntity<Void> put(
        @PathVariable("id") String id,
        @RequestBody FlugDTO flugDTO,
        @RequestHeader(HttpHeaders.IF_MATCH) String version,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );

    @PutExchange(url = "/{id}", version = VERSION_3)
    ResponseEntity<Void> putOhneVersion(
        @PathVariable("id") String id,
        @RequestBody FlugDTO flugDTO,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );

    @DeleteExchange(url = "/{id}", version = VERSION_3)
    ResponseEntity<Void> deleteById(
        @PathVariable("id") String id,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );
}
