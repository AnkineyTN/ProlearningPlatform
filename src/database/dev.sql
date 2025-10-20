CREATE TYPE user_language AS ENUM ('VI', 'EN');
CREATE TYPE authority AS ENUM ('ROLE_ADMIN', 'ROLE_USER', 'ROLE_TEACHER');
CREATE TYPE user_education AS ENUM (
    'HIGH_SCHOOL',
    'COLLEGE',
    'GRAD_SCHOOL',
    'MED_SCHOOL',
    'OTHER'
    );
CREATE TYPE user_hear_app_from AS ENUM (
    'YOUTUBE',
    'TIKTOK',
    'CHATGPT',
    'FACEBOOK',
    'GOOGLE',
    'INSTAGRAM',
    'CLASSMATE',
    'REDDIT',
    'OTHER'
    );

CREATE TABLE user
(
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    first_name    VARCHAR(100)        NOT NULL,
    last_name     VARCHAR(100)        NOT NULL,
    password      VARCHAR(255)        NOT NULL,
    recovery_code character varying(255) NULL,
    language      user_language            default 'VI',
    education     user_education           default 'HIGH_SCHOOL',
    hear_app_from user_hear_app_from default 'GOOGLE',
    account_type VARCHAR(50) default 'FREE',
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE authorities
(
    id        bigserial primary key  NOT NULL,
    email     character varying(128) NOT NULL,
    authority authority              NOT NULL DEFAULT 'ROLE_USER'
);

CREATE TABLE set
(
    id          SERIAL PRIMARY KEY,
    id_user     INTEGER NOT NULL,
    title       VARCHAR(255),
    description TEXT,
    privacy     VARCHAR(50),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user
        FOREIGN KEY (id_user)
            REFERENCES "user" (id)
            ON DELETE CASCADE
);

CREATE TABLE note
(
    id          SERIAL PRIMARY KEY,
    note_url    VARCHAR(255),
    title       VARCHAR(255),
    content     TEXT,
    description TEXT,
    privacy     VARCHAR(50),
    status      VARCHAR(50),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_set      INT,
    CONSTRAINT fk_note_set
        FOREIGN KEY (id_set)
            REFERENCES set (id)
            ON DELETE CASCADE
);

CREATE TABLE note_docs
(
    file_name  TEXT         NOT NULL,
    file_url   TEXT         NOT NULL,
    extension  TEXT         NOT NULL,
    public_id  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_note    INT          NOT NULL,
    CONSTRAINT fk_note
        FOREIGN KEY (id_note)
            REFERENCES note (id)
            ON DELETE CASCADE
);









