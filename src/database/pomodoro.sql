
CREATE TYPE pomodoro_session_type AS ENUM ('POMODORO', 'SHORT_BREAK', 'LONG_BREAK');
CREATE TYPE asset_source           AS ENUM ('SYSTEM', 'USER');

CREATE TABLE pomodoro_setting (
    id                  BIGSERIAL PRIMARY KEY,
    id_user             BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    pomodoro_duration   INT         NOT NULL DEFAULT 1500,   -- seconds (25 min)
    short_break         INT         NOT NULL DEFAULT 300,    -- 5 min
    long_break          INT         NOT NULL DEFAULT 900,    -- 15 min
    long_break_interval INT         NOT NULL DEFAULT 4,      -- sau bao nhiêu pomodoro thì long break
    auto_start_break    BOOLEAN     NOT NULL DEFAULT FALSE,
    auto_start_pomodoro BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pomodoro_space (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    description VARCHAR(1024),
    source      asset_source  NOT NULL DEFAULT 'SYSTEM',
    id_asset    BIGINT        NOT NULL REFERENCES asset(id),
    id_user     BIGINT        REFERENCES users(id) ON DELETE CASCADE,  -- NULL nếu là SYSTEM
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE pomodoro_sound (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL,
    description VARCHAR(1024),
    source      asset_source  NOT NULL DEFAULT 'SYSTEM',
    id_asset    BIGINT        NOT NULL REFERENCES asset(id),
    id_user     BIGINT        REFERENCES users(id) ON DELETE CASCADE,  -- NULL nếu là SYSTEM
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE TABLE pomodoro_user_active_sound (
    id          BIGSERIAL PRIMARY KEY,
    id_user     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_sound    BIGINT NOT NULL REFERENCES pomodoro_sound(id) ON DELETE CASCADE,
    volume      FLOAT  NOT NULL DEFAULT 0.5 CHECK (volume >= 0.0 AND volume <= 1.0),
    UNIQUE (id_user, id_sound)
);

CREATE TABLE pomodoro_user_active_space (
    id          BIGSERIAL PRIMARY KEY,
    id_user     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    id_space    BIGINT NOT NULL REFERENCES pomodoro_space(id) ON DELETE CASCADE
);

CREATE TABLE pomodoro_user_favorite_space (
    id          BIGSERIAL PRIMARY KEY,
    id_user     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_space    BIGINT NOT NULL REFERENCES pomodoro_space(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (id_user, id_space)
);

CREATE TABLE pomodoro_user_favorite_sound (
    id          BIGSERIAL PRIMARY KEY,
    id_user     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_sound    BIGINT NOT NULL REFERENCES pomodoro_sound(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (id_user, id_sound)
);

CREATE TABLE pomodoro_session (
    id           BIGSERIAL PRIMARY KEY,
    id_user      BIGINT                NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type         pomodoro_session_type NOT NULL,
    duration     INT                   NOT NULL,  -- số giây thực tế (có thể < planned nếu bỏ dở)
    planned      INT                   NOT NULL,  -- số giây dự kiến
    completed    BOOLEAN               NOT NULL DEFAULT TRUE,
    started_at   TIMESTAMPTZ           NOT NULL,
    ended_at     TIMESTAMPTZ           NOT NULL,
    created_at   TIMESTAMPTZ           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pomodoro_session_user_started ON pomodoro_session (id_user, started_at DESC);
CREATE INDEX idx_pomodoro_session_user_type    ON pomodoro_session (id_user, type);
CREATE INDEX idx_pomodoro_space_source         ON pomodoro_space (source, is_active);
CREATE INDEX idx_pomodoro_sound_source         ON pomodoro_sound (source, is_active);