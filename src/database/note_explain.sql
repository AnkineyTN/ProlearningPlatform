CREATE TABLE note_explain
(
    id         BIGSERIAL PRIMARY KEY,
    id_note    BIGINT       NOT NULL,
    source     VARCHAR(255) NOT NULL,
    term  TEXT         NOT NULL,
    explain    TEXT         NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_explanations_note FOREIGN KEY (id_note) REFERENCES note (id)
);