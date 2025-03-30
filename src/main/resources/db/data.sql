-- 1. 사용자 데이터 삽입 (provider, provider_id 제거)
INSERT INTO user (id, name, email, role, status)
VALUES
    ('bc0de3e8-d0e5-11ef-97fd-2cf05d34818a', '신짱구', 'shinnosuke@test.com', 'NORMAL', 'ACTIVE'),
    ('2f81d4a0-d0e5-11ef-97fd-2cf05d34818b', '봉미선', 'misun@test.com', 'NORMAL', 'ACTIVE'),
    ('3f81d4a0-d0e5-11ef-97fd-2cf05d34818c', '신형만', 'hyeongman@test.com', 'NORMAL', 'ACTIVE'),
    ('4f81d4a0-d0e5-11ef-97fd-2cf05d34818d', '신짱아', 'jjangah@test.com', 'NORMAL', 'ACTIVE'),
    ('5f81d4a0-d0e5-11ef-97fd-2cf05d34818e', '김철수', 'chulsoo@test.com', 'NORMAL', 'ACTIVE'),
    ('6f81d4a0-d0e5-11ef-97fd-2cf05d34818f', '한유리', 'yuri@test.com', 'NORMAL', 'ACTIVE'),
    ('7f81d4a0-d0e5-11ef-97fd-2cf05d3481aa', '이훈이', 'hoon@test.com', 'NORMAL', 'ACTIVE'),
    ('8f81d4a0-d0e5-11ef-97fd-2cf05d3481bb', '맹구', 'maenggu@test.com', 'NORMAL', 'ACTIVE'),
    ('9f81d4a0-d0e5-11ef-97fd-2cf05d3481cc', '한수지', 'suji@test.com', 'NORMAL', 'ACTIVE'),
    ('af81d4a0-d0e5-11ef-97fd-2cf05d3481dd', '나미리', 'namiri@test.com', 'NORMAL', 'ACTIVE'),
    ('bf81d4a0-d0e5-11ef-97fd-2cf05d3481ee', '차은주', 'eunju@test.com', 'NORMAL', 'ACTIVE'),
    ('cf81d4a0-d0e5-11ef-97fd-2cf05d3481ff', '치타', 'cheetah@test.com', 'NORMAL', 'ACTIVE'),
    ('df81d4a0-d0e5-11ef-97fd-2cf05d34811a', '오수', 'osu@test.com', 'NORMAL', 'ACTIVE'),
    ('ef81d4a0-d0e5-11ef-97fd-2cf05d34812b', '고뭉치', 'gomungchi@test.com', 'NORMAL', 'ACTIVE'),
    ('ff81d4a0-d0e5-11ef-97fd-2cf05d34813c', '부리부리', 'buriburi@test.com', 'NORMAL', 'ACTIVE'),
    ('0f82d4a0-d0e5-11ef-97fd-2cf05d34814d', '원장', 'wonjang@test.com', 'NORMAL', 'ACTIVE');

-- 2. 소셜 로그인 연결 정보 추가
INSERT INTO user_connection (user_id, provider, provider_id, created_at, updated_at)
VALUES
    ('bc0de3e8-d0e5-11ef-97fd-2cf05d34818a', 'KAKAO', 'kakao_123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('2f81d4a0-d0e5-11ef-97fd-2cf05d34818b', 'KAKAO', 'kakao_456', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('3f81d4a0-d0e5-11ef-97fd-2cf05d34818c', 'NAVER', 'naver_789', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('4f81d4a0-d0e5-11ef-97fd-2cf05d34818d', 'NAVER', 'naver_123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('5f81d4a0-d0e5-11ef-97fd-2cf05d34818e', 'KAKAO', 'kakao_012', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('6f81d4a0-d0e5-11ef-97fd-2cf05d34818f', 'NAVER', 'naver_456', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('7f81d4a0-d0e5-11ef-97fd-2cf05d3481aa', 'KAKAO', 'kakao_345', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('8f81d4a0-d0e5-11ef-97fd-2cf05d3481bb', 'NAVER', 'naver_678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('9f81d4a0-d0e5-11ef-97fd-2cf05d3481cc', 'NAVER', 'naver_890', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('af81d4a0-d0e5-11ef-97fd-2cf05d3481dd', 'KAKAO', 'kakao_567', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bf81d4a0-d0e5-11ef-97fd-2cf05d3481ee', 'NAVER', 'naver_234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('cf81d4a0-d0e5-11ef-97fd-2cf05d3481ff', 'KAKAO', 'kakao_901', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('df81d4a0-d0e5-11ef-97fd-2cf05d34811a', 'NAVER', 'naver_654', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ef81d4a0-d0e5-11ef-97fd-2cf05d34812b', 'KAKAO', 'kakao_789', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ff81d4a0-d0e5-11ef-97fd-2cf05d34813c', 'NAVER', 'naver_321', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('0f82d4a0-d0e5-11ef-97fd-2cf05d34814d', 'NAVER', 'naver_111', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. 프로필 데이터 삽입 (이미 가입이 완료된 사용자로 가정)
INSERT INTO user_profile (user_id, nickname, image_path, created_at, updated_at)
VALUES
    ('bc0de3e8-d0e5-11ef-97fd-2cf05d34818a', '짱구', '/profiles/shinnosuke.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('2f81d4a0-d0e5-11ef-97fd-2cf05d34818b', '미선', '/profiles/misun.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('3f81d4a0-d0e5-11ef-97fd-2cf05d34818c', '형만', '/profiles/hyeongman.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('4f81d4a0-d0e5-11ef-97fd-2cf05d34818d', '짱아', '/profiles/jjangah.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('5f81d4a0-d0e5-11ef-97fd-2cf05d34818e', '철수', '/profiles/chulsoo.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('6f81d4a0-d0e5-11ef-97fd-2cf05d34818f', '유리', '/profiles/yuri.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('7f81d4a0-d0e5-11ef-97fd-2cf05d3481aa', '훈이', '/profiles/hoon.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('8f81d4a0-d0e5-11ef-97fd-2cf05d3481bb', '맹구', '/profiles/maenggu.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('9f81d4a0-d0e5-11ef-97fd-2cf05d3481cc', '수지', '/profiles/suji.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('af81d4a0-d0e5-11ef-97fd-2cf05d3481dd', '미리', '/profiles/namiri.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bf81d4a0-d0e5-11ef-97fd-2cf05d3481ee', '은주', '/profiles/eunju.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('cf81d4a0-d0e5-11ef-97fd-2cf05d3481ff', '치타', '/profiles/cheetah.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('df81d4a0-d0e5-11ef-97fd-2cf05d34811a', '오수', '/profiles/osu.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ef81d4a0-d0e5-11ef-97fd-2cf05d34812b', '뭉치', '/profiles/gomungchi.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ff81d4a0-d0e5-11ef-97fd-2cf05d34813c', '부리', '/profiles/buriburi.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('0f82d4a0-d0e5-11ef-97fd-2cf05d34814d', '원장', '/profiles/wonjang.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. 모임 생성
INSERT INTO meeting (id, name, icon, creator_id)
VALUES (RANDOM_UUID(), '떡잎 유치원 해바라기반', 'sunflower_class_icon.png',
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
       (RANDOM_UUID(), '짱구네', 'jjanggu_family_icon.png',
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com'));

-- 5. 모임원 추가: 짱구네
INSERT INTO member (id, meeting_id, user_id, role)
VALUES (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com'), 'CREATOR'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        (SELECT id FROM user WHERE email = 'misun@test.com'), 'MEMBER'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        (SELECT id FROM user WHERE email = 'hyeongman@test.com'), 'MEMBER'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        (SELECT id FROM user WHERE email = 'jjangah@test.com'), 'MEMBER');

-- 6. 모임원 추가: 떡잎 유치원 해바라기반
INSERT INTO member (id, meeting_id, user_id, role)
VALUES (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반'),
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com'), 'CREATOR'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반'),
        (SELECT id FROM user WHERE email = 'chulsoo@test.com'), 'MEMBER'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반'),
        (SELECT id FROM user WHERE email = 'yuri@test.com'), 'MEMBER'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반'),
        (SELECT id FROM user WHERE email = 'hoon@test.com'), 'MEMBER'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반'),
        (SELECT id FROM user WHERE email = 'maenggu@test.com'), 'MEMBER'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반'),
        (SELECT id FROM user WHERE email = 'suji@test.com'), 'MEMBER');

-- 7. 활동 생성
INSERT INTO activity (id, meeting_id, type, creator_id)
VALUES (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반'),
        'EXPENSE', (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        'EXPENSE', (SELECT id FROM user WHERE email = 'chulsoo@test.com'));

-- 8. 지출 기록: 짱구네 관련
INSERT INTO expense (id, activity_id, creator_id, amount, category, description, expense_date)
VALUES (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com'),
        15000, 'FOOD', '떡볶이', '2025-01-11'),
       (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
        (SELECT id FROM user WHERE email = 'yuri@test.com'),
        8000, 'TRANSPORT', '택시', '2025-01-12'),
       (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
        (SELECT id FROM user WHERE email = 'hoon@test.com'),
        50000, 'SHOPPING', '액션가면', '2025-01-13'),
       (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com'),
        20000, 'DATE', '놀이공원 티켓', '2025-01-14');

-- 9. 지출 기록: 떡잎 유치원 해바라기반 관련
INSERT INTO expense (id, activity_id, creator_id, amount, category, description, expense_date)
VALUES (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
        (SELECT id FROM user WHERE email = 'chulsoo@test.com'),
        25000, 'FOOD', '라면', '2025-01-13'),
       (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
        (SELECT id FROM user WHERE email = 'hoon@test.com'),
        12000, 'DATE', '영화 티켓', '2025-01-14'),
       (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
        (SELECT id FROM user WHERE email = 'suji@test.com'),
        18000, 'TRANSPORT', '지하철', '2025-01-15'),
       (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
        (SELECT id FROM user WHERE email = 'maenggu@test.com'),
        30000, 'FOOD', '초밥', '2025-01-16'),
       (RANDOM_UUID(),
        (SELECT id
         FROM activity
         WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
           AND type = 'EXPENSE'
           AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
        (SELECT id FROM user WHERE email = 'yuri@test.com'),
        7000, 'FOOD', '아이스크림', '2025-01-17');

-- 10. 반응 추가: 짱구네 관련 지출에 대한 반응
INSERT INTO reaction (id, expense_id, reactor_id, emoji)
VALUES (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '떡볶이' AND amount = 15000),
        (SELECT id FROM user WHERE email = 'chulsoo@test.com'), '👍'),
       (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '택시' AND amount = 8000),
        (SELECT id FROM user WHERE email = 'hoon@test.com'), '❤️'),
       (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '액션가면' AND amount = 50000),
        (SELECT id FROM user WHERE email = 'yuri@test.com'), '😆'),
       (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '놀이공원 티켓' AND amount = 20000),
        (SELECT id FROM user WHERE email = 'chulsoo@test.com'), '😮');

-- 11. 반응 추가: 떡잎 유치원 해바라기반 관련 지출에 대한 반응
INSERT INTO reaction (id, expense_id, reactor_id, emoji)
VALUES (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '라면' AND amount = 25000),
        (SELECT id FROM user WHERE email = 'hoon@test.com'), '👍'),
       (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '영화 티켓' AND amount = 12000),
        (SELECT id FROM user WHERE email = 'maenggu@test.com'), '❤️'),
       (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '지하철' AND amount = 18000),
        (SELECT id FROM user WHERE email = 'suji@test.com'), '😢'),
       (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '초밥' AND amount = 30000),
        (SELECT id FROM user WHERE email = 'yuri@test.com'), '😆'),
       (RANDOM_UUID(),
        (SELECT id FROM expense WHERE description = '아이스크림' AND amount = 7000),
        (SELECT id FROM user WHERE email = 'chulsoo@test.com'), '😡');
