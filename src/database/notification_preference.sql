CREATE TABLE notification_preference (
    id                              BIGSERIAL       PRIMARY KEY,
    id_user                         BIGINT          NOT NULL UNIQUE
     REFERENCES users(id) ON DELETE CASCADE,

    due_card_reminder_enabled       BOOLEAN         NOT NULL DEFAULT TRUE,
    system_announcement_enabled     BOOLEAN         NOT NULL DEFAULT TRUE,
    account_activity_enabled        BOOLEAN         NOT NULL DEFAULT TRUE,

    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
