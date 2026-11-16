CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    imya_polzovatelya      VARCHAR(255) NOT NULL,
    pochta         VARCHAR(255) NOT NULL,
    parol_hesh VARCHAR(255) NOT NULL,
    udalen    BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX ux_app_user_username ON app_user (lower(imya_polzovatelya));
CREATE UNIQUE INDEX ux_app_user_email    ON app_user (lower(pochta));

CREATE TABLE project (
    id          BIGSERIAL PRIMARY KEY,
    nazvanie        VARCHAR(255) NOT NULL,
    opisanie TEXT
);

CREATE TABLE project_member (
    id         BIGSERIAL PRIMARY KEY,
    proekt_id BIGINT      NOT NULL REFERENCES project (id) ON DELETE CASCADE,
    polzovatel_id    BIGINT      NOT NULL REFERENCES app_user (id),
    rol       VARCHAR(20) NOT NULL CHECK (rol IN ('TESTER', 'DEVELOPER', 'TEAM_LEAD')),
    UNIQUE (proekt_id, polzovatel_id)
);

CREATE TABLE release (
    id           BIGSERIAL PRIMARY KEY,
    proekt_id   BIGINT       NOT NULL REFERENCES project (id) ON DELETE CASCADE,
    nazvanie         VARCHAR(255) NOT NULL,
    planovaya_data TIMESTAMPTZ  NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PLANNED' CHECK (status IN ('PLANNED', 'RELEASED'))
);
CREATE INDEX ix_release_project ON release (proekt_id);

CREATE TABLE bug (
    id                 BIGSERIAL PRIMARY KEY,
    proekt_id         BIGINT       NOT NULL REFERENCES project (id) ON DELETE CASCADE,
    avtor_id          BIGINT       NOT NULL REFERENCES app_user (id),
    otvetstvennyi_id        BIGINT       REFERENCES app_user (id),
    reliz_id         BIGINT       REFERENCES release (id) ON DELETE SET NULL,
    nazvanie              VARCHAR(255) NOT NULL,
    prioritet           VARCHAR(20)  NOT NULL CHECK (prioritet IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    opisanie        TEXT         NOT NULL,
    shagi_vosproizvedeniya TEXT         NOT NULL,
    ozhidaemyi_rezultat    TEXT,
    fakticheskii_rezultat      TEXT,
    status             VARCHAR(20)  NOT NULL DEFAULT 'NEW'
                       CHECK (status IN ('NEW', 'IN_PROGRESS', 'IN_REVIEW', 'CLOSED')),
    data_sozdaniya         TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_bug_project_status ON bug (proekt_id, status, data_sozdaniya DESC);
CREATE INDEX ix_bug_assignee       ON bug (otvetstvennyi_id);
CREATE INDEX ix_bug_author         ON bug (avtor_id);
CREATE INDEX ix_bug_release        ON bug (reliz_id);

CREATE TABLE comment (
    id         BIGSERIAL PRIMARY KEY,
    bag_id     BIGINT      NOT NULL REFERENCES bug (id) ON DELETE CASCADE,
    avtor_id  BIGINT      NOT NULL REFERENCES app_user (id),
    tekst       TEXT        NOT NULL,
    data_sozdaniya TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_comment_bug ON comment (bag_id);

CREATE TABLE attachment (
    id           BIGSERIAL PRIMARY KEY,
    bag_id       BIGINT       REFERENCES bug (id) ON DELETE CASCADE,
    kommentarii_id   BIGINT       REFERENCES comment (id) ON DELETE CASCADE,
    imya_faila    VARCHAR(255) NOT NULL,
    tip_soderzhimogo VARCHAR(255) NOT NULL,
    razmer_bait   BIGINT       NOT NULL,
    kluch_hranilishcha  VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT ck_attachment_owner CHECK ((bag_id IS NULL) <> (kommentarii_id IS NULL))
);
CREATE INDEX ix_attachment_bug     ON attachment (bag_id);
CREATE INDEX ix_attachment_comment ON attachment (kommentarii_id);

CREATE TABLE commit_link (
    id     BIGSERIAL PRIMARY KEY,
    bag_id BIGINT      NOT NULL REFERENCES bug (id) ON DELETE CASCADE,
    tip   VARCHAR(20) NOT NULL CHECK (tip IN ('COMMIT', 'MERGE_REQUEST')),
    url    TEXT        NOT NULL
);
CREATE INDEX ix_commit_link_bug ON commit_link (bag_id);

CREATE TABLE history_entry (
    id         BIGSERIAL PRIMARY KEY,
    bag_id     BIGINT      NOT NULL REFERENCES bug (id) ON DELETE CASCADE,
    avtor_izmeneniya_id   BIGINT      NOT NULL REFERENCES app_user (id),
    data_izmeneniya TIMESTAMPTZ NOT NULL DEFAULT now(),
    sobytie      VARCHAR(30) NOT NULL CHECK (sobytie IN (
                   'CREATED', 'TAKEN_IN_WORK', 'ABANDONED', 'ASSIGNEE_RELEASED',
                   'SUBMITTED_FOR_REVIEW', 'RETURNED_TO_WORK', 'CLOSED', 'RELEASE_CHANGED')),
    pole      VARCHAR(100),
    staroe_znachenie  TEXT,
    novoe_znachenie  TEXT
);
CREATE INDEX ix_history_bug ON history_entry (bag_id, data_izmeneniya);
