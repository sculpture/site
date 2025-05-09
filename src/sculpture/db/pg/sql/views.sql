-- :name -views!
-- :command :execute
BEGIN;

DROP VIEW IF EXISTS extended_sculptures;

CREATE VIEW extended_sculptures AS (
  SELECT
    sculptures.*,
    json_agg(distinct to_jsonb(photos)) AS "photos",
    json_agg(distinct artists) AS "artists",
    json_agg(distinct materials) AS "materials",
    (json_agg(distinct cities))->0 AS "city",
    json_agg(distinct "sculpture-tags") AS "sculpture-tags",
    -- moving distinct to before the first select, loses the order
    -- so we do the distinct in clojure
    json_agg((SELECT distinct x FROM (SELECT "regions".name, "regions".slug ORDER BY ST_Area(ST_Envelope(regions.shape::geometry)) ASC) AS x)) AS "regions",
    json_agg((SELECT distinct y FROM (SELECT "nearby-regions".name, "nearby-regions".slug, "nearby-regions".id ORDER BY ST_Distance("nearby-regions".shape, sculptures.location) ASC) AS y)) AS "regions-nearby"
  FROM
    sculptures
  LEFT JOIN artists_sculptures ON artists_sculptures."sculpture-id" = sculptures.id
  LEFT JOIN artists ON artists_sculptures."artist-id" = artists.id
  LEFT JOIN materials_sculptures ON materials_sculptures."sculpture-id" = sculptures.id
  LEFT JOIN regions ON ST_Covers(regions.shape, sculptures.location)
  LEFT JOIN regions AS "nearby-regions" ON ST_DWithin("nearby-regions".shape, sculptures.location, 100) AND ST_Distance("nearby-regions".shape, sculptures.location) > 0
--  LEFT JOIN regions AS "nearby-regions" ON ST_DWithin("nearby-regions".shape, sculptures.location, 100) AND NOT(ST_Covers(regions.shape, sculptures.location))
 -- LEFT JOIN regions AS "nearby-regions" ON ST_DWithin("nearby-regions".shape, sculptures.location, 100)
  LEFT JOIN photos ON photos."sculpture-id" = sculptures.id
  LEFT JOIN materials ON materials_sculptures."material-id" = materials.id
  LEFT JOIN "sculptures_sculpture-tags" ON "sculptures_sculpture-tags"."sculpture-id" = sculptures.id
  LEFT JOIN "sculpture-tags" ON "sculptures_sculpture-tags"."sculpture-tag-id" = "sculpture-tags".id
  LEFT JOIN "cities" ON "cities"."id" = "sculptures"."city-id"
  GROUP BY
    sculptures.id
);

DROP VIEW IF EXISTS artists_with_related_ids;

CREATE VIEW artists_with_related_ids AS (
  SELECT
    artists.*,
    json_agg(distinct "artist-tags") AS "tags",
    array_agg(distinct "artists_artist-tags"."artist-tag-id") AS "tag-ids",
    json_agg(distinct "nationalities") AS "nationalities",
    array_agg(distinct "artists_nationalities"."nationality-id") AS "nationality-ids",
    json_agg(distinct "sculptures") AS "sculptures",
    array_agg(distinct "sculptures".id) AS "sculpture-ids",
    count("artists_sculptures"."sculpture-id") AS "sculpture-count"
  FROM
    artists
  LEFT JOIN "artists_sculptures" ON "artists_sculptures"."artist-id" = "artists".id
  LEFT JOIN "sculptures" ON "sculptures".id = "artists_sculptures"."sculpture-id"
  LEFT JOIN "artists_artist-tags" ON "artists_artist-tags"."artist-id" = "artists".id
  LEFT JOIN "artists_nationalities" ON "artists_nationalities"."artist-id" = "artists".id
  LEFT JOIN "nationalities" ON "artists_nationalities"."nationality-id" = "nationalities".id
  LEFT JOIN "artist-tags" ON "artists_artist-tags"."artist-tag-id" = "artist-tags".id
  GROUP BY
    artists.id
);

DROP VIEW IF EXISTS sculptures_with_related_ids;

CREATE VIEW sculptures_with_related_ids AS (
  SELECT
    sculptures.*,
    array_agg(distinct "materials_sculptures"."material-id") AS "material-ids",
    array_agg(distinct "artists_sculptures"."artist-id") AS "artist-ids",
    array_agg(distinct "sculptures_sculpture-tags"."sculpture-tag-id") AS "tag-ids",
    array_agg(distinct "regions".id) AS "region-ids",
    array_agg(distinct "nearby-regions".id) AS "nearby-region-ids"
  FROM
    sculptures
  LEFT JOIN materials_sculptures ON "materials_sculptures"."sculpture-id" = "sculptures".id
  LEFT JOIN artists_sculptures ON "artists_sculptures"."sculpture-id" = "sculptures".id
  LEFT JOIN "sculptures_sculpture-tags" ON "sculptures_sculpture-tags"."sculpture-id" = "sculptures".id
  LEFT JOIN "regions" ON ST_Covers(regions.shape, sculptures.location)
  LEFT JOIN "regions" AS "nearby-regions" ON ST_DWithin("nearby-regions".shape, sculptures.location, 100) AND ST_Distance("nearby-regions".shape, sculptures.location) > 0
  GROUP BY
    sculptures.id
);

DROP VIEW IF EXISTS regions_with_related_ids;

CREATE VIEW regions_with_related_ids AS (
  SELECT
    regions.*,
    ST_AsGeoJSON(regions.shape) AS geojson,
    ROUND(ST_Area(regions.shape) / 1000) AS area,
    ST_NPoints(regions.shape::geometry) AS "points-count",
    array_agg(distinct "regions_region-tags"."region-tag-id") AS "tag-ids",
    array_agg(distinct "sculptures".id) AS "sculpture-ids",
    count(distinct "sculptures".id) AS "sculpture-count",
    -- oddly, this is much more performant than putting it in the GROUP BY
    (array_agg("parents".id))[1] AS "parent-id"
  FROM
    regions
  LEFT JOIN "regions_region-tags" ON "regions_region-tags"."region-id" = "regions".id
  LEFT JOIN "sculptures" ON ST_DWithin("regions".shape, sculptures.location, 100)
  LEFT JOIN "regions" AS "parents" ON  "parents".id = (
    SELECT "ancestors".id
    FROM "regions" AS "ancestors"
    WHERE
      "regions".id != "ancestors".id AND
      "regions".shape::geometry @ "ancestors".shape::geometry AND
      ST_CoveredBy(ST_Centroid("regions".shape::geometry),"ancestors".shape)
      -- technically, more exact, but slower, and above is good enough
      --ST_Covers("ancestors".shape::geometry,"regions".shape::geometry)
    ORDER BY
      ST_Area("ancestors".shape::geometry)
    LIMIT 1
)
  GROUP BY
    regions.id
);

DROP VIEW IF EXISTS materials_with_related_ids;

CREATE VIEW materials_with_related_ids AS (
  SELECT
    materials.*,
    array_agg(distinct "materials_sculptures"."sculpture-id") AS "sculpture-ids"
  FROM
    materials
  LEFT JOIN "materials_sculptures" ON "materials_sculptures"."material-id" = "materials".id
  GROUP BY
    materials.id
);

DROP VIEW IF EXISTS "sculpture-tags_with_counts";

CREATE VIEW "sculpture-tags_with_counts" AS (
  SELECT
    "sculpture-tags".*,
    count("sculptures_sculpture-tags"."sculpture-id") AS "sculpture-count"
  FROM
    "sculpture-tags"
  LEFT JOIN "sculptures_sculpture-tags" ON "sculptures_sculpture-tags"."sculpture-tag-id" = "sculpture-tags".id
  GROUP BY
    "sculpture-tags".id
);



COMMIT;
