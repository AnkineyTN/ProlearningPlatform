-- Feature: User Avatar
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(1024);

-- Feature: Admin Block User
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_blocked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS block_reason TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS blocked_at TIMESTAMP WITH TIME ZONE;

-- Feature: User Appeal
CREATE TABLE IF NOT EXISTS user_appeal (
    id           BIGSERIAL PRIMARY KEY,
    id_user      BIGINT REFERENCES users(id) ON DELETE SET NULL,
    email        VARCHAR(255) NOT NULL,
    reason       TEXT NOT NULL,
    status       VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    admin_note   TEXT,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_user_appeal_status ON user_appeal(status);
CREATE INDEX IF NOT EXISTS idx_user_appeal_user   ON user_appeal(id_user);
