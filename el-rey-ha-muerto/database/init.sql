-- Esquema inicial de "El Rey ha Muerto"

CREATE TABLE IF NOT EXISTS game_sessions (
    id          BIGSERIAL PRIMARY KEY,
    player_name VARCHAR(50) NOT NULL,
    started_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    ended_at    TIMESTAMP,
    days_survived INT        NOT NULL DEFAULT 0,
    is_alive    BOOLEAN     NOT NULL DEFAULT TRUE,
    cause_of_death TEXT
);

CREATE TABLE IF NOT EXISTS king_stats (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT      NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    day         INT         NOT NULL DEFAULT 1,
    hygiene     INT         NOT NULL DEFAULT 100 CHECK (hygiene BETWEEN 0 AND 100),
    hunger      INT         NOT NULL DEFAULT 100 CHECK (hunger BETWEEN 0 AND 100),
    popularity  INT         NOT NULL DEFAULT 50  CHECK (popularity BETWEEN 0 AND 100),
    wealth      INT         NOT NULL DEFAULT 100 CHECK (wealth BETWEEN 0 AND 100)
);

CREATE TABLE IF NOT EXISTS game_events (
    id          BIGSERIAL PRIMARY KEY,
    source      VARCHAR(20) NOT NULL CHECK (source IN ('manual', 'scraped', 'ai_generated')),
    day_target  INT,
    title       VARCHAR(200) NOT NULL,
    description TEXT        NOT NULL,
    scraped_at  TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS choices (
    id              BIGSERIAL PRIMARY KEY,
    event_id        BIGINT      NOT NULL REFERENCES game_events(id) ON DELETE CASCADE,
    label           CHAR(1)     NOT NULL CHECK (label IN ('A','B','C')),
    text            TEXT        NOT NULL,
    hidden_flag     VARCHAR(50),
    stat_hygiene    INT         NOT NULL DEFAULT 0,
    stat_hunger     INT         NOT NULL DEFAULT 0,
    stat_popularity INT         NOT NULL DEFAULT 0,
    stat_wealth     INT         NOT NULL DEFAULT 0,
    immediate_death BOOLEAN     NOT NULL DEFAULT FALSE,
    death_message   TEXT
);

CREATE TABLE IF NOT EXISTS session_choices (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT      NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    event_id    BIGINT      NOT NULL REFERENCES game_events(id),
    choice_id   BIGINT      NOT NULL REFERENCES choices(id),
    day         INT         NOT NULL,
    chosen_at   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS active_flags (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT      NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    flag_name   VARCHAR(50) NOT NULL,
    trigger_day INT         NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);
