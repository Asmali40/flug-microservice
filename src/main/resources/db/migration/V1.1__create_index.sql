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


-- https://www.postgresql.org/docs/current/manage-ag-tablespaces.html
SET default_tablespace = flugspace;

-- Indexe mit pgAdmin auflisten: "Query Tool" verwenden mit
--  SELECT   tablename, indexname, indexdef, tablespace
--  FROM     pg_indexes
--  WHERE    schemaname = 'kunde'
--  ORDER BY tablename, indexname;

-- default: btree
-- https://www.postgresql.org/docs/current/sql-createindex.html

CREATE INDEX IF NOT EXISTS flug_start_ort_idx ON flug(start_ort);
CREATE INDEX IF NOT EXISTS flug_ziel_ort_idx ON flug(ziel_ort);
CREATE INDEX IF NOT EXISTS passagier_nachname_idx ON passagier(nachname);
CREATE INDEX IF NOT EXISTS passagier_flug_id_idx ON passagier(flug_id);
