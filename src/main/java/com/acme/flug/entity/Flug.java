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
package com.acme.flug.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

/// Daten eines Fluges.
/// In DDD ist Flug ein Aggregate Root.
@Entity
@NamedEntityGraph(name = Flug.FLUGZEUG_GRAPH, attributeNodes = @NamedAttributeNode("flugzeug"))
@NamedEntityGraph(
    name = Flug.FLUGZEUG_PASSAGIERE_GRAPH,
    attributeNodes = {@NamedAttributeNode("flugzeug"), @NamedAttributeNode("passagiere")}
)
@SuppressWarnings({
    "ClassFanOutComplexity",
    "DeclarationOrder",
    "JavadocDeclaration",
    "MissingSummary",
    "RedundantSuppression"
})
public class Flug {

    /// NamedEntityGraph für das Attribut "flugzeug".
    public static final String FLUGZEUG_GRAPH = "Flug.flugzeug";

    /// NamedEntityGraph für die Attribute "flugzeug" und "passagiere".
    public static final String FLUGZEUG_PASSAGIERE_GRAPH = "Flug.flugzeugPassagiere";

    /// Primärschlüssel des Fluges.
    @Id
    @GeneratedValue
    private UUID id;

    /// Versionsnummer für optimistische Synchronisation.
    @Version
    private int version;

    /// Typ des Fluges.
    @Enumerated(STRING)
    private FlugTyp typ;

    /// Startort des Fluges.
    private String startOrt;

    /// Zielort des Fluges.
    private String zielOrt;

    /// Zeitpunkt des Abflugs.
    private LocalDateTime abflugZeit;

    /// Zeitpunkt der Ankunft.
    private LocalDateTime ankunftZeit;

    /// Benutzername (Owner).
    private String username;

    /// Zugehöriges Flugzeug (1:1-Beziehung).
    @OneToOne(optional = false, cascade = {PERSIST, REMOVE}, fetch = LAZY, orphanRemoval = true)
    @JoinColumn(name = "flugzeug_id")
    private Flugzeug flugzeug;

    /// Passagiere des Fluges (1:N-Beziehung).
    @OneToMany(cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "flug_id")
    private List<Passagier> passagiere;

    /// Zeitstempel beim initialen INSERT.
    @CreationTimestamp
    private LocalDateTime erzeugt;

    /// Zeitstempel beim letzten UPDATE.
    @UpdateTimestamp
    private LocalDateTime aktualisiert;

    /// Standardkonstruktor für Jakarta Persistence.
    public Flug() {
        // leer
    }

    /// Konstruktor für Builder/Tests.
    ///
    /// @param id ID
    /// @param version Version
    /// @param typ Typ
    /// @param startOrt Startort
    /// @param zielOrt Zielort
    /// @param abflugZeit Abflugzeit
    /// @param ankunftZeit Ankunftzeit
    /// @param username Username
    /// @param flugzeug Flugzeug
    /// @param passagiere Passagiere
    /// @param erzeugt Zeitstempel beim INSERT
    /// @param aktualisiert Zeitstempel beim UPDATE
    @SuppressWarnings("ParameterNumber")
    public Flug(
        final UUID id,
        final int version,
        final FlugTyp typ,
        final String startOrt,
        final String zielOrt,
        final LocalDateTime abflugZeit,
        final LocalDateTime ankunftZeit,
        final String username,
        final Flugzeug flugzeug,
        final List<Passagier> passagiere,
        final LocalDateTime erzeugt,
        final LocalDateTime aktualisiert
    ) {
        this.id = id;
        this.version = version;
        this.typ = typ;
        this.startOrt = startOrt;
        this.zielOrt = zielOrt;
        this.abflugZeit = abflugZeit;
        this.ankunftZeit = ankunftZeit;
        this.username = username;
        this.flugzeug = flugzeug;
        this.passagiere = passagiere;
        this.erzeugt = erzeugt;
        this.aktualisiert = aktualisiert;
    }

    /// Setzt die Werte des Flugs (ohne Child-Liste).
    /// Passagiere werden bewusst nicht gesetzt (analog Murat: Crew bleibt außen vor).
    ///
    /// @param flug Flugdaten
    public void set(final Flug flug) {
        typ = flug.typ;
        startOrt = flug.startOrt;
        zielOrt = flug.zielOrt;
        abflugZeit = flug.abflugZeit;
        ankunftZeit = flug.ankunftZeit;
        username = flug.username;
        flugzeug = flug.flugzeug;
        // passagiere absichtlich NICHT setzen
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public FlugTyp getTyp() {
        return typ;
    }

    public void setTyp(final FlugTyp typ) {
        this.typ = typ;
    }

    public String getStartOrt() {
        return startOrt;
    }

    public void setStartOrt(final String startOrt) {
        this.startOrt = startOrt;
    }

    public String getZielOrt() {
        return zielOrt;
    }

    public void setZielOrt(final String zielOrt) {
        this.zielOrt = zielOrt;
    }

    public LocalDateTime getAbflugZeit() {
        return abflugZeit;
    }

    public void setAbflugZeit(final LocalDateTime abflugZeit) {
        this.abflugZeit = abflugZeit;
    }

    public LocalDateTime getAnkunftZeit() {
        return ankunftZeit;
    }

    public void setAnkunftZeit(final LocalDateTime ankunftZeit) {
        this.ankunftZeit = ankunftZeit;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public Flugzeug getFlugzeug() {
        return flugzeug;
    }

    public void setFlugzeug(final Flugzeug flugzeug) {
        this.flugzeug = flugzeug;
    }

    public List<Passagier> getPassagiere() {
        return passagiere;
    }

    public void setPassagiere(final List<Passagier> passagiere) {
        this.passagiere = passagiere;
    }

    public LocalDateTime getErzeugt() {
        return erzeugt;
    }

    public void setErzeugt(final LocalDateTime erzeugt) {
        this.erzeugt = erzeugt;
    }

    public LocalDateTime getAktualisiert() {
        return aktualisiert;
    }

    public void setAktualisiert(final LocalDateTime aktualisiert) {
        this.aktualisiert = aktualisiert;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Flug flug && Objects.equals(id, flug.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /// Ohne Beziehungen, damit kein Lazy Fetching ausgelöst wird.
    @Override
    public String toString() {
        return "Flug{" +
            "id=" + id +
            ", version=" + version +
            ", typ=" + typ +
            ", startOrt='" + startOrt + '\'' +
            ", zielOrt='" + zielOrt + '\'' +
            ", abflugZeit=" + abflugZeit +
            ", ankunftZeit=" + ankunftZeit +
            ", username='" + username + '\'' +
            ", erzeugt=" + erzeugt +
            ", aktualisiert=" + aktualisiert +
            '}';
    }
}
