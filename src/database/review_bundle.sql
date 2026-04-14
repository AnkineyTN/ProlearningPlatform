-- Migration: Review Bundle feature
-- Allows nullable set_id for REVIEW_AI flashcards/exams,
-- adds create_method to exams, and creates review_bundles table.

ALTER TABLE flashcard ALTER COLUMN id_set SET NOT NULL;

ALTER TABLE exams ALTER COLUMN set_id SET NOT NULL;
ALTER TABLE exams DROP CONSTRAINT fk_exam_set;
ALTER TABLE exams ADD CONSTRAINT fk_exam_set
    FOREIGN KEY (set_id) REFERENCES set(id) ON DELETE CASCADE;

ALTER TABLE exams ADD COLUMN create_method VARCHAR(50) NOT NULL DEFAULT 'MANUAL';

CREATE TABLE review_bundles (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT                      NOT NULL,
    notification_id BIGINT,
    card_ids        JSONB                        NOT NULL DEFAULT '[]',
    period_from     TIMESTAMP WITH TIME ZONE     NOT NULL,
    period_to       TIMESTAMP WITH TIME ZONE     NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT NOW(),
    set_id          BIGINT                       NOT NULL,

    CONSTRAINT fk_review_bundle_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_bundle_notification
        FOREIGN KEY (notification_id) REFERENCES notification(id) ON DELETE SET NULL,
    CONSTRAINT fk_review_bundle_set
        FOREIGN KEY (set_id) REFERENCES set(id) ON DELETE CASCADE
);

CREATE INDEX idx_review_bundles_user_id ON review_bundles(user_id);
CREATE INDEX idx_review_bundles_set_id  ON review_bundles(set_id);
