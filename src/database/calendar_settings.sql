-- Add calendar_event_id to todo table
ALTER TABLE todo ADD COLUMN IF NOT EXISTS calendar_event_id VARCHAR(255);

-- User calendar settings table
CREATE TABLE IF NOT EXISTS user_calendar_settings (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    calendar_sync_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    refresh_token VARCHAR(2048),
    access_token  VARCHAR(2048),
    token_expires_at TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_calendar_settings_user_id ON user_calendar_settings(user_id);
