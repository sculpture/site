-- :name -drop!
-- :command :execute
--

BEGIN;

DROP SCHEMA public CASCADE;

CREATE SCHEMA public;

COMMIT;
