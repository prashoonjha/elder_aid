-- City is now title-cased on save; bring existing rows in line so old and new
-- tasks display consistently. initcap handles the per-word capitalisation.
UPDATE task_requests SET city = initcap(trim(city)) WHERE city IS NOT NULL AND city <> initcap(trim(city));
UPDATE elderly_profiles SET city = initcap(trim(city)) WHERE city IS NOT NULL AND city <> initcap(trim(city));
