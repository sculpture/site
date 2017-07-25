-- :name -exists?
-- :result :one
SELECT EXISTS (
  SELECT
    1
  FROM
    :i:entity-type
  WHERE
    id = :id
  LIMIT
    1
);

-- :name -select-all-with-type
-- :result :many
SELECT
  *
FROM
  :i:type;

-- :name -select-sculptures-for-region
-- :result :many
WITH target_sculptures AS (
  SELECT
    sculptures.id
  FROM
    sculptures,
    regions
  WHERE
    ST_DWithin(regions.shape, sculptures.location, 250) AND
    regions.slug = lower(:region-slug)
)
SELECT
  extended_sculptures.*
FROM
  extended_sculptures,
  target_sculptures
WHERE
  extended_sculptures.id = target_sculptures.id;

-- :name -select-sculptures-for-artist
-- :result :many
WITH target_sculptures AS (
  SELECT
    artists_sculptures."sculpture-id" AS id
  FROM
    artists_sculptures,
    artists
  WHERE
    artists.id = artists_sculptures."artist-id" AND
    artists.slug = lower(:artist-slug)
)
SELECT
  extended_sculptures.*
FROM
  extended_sculptures,
  target_sculptures
WHERE
    extended_sculptures.id = target_sculptures.id;

-- :name -select-sculptures-for-decade
-- :result :many
SELECT
  extended_sculptures.*
FROM
  extended_sculptures
WHERE
  extended_sculptures.date BETWEEN :date-start AND :date-end;

-- :name -select-sculptures-for-artist-tag-slug
-- :result :many
SELECT
  extended_sculptures.*
FROM
  extended_sculptures,
  artists_sculptures,
  "artist-tags",
  artists,
  "artists_artist-tags"
WHERE
  extended_sculptures.id = artists_sculptures."sculpture-id" AND
  artists_sculptures."artist-id" = artists.id AND
  "artists_artist-tags"."artist-id" = artists.id AND
  "artists_artist-tags"."artist-tag-id" = "artist-tags".id AND
  "artist-tags".slug = lower(:artist-tag-slug);

-- :name -select-sculptures-for-artist-gender
-- :result :many
WITH target_sculptures AS (
  SELECT
    artists_sculptures."sculpture-id" AS id
  FROM
    artists_sculptures,
    artists
  WHERE
    artists_sculptures."artist-id" = artists.id AND
    artists.gender = :artist-gender
)
SELECT
  extended_sculptures.*
FROM
  target_sculptures,
  extended_sculptures
WHERE
  extended_sculptures.id = target_sculptures.id;

-- :name -select-sculptures-for-sculpture-tag-slug
-- :result :many
WITH target_sculptures AS (
  SELECT
    sculptures.id
  FROM
    sculptures,
    "sculptures_sculpture-tags",
    "sculpture-tags"
  WHERE
    sculptures.id = "sculptures_sculpture-tags"."sculpture-id" AND
    "sculptures_sculpture-tags"."sculpture-tag-id" = "sculpture-tags".id AND
    "sculpture-tags".slug = lower(:sculpture-tag-slug)
)
SELECT
  extended_sculptures.*
FROM
  extended_sculptures,
  target_sculptures
WHERE
  extended_sculptures.id = target_sculptures.id;

-- :name -select-sculptures-for-material-slug
-- :result :many
SELECT
  extended_sculptures.*
FROM
  extended_sculptures
JOIN materials_sculptures ON materials_sculptures."sculpture-id" = extended_sculptures.id
JOIN materials ON materials_sculptures."material-id" = materials.id
WHERE
  materials.slug = :material-slug;

-- :name -select-regions
-- :result :many
SELECT
  regions.id,
  regions.name,
  regions.slug,
  count(sculptures) AS "sculpture-count"
FROM
  regions,
  sculptures
WHERE
  ST_DWithin(regions.shape, sculptures.location, 250)
GROUP BY
  regions.id;

-- :name -select-sculpture-with-slug
-- :result :one
SELECT
  *
FROM
  extended_sculptures
WHERE
  extended_sculptures.slug = lower(:slug)
LIMIT
  1;

-- :name -select-random-sculpture-slug
-- :result :one
SELECT
  slug
FROM
  sculptures
OFFSET
  floor(random() * (SELECT COUNT(*) FROM sculptures))
LIMIT
  1;

-- :name -select-user-with-email
-- :result :one
SELECT
  *
FROM
  users
WHERE
  email = lower(:email)
LIMIT
  1;

-- :name -select-entity-with-id
-- :result :one
SELECT
  *
FROM
  :i:entity-type
WHERE
  id = :id
LIMIT
  1;


-- :name -select-entity-with-slug
-- :result :one
SELECT
  *
FROM
  :i:entity-type
WHERE
  slug = lower(:slug)
LIMIT
  1;
