DO $$
DECLARE
    h       TEXT := '$2a$10$c8vvg24phHZzy.qZKjb.c.B6M.VuJhxGz2xKt0fBcpwFh.r6.zS9C';
    anna    BIGINT;
    oleg    BIGINT;
    ivan    BIGINT;
    p1      BIGINT;
    p2      BIGINT;
    r_old   BIGINT;
    r_pl1   BIGINT;
    r_pl2   BIGINT;
    r_m     BIGINT;
    b       BIGINT;
    c       BIGINT;
BEGIN
    INSERT INTO app_user (username, email, password_hash) VALUES ('anna_lead',   'anna@example.com', h) RETURNING id INTO anna;
    INSERT INTO app_user (username, email, password_hash) VALUES ('oleg_tester', 'oleg@example.com', h) RETURNING id INTO oleg;
    INSERT INTO app_user (username, email, password_hash) VALUES ('ivan_dev',    'ivan@example.com', h) RETURNING id INTO ivan;

    INSERT INTO project (name, description) VALUES ('Интернет-магазин', 'Сайт с каталогом и корзиной') RETURNING id INTO p1;
    INSERT INTO project (name, description) VALUES ('Мобильное приложение', 'Клиент для iOS и Android') RETURNING id INTO p2;

    INSERT INTO project_member (project_id, user_id, role) VALUES
        (p1, anna, 'TEAM_LEAD'), (p1, oleg, 'TESTER'), (p1, ivan, 'DEVELOPER'),
        (p2, ivan, 'TEAM_LEAD'), (p2, oleg, 'TESTER'), (p2, anna, 'DEVELOPER');

    INSERT INTO release (project_id, name, planned_date, status) VALUES (p1, 'v1.0',   '2026-10-15T00:00:00Z', 'RELEASED') RETURNING id INTO r_old;
    INSERT INTO release (project_id, name, planned_date, status) VALUES (p1, 'v1.1',   '2026-12-01T00:00:00Z', 'PLANNED')  RETURNING id INTO r_pl1;
    INSERT INTO release (project_id, name, planned_date, status) VALUES (p1, 'v1.2',   '2027-01-20T00:00:00Z', 'PLANNED')  RETURNING id INTO r_pl2;
    INSERT INTO release (project_id, name, planned_date, status) VALUES (p2, 'Beta 1', '2026-12-15T00:00:00Z', 'PLANNED')  RETURNING id INTO r_m;


    INSERT INTO bug (project_id, author_id, title, priority, description, steps_to_reproduce, expected_result, actual_result, status, created_at)
    VALUES (p1, oleg, 'Не работает поиск по каталогу', 'HIGH', 'Поиск возвращает пустой список',
            '1. Открыть каталог. 2. Ввести "ноутбук".', 'Список товаров', 'Пустой список', 'NEW', now() - interval '8 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '8 days');

    INSERT INTO bug (project_id, author_id, title, priority, description, steps_to_reproduce, status, created_at)
    VALUES (p1, oleg, 'Опечатка на странице оплаты', 'LOW', 'В слове "Оплатить" лишняя буква',
            'Открыть корзину, нажать "К оплате".', 'NEW', now() - interval '7 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '7 days');

    INSERT INTO bug (project_id, author_id, release_id, title, priority, description, steps_to_reproduce, status, created_at)
    VALUES (p1, anna, r_pl1, 'Кнопка "Оформить" активна при пустой корзине', 'MEDIUM', 'Можно оформить заказ без товаров',
            'Открыть пустую корзину.', 'NEW', now() - interval '6 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, anna, 'CREATED', now() - interval '6 days');
    INSERT INTO history_entry (bug_id, actor_id, event, field, old_value, new_value, changed_at)
    VALUES (b, anna, 'RELEASE_CHANGED', 'reliz', NULL, 'v1.1', now() - interval '5 days');

    INSERT INTO bug (project_id, author_id, assignee_id, release_id, title, priority, description, steps_to_reproduce, status, created_at)
    VALUES (p1, oleg, ivan, r_pl1, 'Не загружаются фото товара', 'CRITICAL', 'Вместо фото серый квадрат',
            'Открыть карточку любого товара.', 'IN_PROGRESS', now() - interval '5 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '5 days');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, ivan, 'TAKEN_IN_WORK', now() - interval '4 days');

    INSERT INTO bug (project_id, author_id, assignee_id, title, priority, description, steps_to_reproduce, status, created_at)
    VALUES (p1, oleg, anna, 'Долго открывается профиль', 'MEDIUM', 'Страница грузится около 10 секунд',
            'Войти и открыть профиль.', 'IN_PROGRESS', now() - interval '4 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '4 days');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, anna, 'TAKEN_IN_WORK', now() - interval '3 days');

    INSERT INTO bug (project_id, author_id, assignee_id, release_id, title, priority, description, steps_to_reproduce, expected_result, actual_result, status, created_at)
    VALUES (p1, oleg, ivan, r_pl2, 'Неверная сумма заказа со скидкой', 'HIGH', 'Скидка применяется дважды',
            '1. Добавить товар. 2. Ввести промокод SALE10.', 'Цена со скидкой 10%', 'Цена со скидкой 20%', 'IN_REVIEW', now() - interval '3 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '3 days');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, ivan, 'TAKEN_IN_WORK', now() - interval '2 days');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, ivan, 'SUBMITTED_FOR_REVIEW', now() - interval '1 day');
    INSERT INTO commit_link (bug_id, type, url) VALUES (b, 'COMMIT', 'https://github.com/example/shop/commit/a1b2c3d');
    INSERT INTO comment (bug_id, author_id, text, created_at)
    VALUES (b, oleg, 'Скриншот с неверной суммой во вложении', now() - interval '2 days 12 hours')
    RETURNING id INTO c;
    INSERT INTO attachment (comment_id, file_name, content_type, size_bytes, storage_key)
    VALUES (c, 'summa.png', 'image/png', 20480, 'seed-0001-summa.png');

    INSERT INTO bug (project_id, author_id, assignee_id, release_id, title, priority, description, steps_to_reproduce, status, created_at)
    VALUES (p1, oleg, ivan, r_old, 'Ошибка при регистрации с пробелом в имени', 'MEDIUM', 'Сервер отвечает 500',
            'Зарегистрироваться с именем "Иван Петров".', 'CLOSED', now() - interval '20 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '20 days');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, ivan, 'TAKEN_IN_WORK', now() - interval '19 days');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, ivan, 'SUBMITTED_FOR_REVIEW', now() - interval '18 days');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CLOSED', now() - interval '17 days');
    INSERT INTO commit_link (bug_id, type, url) VALUES (b, 'MERGE_REQUEST', 'https://github.com/example/shop/pull/12');

    INSERT INTO bug (project_id, author_id, assignee_id, release_id, title, priority, description, steps_to_reproduce, status, created_at)
    VALUES (p1, oleg, ivan, r_old, 'Логотип не виден на тёмной теме', 'LOW', 'Чёрный логотип на тёмном фоне',
            'Включить тёмную тему.', 'CLOSED', now() - interval '25 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '25 days');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CLOSED', now() - interval '22 days');
    INSERT INTO commit_link (bug_id, type, url) VALUES (b, 'COMMIT', 'https://github.com/example/shop/commit/f9e8d7c');

    INSERT INTO bug (project_id, author_id, title, priority, description, steps_to_reproduce, status, created_at)
    VALUES (p2, oleg, 'Приложение падает при повороте экрана', 'CRITICAL', 'Краш на странице заказов',
            'Открыть заказы и повернуть телефон.', 'NEW', now() - interval '2 days')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '2 days');

    INSERT INTO bug (project_id, author_id, assignee_id, release_id, title, priority, description, steps_to_reproduce, status, created_at)
    VALUES (p2, oleg, anna, r_m, 'Не приходят push-уведомления', 'HIGH', 'Уведомления не приходят на Android 14',
            'Включить уведомления и оформить заказ.', 'IN_PROGRESS', now() - interval '1 day')
    RETURNING id INTO b;
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, oleg, 'CREATED', now() - interval '1 day');
    INSERT INTO history_entry (bug_id, actor_id, event, changed_at) VALUES (b, anna, 'TAKEN_IN_WORK', now() - interval '20 hours');
END $$;
