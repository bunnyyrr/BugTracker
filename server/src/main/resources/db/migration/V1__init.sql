CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX ux_app_user_username ON app_user (lower(username));
CREATE UNIQUE INDEX ux_app_user_email    ON app_user (lower(email));

CREATE TABLE project (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE project_member (
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT      NOT NULL REFERENCES project (id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES app_user (id),
    role       VARCHAR(20) NOT NULL CHECK (role IN ('TESTER', 'DEVELOPER', 'TEAM_LEAD')),
    UNIQUE (project_id, user_id)
);

CREATE TABLE release (
    id           BIGSERIAL PRIMARY KEY,
    project_id   BIGINT       NOT NULL REFERENCES project (id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    planned_date TIMESTAMPTZ  NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PLANNED' CHECK (status IN ('PLANNED', 'RELEASED'))
);
CREATE INDEX ix_release_project ON release (project_id);

CREATE TABLE bug (
    id                 BIGSERIAL PRIMARY KEY,
    project_id         BIGINT       NOT NULL REFERENCES project (id) ON DELETE CASCADE,
    author_id          BIGINT       NOT NULL REFERENCES app_user (id),
    assignee_id        BIGINT       REFERENCES app_user (id),
    release_id         BIGINT       REFERENCES release (id) ON DELETE SET NULL,
    title              VARCHAR(255) NOT NULL,
    priority           VARCHAR(20)  NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    description        TEXT         NOT NULL,
    steps_to_reproduce TEXT         NOT NULL,
    expected_result    TEXT,
    actual_result      TEXT,
    status             VARCHAR(20)  NOT NULL DEFAULT 'NEW'
                       CHECK (status IN ('NEW', 'IN_PROGRESS', 'IN_REVIEW', 'CLOSED')),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_bug_project_status ON bug (project_id, status, created_at DESC);
CREATE INDEX ix_bug_assignee       ON bug (assignee_id);
CREATE INDEX ix_bug_author         ON bug (author_id);
CREATE INDEX ix_bug_release        ON bug (release_id);

CREATE TABLE comment (
    id         BIGSERIAL PRIMARY KEY,
    bug_id     BIGINT      NOT NULL REFERENCES bug (id) ON DELETE CASCADE,
    author_id  BIGINT      NOT NULL REFERENCES app_user (id),
    text       TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_comment_bug ON comment (bug_id);

CREATE TABLE attachment (
    id           BIGSERIAL PRIMARY KEY,
    bug_id       BIGINT       REFERENCES bug (id) ON DELETE CASCADE,
    comment_id   BIGINT       REFERENCES comment (id) ON DELETE CASCADE,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    storage_key  VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT ck_attachment_owner CHECK ((bug_id IS NULL) <> (comment_id IS NULL))
);
CREATE INDEX ix_attachment_bug     ON attachment (bug_id);
CREATE INDEX ix_attachment_comment ON attachment (comment_id);

CREATE TABLE commit_link (
    id     BIGSERIAL PRIMARY KEY,
    bug_id BIGINT      NOT NULL REFERENCES bug (id) ON DELETE CASCADE,
    type   VARCHAR(20) NOT NULL CHECK (type IN ('COMMIT', 'MERGE_REQUEST')),
    url    TEXT        NOT NULL
);
CREATE INDEX ix_commit_link_bug ON commit_link (bug_id);

CREATE TABLE history_entry (
    id         BIGSERIAL PRIMARY KEY,
    bug_id     BIGINT      NOT NULL REFERENCES bug (id) ON DELETE CASCADE,
    actor_id   BIGINT      NOT NULL REFERENCES app_user (id),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    event      VARCHAR(30) NOT NULL CHECK (event IN (
                   'CREATED', 'TAKEN_IN_WORK', 'ABANDONED', 'ASSIGNEE_RELEASED',
                   'SUBMITTED_FOR_REVIEW', 'RETURNED_TO_WORK', 'CLOSED', 'RELEASE_CHANGED')),
    field      VARCHAR(100),
    old_value  TEXT,
    new_value  TEXT
);
CREATE INDEX ix_history_bug ON history_entry (bug_id, changed_at);
