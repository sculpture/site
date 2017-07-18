-- name:-select-sculptures-for-region
--
WITH target_sculptures AS (
  SELECT
    sculptures.id
  FROM
    sculptures,
    regions
  WHERE
    ST_Covers(regions.shape, sculptures.location) AND
    regions.slug = lower(:region_slug)
)
SELECT
  extended_sculptures.*
FROM
  extended_sculptures,
  target_sculptures
WHERE
  extended_sculptures.id = target_sculptures.id;

-- name:-select-sculptures-for-artist
--
WITH target_sculptures AS (
  SELECT
    artists_sculptures."sculpture-id" AS id
  FROM
    artists_sculptures,
    artists
  WHERE
    artists.id = artists_sculptures."artist-id" AND
    artists.slug = lower(:artist_slug)
)
SELECT
  extended_sculptures.*
FROM
  extended_sculptures,
  target_sculptures
WHERE
    extended_sculptures.id = target_sculptures.id;

-- name:-select-sculptures-for-decade
--
SELECT
  extended_sculptures.*
FROM
  extended_sculptures
WHERE
  extended_sculptures.date BETWEEN :date_start AND :date_end;

-- name:-select-sculptures-for-artist-tag-slug
--
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
  "artist-tags".slug = lower(:artist_tag_slug);

-- name:-select-sculptures-for-artist-gender
--
WITH target_sculptures AS (
  SELECT
    artists_sculptures."sculpture-id" AS id
  FROM
    artists_sculptures,
    artists
  WHERE
    artists_sculptures."artist-id" = artists.id AND
    artists.gender = :artist_gender
)
SELECT
  extended_sculptures.*
FROM
  target_sculptures,
  extended_sculptures
WHERE
  extended_sculptures.id = target_sculptures.id;

-- name:-select-sculptures-for-sculpture-tag-slug
--
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
    "sculpture-tags".slug = lower(:sculpture_tag_slug)
)
SELECT
  extended_sculptures.*
FROM
  extended_sculptures,
  target_sculptures
WHERE
  extended_sculptures.id = target_sculptures.id;

-- name:-select-regions
--
SELECT
  regions.id,
  regions.name,
  regions.slug,
  count(sculptures) AS "sculpture-count"
FROM
  regions,
  sculptures
WHERE
  ST_Covers(regions.shape, sculptures.location)
GROUP BY
  regions.id;

-- name:-select-artists
--
SELECT
  *
FROM
  artists;

-- name:-select-artist-with-slug
--
SELECT
  *
FROM
  artists
WHERE
  artists.slug = lower(:slug);

-- name:-select-sculpture-with-slug
--
SELECT
  *
FROM
  extended_sculptures
WHERE
  extended_sculptures.slug = lower(:slug);
