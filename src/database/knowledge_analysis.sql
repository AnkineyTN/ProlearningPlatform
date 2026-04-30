ALTER TABLE card_item ADD COLUMN IF NOT EXISTS topic VARCHAR(255) NULL;
ALTER TABLE questions ADD COLUMN IF NOT EXISTS topic VARCHAR(255) NULL;

CREATE INDEX IF NOT EXISTS idx_card_item_topic ON card_item(topic) WHERE topic IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_questions_topic  ON questions(topic)  WHERE topic IS NOT NULL;

CREATE TABLE IF NOT EXISTS knowledge_analysis (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source_type           VARCHAR(10) NOT NULL,
    source_id             BIGINT      NOT NULL,
    session_ref_id        BIGINT      NULL,
    topic_accuracies      JSONB       NOT NULL,
    strengths             TEXT        NOT NULL,
    weaknesses            TEXT        NOT NULL,
    improvements          TEXT        NOT NULL,
    contributing_sources  JSONB       NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_ka_session_ref UNIQUE (session_ref_id, source_type)
);

CREATE INDEX IF NOT EXISTS idx_ka_user_source ON knowledge_analysis(user_id, source_type, source_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_ka_user_created ON knowledge_analysis(user_id, created_at DESC);
