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
package com.acme.flug.repository;

import com.acme.flug.entity.Flug;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import static com.acme.flug.entity.Flug.FLUGZEUG_GRAPH;
import static com.acme.flug.entity.Flug.FLUGZEUG_PASSAGIERE_GRAPH;

public interface FlugRepository extends JpaRepository<Flug, UUID>, JpaSpecificationExecutor<Flug> {

    @EntityGraph(FLUGZEUG_GRAPH)
    @Override
    Page<Flug> findAll(Pageable pageable);

    @EntityGraph(FLUGZEUG_GRAPH)
    @Override
    Page<Flug> findAll(@Nullable Specification<Flug> spec, Pageable pageable);

    /// Flug einschließlich Flugzeug anhand der ID suchen.
    ///
    /// @param id Flug-ID
    /// @return Gefundener Flug oder null
    @Query("""
        SELECT f
        FROM   #{#entityName} f
        WHERE  f.id = :id
        """)
    @EntityGraph(FLUGZEUG_GRAPH)
    @Nullable
    Flug findByIdFetchFlugzeug(UUID id);

    /// Flug einschließlich Flugzeug und Passagiere anhand der ID suchen.
    ///
    /// @param id Flug-ID
    /// @return Gefundener Flug oder null
    @Query("""
        SELECT f
        FROM   #{#entityName} f
        WHERE  f.id = :id
        """)
    @EntityGraph(FLUGZEUG_PASSAGIERE_GRAPH)
    @Nullable
    Flug findByIdFetchFlugzeugUndPassagiere(UUID id);
}
