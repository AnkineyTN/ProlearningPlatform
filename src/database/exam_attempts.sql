-- Migration: exam attempts and answers for exam-taking feature
-- Supports: attempt history, per-exam leaderboard/ranking

CREATE TYPE exam_attempt_status AS ENUM ('IN_PROGRESS', 'SUBMITTED');

CREATE TABLE exam_attempts (
    id           BIGSERIAL PRIMARY KEY,
    exam_id      BIGINT                      NOT NULL,
    user_id      BIGINT                      NOT NULL,
    status       exam_attempt_status         NOT NULL DEFAULT 'IN_PROGRESS',
    started_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deadline_at  TIMESTAMP WITHOUT TIME ZONE,
    submitted_at TIMESTAMP WITHOUT TIME ZONE,
    score        NUMERIC(5, 2),
    total_points INTEGER                     NOT NULL DEFAULT 0,

    CONSTRAINT fk_attempt_exam
        FOREIGN KEY (exam_id)
            REFERENCES exams (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_attempt_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_exam_attempts_user_id ON exam_attempts (user_id);

CREATE INDEX idx_exam_attempts_ranking
    ON exam_attempts (exam_id, score DESC NULLS LAST)
    WHERE status = 'SUBMITTED';

CREATE INDEX idx_exam_attempts_exam_user ON exam_attempts (exam_id, user_id);

-- -------------------------------------------------------

CREATE TABLE exam_answers (
    id                 BIGSERIAL PRIMARY KEY,
    attempt_id         BIGINT NOT NULL,
    question_id        BIGINT NOT NULL,
    selected_option_id BIGINT,
    essay_answer       TEXT,
    is_correct         BOOLEAN,

    CONSTRAINT fk_answer_attempt
        FOREIGN KEY (attempt_id)
            REFERENCES exam_attempts (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_answer_question
        FOREIGN KEY (question_id)
            REFERENCES questions (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_answer_option
        FOREIGN KEY (selected_option_id)
            REFERENCES question_options (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_exam_answers_attempt_id ON exam_answers (attempt_id);
