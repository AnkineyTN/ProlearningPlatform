-- Fix: add ON DELETE CASCADE to FK constraints referencing users(id)
-- Without these, deleting a user fails if they have notifications, study sessions, roadmaps, or payment records.

ALTER TABLE notification
    DROP CONSTRAINT IF EXISTS "FK_NOTIFICATION_ON_USER",
    ADD CONSTRAINT "FK_NOTIFICATION_ON_USER"
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE study_session
    DROP CONSTRAINT IF EXISTS study_session_user_id_fkey,
    ADD CONSTRAINT study_session_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE roadmaps
    DROP CONSTRAINT IF EXISTS roadmaps_user_id_fkey,
    ADD CONSTRAINT roadmaps_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE payment_transactions
    DROP CONSTRAINT IF EXISTS payment_transactions_user_id_fkey,
    ADD CONSTRAINT payment_transactions_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
