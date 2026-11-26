ALTER TABLE project_member DROP COLUMN id;
ALTER TABLE project_member ADD PRIMARY KEY (project_id, user_id);
ALTER TABLE project_member DROP CONSTRAINT project_member_project_id_user_id_key;
