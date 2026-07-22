-- Emails are now normalised (lower-cased, trimmed) on register and login, so
-- existing rows need the same treatment or those accounts can no longer log in.
UPDATE app_users SET email = lower(trim(email)) WHERE email <> lower(trim(email));
