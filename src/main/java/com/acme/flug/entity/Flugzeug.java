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
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/// Daten eines Flugzeugs.
/// Ein Flug besitzt genau ein Flugzeug (1:1).
@Entity
@SuppressWarnings({"JavadocDeclaration", "RequireEmptyLineBeforeBlockTagGroup", "MissingSummary"})
public class Flugzeug {

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

    /// Der Hersteller des Flugzeugs.
    private String hersteller;

    /// Die Modellbezeichnung.
    private String modell;

    /// Anzahl der Sitzplätze.
    private int sitzplaetze;

    /// Reichweite in Kilometern.
    private double reichweiteKm;

    /// Standard-Konstruktor für _Jakarta Persistence_.
    public Flugzeug() {
    }

    /// Konstruktor für eine Builder-Klasse.
    ///
    /// @param id Die ID.
    /// @param hersteller Hersteller.
    /// @param modell Modell.
    /// @param sitzplaetze Sitzplätze.
    /// @param reichweiteKm Reichweite.
    /// @param erzeugt Zeitstempel beim INSERT.
    /// @param aktualisiert Zeitstempel beim UPDATE.
    @SuppressWarnings("ParameterNumber")
    public Flugzeug(
        final UUID id,
        final String hersteller,
        final String modell,
        final int sitzplaetze,
        final double reichweiteKm,
        final LocalDateTime erzeugt,
        final LocalDateTime aktualisiert
    ) {
        this.id = id;
        this.hersteller = hersteller;
        this.modell = modell;
        this.sitzplaetze = sitzplaetze;
        this.reichweiteKm = reichweiteKm;
        this.erzeugt = erzeugt;
        this.aktualisiert = aktualisiert;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getHersteller() {
        return hersteller;
    }

    public void setHersteller(final String hersteller) {
        this.hersteller = hersteller;
    }

    public String getModell() {
        return modell;
    }

    public void setModell(final String modell) {
        this.modell = modell;
    }

    public int getSitzplaetze() {
        return sitzplaetze;
    }

    public void setSitzplaetze(final int sitzplaetze) {
        this.sitzplaetze = sitzplaetze;
    }

    public double getReichweiteKm() {
        return reichweiteKm;
    }

    public void setReichweiteKm(final double reichweiteKm) {
        this.reichweiteKm = reichweiteKm;
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
        return other instanceof Flugzeug flugzeug && Objects.equals(id, flugzeug.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Flugzeug{" +
            "id=" + id +
            ", hersteller='" + hersteller + '\'' +
            ", modell='" + modell + '\'' +
            ", sitzplaetze=" + sitzplaetze +
            ", reichweiteKm=" + reichweiteKm +
            '}';
    }
}
