-- :name -search
-- :result :many
-- id, type, title, subtitle, image-url
WITH search_results AS (
  (
    SELECT
      sculptures.id AS id,
      'sculpture' AS type,
      sculptures.title AS title,
      string_agg(distinct(artists.name), ', ') AS subtitle,
      (array_agg(photos.id))[1] AS "photo-id"
    FROM
      sculptures
      LEFT JOIN artists_sculptures ON artists_sculptures."sculpture-id" = sculptures.id
      LEFT JOIN artists ON artists.id = artists_sculptures."artist-id"
      LEFT JOIN photos ON photos."sculpture-id" = sculptures.id
    WHERE
      'sculpture' IN (:v*:types) AND
      (
        sculptures.title ILIKE concat('%',:ilike-query,'%') OR
        to_tsvector('english',sculptures.title) @@ :raw-tsquery::tsquery OR
        to_tsvector('english',sculptures.title) @@ plainto_tsquery(:parsed-tsquery)
      )
    GROUP BY
     sculptures.id
    ORDER BY
      ts_rank_cd(to_tsvector('english',sculptures.title), plainto_tsquery(:parsed-tsquery)) DESC,
      word_similarity(sculptures.title,:ilike-query) DESC
    LIMIT :limit
  )

  UNION

  (
    SELECT
      "sculpture-tags".id AS id,
      'sculpture-tag' AS type,
      "sculpture-tags".name AS title,
      NULL as subtitle,
      (array_agg(photos.id))[1] AS "photo-id"
    FROM
      "sculpture-tags"
      LEFT JOIN "sculptures_sculpture-tags" ON "sculptures_sculpture-tags"."sculpture-tag-id" = "sculpture-tags".id
      LEFT JOIN sculptures ON sculptures.id = "sculptures_sculpture-tags"."sculpture-id"
      LEFT JOIN photos ON photos."sculpture-id" = sculptures.id
    WHERE
      'sculpture-tag' IN (:v*:types) AND
      (
        "sculpture-tags".name ILIKE concat('%',:ilike-query,'%') OR
        to_tsvector('english',"sculpture-tags".name) @@ :raw-tsquery::tsquery OR
        to_tsvector('english',"sculpture-tags".name) @@ plainto_tsquery(:parsed-tsquery)
      )
    GROUP BY
      "sculpture-tags".id
    ORDER BY
        ts_rank_cd(to_tsvector('english',"sculpture-tags".name), plainto_tsquery(:parsed-tsquery)) DESC,
        word_similarity("sculpture-tags".name,:ilike-query) DESC
    LIMIT :limit
  )

  UNION

  (
    SELECT
      artists.id AS id,
      'artist' AS type,
      artists.name AS title,
      NULL as subtitle,
      (array_agg(photos.id))[1] AS "photo-id"
    FROM
      artists
      LEFT JOIN artists_sculptures ON artists_sculptures."artist-id" = artists.id
      LEFT JOIN sculptures ON sculptures.id = artists_sculptures."sculpture-id"
      LEFT JOIN photos ON photos."sculpture-id" = sculptures.id
    WHERE
      'artist' IN (:v*:types) AND
      (
        "artists".name ILIKE concat('%',:ilike-query,'%') OR
        to_tsvector('english',"artists".name) @@ :raw-tsquery::tsquery OR
        to_tsvector('english',"artists".name) @@ plainto_tsquery(:parsed-tsquery)
      )
    GROUP BY
      artists.id
    ORDER BY
        ts_rank_cd(to_tsvector('english',"artists".name), plainto_tsquery(:parsed-tsquery)) DESC,
        word_similarity("artists".name,:ilike-query) DESC
    LIMIT :limit
  )

  UNION

  (
    SELECT
      materials.id AS id,
      'material' AS type,
      materials.name AS title,
      NULL as subtitle,
      (array_agg(photos.id))[1] AS "photo-id"
    FROM
      materials
      LEFT JOIN materials_sculptures ON materials_sculptures."material-id" = materials.id
      LEFT JOIN sculptures ON sculptures.id = materials_sculptures."sculpture-id"
      LEFT JOIN photos ON photos."sculpture-id" = sculptures.id
    WHERE
      'material' IN (:v*:types) AND
      (
        "materials".name ILIKE concat('%',:ilike-query,'%') OR
        to_tsvector('english',"materials".name) @@ :raw-tsquery::tsquery OR
        to_tsvector('english',"materials".name) @@ plainto_tsquery(:parsed-tsquery)
      )
    GROUP BY
      materials.id
    ORDER BY
      ts_rank_cd(to_tsvector('english',"materials".name), plainto_tsquery(:parsed-tsquery)) DESC,
      word_similarity("materials".name,:ilike-query) DESC
    LIMIT :limit
  )

  UNION

  (
    SELECT
      categories.id AS id,
      'category' AS type,
      categories.name AS title,
      NULL as subtitle,
      NULL as "photo-id"
    FROM
      categories
    WHERE
      'category' IN (:v*:types) AND
      (
          "categories".name ILIKE concat('%',:ilike-query,'%') OR
          to_tsvector('english',"categories".name) @@ :raw-tsquery::tsquery OR
          to_tsvector('english',"categories".name) @@ plainto_tsquery(:parsed-tsquery)
        )
    ORDER BY
      ts_rank_cd(to_tsvector('english',"categories".name), plainto_tsquery(:parsed-tsquery)) DESC,
      word_similarity("categories".name,:ilike-query) DESC
    LIMIT :limit
  )


  UNION

  (
    SELECT
      "region-tags".id AS id,
      'region-tag' AS type,
      "region-tags".name AS title,
      NULL as subtitle,
      NULL as "photo-id"
    FROM
      "region-tags"
    WHERE
      'region-tag' IN (:v*:types) AND
      (
          "region-tags".name ILIKE concat('%',:ilike-query,'%') OR
          to_tsvector('english',"region-tags".name) @@ :raw-tsquery::tsquery OR
          to_tsvector('english',"region-tags".name) @@ plainto_tsquery(:parsed-tsquery)
        )
    ORDER BY
      ts_rank_cd(to_tsvector('english',"region-tags".name), plainto_tsquery(:parsed-tsquery)) DESC,
      word_similarity("region-tags".name,:ilike-query) DESC
    LIMIT :limit
  )

  UNION

  (
    SELECT
      "artist-tags".id AS id,
      'artist-tag' AS type,
      "artist-tags".name AS title,
      NULL as subtitle,
      NULL as "photo-id"
    FROM
      "artist-tags"
    WHERE
      'artist-tag' IN (:v*:types) AND
      (
        "artist-tags".name ILIKE concat('%',:ilike-query,'%') OR
        to_tsvector('english',"artist-tags".name) @@ :raw-tsquery::tsquery OR
        to_tsvector('english',"artist-tags".name) @@ plainto_tsquery(:parsed-tsquery)
      )
    ORDER BY
      ts_rank_cd(to_tsvector('english',"artist-tags".name), plainto_tsquery(:parsed-tsquery)) DESC,
      word_similarity("artist-tags".name,:ilike-query) DESC
    LIMIT :limit
  )

  UNION

  (
    SELECT
      cities.id AS id,
      'city' AS type,
      cities.city AS title,
      concat(cities.region, ', ', cities.country) as subtitle,
      (array_agg(photos.id))[1] AS "photo-id"
    FROM
      cities
      LEFT JOIN sculptures ON sculptures."city-id" = cities.id
      LEFT JOIN photos ON photos."sculpture-id" = sculptures.id
    WHERE
      'city' IN (:v*:types) AND
      (
        "cities".city ILIKE concat('%',:ilike-query,'%') OR
        to_tsvector('english',"cities".city) @@ :raw-tsquery::tsquery OR
        to_tsvector('english',"cities".city) @@ plainto_tsquery(:parsed-tsquery)
      )
    GROUP BY
      cities.id
    ORDER BY
      ts_rank_cd(to_tsvector('english',"cities".city), plainto_tsquery(:parsed-tsquery)) DESC,
      word_similarity("cities".city,:ilike-query) DESC
    LIMIT :limit
  )

  UNION

  (
    SELECT
      nationalities.id AS id,
      'nationality' AS type,
      nationalities.demonym AS title,
      NULL AS subtitle,
      NULL AS "photo-id"
    FROM
      nationalities
    WHERE
      'nationality' IN (:v*:types) AND
      (
        "nationalities".demonym ILIKE concat('%',:ilike-query,'%') OR
        to_tsvector('english',"nationalities".demonym) @@ :raw-tsquery::tsquery OR
        to_tsvector('english',"nationalities".demonym) @@ plainto_tsquery(:parsed-tsquery)
      )
    ORDER BY
      ts_rank_cd(to_tsvector('english',"nationalities".demonym), plainto_tsquery(:parsed-tsquery)) DESC,
      word_similarity("nationalities".demonym,:ilike-query) DESC
    LIMIT :limit
  )

  UNION

  (
    SELECT
      regions.id AS id,
      'region' AS type,
      regions.name AS title,
      NULL as subtitle,
      NULL as "photo-id"
    FROM
      regions
    WHERE
      'region' IN (:v*:types) AND
      (
        "regions".name ILIKE concat('%',:ilike-query,'%') OR
        to_tsvector('english',"regions".name) @@ :raw-tsquery::tsquery OR
        to_tsvector('english',"regions".name) @@ plainto_tsquery(:parsed-tsquery)
      )
    ORDER BY
      ts_rank_cd(to_tsvector('english',"regions".name), plainto_tsquery(:parsed-tsquery)) DESC,
      word_similarity("regions".name,:ilike-query) DESC
    LIMIT :limit
  )
)
SELECT *
FROM search_results
ORDER BY
   ts_rank_cd(to_tsvector('english',title), plainto_tsquery(:parsed-tsquery)) DESC,
   word_similarity(title,:ilike-query) DESC
LIMIT :limit;

-- :name -exists?
-- :result :one
SELECT EXISTS (
  SELECT
    1
  FROM
    :i:type
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

-- :name -entity-counts
-- :result :one
SELECT
  (SELECT count(sculptures) FROM sculptures) AS sculptures,
  (SELECT count(artists) FROM artists) AS artists,
  (SELECT count(cities) FROM cities) AS cities,
  (SELECT count(materials) FROM materials) AS materials,
  (SELECT count(regions) FROM regions) AS regions;

-- :name -select-sculptures-for-region
-- :result :many
WITH target_sculptures AS (
  SELECT
    sculptures.id
  FROM
    sculptures,
    regions
  WHERE
    ST_DWithin(regions.shape, sculptures.location, 100) AND
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
