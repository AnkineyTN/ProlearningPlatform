CREATE TABLE IF NOT EXISTS user_favorite_resources (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    resource_id   BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL, -- 'NOTE', 'FLASHCARD', 'EXAM'
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_favorite_resource UNIQUE (user_id, resource_id, resource_type)
);

CREATE INDEX IF NOT EXISTS idx_favorite_user_resource ON user_favorite_resources(user_id, resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_favorite_created_at ON user_favorite_resources(created_at);
