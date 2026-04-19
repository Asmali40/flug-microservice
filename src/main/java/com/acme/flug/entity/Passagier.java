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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/// Daten eines Passagiers.
/// Ein Flug hat mehrere Passagiere (1:N).
@Entity
@SuppressWarnings({"JavadocDeclaration", "RequireEmptyLineBeforeBlockTagGroup", "MissingSummary"})
public class Passagier {

    /// Zeitstempel beim initialen INSERT.
    @CreationTimestamp
    private LocalDateTime erzeugt;

    /// Zeitstempel beim letzten UPDATE.
    @UpdateTimestamp
    private LocalDateTime aktualisiert;

    /// Die ID.
    @Id
    @GeneratedValue
    private UUID id;

    /// Der Vorname des Passagiers.
    private String vorname;

    /// Der Nachname des Passagiers.
    private String nachname;

    /// Das Geburtsdatum.
    private LocalDate geburtsdatum;

    /// Der Sitzplatz im Flugzeug.
    private String sitzplatz;

    /// Standard-Konstruktor für _Jakarta Persistence_.
    public Passagier() {
    }

    /// Konstruktor für eine Builder-Klasse.
    ///
    /// @param id Die ID.
    /// @param vorname Vorname.
    /// @param nachname Nachname.
    /// @param geburtsdatum Geburtsdatum.
    /// @param sitzplatz Sitzplatz.
    /// @param erzeugt Zeitstempel beim INSERT.
    /// @param aktualisiert Zeitstempel beim UPDATE.
    @SuppressWarnings("ParameterNumber")
    public Passagier(
        final UUID id,
        final String vorname,
        final String nachname,
        final LocalDate geburtsdatum,
        final String sitzplatz,
        final LocalDateTime erzeugt,
        final LocalDateTime aktualisiert
    ) {
        this.id = id;
        this.vorname = vorname;
        this.nachname = nachname;
        this.geburtsdatum = geburtsdatum;
        this.sitzplatz = sitzplatz;
        this.erzeugt = erzeugt;
        this.aktualisiert = aktualisiert;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(final String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(final String nachname) {
        this.nachname = nachname;
    }

    public LocalDate getGeburtsdatum() {
        return geburtsdatum;
    }

    public void setGeburtsdatum(final LocalDate geburtsdatum) {
        this.geburtsdatum = geburtsdatum;
    }

    public String getSitzplatz() {
        return sitzplatz;
    }

    public void setSitzplatz(final String sitzplatz) {
        this.sitzplatz = sitzplatz;
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
        return other instanceof Passagier passagier && Objects.equals(id, passagier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Passagier{" +
            "id=" + id +
            ", vorname='" + vorname + '\'' +
            ", nachname='" + nachname + '\'' +
            ", geburtsdatum=" + geburtsdatum +
            ", sitzplatz='" + sitzplatz + '\'' +
            '}';
    }
}
