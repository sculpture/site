-- name: -insert-sculpture
--
INSERT INTO sculptures ("id", "title", "slug", "size", "note", "date", "date-precision", "commissioned-by", "location")
VALUES (:id, :title, :slug, :size, :note, :date, :date_precision, :commissioned_by, ST_Point(:location_lng, :location_lat))
RETURNING true;

-- name: -insert-region
--
INSERT INTO regions ("id", "name", "slug", "shape")
VALUES (:id, :name, :slug, ST_GeomFromGeoJSON(:shape))
RETURNING true;

-- name: -insert-photo
--
INSERT INTO photos ("id", "captured-at", "user-id", "colors", "width", "height", "sculpture-id")
VALUES (:id, :captured_at, :user_id, :colors::json, :width, :height, :sculpture_id)
RETURNING true;

-- name: -insert-user
--
INSERT INTO users ("id", "email", "name", "avatar")
VALUES (:id, :email, :name, :avatar)
RETURNING true;

-- name: -insert-artist
--
INSERT INTO artists ("id", "name", "slug", "gender", "link-website", "link-wikipedia", "bio", "birth-date", "birth-date-precision", "death-date", "death-date-precision")
VALUES (:id, :name, :slug, :gender, :link_website, :link_wikipedia, :bio, :birth_date, :birth_date_precision, :death_date, :death_date_precision)
RETURNING true;

-- name: -insert-artist-tag
--
INSERT INTO "artist-tags" ("id", "name", "slug")
VALUES (:id, :name, :slug)
RETURNING true;

-- name: -insert-sculpture-tag
--
INSERT INTO "sculpture-tags" ("id", "name", "slug")
VALUES (:id, :name, :slug)
RETURNING true;

-- name: -insert-material
--
INSERT INTO materials ("id", "name", "slug")
VALUES (:id, :name, :slug)
RETURNING true;

-- name: -insert-region-tag
--
INSERT INTO "region-tags" ("id", "name", "slug")
VALUES (:id, :name, :slug)
RETURNING true;

-- name: -relate-artist-sculpture
--
INSERT INTO artists_sculptures ("artist-id", "sculpture-id")
VALUES (:artist_id, :sculpture_id)
RETURNING true;

-- name: -relate-region-region-tag
--
INSERT INTO "regions_region-tags" ("region-id", "region-tag-id")
VALUES (:region_id, :region_tag_id)
RETURNING true;

-- name: -relate-artist-artist-tag
--
INSERT INTO "artists_artist-tags" ("artist-id", "artist-tag-id")
VALUES (:artist_id, :artist_tag_id)
RETURNING true;

-- name: -relate-sculpture-sculpture-tag
--
INSERT INTO "sculptures_sculpture-tags" ("sculpture-id", "sculpture-tag-id")
VALUES (:sculpture_id, :sculpture_tag_id)
RETURNING true;

-- name: -relate-material-sculpture
--
INSERT INTO "materials_sculptures" ("material-id", "sculpture-id")
VALUES (:material_id, :sculpture_id)
RETURNING true;
