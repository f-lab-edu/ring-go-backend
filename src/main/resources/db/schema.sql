-- 1. 사용자 테이블
CREATE TABLE user
(
    id          UUID         NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    email       VARCHAR(320) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'NORMAL',
    provider    VARCHAR(50)  NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_provider_id UNIQUE (provider, provider_id)
);

-- 2. 모임 테이블
CREATE TABLE meeting
(
    id         UUID         NOT NULL COMMENT '모임 ID',
    name       VARCHAR(50)  NOT NULL COMMENT '모임 이름',
    icon       VARCHAR(255) NOT NULL COMMENT '모임 아이콘',
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '모임 상태(ACTIVE/ENDED/DELETED)',
    creator_id UUID         NOT NULL COMMENT '생성자 ID',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT fk_meeting_creator FOREIGN KEY (creator_id) REFERENCES user (id)
) COMMENT ='모임';

-- 3. 이벤트 테이블
CREATE TABLE event
(
    id                    UUID           NOT NULL COMMENT '이벤트 ID',
    name                  VARCHAR(100)   NOT NULL COMMENT '이벤트명',
    price                 DECIMAL(10, 2) NOT NULL COMMENT '가격',
    required_ticket_count INT            NOT NULL COMMENT '필요 응모권 수',
    total_item_count      INT            NOT NULL COMMENT '총 상품 수량',
    available_item_count  INT            NOT NULL COMMENT '잔여 상품 수량',
    start_at              DATETIME(6)    NOT NULL COMMENT '시작일시',
    end_at                DATETIME(6)    NOT NULL COMMENT '종료일시',
    status                VARCHAR(20)    NOT NULL DEFAULT 'ON_SALE' COMMENT '상태(ON_SALE/SOLD_OUT/ENDED)',
    created_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at            DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id)
) COMMENT ='이벤트';

-- 4. 모임원 테이블
CREATE TABLE member
(
    id         UUID        NOT NULL COMMENT '모임원 ID',
    meeting_id UUID        NOT NULL COMMENT '모임 ID',
    user_id    UUID        NOT NULL COMMENT '사용자 ID',
    role       VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '역할(CREATOR/MEMBER)',
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태(ACTIVE/KICKED)',
    joined_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '가입일시',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT uk_member_meeting_user UNIQUE (meeting_id, user_id),
    CONSTRAINT fk_member_meeting FOREIGN KEY (meeting_id) REFERENCES meeting (id),
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES user (id)
) COMMENT ='모임원';

-- 5. 활동 테이블
CREATE TABLE activity
(
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '활동 ID',
    type       VARCHAR(20) NOT NULL COMMENT '활동 유형',
    meeting_id UUID        NOT NULL COMMENT '모임 ID',
    creator_id UUID        NOT NULL COMMENT '생성자 ID',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT fk_activity_meeting FOREIGN KEY (meeting_id) REFERENCES meeting (id),
    CONSTRAINT fk_activity_creator FOREIGN KEY (creator_id) REFERENCES user (id)
) COMMENT ='활동';

-- 6. 응모권 테이블
CREATE TABLE ticket
(
    id         UUID        NOT NULL COMMENT '응모권 ID',
    user_id    UUID        NOT NULL COMMENT '사용자 ID',
    balance    INT         NOT NULL DEFAULT 0 COMMENT '잔액',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT uk_ticket_user UNIQUE (user_id),
    CONSTRAINT fk_ticket_user FOREIGN KEY (user_id) REFERENCES user (id)
) COMMENT ='응모권';

-- 7. 이벤트 참여 테이블
CREATE TABLE event_participation
(
    id         UUID        NOT NULL COMMENT '이벤트 참여 ID',
    event_id   UUID        NOT NULL COMMENT '이벤트 ID',
    user_id    UUID        NOT NULL COMMENT '사용자 ID',
    status     VARCHAR(20) NOT NULL DEFAULT 'PARTICIPATING' COMMENT '상태(PARTICIPATING/SUCCESS/FAILED/CLOSED)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT uk_event_participation_event_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_event_participation_event FOREIGN KEY (event_id) REFERENCES event (id),
    CONSTRAINT fk_event_participation_user FOREIGN KEY (user_id) REFERENCES user (id)
) COMMENT ='이벤트 참여';

-- 8. 지출 테이블
CREATE TABLE expense
(
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '지출 ID',
    activity_id  BIGINT NOT NULL COMMENT '활동 ID',
    creator_id   UUID           NOT NULL COMMENT '작성자 ID',
    amount       DECIMAL(10, 2) NOT NULL COMMENT '금액',
    category     VARCHAR(20)    NOT NULL COMMENT '카테고리',
    description  TEXT COMMENT '설명',
    expense_date DATE           NOT NULL COMMENT '지출일자',
    created_at   DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at   DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT fk_expense_activity FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT fk_expense_creator FOREIGN KEY (creator_id) REFERENCES user (id)
) COMMENT ='지출';

-- 9. 응모권 이력 테이블
CREATE TABLE ticket_history
(
    id          UUID        NOT NULL COMMENT '응모권 이력 ID',
    ticket_id   UUID        NOT NULL COMMENT '응모권 ID',
    meeting_id  UUID        NOT NULL COMMENT '모임 ID (지급 시에만 사용)',
    type        VARCHAR(20) NOT NULL COMMENT '이력 유형(EARNED/EXPIRED)',
    amount      INT         NOT NULL COMMENT '수량',
    description TEXT COMMENT '설명',
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    PRIMARY KEY (id),
    CONSTRAINT fk_ticket_history_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id),
    CONSTRAINT fk_ticket_history_meeting FOREIGN KEY (meeting_id) REFERENCES meeting (id)
) COMMENT ='응모권 이력';

-- 10. 결제 테이블
CREATE TABLE payment
(
    id                     UUID           NOT NULL COMMENT '결제 ID',
    event_participation_id UUID           NOT NULL COMMENT '이벤트 참여 ID',
    method                 VARCHAR(20)    NOT NULL COMMENT '결제 수단',
    status                 VARCHAR(20)    NOT NULL DEFAULT 'PENDING' COMMENT '상태(PENDING/COMPLETED/CANCELLED/FAILED)',
    amount                 DECIMAL(10, 2) NOT NULL COMMENT '금액',
    created_at             DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at             DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT uk_payment_event_participation UNIQUE (event_participation_id),
    CONSTRAINT fk_payment_event_participation FOREIGN KEY (event_participation_id) REFERENCES event_participation (id)
) COMMENT ='결제';

-- 11. 댓글 테이블
CREATE TABLE comment
(
    id         BIGINT      NOT NULL COMMENT '댓글 ID',
    expense_id BIGINT      NOT NULL COMMENT '지출 ID',
    writer_id  UUID        NOT NULL COMMENT '작성자 ID',
    content    TEXT        NOT NULL COMMENT '내용',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_expense FOREIGN KEY (expense_id) REFERENCES expense (id),
    CONSTRAINT fk_comment_writer FOREIGN KEY (writer_id) REFERENCES user (id)
) COMMENT ='댓글';

-- 12. 반응 테이블
CREATE TABLE reaction
(
    id         UUID        NOT NULL COMMENT '반응 ID',
    expense_id BIGINT        NOT NULL COMMENT '지출 ID',
    reactor_id UUID        NOT NULL COMMENT '반응자 ID',
    emoji      VARCHAR(10) NOT NULL COMMENT '이모지',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    PRIMARY KEY (id),
    CONSTRAINT uk_reaction_expense_reactor UNIQUE (expense_id, reactor_id),
    CONSTRAINT fk_reaction_expense FOREIGN KEY (expense_id) REFERENCES expense (id),
    CONSTRAINT fk_reaction_reactor FOREIGN KEY (reactor_id) REFERENCES user (id)
) COMMENT ='반응';

-- 13. 투표 테이블
CREATE TABLE vote
(
    id         UUID           NOT NULL COMMENT '투표 ID',
    meeting_id UUID           NOT NULL COMMENT '모임 ID',
    creator_id UUID           NOT NULL COMMENT '생성자 ID',
    content    TEXT           NOT NULL COMMENT '투표 내용',
    amount     DECIMAL(10, 2) NOT NULL COMMENT '예상 지출 금액',
    category   VARCHAR(20)    NOT NULL COMMENT '지출 카테고리',
    status     VARCHAR(20)    NOT NULL DEFAULT 'PROGRESS' COMMENT '상태(PROGRESS/APPROVED/REJECTED)',
    created_at DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT fk_vote_meeting FOREIGN KEY (meeting_id) REFERENCES meeting (id),
    CONSTRAINT fk_vote_creator FOREIGN KEY (creator_id) REFERENCES user (id)
) COMMENT ='투표';

-- 14. 투표 참여 테이블
CREATE TABLE vote_participation
(
    id         UUID        NOT NULL COMMENT '투표 참여 ID',
    vote_id    UUID        NOT NULL COMMENT '투표 ID',
    voter_id   UUID        NOT NULL COMMENT '투표자 ID',
    agree      BOOLEAN     NOT NULL COMMENT '찬성 여부',
    voted_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '투표일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT uk_vote_participation_vote_voter UNIQUE (vote_id, voter_id),
    CONSTRAINT fk_vote_participation_vote FOREIGN KEY (vote_id) REFERENCES vote (id),
    CONSTRAINT fk_vote_participation_voter FOREIGN KEY (voter_id) REFERENCES user (id)
) COMMENT ='투표 참여';

-- 15. 알림 테이블
CREATE TABLE notification
(
    id          UUID         NOT NULL COMMENT '알림 ID',
    receiver_id UUID         NOT NULL COMMENT '수신자 ID',
    type        VARCHAR(50)  NOT NULL COMMENT '알림 유형',
    message     TEXT         NOT NULL COMMENT '메시지',
    is_read     BOOLEAN      NOT NULL DEFAULT false COMMENT '읽음 여부',
    fcm_token   VARCHAR(255) NULL COMMENT 'FCM 토큰',
    data        JSON         NULL COMMENT '추가 데이터',
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_receiver FOREIGN KEY (receiver_id) REFERENCES user (id)
) COMMENT ='알림';


-- 16. 모임 초대 테이블
CREATE TABLE invite_links
(
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '초대 링크 ID',
    meeting_id UUID        NOT NULL COMMENT '모임 ID',
    code       VARCHAR(36) NOT NULL COMMENT '초대 코드',
    creator_id UUID        NOT NULL COMMENT '생성자 ID',
    expired_at DATETIME(6) NOT NULL COMMENT '만료일시',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '수정일시',
    PRIMARY KEY (id),
    CONSTRAINT uk_invite_links_code UNIQUE (code),
    CONSTRAINT fk_invite_links_meeting FOREIGN KEY (meeting_id) REFERENCES meeting (id),
    CONSTRAINT fk_invite_links_creator FOREIGN KEY (creator_id) REFERENCES user (id)
) COMMENT '초대 링크';

-- 인덱스 생성
CREATE INDEX idx_ticket_history_meeting ON ticket_history (meeting_id, created_at);
CREATE INDEX idx_meeting_creator ON meeting (creator_id);
CREATE INDEX idx_member_user ON member (user_id);
CREATE INDEX idx_member_meeting ON member (meeting_id);
CREATE INDEX idx_expense_creator ON expense (creator_id);
CREATE INDEX idx_activity_meeting ON activity (meeting_id);
CREATE INDEX idx_invite_links_expired_at ON invite_links (expired_at);