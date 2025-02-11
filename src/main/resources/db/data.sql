-- 1. 사용자 데이터 삽입
INSERT INTO user (id, name, email, role, provider, provider_id)
VALUES ('bc0de3e8-d0e5-11ef-97fd-2cf05d34818a', '신짱구', 'shinnosuke@test.com', 'NORMAL', 'KAKAO', 'kakao_123'),
       (RANDOM_UUID(), '봉미선', 'misun@test.com', 'NORMAL', 'KAKAO', 'kakao_456'),
       (RANDOM_UUID(), '신형만', 'hyeongman@test.com', 'NORMAL', 'NAVER', 'naver_789'),
       (RANDOM_UUID(), '신짱아', 'jjangah@test.com', 'NORMAL', 'NAVER', 'naver_123'),
       (RANDOM_UUID(), '김철수', 'chulsoo@test.com', 'NORMAL', 'KAKAO', 'kakao_012'),
       (RANDOM_UUID(), '한유리', 'yuri@test.com', 'NORMAL', 'NAVER', 'naver_456'),
       (RANDOM_UUID(), '이훈이', 'hoon@test.com', 'NORMAL', 'KAKAO', 'kakao_345'),
       (RANDOM_UUID(), '맹구', 'maenggu@test.com', 'NORMAL', 'NAVER', 'naver_678'),
       (RANDOM_UUID(), '한수지', 'suji@test.com', 'NORMAL', 'NAVER', 'naver_890'),
       (RANDOM_UUID(), '나미리', 'namiri@test.com', 'NORMAL', 'KAKAO', 'kakao_567'),
       (RANDOM_UUID(), '차은주', 'eunju@test.com', 'NORMAL', 'NAVER', 'naver_234'),
       (RANDOM_UUID(), '치타', 'cheetah@test.com', 'NORMAL', 'KAKAO', 'kakao_901'),
       (RANDOM_UUID(), '오수', 'osu@test.com', 'NORMAL', 'NAVER', 'naver_654'),
       (RANDOM_UUID(), '고뭉치', 'gomungchi@test.com', 'NORMAL', 'KAKAO', 'kakao_789'),
       (RANDOM_UUID(), '부리부리', 'buriburi@test.com', 'NORMAL', 'NAVER', 'naver_321'),
       (RANDOM_UUID(), '원장', 'wonjang@test.com', 'NORMAL', 'NAVER', 'naver_111');

-- 2. 모임 생성
INSERT INTO meeting (id, name, icon, creator_id)
VALUES (RANDOM_UUID(), '떡잎 유치원 해바라기반', 'sunflower_class_icon.png',
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
       (RANDOM_UUID(), '짱구네', 'jjanggu_family_icon.png',
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com'));

-- 3. 모임원 추가: 짱구네
INSERT INTO member (id, meeting_id, user_id, role)
VALUES (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        (SELECT id FROM user WHERE email = 'shinnosuke@test.com'), 'CREATOR'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        (SELECT id FROM user WHERE email = 'misun@test.com'), 'MEMBER'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        (SELECT id FROM user WHERE email = 'hyeongman@test.com'), 'MEMBER'),
       (RANDOM_UUID(), (SELECT id FROM meeting WHERE name = '짱구네'),
        (SELECT id FROM user WHERE email = 'jjangah@test.com'), 'MEMBER');

-- 4. 모임원 추가: 떡잎 유치원 해바라기반
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

-- 5. 활동 생성
INSERT INTO activity (meeting_id, type, creator_id)
VALUES ((SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반'),
        'EXPENSE', (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
       ((SELECT id FROM meeting WHERE name = '짱구네'),
        'EXPENSE', (SELECT id FROM user WHERE email = 'chulsoo@test.com'));

-- 6. 지출 기록: 짱구네 관련
INSERT INTO expense (activity_id, creator_id, amount, category, description, expense_date)
VALUES (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
           (SELECT id FROM user WHERE email = 'chulsoo@test.com'),
           25000, 'FOOD', '라면', '2025-01-13'),
       (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
           (SELECT id FROM user WHERE email = 'hoon@test.com'),
           12000, 'DATE', '영화 티켓', '2025-01-14'),
       (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
           (SELECT id FROM user WHERE email = 'suji@test.com'),
           18000, 'TRANSPORT', '지하철', '2025-01-15'),
       (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
           (SELECT id FROM user WHERE email = 'maenggu@test.com'),
           30000, 'FOOD', '초밥', '2025-01-16'),
       (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '짱구네')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'chulsoo@test.com')),
           (SELECT id FROM user WHERE email = 'yuri@test.com'),
           7000, 'FOOD', '아이스크림', '2025-01-17');

-- 7. 지출 기록: 떡잎 유치원 해바라기반 관련
INSERT INTO expense (activity_id, creator_id, amount, category, description, expense_date)
VALUES (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
           (SELECT id FROM user WHERE email = 'shinnosuke@test.com'),
           15000, 'FOOD', '떡볶이', '2025-01-11'),
       (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
           (SELECT id FROM user WHERE email = 'yuri@test.com'),
           8000, 'TRANSPORT', '택시', '2025-01-12'),
       (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
           (SELECT id FROM user WHERE email = 'hoon@test.com'),
           50000, 'SHOPPING', '액션가면', '2025-01-13'),
       (
           (SELECT id
            FROM activity
            WHERE meeting_id = (SELECT id FROM meeting WHERE name = '떡잎 유치원 해바라기반')
              AND type = 'EXPENSE'
              AND creator_id = (SELECT id FROM user WHERE email = 'shinnosuke@test.com')),
           (SELECT id FROM user WHERE email = 'shinnosuke@test.com'),
           20000, 'DATE', '놀이공원 티켓', '2025-01-14');

-- 8. 반응 추가: 짱구네 관련 지출에 대한 반응
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

-- 9. 반응 추가: 떡잎 유치원 해바라기반 관련 지출에 대한 반응
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
