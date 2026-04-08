-- Region comments on PDF / image attachments (percent-based rect, 0–100).
-- Apply manually when ddl-auto is none.

CREATE TABLE IF NOT EXISTS note_file_region_comment
(
    id                 BIGSERIAL PRIMARY KEY,
    id_note            BIGINT                   NOT NULL REFERENCES note (id) ON DELETE CASCADE,
    id_asset           BIGINT                   NOT NULL REFERENCES asset (id) ON DELETE CASCADE,
    attachment_kind    VARCHAR(16)              NOT NULL,
    page_number        INTEGER                  NOT NULL,
    rect_x             DOUBLE PRECISION         NOT NULL,
    rect_y             DOUBLE PRECISION         NOT NULL,
    rect_width         DOUBLE PRECISION         NOT NULL,
    rect_height        DOUBLE PRECISION         NOT NULL,
    content            TEXT                     NOT NULL,
    client_comment_id  VARCHAR(64),
    id_user            BIGINT                   NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at         TIMESTAMP WITH TIME ZONE,
    updated_at         TIMESTAMP WITH TIME ZONE,

    CONSTRAINT chk_nfrc_attachment_kind CHECK (attachment_kind IN ('DOC', 'IMAGE')),
    CONSTRAINT chk_nfrc_page_positive CHECK (page_number >= 1),
    CONSTRAINT chk_nfrc_rect_dims CHECK (rect_width >= 0 AND rect_height >= 0)
);

CREATE INDEX IF NOT EXISTS idx_nfrc_note ON note_file_region_comment (id_note);
CREATE INDEX IF NOT EXISTS idx_nfrc_note_asset ON note_file_region_comment (id_note, id_asset);
