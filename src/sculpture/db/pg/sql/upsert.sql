-- :name -upsert-sculpture!
-- :command :returning-execute
INSERT INTO sculptures ("id", "type", "title", "slug", "size", "note", "date", "date-precision", "commissioned-by", "location", "location-precision")
VALUES (:id, :type, :title, :slug, :size, :note, :date, :date-precision, :commissioned-by, ST_Point(:location-lng, :location-lat), :location-precision)
ON CONFLICT (id) DO
UPDATE
SET
  "title" = :title,
  "slug" = :slug,
  "size" = :size,
  "note" = :note,
  "date" = :date,
  "date-precision" = :date-precision,
  "commissioned-by" = :commissioned-by,
  "location" = ST_Point(:location-lng, :location-lat),
  "location-precision" = :location-precision
WHERE "sculptures".id = :id
RETURNING true;

-- :name -upsert-region!
-- :command :returning-execute
INSERT INTO regions ("id", "type", "name", "slug", "shape")
VALUES (:id, :type, :name, :slug, ST_GeomFromGeoJSON(:shape))
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug,
  "shape" = ST_GeomFromGeoJSON(:shape)
WHERE "regions".id = :id
RETURNING true;

-- :name -upsert-photo!
-- :command :returning-execute
INSERT INTO photos ("id", "type", "captured-at", "user-id", "colors", "width", "height", "sculpture-id")
VALUES (:id, :type, :captured-at, :user-id, :colors::json, :width, :height, :sculpture-id)
ON CONFLICT (id) DO
UPDATE
SET
  "captured-at" = :captured-at,
  "user-id" = :user-id,
  "colors" = :colors::json,
  "width" = :width,
  "height" = :height,
  "sculpture-id" = :sculpture-id
WHERE "photos".id = :id
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
INSERT INTO artists ("id", "type", "name", "slug", "nationality", "gender", "link-website", "link-wikipedia", "bio", "birth-date", "birth-date-precision", "death-date", "death-date-precision")
VALUES (:id, :type, :name, :slug, :nationality, :gender, :link-website, :link-wikipedia, :bio, :birth-date, :birth-date-precision, :death-date, :death-date-precision)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug,
  "nationality" = :nationality,
  "gender" = :gender,
  "link-website" = :link-website,
  "link-wikipedia" = :link-wikipedia,
  "bio" = :bio,
  "birth-date" = :birth-date,
  "birth-date-precision" = :birth-date-precision,
  "death-date" = :death-date,
  "death-date-precision" = :death-date-precision
WHERE "artists".id = :id
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

-- :name -upsert-sculpture-tag!
-- :command :returning-execute
INSERT INTO "sculpture-tags" ("id", "type", "name", "slug")
VALUES (:id, :type, :name, :slug)
ON CONFLICT (id) DO
UPDATE
SET
  "name" = :name,
  "slug" = :slug
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

-- :name -relate-artist-sculpture!
-- :command :returning-execute
INSERT INTO artists_sculptures ("artist-id", "sculpture-id")
VALUES (:artist-id, :sculpture-id)
ON CONFLICT ("artist-id", "sculpture-id") DO
NOTHING
RETURNING true;

-- :name -relate-region-region-tag!
-- :command :returning-execute
INSERT INTO "regions_region-tags" ("region-id", "region-tag-id")
VALUES (:region-id, :region-tag-id)
ON CONFLICT ("region-id", "region-tag-id") DO
NOTHING
RETURNING true;

-- :name -relate-artist-artist-tag!
-- :command :returning-execute
INSERT INTO "artists_artist-tags" ("artist-id", "artist-tag-id")
VALUES (:artist-id, :artist-tag-id)
ON CONFLICT ("artist-id", "artist-tag-id") DO
NOTHING
RETURNING true;

-- :name -relate-sculpture-sculpture-tag!
-- :command :returning-execute
INSERT INTO "sculptures_sculpture-tags" ("sculpture-id", "sculpture-tag-id")
VALUES (:sculpture-id, :sculpture-tag-id)
ON CONFLICT ("sculpture-id", "sculpture-tag-id") DO
NOTHING
RETURNING true;

-- :name -relate-material-sculpture!
-- :command :returning-execute
INSERT INTO "materials_sculptures" ("material-id", "sculpture-id")
VALUES (:material-id, :sculpture-id)
ON CONFLICT ("material-id", "sculpture-id") DO
NOTHING
RETURNING true;
