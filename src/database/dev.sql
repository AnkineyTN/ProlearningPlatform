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
    id
                  SERIAL
        PRIMARY
            KEY,
    email
                  VARCHAR(255) UNIQUE NOT NULL,
    first_name    VARCHAR(100)        NOT NULL,
    last_name     VARCHAR(100)        NOT NULL,
    password      VARCHAR(255)        NOT NULL,
    recovery_code character varying(255) NULL,
    language      user_language            default 'VI',
    education     user_education           default 'HIGH_SCHOOL',
    hear_app_from user_hear_app_from       default 'GOOGLE',
    account_type  VARCHAR(50)              default 'FREE',
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
            REFERENCES users (id)
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
    id_note  BIGINT NOT NULL,
    id_asset BIGINT NOT NULL,
    PRIMARY KEY (id_note, id_asset), -- composite primary key
    FOREIGN KEY (id_note) REFERENCES note (id),
    FOREIGN KEY (id_asset) REFERENCES asset (id)
);

CREATE TABLE note_imgs
(
    id_note  BIGINT NOT NULL,
    id_asset BIGINT NOT NULL,
    PRIMARY KEY (id_note, id_asset), -- composite primary key
    FOREIGN KEY (id_note) REFERENCES note (id),
    FOREIGN KEY (id_asset) REFERENCES asset (id)
);

CREATE TABLE flashcard
(
    id            bigserial              NOT NULL,
    title         character varying(255) NOT NULL,
    description   text NULL,
    status        character varying(50)  NOT NULL DEFAULT 'NOT_COMPLETED'::character varying,
    create_method character varying(50)  NOT NULL,
    id_user       integer                NOT NULL,
    privacy       character varying(50)  NOT NULL DEFAULT 'PRIVATE'::character varying,
    created_at    timestamp without time zone NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp without time zone NULL DEFAULT CURRENT_TIMESTAMP,
    known         integer NULL DEFAULT 0,
    learning      integer NULL DEFAULT 0,
    remain        integer NULL DEFAULT 0,
    id_set        integer                NOT NULL,
    last_study    timestamp without time zone NULL,

    FOREIGN KEY (id_user) REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT chk_status CHECK (status IN ('COMPLETED', 'NOT_COMPLETED', 'LEARNING')),
    CONSTRAINT chk_privacy CHECK (privacy IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT chk_known_non_negative CHECK (known >= 0),
    CONSTRAINT chk_learning_non_negative CHECK (learning >= 0),
    CONSTRAINT chk_remain_non_negative CHECK (remain >= 0)
);

CREATE TABLE card_item
(
    id           BIGSERIAL PRIMARY KEY,
    flashcard_id BIGINT      NOT NULL,
    front_card   TEXT        NOT NULL,
    back_card    TEXT        NOT NULL,
    image_url    VARCHAR(255),

    card_status  VARCHAR(50) NOT NULL DEFAULT 'NEW',

    created_at   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (flashcard_id) REFERENCES flashcard (id) ON DELETE CASCADE,

    CONSTRAINT chk_card_status CHECK (card_status IN ('NEW', 'LEARNING', 'KNOWN'))
);

CREATE TABLE image_asset
(
    id         BIGSERIAL PRIMARY KEY,
    public_id  varchar(255) not null,
    url        varchar(255) not null,
    id_user    int          not null,
    status     varchar(10)  not null default 'PENDING',

    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,

    foreign key (id_user) references users (id) on delete cascade,
    constraint chk_status check (status in ('PENDING', 'ACTIVE'))
);








