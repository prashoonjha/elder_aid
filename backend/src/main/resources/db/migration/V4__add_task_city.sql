-- V4: a separate city column lets the browse listing show a general area
-- ("Helsinki", "Turku") to workers without exposing the exact address_line
-- before a booking is actually confirmed.
ALTER TABLE task_requests ADD COLUMN city VARCHAR(100);
