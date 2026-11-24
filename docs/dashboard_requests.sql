-- Вкладка "Актуальные", все незакрытые баги проекта
SELECT b.id, b.title, b.priority, b.status, b.assignee_id, b.created_at,
       r.planned_date AS deadline
FROM bug b
LEFT JOIN release r ON r.id = b.release_id
WHERE b.project_id = :projectId
  AND b.status <> 'CLOSED'
  AND (CAST(:text AS text) IS NULL
       OR b.title ILIKE '%' || CAST(:text AS text) || '%'
       OR CAST(b.id AS text) = CAST(:text AS text))
ORDER BY b.created_at DESC, b.id DESC
LIMIT 6 OFFSET (:page - 1) * 6;

-- Вкладка "Мои баги", пользователь автор или ответственный, любой статус
SELECT b.id, b.title, b.priority, b.status, b.assignee_id, b.created_at,
       r.planned_date AS deadline
FROM bug b
LEFT JOIN release r ON r.id = b.release_id
WHERE b.project_id = :projectId
  AND (b.author_id = :userId OR b.assignee_id = :userId)
  AND (CAST(:text AS text) IS NULL
       OR b.title ILIKE '%' || CAST(:text AS text) || '%'
       OR CAST(b.id AS text) = CAST(:text AS text))
ORDER BY b.created_at DESC, b.id DESC
LIMIT 6 OFFSET (:page - 1) * 6;

-- Вкладка "Закрытые"
SELECT b.id, b.title, b.priority, b.status, b.assignee_id, b.created_at,
       r.planned_date AS deadline
FROM bug b
LEFT JOIN release r ON r.id = b.release_id
WHERE b.project_id = :projectId
  AND b.status = 'CLOSED'
  AND (CAST(:text AS text) IS NULL
       OR b.title ILIKE '%' || CAST(:text AS text) || '%'
       OR CAST(b.id AS text) = CAST(:text AS text))
ORDER BY b.created_at DESC, b.id DESC
LIMIT 6 OFFSET (:page - 1) * 6;

-- Общее число для пагинации
SELECT count(*) AS total
FROM bug b
WHERE b.project_id = :projectId
  AND b.status <> 'CLOSED'
  AND (CAST(:text AS text) IS NULL
       OR b.title ILIKE '%' || CAST(:text AS text) || '%'
       OR CAST(b.id AS text) = CAST(:text AS text));
