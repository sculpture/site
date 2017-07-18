BEGIN;

CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "postgis_topology";

CREATE TABLE IF NOT EXISTS sculptures (
  -- required:
  "id" uuid PRIMARY KEY,
  "title" text NOT NULL,
  "slug" text NOT NULL,
  -- optional:
  "size" integer,
  "note" text,
  "date" date,
  "date-precision" text, -- could be enum
  "commissioned-by" text,
  "location" geography(point,4326)
);

CREATE INDEX sculptures_slug ON sculptures(lower(slug));
CREATE INDEX sculptures_dates ON sculptures(date);
CREATE INDEX sculptures_gix ON sculptures USING GIST ( location );

CREATE TABLE IF NOT EXISTS artists (
  -- required:
  "id" uuid PRIMARY KEY,
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
  "name" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX materials_slug ON materials(lower(slug));

CREATE TABLE IF NOT EXISTS regions (
  -- required:
  "id" uuid PRIMARY KEY,
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
  "email" text NOT NULL,
  "name" text NOT NULL,
  -- optional:
  "avatar" text
);

CREATE INDEX users_email ON users(lower(email));

CREATE TABLE IF NOT EXISTS photos (
  -- required:
  "id" uuid PRIMARY KEY,
  "captured-at" timestamp with time zone NOT NULL,
  "user-id" uuid REFERENCES users(id) NOT NULL,
  "colors" json NOT NULL,
  "width" integer NOT NULL,
  "height" integer NOT NULL,
  --
  "sculpture-id" uuid REFERENCES sculptures(id)
);

CREATE TABLE IF NOT EXISTS "sculpture-tags" (
  -- required:
  "id" uuid PRIMARY KEY,
  "name" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX "sculpture-tags_slug" ON "sculpture-tags"(lower(slug));

CREATE TABLE IF NOT EXISTS "region-tags" (
  -- required:
  "id" uuid PRIMARY KEY,
  "name" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX "region-tags_slug" ON "region-tags"(lower(slug));

CREATE TABLE IF NOT EXISTS "artist-tags" (
  -- required:
  "id" uuid PRIMARY KEY,
  "name" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX "artist-tags_slug" ON "artist-tags"(lower(slug));

-- relationships:

CREATE TABLE IF NOT EXISTS artists_sculptures (
  "artist-id" uuid references artists(id),
  "sculpture-id" uuid references sculptures(id)
);

CREATE TABLE IF NOT EXISTS materials_sculptures (
  "material-id" uuid references materials(id),
  "sculpture-id" uuid references sculptures(id)
);

CREATE TABLE IF NOT EXISTS "sculptures_sculpture-tags" (
  "sculpture-tag-id" uuid references "sculpture-tags"(id),
  "sculpture-id" uuid references sculptures(id)
);

CREATE TABLE IF NOT EXISTS "artists_artist-tags" (
  "artist-tag-id" uuid references "artist-tags"(id),
  "artist-id" uuid references artists(id)
);

CREATE TABLE IF NOT EXISTS "regions_region-tags" (
  "region-tag-id" uuid references "region-tags"(id),
  "region-id" uuid references regions(id)
);

COMMIT;

-- views:

CREATE VIEW extended_sculptures AS (
  SELECT
    sculptures.*,
    json_agg(distinct to_jsonb(photos)) AS "photos",
    json_agg(distinct artists) AS "artists",
    json_agg(distinct materials) AS "materials",
    json_agg(regions ORDER BY ST_Area(ST_Envelope(regions.shape::geometry)) ASC) AS "regions"
  FROM
    sculptures,
    photos,
    artists,
    artists_sculptures,
    materials_sculptures,
    materials,
    regions
  WHERE
    ST_Covers(regions.shape, sculptures.location) AND
    photos."sculpture-id" = sculptures.id AND
    artists_sculptures."artist-id" = artists.id AND
    artists_sculptures."sculpture-id" = sculptures.id AND
    materials_sculptures."sculpture-id" = sculptures.id AND
    materials_sculptures."material-id" = materials.id
  GROUP BY sculptures.id
);
