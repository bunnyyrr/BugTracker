-- Является ли пользователь единственным тимлидом какого-либо проекта
SELECT EXISTS (
    SELECT 1
    FROM project_member pm
    WHERE pm.user_id = :userId
      AND pm.role = 'TEAM_LEAD'
      AND NOT EXISTS (
          SELECT 1
          FROM project_member other
          WHERE other.project_id = pm.project_id
            AND other.role = 'TEAM_LEAD'
            AND other.user_id <> pm.user_id
      )
) AS is_only_team_lead;

-- Единственный ли он тимлид в конкретном проекте
SELECT EXISTS (
    SELECT 1
    FROM project_member pm
    WHERE pm.project_id = :projectId
      AND pm.user_id = :userId
      AND pm.role = 'TEAM_LEAD'
      AND NOT EXISTS (
          SELECT 1
          FROM project_member other
          WHERE other.project_id = pm.project_id
            AND other.role = 'TEAM_LEAD'
            AND other.user_id <> pm.user_id
      )
) AS is_only_team_lead_in_project;

-- Баги взятые пользователем в работу
SELECT b.id, b.project_id
FROM bug b
WHERE b.assignee_id = :userId
  AND b.status = 'IN_PROGRESS';

-- Участники проекта с ролями
SELECT pm.user_id,
       CASE WHEN u.is_deleted THEN 'Удалённый пользователь' ELSE u.username END AS username,
       pm.role
FROM project_member pm
JOIN app_user u ON u.id = pm.user_id
WHERE pm.project_id = :projectId
ORDER BY CASE pm.role WHEN 'TEAM_LEAD' THEN 1 WHEN 'DEVELOPER' THEN 2 ELSE 3 END,
         u.username;

-- Список проектов пользователя с ролью и числом незакрытых багов
SELECT p.id, p.name, p.description, pm.role,
       (SELECT count(*) FROM bug b
         WHERE b.project_id = p.id AND b.status <> 'CLOSED') AS open_bug_count
FROM project p
JOIN project_member pm ON pm.project_id = p.id
WHERE pm.user_id = :userId
ORDER BY p.id;
