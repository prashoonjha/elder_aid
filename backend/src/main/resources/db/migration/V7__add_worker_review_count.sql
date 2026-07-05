-- A worker can complete a task without receiving a review, so the number of
-- reviews is genuinely different from completed_tasks_count and gets its own
-- column. Both average_rating and review_count are recalculated from the
-- reviews table (the source of truth) whenever a new review lands.
ALTER TABLE worker_profiles
    ADD COLUMN review_count INTEGER NOT NULL DEFAULT 0;
