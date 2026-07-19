-- Simplify worker verification: drop the tier ladder and the criminal-record
-- extract (special-category GDPR data a marketplace shouldn't store as files).
-- verification_tier (NONE / TIER1_ID_VERIFIED / TIER2_BACKGROUND_CHECKED)
-- becomes verification_status (UNVERIFIED / PENDING / VERIFIED).

ALTER TABLE worker_profiles RENAME COLUMN verification_tier TO verification_status;

-- Map existing tier values onto the new status. Any tier above NONE means the
-- worker had at least ID verification, which is now simply "verified".
UPDATE worker_profiles SET verification_status = 'UNVERIFIED' WHERE verification_status = 'NONE';
UPDATE worker_profiles SET verification_status = 'VERIFIED'
    WHERE verification_status IN ('TIER1_ID_VERIFIED', 'TIER2_BACKGROUND_CHECKED');

-- Remove any uploaded criminal-record extracts; the type no longer exists.
DELETE FROM verification_documents WHERE document_type = 'CRIMINAL_RECORD_EXTRACT';

-- The index survives the column rename but keeps its old name; rename it to match.
ALTER INDEX idx_worker_profiles_verification_tier RENAME TO idx_worker_profiles_verification_status;
