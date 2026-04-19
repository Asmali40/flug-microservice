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
import com.acme.flug.repository.FlugRepository;
import com.acme.flug.repository.SpecificationBuilder;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/// Geschäftslogik für Fluege.
/// ![Klassendiagramm](../../../../../asciidoc/FlugService.svg)
/// Schreiboperationen werden mit Transaktionen durchgeführt und Lese-Operationen mit Readonly-Transaktionen:
/// [siehe Dokumentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions)
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
// Maven: ![Klassendiagramm](../../../../../../generated-docs/FlugService.svg)
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions
@Service
@Transactional(readOnly = true)
public class FlugService {
    private final FlugRepository repo;
    private final SpecificationBuilder specificationBuilder;
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit `package private` für _Constructor Injection_ bei _Spring_.
    ///
    /// @param repo Injiziertes Repository für _Spring Data_.
    /// @param specificationBuilder Builder-Pattern für _Specification_s bei _Spring Data_.
    FlugService(final FlugRepository repo, final SpecificationBuilder specificationBuilder) {
        this.repo = repo;
        this.specificationBuilder = specificationBuilder;
    }

    /// Flug inkl. Flugzeug anhand der ID suchen.
    ///
    /// @param id Die Id des gesuchten Flugs
    /// @return Der gefundene Flug
    /// @throws NotFoundException Falls kein Flug gefunden wurde
    public Flug findByIdMitFlugzeug(final UUID id) {
        getLogger().debug("findById: id={}", id);

        final var flug = repo.findByIdFetchFlugzeugUndPassagiere(id);
        getLogger().trace("findById: flug={}", flug);

        if (flug == null) {
            throw new NotFoundException(id);
        }
        getLogger().debug("findById: flug={}", flug);
        return flug;
    }

    /// Flüge anhand von Suchparametern suchen (Specification).
    public Page<Flug> find(
        final Map<String, List<String>> suchparameter,
        final Pageable pageable
    ) {
        getLogger().debug("find: suchparameter={}, pageable={}", suchparameter, pageable);

        if (suchparameter.isEmpty()) {
            return repo.findAll(pageable);
        }

        final var specification = specificationBuilder.build(suchparameter);
        if (specification == null) {
            throw new NotFoundException(suchparameter);
        }

        final var flugPage = repo.findAll(specification, pageable);
        if (flugPage.isEmpty()) {
            throw new NotFoundException(suchparameter);
        }

        return flugPage;
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(FlugService.class));
    }
}
