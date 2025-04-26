-- :name -upsert-sculpture!
-- :command :returning-execute
INSERT INTO sculptures ("id", "type", "title", "slug", "size", "note", "date", "display-date", "link-wikipedia", "commissioned-by", "city-id", "location", "location-precision")
VALUES (:id, :type, :title, :slug, :size, :note, :date, :display-date, :link-wikipedia, :commissioned-by, :city-id, ST_Point(:location-lng, :location-lat), :location-precision)
ON CONFLICT (id) DO
UPDATE
SET
  "title" = :title,
  "slug" = :slug,
  "size" = :size,
  "note" = :note,
  "date" = :date,
  "display-date" = :display-date,
  "link-wikipedia" = :link-wikipedia,
  "commissioned-by" = :commissioned-by,
  "city-id" = :city-id,
  "location" = ST_Point(:location-lng, :location-lat),
  "location-precision" = :location-precision
WHERE "sculptures".id = :id
RETURNING true;

-- :name -upsert-city!
-- :command :returning-execute
INSERT INTO cities ("id", "type", "slug", "city", "region", "country")
VALUES (:id, :type, :slug, :city, :region, :country)
ON CONFLICT (id) DO
UPDATE
SET
  "slug" = :slug,
  "city" = :city,
  "region" = :region,
  "country" = :country
WHERE
  "cities".id = :id
RETURNING true;

-- :name -upsert-region!
-- :command :returning-execute
INSERT INTO regions ("id", "type", "name", "slug", "shape")
VALUES (:id, :type, :name, :slug, ST_GeomFromGeoJSON(:geojson))
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug,
  "shape" = ST_GeomFromGeoJSON(:geojson)
WHERE "regions".id = :id
RETURNING true;

-- :name -upsert-photo!
-- :command :returning-execute
INSERT INTO photos ("id", "type", "captured-at", "user-id", "featured?", "colors", "width", "height", "sculpture-id", "segment-id")
VALUES (:id, :type, :captured-at, :user-id, :featured?, :colors::json, :width, :height, :sculpture-id, :segment-id)
ON CONFLICT (id) DO
UPDATE
SET
  "captured-at" = :captured-at,
  "user-id" = :user-id,
  "featured?" = :featured?,
  "colors" = :colors::json,
  "width" = :width,
  "height" = :height,
  "sculpture-id" = :sculpture-id,
  "segment-id" = :segment-id
WHERE "photos".id = :id
RETURNING true;

-- :name -upsert-segment!
-- :command :returning-execute
INSERT INTO segments ("id", "type", "name", "slug", "sculpture-id")
VALUES (:id, :type, :name, :slug, :sculpture-id)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug,
  "sculpture-id" = :sculpture-id
WHERE "segments".id = :id
RETURNING true;

-- :name -upsert-user!
-- :command :returning-execute
INSERT INTO users ("id", "type", "email", "name", "avatar")
VALUES (:id, :type, :email, :name, :avatar)
ON CONFLICT (id) DO
UPDATE
SET
  "email" = :email,
  "name" = :name,
  "avatar" = :avatar
WHERE "users".id = :id
RETURNING true;

-- :name -upsert-artist!
-- :command :returning-execute
INSERT INTO artists ("id", "type", "name", "slug", "gender", "link-website", "link-wikipedia", "bio", "birth-date", "death-date")
VALUES (:id, :type, :name, :slug, :gender, :link-website, :link-wikipedia, :bio, :birth-date, :death-date)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug,
  "gender" = :gender,
  "link-website" = :link-website,
  "link-wikipedia" = :link-wikipedia,
  "bio" = :bio,
  "birth-date" = :birth-date,
  "death-date" = :death-date
WHERE "artists".id = :id
RETURNING true;

-- :name -upsert-nationality!
-- :command :returning-execute
INSERT INTO "nationalities" ("id", "type", "demonym", "nation", "slug")
VALUES (:id, :type, :demonym, :nation, :slug)
ON CONFLICT (id) DO
UPDATE
SET
  "demonym" = :demonym,
  "nation" = :nation,
  "slug" = :slug
WHERE "nationalities".id = :id
RETURNING true;

-- :name -upsert-artist-tag!
-- :command :returning-execute
INSERT INTO "artist-tags" ("id", "type", "name", "slug")
VALUES (:id, :type, :name, :slug)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug
WHERE "artist-tags".id = :id
RETURNING true;

-- :name -upsert-category!
-- :command :returning-execute
INSERT INTO "categories" ("id", "type", "name", "slug")
VALUES (:id, :type, :name, :slug)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug
WHERE "categories".id = :id
RETURNING true;

-- :name -upsert-sculpture-tag!
-- :command :returning-execute
INSERT INTO "sculpture-tags" ("id", "type", "name", "slug", "category-id")
VALUES (:id, :type, :name, :slug, :category-id)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug,
  "category-id" = :category-id
WHERE "sculpture-tags".id = :id
RETURNING true;

-- :name -upsert-material!
-- :command :returning-execute
INSERT INTO materials ("id", "type", "name", "slug")
VALUES (:id, :type, :name, :slug)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug
WHERE "materials".id = :id
RETURNING true;

-- :name -upsert-region-tag!
-- :command :returning-execute
INSERT INTO "region-tags" ("id", "type", "name", "slug")
VALUES (:id, :type, :name, :slug)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug
WHERE "region-tags".id = :id
RETURNING true;

-- :name -delete-relations-artist-sculpture!
-- :command :returning-execute
DELETE FROM artists_sculptures
WHERE "sculpture-id" = :sculpture-id
RETURNING true;

-- :name -relate-artist-sculpture!
-- :command :returning-execute
INSERT INTO artists_sculptures ("artist-id", "sculpture-id")
VALUES (:artist-id, :sculpture-id)
ON CONFLICT ("artist-id", "sculpture-id") DO
NOTHING
RETURNING true;

-- :name -delete-relations-region-region-tag!
-- :command :returning-execute
DELETE FROM "regions_region-tags"
WHERE "region-id" = :region-id
RETURNING true;

-- :name -relate-region-region-tag!
-- :command :returning-execute
INSERT INTO "regions_region-tags" ("region-id", "region-tag-id")
VALUES (:region-id, :region-tag-id)
ON CONFLICT ("region-id", "region-tag-id") DO
NOTHING
RETURNING true;

-- :name -delete-relations-artist-artist-tag!
-- :command :returning-execute
DELETE FROM "artists_artist-tags"
WHERE "artist-id" = :artist-id
RETURNING true;

-- :name -relate-artist-artist-tag!
-- :command :returning-execute
INSERT INTO "artists_artist-tags" ("artist-id", "artist-tag-id")
VALUES (:artist-id, :artist-tag-id)
ON CONFLICT ("artist-id", "artist-tag-id") DO
NOTHING
RETURNING true;

-- :name -delete-relations-artist-nationality!
-- :command :returning-execute
DELETE FROM "artists_nationalities"
WHERE "artist-id" = :artist-id
RETURNING true;

-- :name -relate-artist-nationality!
-- :command :returning-execute
INSERT INTO "artists_nationalities" ("artist-id", "nationality-id")
VALUES (:artist-id, :nationality-id)
ON CONFLICT ("artist-id", "nationality-id") DO
NOTHING
RETURNING true;

-- :name -delete-relations-sculpture-sculpture-tag!
-- :command :returning-execute
DELETE FROM "sculptures_sculpture-tags"
WHERE "sculpture-id" = :sculpture-id
RETURNING true;

-- :name -relate-sculpture-sculpture-tag!
-- :command :returning-execute
INSERT INTO "sculptures_sculpture-tags" ("sculpture-id", "sculpture-tag-id")
VALUES (:sculpture-id, :sculpture-tag-id)
ON CONFLICT ("sculpture-id", "sculpture-tag-id") DO
NOTHING
RETURNING true;

-- :name -delete-relations-material-sculpture!
-- :command :returning-execute
DELETE FROM materials_sculptures
WHERE "sculpture-id" = :sculpture-id
RETURNING true;

-- :name -relate-material-sculpture!
-- :command :returning-execute
INSERT INTO "materials_sculptures" ("material-id", "sculpture-id")
VALUES (:material-id, :sculpture-id)
ON CONFLICT ("material-id", "sculpture-id") DO
NOTHING
RETURNING true;
