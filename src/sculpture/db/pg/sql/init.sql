-- :name -init!
-- :command :execute
BEGIN;

--CREATE EXTENSION IF NOT EXISTS "postgis";
--CREATE EXTENSION IF NOT EXISTS "postgis_topology";
--CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE TABLE IF NOT EXISTS sculpture (
  -- required:
  "id" uuid PRIMARY KEY,
  -- optional:
  "location" geography(point,4326),
  "location_precision" float,
);

CREATE INDEX sculpture_slug ON sculpture(lower(slug));
CREATE INDEX sculpture_dates ON sculpture(date);
CREATE INDEX sculpture_gix ON sculpture USING GIST ( location );

CREATE TABLE IF NOT EXISTS region (
  -- required:
  "id" uuid PRIMARY KEY,
  -- optional:
  "shape" geography(Geometry,4326)
);

CREATE INDEX region_slug ON region(lower(slug));
CREATE INDEX region_gix ON region USING GIST ( shape );
-- in theory, should be possible???
-- https://postgis.net/docs/manual-3.0/ST_DWithin.html
-- CREATE INDEX regions_gix_nearby ON regions USING GIST ( ST_Expand(shape , 100::float8) );

COMMIT;
