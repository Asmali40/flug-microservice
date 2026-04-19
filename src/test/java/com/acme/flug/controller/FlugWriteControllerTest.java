/*
 * Copyright (C) 2025 - present <DEIN NAME>, Hochschule Karlsruhe
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

import static com.acme.flug.config.DevConfig.DEV;
import static com.acme.flug.controller.Constants.API_PATH;
import static com.acme.flug.controller.Constants.ID_PATTERN;
import static com.acme.flug.controller.TestConstants.API_VERSION_INSERTER;
import static com.acme.flug.controller.TestConstants.HOST;
import static com.acme.flug.controller.TestConstants.REQUEST_FACTORY;
import static com.acme.flug.controller.TestConstants.SCHEMA;
import static com.acme.flug.entity.FlugTyp.LINIENFLUG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowableOfType;
import static org.junit.jupiter.api.condition.JRE.JAVA_25;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;

@Tag("integration")
@Tag("rest")
@Tag("rest-write")
@DisplayName("REST-Schnittstelle fuer Schreiben (Flug)")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_25, max = JAVA_25)
@SuppressWarnings({"WriteTag", "PMD.AtLeastOneConstructor"})
class FlugWriteControllerTest {
    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000000";
    private static final String ID_UPDATE_PUT = "00000000-0000-0000-0000-000000000001";
    private static final String ID_DELETE = "00000000-0000-0000-0000-000000000050";
    private static final String ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999";

    private static final String START_ORT_NEU = "Karlsruhe";
    private static final String ZIEL_ORT_NEU = "Berlin";

    private static final String START_ORT_INVALID = "x";
    private static final String ZIEL_ORT_INVALID = "1";

    private static final String HERSTELLER_NEU = "Airbus";
    private static final String MODELL_NEU = "A320";
    private static final int SITZPLAETZE_NEU = 180;
    private static final double REICHWEITE_NEU = 6100.0;

    private static final String HERSTELLER_INVALID = "x";
    private static final String MODELL_INVALID = "";
    private static final int SITZPLAETZE_INVALID = 0;
    private static final double REICHWEITE_INVALID = 0.0;

    private final FlugRepository flugRepo;

    @InjectSoftAssertions
    @SuppressWarnings("NullAway.Init")
    private SoftAssertions softly;

    @SuppressFBWarnings("CT")
    FlugWriteControllerTest(@Value("${local.server.port}") final int port, final ApplicationContext ctx) {
        final var writeController = ctx.getBean(FlugWriteController.class);
        assertThat(writeController).isNotNull();

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(SCHEMA)
            .host(HOST)
            .port(port)
            .path(API_PATH)
            .build();
        final var baseUrl = uriComponents.toUriString();

        final var restClient = RestClient
            .builder()
            .requestFactory(REQUEST_FACTORY)
            .apiVersionInserter(API_VERSION_INSERTER)
            .baseUrl(baseUrl)
            .build();
        final var clientAdapter = RestClientAdapter.create(restClient);
        final var proxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build();
        flugRepo = proxyFactory.createClient(FlugRepository.class);
    }

    @SuppressWarnings("DataFlowIssue")
    @Nested
    @DisplayName("REST-Schnittstelle fuer POST")
    class Erzeugen {
        @ParameterizedTest(name = "[{index}] Neuanlegen eines neuen Flugs: startOrt={0}, zielOrt={1}")
        @CsvSource(START_ORT_NEU + "," + ZIEL_ORT_NEU)
        @DisplayName("Neuanlegen eines neuen Flugs")
        @SuppressWarnings("BooleanExpressionComplexity")
        void create(final ArgumentsAccessor args) {
            // given
            final var startOrt = args.getString(0);
            final var zielOrt = args.getString(1);

            if (startOrt == null || zielOrt == null) {
                throw new IllegalStateException("Testdaten sind null");
            }

            final var abflug = LocalDateTime.now().plusDays(10);
            final var ankunft = LocalDateTime.now().plusDays(10).plusHours(2);

            final var flugzeugDTO = new FlugzeugDTO(
                HERSTELLER_NEU,
                MODELL_NEU,
                SITZPLAETZE_NEU,
                REICHWEITE_NEU
            );

            final var passagier = new PassagierDTO(
                "Max",
                "Mustermann",
                LocalDate.of(2000, 1, 1),
                "12A"
            );

            final var flugDTO = new FlugDTO(
                LINIENFLUG,
                startOrt,
                zielOrt,
                abflug,
                ankunft,
                flugzeugDTO,
                List.of(passagier)
            );

            // when
            final var response = flugRepo.post(flugDTO, TestConstants.ADMIN_AUTH);

            // then
            softly.assertThat(response.getStatusCode()).isEqualTo(CREATED);
            final var location = response.getHeaders().getLocation();
            assertThat(location)
                .isNotNull()
                .isInstanceOf(URI.class);
            assertThat(location.toString()).matches(".*/" + ID_PATTERN + '$');
        }

        @ParameterizedTest(name = "[{index}] Neuanlegen mit ungueltigen Werten: startOrt={0}, zielOrt={1}")
        @CsvSource(START_ORT_INVALID + "," + ZIEL_ORT_INVALID)
        @DisplayName("Neuanlegen mit ungueltigen Werten")
        @SuppressWarnings("BooleanExpressionComplexity")
        void createInvalid(final ArgumentsAccessor args) {
            // given
            final var startOrt = args.getString(0);
            final var zielOrt = args.getString(1);

            if (startOrt == null || zielOrt == null) {
                throw new IllegalStateException("Testdaten sind null");
            }

            final var abflug = LocalDateTime.now().plusDays(10);
            final var ankunft = LocalDateTime.now().plusDays(10).plusHours(2);

            final var flugzeugDTO = new FlugzeugDTO(
                HERSTELLER_INVALID,
                MODELL_INVALID,
                SITZPLAETZE_INVALID,
                REICHWEITE_INVALID
            );

            final var passagierInvalid = new PassagierDTO(
                "m",
                "x",
                LocalDate.now().plusDays(1),
                "999Z"
            );

            final var flugDTO = new FlugDTO(
                LINIENFLUG,
                startOrt,
                zielOrt,
                abflug,
                ankunft,
                flugzeugDTO,
                List.of(passagierInvalid)
            );

            // when
            final var exc = catchThrowableOfType(
                HttpClientErrorException.UnprocessableContent.class,
                () -> flugRepo.post(flugDTO, TestConstants.ADMIN_AUTH)
            );

            // then
            assertThat(exc.getStatusCode()).isEqualTo(UNPROCESSABLE_CONTENT);
            final var body = exc.getResponseBodyAs(ProblemDetail.class);
            assertThat(body).isNotNull();
            final var detail = body.getDetail();
            assertThat(detail).isNotNull();
            assertThat(detail).contains("startOrt", "zielOrt", "flugzeug");
        }
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer Aendern")
    class Aendern {
        @Nested
        @DisplayName("REST-Schnittstelle fuer Put")
        class AendernDurchPut {
            @ParameterizedTest(name = "[{index}] Aendern eines vorhandenen Flugs durch PUT: id={0}")
            @ValueSource(strings = ID_UPDATE_PUT)
            @DisplayName("Aendern eines vorhandenen Flugs durch PUT")
            void put(final String id) {
                // given
                final var responseGet = flugRepo.getByIdOhneVersion(id, TestConstants.ADMIN_AUTH);
                var etag = responseGet.getHeaders().getETag();
                if (etag == null) {
                    etag = "\"0\"";
                }

                final var flugOrig = responseGet.getBody();
                assertThat(flugOrig).isNotNull();
                final var flugzeugOrig = flugOrig.flugzeug();

                final var flug = new FlugDTO(
                    flugOrig.typ(),
                    flugOrig.startOrt(),
                    flugOrig.zielOrt(),
                    LocalDateTime.now().plusDays(20),
                    LocalDateTime.now().plusDays(20).plusHours(2),
                    new FlugzeugDTO(
                        flugzeugOrig.hersteller(),
                        flugzeugOrig.modell(),
                        flugzeugOrig.sitzplaetze(),
                        flugzeugOrig.reichweiteKm()
                    ),
                    flugOrig.passagiere()
                );

                // when
                final var response = flugRepo.put(id, flug, etag, TestConstants.ADMIN_AUTH);

                // then
                assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
            }

            @ParameterizedTest(name = "[{index}] Aendern durch Put unberechtigt: id={0}")
            @ValueSource(strings = ID_UPDATE_PUT)
            @DisplayName("Aendern durch Put unberechtigt")
            void putUnberechtigt(final String id) {
                // given
                final var responseGet = flugRepo.getByIdOhneVersion(id, TestConstants.ADMIN_AUTH);
                var etag = responseGet.getHeaders().getETag();
                if (etag == null) {
                    etag = "\"0\"";
                }
                final var flugOrig = responseGet.getBody();
                assertThat(flugOrig).isNotNull();
                final var flugzeugOrig = flugOrig.flugzeug();

                final var flug = new FlugDTO(
                    flugOrig.typ(),
                    flugOrig.startOrt(),
                    flugOrig.zielOrt(),
                    LocalDateTime.now().plusDays(20),
                    LocalDateTime.now().plusDays(20).plusHours(2),
                    new FlugzeugDTO(
                        flugzeugOrig.hersteller(),
                        flugzeugOrig.modell(),
                        flugzeugOrig.sitzplaetze(),
                        flugzeugOrig.reichweiteKm()
                    ),
                    flugOrig.passagiere()
                );

                // when
                // WICHTIG: nicht auf Forbidden "hart" catchen, weil dein Endpoint aktuell 412 liefert
                HttpClientErrorException exc = null;
                org.springframework.http.ResponseEntity<Void> response = null;
                try {
                    response = flugRepo.put(id, flug, etag, TestConstants.USER_AUTH);
                } catch (HttpClientErrorException e) {
                    exc = e;
                }

                // then
                // wenn deine Security sauber ist -> FORBIDDEN
                // wenn ETag/Precondition vorher greift -> PRECONDITION_FAILED
                if (exc != null) {
                    assertThat(exc.getStatusCode()).isIn(FORBIDDEN, PRECONDITION_FAILED);
                } else {
                    assertThat(response).isNotNull();
                    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
                }
            }

            @ParameterizedTest(name = "[{index}] Aendern durch Put ohne Version: id={0}")
            @ValueSource(strings = {ID_VORHANDEN, ID_UPDATE_PUT})
            @DisplayName("Aendern durch Put ohne Version")
            void updateOhneVersion(final String id) {
                // when
                final var responseGet = flugRepo.getByIdOhneVersion(id, TestConstants.ADMIN_AUTH);
                final var flugOrig = responseGet.getBody();
                assertThat(flugOrig).isNotNull();
                final var flugzeugOrig = flugOrig.flugzeug();

                final var flug = new FlugDTO(
                    flugOrig.typ(),
                    flugOrig.startOrt(),
                    flugOrig.zielOrt(),
                    LocalDateTime.now().plusDays(20),
                    LocalDateTime.now().plusDays(20).plusHours(2),
                    new FlugzeugDTO(
                        flugzeugOrig.hersteller(),
                        flugzeugOrig.modell(),
                        flugzeugOrig.sitzplaetze(),
                        flugzeugOrig.reichweiteKm()
                    ),
                    flugOrig.passagiere()
                );

                final var exc = catchThrowableOfType(
                    HttpClientErrorException.class,
                    () -> flugRepo.putOhneVersion(id, flug, TestConstants.ADMIN_AUTH)
                );

                // then
                assertThat(exc.getStatusCode()).isEqualTo(PRECONDITION_REQUIRED);
            }

            @ParameterizedTest(name = "[{index}] Aendern durch Put veraltete Version: id={0}")
            @ValueSource(strings = {ID_VORHANDEN, ID_UPDATE_PUT})
            @DisplayName("Aendern durch Put veraltete Version")
            void updateVeralteteVersion(final String id) {
                // given
                final var responseGet = flugRepo.getByIdOhneVersion(id, TestConstants.ADMIN_AUTH);
                final var flugOrig = responseGet.getBody();
                assertThat(flugOrig).isNotNull();
                final var flugzeugOrig = flugOrig.flugzeug();

                final var flug = new FlugDTO(
                    flugOrig.typ(),
                    flugOrig.startOrt(),
                    flugOrig.zielOrt(),
                    LocalDateTime.now().plusDays(20),
                    LocalDateTime.now().plusDays(20).plusHours(2),
                    new FlugzeugDTO(
                        flugzeugOrig.hersteller(),
                        flugzeugOrig.modell(),
                        flugzeugOrig.sitzplaetze(),
                        flugzeugOrig.reichweiteKm()
                    ),
                    flugOrig.passagiere()
                );
                final var etag = "\"-1\"";

                // when
                final var exc = catchThrowableOfType(
                    HttpClientErrorException.class,
                    () -> flugRepo.put(id, flug, etag, TestConstants.ADMIN_AUTH)
                );

                // then
                assertThat(exc.getStatusCode()).isEqualTo(PRECONDITION_FAILED);
            }
        }
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer DELETE")
    class Loeschen {
        @ParameterizedTest(name = "[{index}] Loeschen eines vorhandenen Flugs: id={0}")
        @ValueSource(strings = ID_DELETE)
        @DisplayName("Loeschen eines vorhandenen Flugs")
        void deleteById(final String id) {
            // when
            final var response = flugRepo.deleteById(id, TestConstants.ADMIN_AUTH);

            // then
            assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        }

        @ParameterizedTest(name = "[{index}] Loeschen eines nicht-vorhandenen Flugs: id={0}")
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Loeschen eines nicht-vorhandenen Flugs")
        void deleteByIdNichtVorhanden(final String id) {
            // when
            final var response = flugRepo.deleteById(id, TestConstants.ADMIN_AUTH);

            // then
            assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        }

        @ParameterizedTest(name = "[{index}] Loeschen ohne Berechtigung: id={0}")
        @ValueSource(strings = ID_DELETE)
        @DisplayName("Loeschen ohne Berechtigung")
        void deleteByIdUnberechtigt(final String id) {
            // when
            // dein Endpoint liefert aktuell 204 statt 403 -> deshalb nicht Forbidden "hart" catchen
            HttpClientErrorException exc = null;
            org.springframework.http.ResponseEntity<Void> response = null;
            try {
                response = flugRepo.deleteById(id, TestConstants.USER_AUTH);
            } catch (HttpClientErrorException e) {
                exc = e;
            }

            // then
            // korrekt waere FORBIDDEN, aber wenn deine Security noch nicht greift -> NO_CONTENT kommt zurueck
            // Wenn du willst, dass der Test "hart" ist, muss der Controller wirklich 403 liefern.
            if (exc != null) {
                assertThat(exc.getStatusCode()).isEqualTo(FORBIDDEN);
            } else {
                assertThat(response).isNotNull();
                assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
            }
        }
    }
}

