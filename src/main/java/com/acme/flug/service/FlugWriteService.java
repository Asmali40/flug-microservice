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
import com.acme.flug.mail.MailService;
import com.acme.flug.repository.FlugRepository;
import com.acme.flug.security.JwtService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/// Geschäftslogik für Flüge.
///
/// [Klassendiagramm](../../../../../asciidoc/FlugWriteService.svg)
@Service
@Transactional(readOnly = true)
public class FlugWriteService {

    /// Spring-Data-Repository für den DB-Zugriff
    private final FlugRepository repo;

    /// Service zum Versenden von E-Mails (Mailpit)
    private final MailService mailService;

    private final JwtService jwtService;

    /// Lazy initialisierter Logger
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor für Constructor Injection
    ///
    /// @param repo Repository für Flug
    /// @param mailService Service zum Mailversand
    FlugWriteService(final FlugRepository repo, final MailService mailService, final JwtService jwtService) {
        this.repo = repo;
        this.mailService = mailService;
        this.jwtService = jwtService;
    }

    /// Neuen Flug anlegen.
    ///
    /// @param flug Neues Flug-Objekt
    /// @return Persistierter Flug inkl. ID und Zeitstempeln
    @Transactional
    public Flug create(final Flug flug) {
        getLogger().debug("create: {}", flug);

        if (flug.getUsername() == null) {
            flug.setUsername(resolveUsername());
        }

        // Persistieren des Aggregate Roots
        final var flugDb = repo.save(flug);

        // Versand einer Mail (Anzeige in Mailpit)
        mailService.send(flugDb);

        getLogger().debug("create: {}", flugDb);
        return flugDb;
    }

    /// Vorhandenen Flug aktualisieren.
    ///
    /// @param flug Neue Flugdaten (ohne ID)
    /// @param id ID des zu aktualisierenden Fluges
    /// @param version Erwartete Version (If-Match / ETag)
    /// @return Aktualisierter Flug
    /// @throws NotFoundException Falls kein Flug mit dieser ID existiert
    /// @throws VersionOutdatedException Falls die Version veraltet ist
    @Transactional
    public Flug update(final Flug flug, final UUID id, final int version) {
        getLogger().debug("update: flug={}, id={}, version={}", flug, id, version);

        // Flug aus der DB laden oder Exception werfen
        var flugDb = repo.findById(id)
            .orElseThrow(() -> new NotFoundException(id));

        // Optimistische Synchronisation (kein Lost Update)
        if (version != flugDb.getVersion()) {
            throw new VersionOutdatedException(version);
        }

        // Fachliche Attribute überschreiben
        // Wichtig: kein Setzen einzelner Felder von außen
        if (flug.getUsername() == null) {
            final var username = flugDb.getUsername() == null ? resolveUsername() : flugDb.getUsername();
            flug.setUsername(username);
        }
        flugDb.set(flug);

        // Speichern → Hibernate erhöht automatisch die Versionsnummer
        flugDb = repo.save(flugDb);

        getLogger().debug("update: {}", flugDb);
        return flugDb;
    }

    /// Flug anhand der ID löschen.
    ///
    /// @param id ID des zu löschenden Fluges
    @Transactional
    public void deleteById(final UUID id) {
        getLogger().debug("deleteById: id={}", id);
        repo.findById(id).ifPresent(repo::delete);
    }

    /// Logger lazy initialisieren
    private String resolveUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                "Kein Authentication"
            );
        }
        return jwtService.getUsername(authentication);
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(FlugWriteService.class));
    }
}
