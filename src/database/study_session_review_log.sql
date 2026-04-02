CREATE TABLE study_session_review_log (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT      NOT NULL REFERENCES study_session(id) ON DELETE CASCADE,
    card_id     BIGINT      NOT NULL REFERENCES card_item(id) ON DELETE CASCADE,
    is_known    BOOLEAN     NOT NULL,
    reviewed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_review_log_session FOREIGN KEY (session_id) REFERENCES study_session(id),
    CONSTRAINT fk_review_log_card    FOREIGN KEY (card_id)    REFERENCES card_item(id)
);

CREATE INDEX idx_review_log_session_id ON study_session_review_log(session_id);

CREATE INDEX idx_review_log_known_date ON study_session_review_log(is_known, reviewed_at);
