CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX ix_bug_title_trgm ON bug USING gin (title gin_trgm_ops);

CREATE INDEX ix_bug_project_open ON bug (project_id, created_at DESC, id DESC)
    WHERE status <> 'CLOSED';

CREATE INDEX ix_bug_project_closed ON bug (project_id, created_at DESC, id DESC)
    WHERE status = 'CLOSED';

CREATE INDEX ix_bug_assignee_in_progress ON bug (assignee_id)
    WHERE status = 'IN_PROGRESS';

CREATE INDEX ix_project_member_user ON project_member (user_id);

CREATE INDEX ix_project_member_team_lead ON project_member (project_id)
    WHERE role = 'TEAM_LEAD';
