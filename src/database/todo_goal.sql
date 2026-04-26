-- Migration: Todo & Goal feature

-- Enum: goal_status (skip if already exists)
CREATE TYPE goal_status AS ENUM ('IN_PROGRESS', 'COMPLETED', 'ARCHIVED');

-- Enum: todo_priority (skip if already exists)
CREATE TYPE todo_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');

-- Table: goal
CREATE TABLE IF NOT EXISTS goal (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_goal_id BIGINT REFERENCES goal(id) ON DELETE SET NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT,
    target_date    DATE,
    color          VARCHAR(10),
    status         VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    type           VARCHAR(10) NOT NULL DEFAULT 'LONG',
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_goal_user_id       ON goal(user_id);
CREATE INDEX IF NOT EXISTS idx_goal_user_status   ON goal(user_id, status);
CREATE INDEX IF NOT EXISTS idx_goal_user_type     ON goal(user_id, type);
CREATE INDEX IF NOT EXISTS idx_goal_parent        ON goal(parent_goal_id);

-- Table: todo
CREATE TABLE IF NOT EXISTS todo (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    goal_id        BIGINT REFERENCES goal(id) ON DELETE SET NULL,
    title          VARCHAR(500) NOT NULL,
    description    TEXT,
    completed      BOOLEAN NOT NULL DEFAULT FALSE,
    priority       VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    due_date       DATE,
    completed_at   TIMESTAMP WITH TIME ZONE,
    type           VARCHAR(10) NOT NULL DEFAULT 'DAILY',
    status         VARCHAR(10) NOT NULL DEFAULT 'TODO',
    set_refs       TEXT,
    note_refs      TEXT,
    flashcard_refs TEXT,
    exam_refs      TEXT,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_todo_user_id        ON todo(user_id);
CREATE INDEX IF NOT EXISTS idx_todo_user_goal      ON todo(user_id, goal_id);
CREATE INDEX IF NOT EXISTS idx_todo_user_completed ON todo(user_id, completed);
CREATE INDEX IF NOT EXISTS idx_todo_goal_id        ON todo(goal_id);
CREATE INDEX IF NOT EXISTS idx_todo_user_type      ON todo(user_id, type);
CREATE INDEX IF NOT EXISTS idx_todo_user_status    ON todo(user_id, status);

-- ── Migration for existing installations ─────────────────────────────────────
-- Run these ALTER TABLE statements if the tables already exist:

ALTER TABLE goal ADD COLUMN IF NOT EXISTS type           VARCHAR(10) NOT NULL DEFAULT 'LONG';
ALTER TABLE goal ADD COLUMN IF NOT EXISTS parent_goal_id BIGINT REFERENCES goal(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_goal_user_type  ON goal(user_id, type);
CREATE INDEX IF NOT EXISTS idx_goal_parent     ON goal(parent_goal_id);

ALTER TABLE todo ADD COLUMN IF NOT EXISTS type           VARCHAR(10) NOT NULL DEFAULT 'DAILY';
ALTER TABLE todo ADD COLUMN IF NOT EXISTS status         VARCHAR(10) NOT NULL DEFAULT 'TODO';
ALTER TABLE todo ADD COLUMN IF NOT EXISTS set_refs       TEXT;
ALTER TABLE todo ADD COLUMN IF NOT EXISTS note_refs      TEXT;
ALTER TABLE todo ADD COLUMN IF NOT EXISTS flashcard_refs TEXT;
ALTER TABLE todo ADD COLUMN IF NOT EXISTS exam_refs      TEXT;
CREATE INDEX IF NOT EXISTS idx_todo_user_type   ON todo(user_id, type);
CREATE INDEX IF NOT EXISTS idx_todo_user_status ON todo(user_id, status);
