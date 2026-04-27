-- Add streak/timezone fields to users
ALTER TABLE users ADD COLUMN IF NOT EXISTS streak_freeze_tokens INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh';

-- Enum for content type
DO $$ BEGIN
    CREATE TYPE activity_content_type AS ENUM ('NOTE', 'FLASHCARD', 'EXAM', 'EDIT');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS activity_log (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date           DATE NOT NULL,
    content_type   activity_content_type NOT NULL,
    set_id         BIGINT NULL,
    todo_id        BIGINT NULL,
    active_duration BIGINT NOT NULL DEFAULT 0,
    raw_duration   BIGINT NOT NULL DEFAULT 0,
    score          INT NULL CHECK (score >= 0 AND score <= 100),
    items_count    INT NOT NULL DEFAULT 0,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_activity_log_user_date ON activity_log(user_id, date);
CREATE INDEX IF NOT EXISTS idx_activity_log_user_content_type ON activity_log(user_id, content_type);
