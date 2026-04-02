CREATE TABLE notification_preference (
    id                          BIGSERIAL       PRIMARY KEY,
    id_user                     BIGINT          NOT NULL UNIQUE
     REFERENCES users(id) ON DELETE CASCADE,

    due_card_reminder_enabled   BOOLEAN         NOT NULL DEFAULT TRUE,
    weekly_summary_enabled      BOOLEAN         NOT NULL DEFAULT TRUE,
    weekly_summary_day          SMALLINT        NOT NULL DEFAULT 7
     CHECK (weekly_summary_day BETWEEN 1 AND 7),

    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    last_summary_sent_at        TIMESTAMPTZ
);

CREATE INDEX idx_noti_pref_weekly_day
    ON notification_preference(weekly_summary_day)
    WHERE weekly_summary_enabled = TRUE;