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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import static com.acme.flug.config.DevConfig.DEV;
import static com.acme.flug.controller.Constants.API_PATH;
import static com.acme.flug.controller.TestConstants.API_VERSION_INSERTER;
import static com.acme.flug.controller.TestConstants.HOST;
import static com.acme.flug.controller.TestConstants.REQUEST_FACTORY;
import static com.acme.flug.controller.TestConstants.SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowableOfType;
import static org.junit.jupiter.api.condition.JRE.JAVA_25;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;

@Tag("integration")
@Tag("rest")
@Tag("rest-get")
@DisplayName("REST-Schnittstelle fuer GET-Requests (Flug)")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_25, max = JAVA_25)
@SuppressWarnings({
    "WriteTag",
    "ClassFanOutComplexity",
    "MissingJavadoc",
    "MissingJavadocType",
    "JavadocVariable",
    "PMD.AtLeastOneConstructor",
    "PMD.LinguisticNaming"
})
class FlugControllerTest {

    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000000";

    private static final String ID_NICHT_VORHANDEN = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    private final FlugRepository flugRepo;

    @InjectSoftAssertions
    @SuppressWarnings("NullAway.Init")
    private SoftAssertions softly;

    @SuppressFBWarnings("CT")
    FlugControllerTest(@Value("${local.server.port}") final int port, final ApplicationContext ctx) {
        final var controller = ctx.getBean(FlugController.class);
        assertThat(controller).isNotNull();

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(SCHEMA)
            .host(HOST)
            .port(port)
            .path(API_PATH)
            .build();
        final var baseUrl = uriComponents.toUriString();

        final var restClient = RestClient.builder()
            .requestFactory(REQUEST_FACTORY)
            .apiVersionInserter(API_VERSION_INSERTER)
            .baseUrl(baseUrl)
            .build();

        final var clientAdapter = RestClientAdapter.create(restClient);
        final var proxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build();
        flugRepo = proxyFactory.createClient(FlugRepository.class);
    }

    @Test
    @DisplayName("Immer erfolgreich")
    void immerErfolgreich() {
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Suche nach allen Fluegen")
    void getAll() {
        // given
        final MultiValueMap<@NonNull String, String> suchparameter =
            MultiValueMap.fromSingleValue(Map.of());

        // when
        final var fluege = flugRepo.get(suchparameter, TestConstants.ADMIN_AUTH);

        // then
        softly.assertThat(fluege.content())
            .isNotNull()
            .isNotEmpty();
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer die Suche anhand der ID")
    class GetById {

        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID: id={0}")
        @ValueSource(strings = ID_VORHANDEN)
        @DisplayName("Suche mit vorhandener ID")
        void getById(final String id) {
            // when
            final var response = flugRepo.getByIdOhneVersion(id, TestConstants.ADMIN_AUTH);

            // then
            final var flug = response.getBody();
            assertThat(flug).isNotNull();
            softly.assertThat(flug.id()).isEqualTo(UUID.fromString(id));
            // weitere Assertions, wenn du Felder kennst:
            // softly.assertThat(flug.<feld>()).isNotNull();
        }

        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID und vorhandener Version: id={0}, version={1}")
        @CsvSource(ID_VORHANDEN + ", 0")
        @DisplayName("Suche mit vorhandener ID und vorhandener Version")
        void getByIdVersionVorhanden(final String id, final String version) {
            // when
            final var response = flugRepo.getById(id, "\"" + version + '"', TestConstants.ADMIN_AUTH);

            // then
            assertThat(response.getStatusCode()).isEqualTo(NOT_MODIFIED);
        }

        @ParameterizedTest(name = "[{index}] Suche mit nicht-vorhandener ID: {0}")
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Suche mit nicht-vorhandener ID")
        void getByIdNichtVorhanden(final String id) {
            // when
            final var exc = catchThrowableOfType(
                HttpClientErrorException.NotFound.class,
                () -> flugRepo.getByIdOhneVersion(id, TestConstants.ADMIN_AUTH)
            );

            // then
            assertThat(exc.getStatusCode()).isEqualTo(NOT_FOUND);
        }
    }
}
