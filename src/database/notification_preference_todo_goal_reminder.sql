-- Migration: Todo & Goal deadline reminder preferences

ALTER TABLE notification_preference
    ADD COLUMN IF NOT EXISTS daily_todo_reminder_enabled   BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS daily_todo_reminder_hour      INT     NOT NULL DEFAULT 20,
    ADD COLUMN IF NOT EXISTS weekly_todo_reminder_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS weekly_todo_reminder_hour     INT     NOT NULL DEFAULT 20,
    ADD COLUMN IF NOT EXISTS goal_deadline_reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS goal_inactive_reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS goal_reminder_hour            INT     NOT NULL DEFAULT 9;
