-- :name -init!
-- :command :execute
BEGIN;

--CREATE EXTENSION IF NOT EXISTS "postgis";
--CREATE EXTENSION IF NOT EXISTS "postgis_topology";
--CREATE EXTENSION IF NOT EXISTS "pg_trgm";

CREATE TABLE IF NOT EXISTS cities (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "city" text NOT NULL,
  "region" text NOT NULL,
  "country" text NOT NULL,
  "slug" text NOT NULL
);

CREATE TABLE IF NOT EXISTS sculptures (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "title" text NOT NULL,
  "slug" text NOT NULL,
  -- optional:
  "size" integer,
  "note" text,
  "date" text,
  "display-date" text,
  "commissioned-by" text,
  "location" geography(point,4326),
  "location-precision" float,
  "link-wikipedia" text,
  "city-id" uuid REFERENCES cities(id)
);

CREATE INDEX sculptures_slug ON sculptures(lower(slug));
CREATE INDEX sculptures_dates ON sculptures(date);
CREATE INDEX sculptures_gix ON sculptures USING GIST ( location );

CREATE TABLE IF NOT EXISTS "segments" (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "name" text NOT NULL,
  "slug" text NOT NULL,
  "sculpture-id" uuid REFERENCES sculptures(id)
);

CREATE INDEX segments_slug ON segments(lower(slug));

CREATE TABLE IF NOT EXISTS "nationalities" (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "nation" text NOT NULL,
  "demonym" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX "nationality_slug" ON "nationalities"(lower(slug));

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
  "birth-date" text,
  "death-date" text
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
  "shape" geography(Geometry,4326)
);

CREATE INDEX regions_slug ON regions(lower(slug));
CREATE INDEX regions_gix ON regions USING GIST ( shape );
-- in theory, should be possible???
-- https://postgis.net/docs/manual-3.0/ST_DWithin.html
-- CREATE INDEX regions_gix_nearby ON regions USING GIST ( ST_Expand(shape , 100::float8) );

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
  "featured?" boolean NOT NULL,
  "user-id" uuid REFERENCES users(id) NOT NULL,
  "colors" json NOT NULL,
  "width" integer NOT NULL,
  "height" integer NOT NULL,
  -- optional:
  "sculpture-id" uuid REFERENCES sculptures(id),
  "segment-id" uuid REFERENCES segments(id)
);

CREATE TABLE IF NOT EXISTS "categories" (
  "id" uuid PRIMARY KEY,
  "slug" text NOT NULL,
  "type" text NOT NULL,
  "name" text NOT NULL
);

CREATE TABLE IF NOT EXISTS "sculpture-tags" (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "name" text NOT NULL,
  "slug" text NOT NULL,
  "category-id" uuid REFERENCES categories(id)
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


CREATE TABLE IF NOT EXISTS "artists_nationalities" (
  "nationality-id" uuid references "nationalities"(id),
  "artist-id" uuid references "artists"(id),
  PRIMARY KEY ("nationality-id", "artist-id")
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

COMMIT;
