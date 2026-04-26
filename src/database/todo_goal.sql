-- Migration: Todo & Goal feature

-- Enum: goal_status (skip if already exists)
CREATE TYPE goal_status AS ENUM ('IN_PROGRESS', 'COMPLETED', 'ARCHIVED');

-- Enum: todo_priority (skip if already exists)
CREATE TYPE todo_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');

-- Table: goal
CREATE TABLE IF NOT EXISTS goal (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    target_date DATE,
    color       VARCHAR(10),
    status      VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_goal_user_id    ON goal(user_id);
CREATE INDEX IF NOT EXISTS idx_goal_user_status ON goal(user_id, status);

-- Table: todo
CREATE TABLE IF NOT EXISTS todo (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    goal_id      BIGINT REFERENCES goal(id) ON DELETE SET NULL,
    title        VARCHAR(500) NOT NULL,
    description  TEXT,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    priority     VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    due_date     DATE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_todo_user_id        ON todo(user_id);
CREATE INDEX IF NOT EXISTS idx_todo_user_goal      ON todo(user_id, goal_id);
CREATE INDEX IF NOT EXISTS idx_todo_user_completed ON todo(user_id, completed);
CREATE INDEX IF NOT EXISTS idx_todo_goal_id        ON todo(goal_id);
