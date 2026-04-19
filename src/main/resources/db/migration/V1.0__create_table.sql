-- Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.

-- docker compose exec postgres bash
-- psql --dbname=kunde --username=kunde [--file=/sql/V1.0__create_table.sql]

-- https://www.postgresql.org/docs/current/manage-ag-tablespaces.html
SET default_tablespace = flugspace;

-- text statt varchar(n):
-- "There is no performance difference among these three types, apart from a few extra CPU cycles
-- to check the length when storing into a length-constrained column"
-- ggf. CHECK(char_length(nachname) <= 255)

-- https://www.postgresql.org/docs/current/sql-createtable.html
-- https://www.postgresql.org/docs/current/datatype.html
CREATE TABLE IF     NOT EXISTS flugzeug (
    id              UUID PRIMARY KEY,
    hersteller      TEXT NOT NULL,
    modell          TEXT NOT NULL,
    sitzplaetze     INTEGER NOT NULL CHECK (sitzplaetze > 0),
    reichweite_km   DOUBLE PRECISION NOT NULL CHECK (reichweite_km > 0),
    version         INTEGER NOT NULL DEFAULT 0,
    erzeugt         TIMESTAMP NOT NULL,
    aktualisiert    TIMESTAMP NOT NULL
);

CREATE TYPE flug_typ AS ENUM ('LINIENFLUG', 'CHARTER', 'FRACHT', 'PRIVAT', 'MILITAER');

CREATE TABLE IF NOT EXISTS flug (
    id              UUID PRIMARY KEY,
    version         INTEGER NOT NULL DEFAULT 0,
    typ             TEXT,
    start_ort       TEXT NOT NULL,
    ziel_ort        TEXT NOT NULL,
    abflug_zeit     TIMESTAMP NOT NULL,
    ankunft_zeit    TIMESTAMP NOT NULL,
    username        TEXT NOT NULL,
    flugzeug_id     UUID NOT NULL UNIQUE REFERENCES flugzeug,
    erzeugt         TIMESTAMP NOT NULL,
    aktualisiert    TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS passagier (
    id              UUID PRIMARY KEY,
    vorname         TEXT NOT NULL,
    nachname        TEXT NOT NULL,
    geburtsdatum    DATE,
    sitzplatz       TEXT NOT NULL,
    flug_id         UUID REFERENCES flug,
    version         INTEGER NOT NULL DEFAULT 0,
    erzeugt         TIMESTAMP NOT NULL,
    aktualisiert    TIMESTAMP NOT NULL
);
