-- V1: core schema for the ElderAid platform
-- Requires PostgreSQL 13+ (gen_random_uuid() is built into core from v13).

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    locale VARCHAR(5) NOT NULL DEFAULT 'fi',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE TABLE elderly_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    address_line VARCHAR(255),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    preferred_language VARCHAR(5) NOT NULL DEFAULT 'fi',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE family_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    elderly_profile_id UUID NOT NULL REFERENCES elderly_profiles(id) ON DELETE CASCADE,
    relationship VARCHAR(50) NOT NULL,
    permission_level VARCHAR(30) NOT NULL DEFAULT 'FULL',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (family_user_id, elderly_profile_id)
);

CREATE TABLE worker_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    profile_photo_url VARCHAR(500),
    verification_tier VARCHAR(30) NOT NULL DEFAULT 'NONE',
    stripe_account_id VARCHAR(255),
    average_rating NUMERIC(3,2) NOT NULL DEFAULT 0,
    completed_tasks_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE verification_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    worker_profile_id UUID NOT NULL REFERENCES worker_profiles(id) ON DELETE CASCADE,
    document_type VARCHAR(30) NOT NULL,
    file_storage_key VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE task_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    elderly_profile_id UUID NOT NULL REFERENCES elderly_profiles(id) ON DELETE CASCADE,
    posted_by_user_id UUID NOT NULL REFERENCES users(id),
    category VARCHAR(50) NOT NULL,
    description TEXT,
    location_lat DOUBLE PRECISION,
    location_lng DOUBLE PRECISION,
    address_line VARCHAR(255),
    scheduled_start TIMESTAMPTZ NOT NULL,
    scheduled_end TIMESTAMPTZ NOT NULL,
    price_offered NUMERIC(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE task_applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_request_id UUID NOT NULL REFERENCES task_requests(id) ON DELETE CASCADE,
    worker_profile_id UUID NOT NULL REFERENCES worker_profiles(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    message TEXT,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (task_request_id, worker_profile_id)
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_request_id UUID NOT NULL UNIQUE REFERENCES task_requests(id),
    worker_profile_id UUID NOT NULL REFERENCES worker_profiles(id),
    status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    check_in_time TIMESTAMPTZ,
    check_out_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL UNIQUE REFERENCES bookings(id),
    amount_total NUMERIC(10,2) NOT NULL,
    platform_commission NUMERIC(10,2) NOT NULL,
    worker_payout NUMERIC(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    stripe_payment_intent_id VARCHAR(255),
    stripe_transfer_id VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES bookings(id),
    rater_user_id UUID NOT NULL REFERENCES users(id),
    rated_user_id UUID NOT NULL REFERENCES users(id),
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE consent_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    consent_type VARCHAR(30) NOT NULL,
    given BOOLEAN NOT NULL,
    policy_version VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Indexes for the lookups the application will do most often
CREATE INDEX idx_task_requests_status ON task_requests(status);
CREATE INDEX idx_task_requests_scheduled_start ON task_requests(scheduled_start);
CREATE INDEX idx_worker_profiles_verification_tier ON worker_profiles(verification_tier);
CREATE INDEX idx_task_applications_status ON task_applications(status);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_family_links_family_user ON family_links(family_user_id);
CREATE INDEX idx_elderly_profiles_user ON elderly_profiles(user_id);
