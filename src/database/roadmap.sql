CREATE TABLE roadmaps (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL REFERENCES users (id),
    title            VARCHAR(255) NOT NULL,
    overview         TEXT,
    estimated_total_hours INTEGER,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_roadmaps_user_id ON roadmaps (user_id);

CREATE TABLE roadmap_chapters (
    id          BIGSERIAL PRIMARY KEY,
    roadmap_id  BIGINT      NOT NULL REFERENCES roadmaps (id) ON DELETE CASCADE,
    chapter_key VARCHAR(50) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    objective   TEXT,
    order_index INTEGER     NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'LOCKED',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_roadmap_chapters_roadmap_id ON roadmap_chapters (roadmap_id);

CREATE TABLE roadmap_topics (
    id             BIGSERIAL PRIMARY KEY,
    chapter_id     BIGINT      NOT NULL REFERENCES roadmap_chapters (id) ON DELETE CASCADE,
    topic_key      VARCHAR(50) NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    order_index    INTEGER     NOT NULL,
    is_completed   BOOLEAN     NOT NULL DEFAULT FALSE,
    content_status VARCHAR(20) NOT NULL DEFAULT 'IDLE',
    summary        TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_roadmap_topics_chapter_id ON roadmap_topics (chapter_id);


