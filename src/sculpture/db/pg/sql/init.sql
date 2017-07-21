-- :name -init!
-- :command :execute
BEGIN;

CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "postgis_topology";

CREATE TABLE IF NOT EXISTS sculptures (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "title" text NOT NULL,
  "slug" text NOT NULL,
  -- optional:
  "size" integer,
  "note" text,
  "date" date,
  "date-precision" text, -- could be enum
  "commissioned-by" text,
  "location" geography(point,4326),
  "location-precision" float
);

CREATE INDEX sculptures_slug ON sculptures(lower(slug));
CREATE INDEX sculptures_dates ON sculptures(date);
CREATE INDEX sculptures_gix ON sculptures USING GIST ( location );

CREATE TABLE IF NOT EXISTS artists (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "name" text NOT NULL,
  "slug" text NOT NULL,
  -- optional:
  "gender" text, -- could be enum
  "link-website" text,
  "link-wikipedia" text,
  "bio" text,
  "birth-date" date,
  "birth-date-precision" text, -- could be enum
  "death-date" date,
  "death-date-precision" text -- could be enum
);

CREATE INDEX artists_slug ON artists(lower(slug));

CREATE TABLE IF NOT EXISTS materials (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "name" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX materials_slug ON materials(lower(slug));

CREATE TABLE IF NOT EXISTS regions (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "name" text NOT NULL,
  "slug" text NOT NULL,
  -- optional:
  "shape" geography(polygon,4326)
);

CREATE INDEX regions_slug ON regions(lower(slug));
CREATE INDEX regions_gix ON regions USING GIST ( shape );

CREATE TABLE IF NOT EXISTS users (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "email" text NOT NULL,
  "name" text NOT NULL,
  -- optional:
  "avatar" text
);

CREATE INDEX users_email ON users(lower(email));

CREATE TABLE IF NOT EXISTS photos (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "captured-at" timestamp with time zone NOT NULL,
  "user-id" uuid REFERENCES users(id) NOT NULL,
  "colors" json NOT NULL,
  "width" integer NOT NULL,
  "height" integer NOT NULL,
  -- optional:
  "sculpture-id" uuid REFERENCES sculptures(id)
);


CREATE TABLE IF NOT EXISTS "sculpture-tags" (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "name" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX "sculpture-tags_slug" ON "sculpture-tags"(lower(slug));

CREATE TABLE IF NOT EXISTS "region-tags" (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "name" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX "region-tags_slug" ON "region-tags"(lower(slug));

CREATE TABLE IF NOT EXISTS "artist-tags" (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "name" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX "artist-tags_slug" ON "artist-tags"(lower(slug));

-- relationships:

CREATE TABLE IF NOT EXISTS artists_sculptures (
  "artist-id" uuid references artists(id),
  "sculpture-id" uuid references sculptures(id),
  PRIMARY KEY ("artist-id", "sculpture-id")
);

CREATE TABLE IF NOT EXISTS materials_sculptures (
  "material-id" uuid references materials(id),
  "sculpture-id" uuid references sculptures(id),
  PRIMARY KEY ("material-id", "sculpture-id")
);

CREATE TABLE IF NOT EXISTS "sculptures_sculpture-tags" (
  "sculpture-tag-id" uuid references "sculpture-tags"(id),
  "sculpture-id" uuid references sculptures(id),
  PRIMARY KEY ("sculpture-tag-id", "sculpture-id")
);

CREATE TABLE IF NOT EXISTS "artists_artist-tags" (
  "artist-tag-id" uuid references "artist-tags"(id),
  "artist-id" uuid references artists(id),
  PRIMARY KEY ("artist-tag-id", "artist-id")
);

CREATE TABLE IF NOT EXISTS "regions_region-tags" (
  "region-tag-id" uuid references "region-tags"(id),
  "region-id" uuid references regions(id),
  PRIMARY KEY ("region-tag-id", "region-id")
);

-- views:

CREATE VIEW extended_sculptures AS (
  SELECT
    sculptures.*,
    json_agg(distinct to_jsonb(photos)) AS "photos",
    json_agg(distinct artists) AS "artists",
    json_agg(distinct materials) AS "materials",
    json_agg(regions ORDER BY ST_Area(ST_Envelope(regions.shape::geometry)) ASC) AS "regions"
  FROM
    sculptures
  LEFT JOIN artists_sculptures ON artists_sculptures."sculpture-id" = sculptures.id
  LEFT JOIN artists ON artists_sculptures."artist-id" = artists.id
  LEFT JOIN materials_sculptures ON materials_sculptures."sculpture-id" = sculptures.id
  LEFT JOIN regions ON ST_Covers(regions.shape, sculptures.location)
  LEFT JOIN photos ON photos."sculpture-id" = sculptures.id
  LEFT JOIN materials ON materials_sculptures."material-id" = materials.id
  GROUP BY
    sculptures.id
);

CREATE VIEW artists_with_related_ids AS (
  SELECT
    artists.*,
    array_agg(distinct "artists_artist-tags"."artist-tag-id") AS "tag-ids"
  FROM
    artists
  LEFT JOIN "artists_artist-tags" ON "artists_artist-tags"."artist-id" = "artists".id
  GROUP BY
    artists.id
);

CREATE VIEW sculptures_with_related_ids AS (
  SELECT
    sculptures.*,
    array_agg(distinct "materials_sculptures"."material-id") AS "material-ids",
    array_agg(distinct "artists_sculptures"."artist-id") AS "artist-ids",
    array_agg(distinct "sculptures_sculpture-tags"."sculpture-tag-id") AS "tag-ids"
  FROM
    sculptures
  LEFT JOIN materials_sculptures ON "materials_sculptures"."sculpture-id" = "sculptures".id
  LEFT JOIN artists_sculptures ON "artists_sculptures"."sculpture-id" = "sculptures".id
  LEFT JOIN "sculptures_sculpture-tags" ON "sculptures_sculpture-tags"."sculpture-id" = "sculptures".id
  GROUP BY
    sculptures.id
);

CREATE VIEW regions_with_related_ids AS (
  SELECT
    regions.*,
    ST_AsGeoJSON(regions.shape) AS geojson,
    array_agg(distinct "regions_region-tags"."region-tag-id") AS "tag-ids"
  FROM
    regions
  LEFT JOIN "regions_region-tags" ON "regions_region-tags"."region-id" = "regions".id
  GROUP BY
    regions.id
);

COMMIT;
