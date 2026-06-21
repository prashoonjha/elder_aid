-- V5: rating was declared SMALLINT in V1, but the Review entity uses
-- Integer, which Hibernate maps to INTEGER by default. Schema validation
-- correctly caught the mismatch on startup - widening the column to match.
ALTER TABLE reviews ALTER COLUMN rating TYPE INTEGER;
