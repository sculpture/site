-- :name -simplify-geojson
-- :result :one
SELECT
ST_AsGeoJSON(
  ST_SimplifyPreserveTopology(
    ST_ConcaveHull(
      ST_GeomFromGeoJSON(
        :geojson
      ),
      0.99
    ),
    0.001
  ),
  5
) AS geojson;

