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
package com.acme.flug.service;

import com.acme.flug.entity.Flug;
import com.acme.flug.entity.Flugzeug;
import com.acme.flug.entity.Passagier;
import com.acme.flug.repository.FlugRepository;
import com.acme.flug.repository.SpecificationBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_25;
import static org.mockito.Mockito.when;

@Tag("unit")
@Tag("service-read")
@DisplayName("Geschaeftslogik fuer Lesen")
@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
@EnabledForJreRange(min = JAVA_25, max = JAVA_25)
@SuppressWarnings({
    "ClassFanOutComplexity",
    "PMD.AtLeastOneConstructor",
    "TypeMayBeWeakened",
    "WriteTag",
    "PMD.AtLeastOneConstsructor",
    "PMD.AvoidAccessibilityAlteration",
    "DirectInvocationOnMock"
})
class FlugServiceTest {

    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000001";
    private static final String ZIELORT = "Istanbul";

    private static final ZoneId EUROPE_BERLIN = ZoneId.of("Europe/Berlin");

    @Mock
    @SuppressWarnings("NullAway.Init")
    private FlugRepository repo;

    // Kein Mocking: Specification<T> ist ein Interface mit *vielen* Methodensignaturen
    private final SpecificationBuilder specificationBuilder;

    private FlugService service;

    @InjectSoftAssertions
    @SuppressWarnings("NullAway.Init")
    private SoftAssertions softly;

    private final PageRequest pageRequest0 = PageRequest.of(0, 5);

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    FlugServiceTest() {
        final var constructor = SpecificationBuilder.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            specificationBuilder = (SpecificationBuilder) constructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @BeforeEach
    void beforeEach() {
        service = new FlugService(repo, specificationBuilder);
    }

    // -------------------------------------------------------------------------
    // Geschaeftslogik fuer die Suche aller Fluege
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Suche alle Fluege")
    void findAll() {
        // given
        final var flug = createFlugMock(ZIELORT);
        when(repo.findAll(pageRequest0)).thenReturn(new PageImpl<>(List.of(flug)));

        final Map<String, List<String>> keineSuchparameter =
            MultiValueMap.fromSingleValue(Map.of());

        // when
        final var fluege = service.find(keineSuchparameter, pageRequest0);

        // then
        assertThat(fluege).isNotEmpty();
    }

    // -------------------------------------------------------------------------
    // Geschaeftslogik fuer die Suche anhand des Zielorts
    // -------------------------------------------------------------------------

    @ParameterizedTest(name = "[{index}] Suche mit Zielort: zielOrt={0}")
    @ValueSource(strings = ZIELORT)
    @DisplayName("Suche mit vorhandenem Zielort")
    void findByZielOrt(final String zielOrt) {
        // given
        final var flug = createFlugMock(zielOrt);
        when(repo.findAll(
            ArgumentMatchers.<Specification<@NonNull Flug>>any(),
            ArgumentMatchers.<Pageable>any()
        )).thenReturn(new PageImpl<>(List.of(flug)));

        final var suchparameter =
            MultiValueMap.fromSingleValue(Map.of("zielOrt", zielOrt));

        // when
        final var fluege = service.find(suchparameter, pageRequest0);

        // then
        assertThat(fluege)
            .isNotNull()
            .isNotEmpty();

        fluege.forEach(f ->
            softly.assertThat(f.getZielOrt()).isEqualTo(zielOrt)
        );
    }

    // -------------------------------------------------------------------------
    // Geschaeftslogik fuer die Suche anhand der ID
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Suche mit vorhandener ID")
    void findById() {
        // given
        final var id = UUID.fromString(ID_VORHANDEN);
        final var flug = createFlugMock(id, ZIELORT);
        when(repo.findByIdFetchFlugzeugUndPassagiere(id)).thenReturn(flug);

        // when
        final var result = service.findByIdMitFlugzeug(id);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getZielOrt()).isEqualTo(ZIELORT);
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden fuer Mock-Objekte (Zimmermann-Stil)
    // -------------------------------------------------------------------------

    private Flug createFlugMock(final String zielOrt) {
        return createFlugMock(randomUUID(), zielOrt);
    }

    private Flug createFlugMock(final UUID id, final String zielOrt) {
        final var flugzeug = new Flugzeug();
        flugzeug.setId(randomUUID());
        flugzeug.setHersteller("Airbus");
        flugzeug.setModell("A320");
        flugzeug.setSitzplaetze(180);
        flugzeug.setReichweiteKm(6100);
        flugzeug.setErzeugt(now(EUROPE_BERLIN));
        flugzeug.setAktualisiert(now(EUROPE_BERLIN));

        final var passagier = new Passagier();
        passagier.setId(randomUUID());
        passagier.setVorname("Max");
        passagier.setNachname("Mustermann");
        passagier.setGeburtsdatum(LocalDate.of(2000, 1, 1));
        passagier.setSitzplatz("12A");
        passagier.setErzeugt(now(EUROPE_BERLIN));
        passagier.setAktualisiert(now(EUROPE_BERLIN));

        final var flug = new Flug();
        flug.setId(id);
        flug.setVersion(0);
        flug.setStartOrt("Karlsruhe");
        flug.setZielOrt(zielOrt);
        flug.setAbflugZeit(now(EUROPE_BERLIN).plusDays(1));
        flug.setAnkunftZeit(now(EUROPE_BERLIN).plusDays(1).plusHours(2));
        flug.setFlugzeug(flugzeug);
        flug.setPassagiere(List.of(passagier));
        flug.setErzeugt(now(EUROPE_BERLIN));
        flug.setAktualisiert(now(EUROPE_BERLIN));

        return flug;
    }
}
