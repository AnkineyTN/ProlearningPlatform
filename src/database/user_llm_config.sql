-- User BYOK LLM configuration
-- Each user can store multiple configs, but only 1 config can be active at a time.
CREATE TABLE IF NOT EXISTS user_llm_config (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    display_name      VARCHAR(64),
    provider          VARCHAR(32)  NOT NULL,          -- enum name: OPENAI | ANTHROPIC | GOOGLE | GROQ
    model             VARCHAR(128) NOT NULL,
    api_key_encrypted VARCHAR(1024) NOT NULL,         -- Base64( IV || AES-256-GCM ciphertext || tag )
    api_key_last4     VARCHAR(8),                      -- non-secret, used to mask when displaying
    is_active         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_llm_config_user_id ON user_llm_config(user_id);

-- Ensure at most 1 active config per user (Postgres partial unique index)
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_llm_config_active
    ON user_llm_config(user_id) WHERE is_active = TRUE;
