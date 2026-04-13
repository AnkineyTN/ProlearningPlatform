ALTER TABLE note ADD COLUMN IF NOT EXISTS yjs_state BYTEA;

CREATE TABLE IF NOT EXISTS note_members (
    id         BIGSERIAL PRIMARY KEY,
    note_id    BIGINT NOT NULL REFERENCES note(id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER')),
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
               CHECK (status IN ('PENDING', 'ACTIVE', 'DECLINED')),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (note_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_note_members_note_id ON note_members(note_id);
CREATE INDEX IF NOT EXISTS idx_note_members_user_id ON note_members(user_id);
CREATE INDEX IF NOT EXISTS idx_note_members_user_status ON note_members(user_id, status);

INSERT INTO note_members (note_id, user_id, role, status, created_at)
SELECT id, id_user, 'OWNER', 'ACTIVE', NOW()
FROM note
WHERE id_user IS NOT NULL
ON CONFLICT (note_id, user_id) DO NOTHING;

-- SELECT * FROM note_members

ALTER TABLE note_members
    ADD COLUMN invite_token VARCHAR(64) UNIQUE,
    ADD COLUMN invite_token_expires_at TIMESTAMP,
    ADD COLUMN invited_email VARCHAR(255); -- dùng khi invite email chưa có tài khoản