-- :name -init!
-- :command :execute
BEGIN;

CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "postgis_topology";

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
  "date" date,
  "date-precision" text, -- could be enum
  "commissioned-by" text,
  "location" geography(point,4326),
  "location-precision" float
  "city-id" uuid REFERENCES cities(id)
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
  "nationality" text,
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
  "shape" geography(Geometry,4326)
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

COMMIT;
