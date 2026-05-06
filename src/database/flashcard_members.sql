-- Migration: Add Flashcard Members Support (Chia sẻ cộng tác cho Flashcard)
CREATE TABLE IF NOT EXISTS flashcard_members (
    id         BIGSERIAL PRIMARY KEY,
    flashcard_id BIGINT NOT NULL REFERENCES flashcard(id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER')),
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
               CHECK (status IN ('PENDING', 'ACTIVE', 'DECLINED')),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (flashcard_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_flashcard_members_flashcard_id ON flashcard_members(flashcard_id);
CREATE INDEX IF NOT EXISTS idx_flashcard_members_user_id ON flashcard_members(user_id);
CREATE INDEX IF NOT EXISTS idx_flashcard_members_user_status ON flashcard_members(user_id, status);

-- Populate owners: Thêm chủ sở hữu hiện tại vào bảng flashcard_members
INSERT INTO flashcard_members (flashcard_id, user_id, role, status, created_at)
SELECT id, id_user, 'OWNER', 'ACTIVE', NOW()
FROM flashcard
WHERE id_user IS NOT NULL
ON CONFLICT (flashcard_id, user_id) DO NOTHING;

-- Add columns for invite token support
ALTER TABLE flashcard_members
    ADD COLUMN IF NOT EXISTS invite_token VARCHAR(64) UNIQUE,
    ADD COLUMN IF NOT EXISTS invite_token_expires_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS invited_email VARCHAR(255); -- dùng khi invite email chưa có tài khoản

-- SELECT * FROM flashcard_members
