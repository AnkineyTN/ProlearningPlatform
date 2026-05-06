CREATE TABLE set_notification_preference (
    id                          BIGSERIAL   PRIMARY KEY,
    set_id                      BIGINT      NOT NULL UNIQUE REFERENCES set(id) ON DELETE CASCADE,
    weekly_summary_enabled      BOOLEAN     NOT NULL DEFAULT TRUE,
    weekly_summary_day          SMALLINT    NOT NULL DEFAULT 7
        CHECK (weekly_summary_day BETWEEN 1 AND 7),
    last_summary_sent_at        TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_set_noti_pref_weekly_day
    ON set_notification_preference(weekly_summary_day)
    WHERE weekly_summary_enabled = TRUE;
