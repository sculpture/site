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
    json_agg(regions ORDER BY ST_Area(ST_Envelope(regions.shape::geometry)) ASC) AS "regions",
    json_agg("nearby-regions" ORDER BY ST_Distance("nearby-regions".shape, sculptures.location) ASC) AS "regions-nearby"
  FROM
    sculptures
  LEFT JOIN artists_sculptures ON artists_sculptures."sculpture-id" = sculptures.id
  LEFT JOIN artists ON artists_sculptures."artist-id" = artists.id
  LEFT JOIN materials_sculptures ON materials_sculptures."sculpture-id" = sculptures.id
  LEFT JOIN regions ON ST_Covers(regions.shape, sculptures.location)
  LEFT JOIN regions AS "nearby-regions" ON ST_DWithin("nearby-regions".shape, sculptures.location, 100) AND ST_Distance("nearby-regions".shape, sculptures.location) > 0
  LEFT JOIN photos ON photos."sculpture-id" = sculptures.id
  LEFT JOIN materials ON materials_sculptures."material-id" = materials.id
  GROUP BY
    sculptures.id
);

DROP VIEW IF EXISTS artists_with_related_ids;

CREATE VIEW artists_with_related_ids AS (
  SELECT
    artists.*,
    array_agg(distinct "artists_artist-tags"."artist-tag-id") AS "tag-ids"
  FROM
    artists
  LEFT JOIN "artists_artist-tags" ON "artists_artist-tags"."artist-id" = "artists".id
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

COMMIT;
