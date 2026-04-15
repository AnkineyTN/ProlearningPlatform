CREATE TABLE IF NOT EXISTS flashcard_members (
    id BIGSERIAL PRIMARY KEY,
    flashcard_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'VIEWER' CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('PENDING', 'ACTIVE', 'DECLINED')),
    invite_token VARCHAR(255) UNIQUE,
    invite_token_expires_at TIMESTAMP,
    invited_email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_flashcard_members_flashcard 
        FOREIGN KEY (flashcard_id) REFERENCES flashcard(id) ON DELETE CASCADE,
    CONSTRAINT fk_flashcard_members_user 
        FOREIGN KEY (user_id) REFERENCES "users"(id) ON DELETE CASCADE
);

CREATE INDEX idx_flashcard_members_flashcard_id ON flashcard_members(flashcard_id);
CREATE INDEX idx_flashcard_members_user_id ON flashcard_members(user_id);
CREATE INDEX idx_flashcard_members_status ON flashcard_members(status);
CREATE INDEX idx_flashcard_members_invite_token ON flashcard_members(invite_token);

CREATE TABLE IF NOT EXISTS exam_members (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'VIEWER' CHECK (role IN ('OWNER', 'EDITOR', 'VIEWER')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('PENDING', 'ACTIVE', 'DECLINED')),
    invite_token VARCHAR(255) UNIQUE,
    invite_token_expires_at TIMESTAMP,
    invited_email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_exam_members_exam 
        FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    CONSTRAINT fk_exam_members_user 
        FOREIGN KEY (user_id) REFERENCES "users"(id) ON DELETE CASCADE
);

CREATE INDEX idx_exam_members_exam_id ON exam_members(exam_id);
CREATE INDEX idx_exam_members_user_id ON exam_members(user_id);
CREATE INDEX idx_exam_members_status ON exam_members(status);
CREATE INDEX idx_exam_members_invite_token ON exam_members(invite_token);