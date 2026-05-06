CREATE TABLE flashcard_game_history (
    id               BIGSERIAL PRIMARY KEY,
    flashcard_id     BIGINT  NOT NULL REFERENCES flashcard(id) ON DELETE CASCADE,
    user_id          BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_cards      INTEGER NOT NULL,
    duration_seconds INTEGER NOT NULL,
    completed_at     TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_game_history_ranking ON flashcard_game_history (flashcard_id, duration_seconds ASC);
CREATE INDEX idx_game_history_user    ON flashcard_game_history (user_id, flashcard_id, completed_at DESC);

CREATE TABLE flashcard_game_wrong_card (
                                           history_id   BIGINT  NOT NULL REFERENCES flashcard_game_history(id) ON DELETE CASCADE,
                                           card_item_id BIGINT  NOT NULL REFERENCES card_item(id) ON DELETE CASCADE,
                                           wrong_count  INTEGER NOT NULL DEFAULT 1,
                                           PRIMARY KEY (history_id, card_item_id)
);
CREATE INDEX idx_game_wrong_card_history ON flashcard_game_wrong_card(history_id);
