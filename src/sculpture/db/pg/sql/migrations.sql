-- add artist-nationality 2021-12-22

CREATE TABLE IF NOT EXISTS "nationalities" (
  -- required:
  "id" uuid PRIMARY KEY,
  "type" text NOT NULL,
  "nation" text NOT NULL,
  "demonym" text NOT NULL,
  "slug" text NOT NULL
);

CREATE INDEX "nationality_slug" ON "nationality"(lower(slug));

CREATE TABLE IF NOT EXISTS "artists_nationalities" (
  "nationality-id" uuid references "nationalities"(id),
  "artist-id" uuid references "artists"(id),
  PRIMARY KEY ("nationality-id", "artist-id")
);

-- add photos.segment-id 2023-07-04

ALTER TABLE "photos"
ADD COLUMN "segment-id" uuid REFERENCES segments(id);

-- add photos.featured? 2023-07-09
ALTER TABLE "photos"
ADD COLUMN "featured?" boolean NOT NULL;
