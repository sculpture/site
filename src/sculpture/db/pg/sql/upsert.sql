-- :name -upsert-sculpture!
-- :command :returning-execute
INSERT INTO sculpture (id, location, location_precision)
VALUES (:id, ST_Point(:location-lng, :location-lat), :location-precision)
ON CONFLICT (id) DO
UPDATE
SET
  location = ST_Point(:location-lng, :location-lat),
  location_precision = :location-precision
WHERE sculpture.id = :id
RETURNING true;

-- :name -upsert-region!
-- :command :returning-execute
INSERT INTO region (id, shape)
VALUES (:id, ST_GeomFromGeoJSON(:geojson))
ON CONFLICT (id) DO
UPDATE
SET
  shape = ST_GeomFromGeoJSON(:geojson)
WHERE region.id = :id
RETURNING true;
