-- V6: a worker who gets rejected needs to know why, otherwise they have no
-- way to fix their resubmission. Free text rather than an enum since the
-- reasons are too varied to usefully categorize ("photo is blurry",
-- "document expired", "name doesn't match account", etc.).
ALTER TABLE verification_documents ADD COLUMN rejection_reason TEXT;
