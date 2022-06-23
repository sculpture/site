-- :name -graph-search-entities
-- :result :many
-- id, type, title, subtitle, image-url
WITH search_results AS (
  SELECT
    sculptures.id AS id,
    'sculpture' AS type,
    sculptures.title AS title,
  string_agg(artists.name, ', ') AS subtitle,
    (array_agg(photos.id))[1] AS "photo-id"
  FROM
    sculptures, artists_sculptures, artists, photos
  WHERE
    sculptures.title ILIKE concat('%',:query,'%') AND
    -- joins
    sculptures.id = artists_sculptures."sculpture-id" AND
    artists.id = artists_sculptures."artist-id" AND
    photos."sculpture-id" = sculptures.id
  GROUP BY
   sculptures.id

  UNION

  SELECT
    "sculpture-tags".id AS id,
    'sculpture-tag' AS type,
    "sculpture-tags".name AS title,
    NULL as subtitle,
    NULL as "photo-id"
  FROM
    "sculpture-tags"
  WHERE
    "sculpture-tags".name ILIKE concat('%',:query,'%')

  UNION

  SELECT
    artists.id AS id,
    'artist' AS type,
    artists.name AS title,
    NULL as subtitle,
    (array_agg(photos.id))[1] AS "photo-id"
  FROM
    artists, artists_sculptures, sculptures, photos
  WHERE
    artists.name ILIKE concat('%',:query,'%') AND
    -- joins
    artists.id = artists_sculptures."artist-id" AND
    sculptures.id = artists_sculptures."sculpture-id" AND
    photos."sculpture-id" = sculptures.id
  GROUP BY
    artists.id

  UNION

  SELECT
    materials.id AS id,
    'material' AS type,
    materials.name AS title,
    NULL as subtitle,
    NULL as "photo-id"
  FROM
    materials
  WHERE
    materials.name ILIKE concat('%',:query,'%')

  UNION

  SELECT
    categories.id AS id,
    'category' AS type,
    categories.name AS title,
    NULL as subtitle,
    NULL as "photo-id"
  FROM
    categories
  WHERE
    categories.name ILIKE concat('%',:query,'%')

  UNION

  SELECT
    "artist-tags".id AS id,
    'artist-tag' AS type,
    "artist-tags".name AS title,
    NULL as subtitle,
    NULL as "photo-id"
  FROM
    "artist-tags"
  WHERE
    "artist-tags".name ILIKE concat('%',:query,'%')

  UNION

  SELECT
    cities.id AS id,
    'city' AS type,
    cities.city AS title,
    concat(cities.region, ', ', cities.country) as subtitle,
    NULL as "photo-id"
  FROM
    cities
  WHERE
    cities.city ILIKE concat('%',:query,'%')

  UNION

  SELECT
    nationalities.id AS id,
    'nationality' AS type,
    nationalities.demonym AS title,
    NULL AS subtitle,
    NULL AS "photo-id"
  FROM
    nationalities
  WHERE
    nationalities.demonym ILIKE concat('%',:query,'%')

  UNION

  SELECT
    regions.id AS id,
    'region' AS type,
    regions.name AS title,
    NULL as subtitle,
    NULL as "photo-id"
  FROM
    regions
  WHERE
    regions.name ILIKE concat('%',:query,'%')
)
SELECT *
FROM search_results
LIMIT :limit;

-- :name -graph-select-sculptures
-- :result :many
SELECT
  sculptures.id,
  sculptures.location,
  sculptures.title
FROM
  sculptures;
