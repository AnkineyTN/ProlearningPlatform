-- Migration: catch-up tracking for hourly reminder schedulers.
-- Each column stores the date the corresponding reminder last fired for the user,
-- so an hourly tick can re-fire a reminder missed while the JVM was off.

ALTER TABLE notification_preference
    ADD COLUMN IF NOT EXISTS last_daily_todo_reminder_at    DATE,
    ADD COLUMN IF NOT EXISTS last_weekly_todo_reminder_at   DATE,
    ADD COLUMN IF NOT EXISTS last_goal_deadline_reminder_at DATE,
    ADD COLUMN IF NOT EXISTS last_goal_inactive_reminder_at DATE;
