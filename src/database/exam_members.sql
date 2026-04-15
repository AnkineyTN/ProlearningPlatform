CREATE TABLE IF NOT EXISTS exam_members (
    id         BIGSERIAL PRIMARY KEY,
    exam_id    BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER')),
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
               CHECK (status IN ('PENDING', 'ACTIVE', 'DECLINED')),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (exam_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_members_exam_id ON exam_members(exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_members_user_id ON exam_members(user_id);
CREATE INDEX IF NOT EXISTS idx_exam_members_user_status ON exam_members(user_id, status);

-- Populate owners: Thêm chủ sở hữu hiện tại vào bảng exam_members
INSERT INTO exam_members (exam_id, user_id, role, status, created_at)
SELECT id, id_user, 'OWNER', 'ACTIVE', NOW()
FROM exams
WHERE id_user IS NOT NULL
ON CONFLICT (exam_id, user_id) DO NOTHING;

-- Add columns for invite token support
ALTER TABLE exam_members
    ADD COLUMN IF NOT EXISTS invite_token VARCHAR(64) UNIQUE,
    ADD COLUMN IF NOT EXISTS invite_token_expires_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS invited_email VARCHAR(255); -- dùng khi invite email chưa có tài khoản

-- SELECT * FROM exam_members
