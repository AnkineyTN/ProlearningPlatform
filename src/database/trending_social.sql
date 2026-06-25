-- Bảng tracking lượt xem tài nguyên phục vụ cho việc tính toán trending
CREATE TABLE IF NOT EXISTS resource_view_log (
    id            BIGSERIAL PRIMARY KEY,
    resource_id   BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL, -- 'NOTE', 'FLASHCARD', 'EXAM'
    viewer_ip     VARCHAR(45) NULL,     -- IPv4 hoặc IPv6
    user_id       BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    viewed_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rvl_resource ON resource_view_log(resource_id, resource_type);
CREATE INDEX IF NOT EXISTS idx_rvl_viewed_at ON resource_view_log(viewed_at);
CREATE INDEX IF NOT EXISTS idx_rvl_resource_time ON resource_view_log(resource_id, resource_type, viewed_at);

CREATE INDEX IF NOT EXISTS idx_activity_log_set_id ON activity_log(set_id) WHERE set_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_activity_log_created_at ON activity_log(created_at);
