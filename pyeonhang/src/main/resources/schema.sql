DROP DATABASE cp_db;
CREATE DATABASE cp_db;
USE cp_db;

-- 1) 권한
CREATE TABLE user_role (
                           role_id      VARCHAR(255) NOT NULL COMMENT '아이디',
                           role_name    VARCHAR(255) NOT NULL COMMENT '이름',
                           use_yn       CHAR(1) DEFAULT 'Y' COMMENT 'Y,N',
                           create_date  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                           update_date  DATETIME DEFAULT NULL COMMENT '수정일',
                           PRIMARY KEY (role_id)
);

-- 2) 사용자 (gender/addr/addr_detail 제거 반영, email UNIQUE, FK -> user_role)
CREATE TABLE users (
                       user_id        VARCHAR(100)  NOT NULL COMMENT '사용자 아이디',
                       passwd         VARCHAR(255)  NOT NULL COMMENT '패스워드',
                       user_name      VARCHAR(100)  NOT NULL COMMENT '사용자 이름',
                       nickname       VARCHAR(100)  NOT NULL COMMENT '닉네임',
                       birth          VARCHAR(100)  NOT NULL COMMENT '생년월일',
                       phone          VARCHAR(100)  NOT NULL COMMENT '전화번호',
                       email          VARCHAR(100)  NOT NULL COMMENT '이메일',
                       use_yn         CHAR(1) DEFAULT 'Y' COMMENT '사용여부,Y,N',
                       del_yn         CHAR(1) DEFAULT 'N' COMMENT '삭제여부,Y,N',
                       user_role      VARCHAR(50) DEFAULT 'USER' COMMENT '권한',
                       create_date    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                       update_date    DATETIME DEFAULT NULL COMMENT '수정일',
                       point_balance  INT DEFAULT 0 COMMENT '보유 포인트',
                       PRIMARY KEY (user_id),
                       UNIQUE KEY uk_users_email (email),
                       KEY user_rol_fk (user_role),
                       CONSTRAINT user_rol_fk FOREIGN KEY (user_role) REFERENCES user_role(role_id)
);

-- 3) 편의점 체인
CREATE TABLE chains (
                        chain_id    BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '편의점 아이디',
                        chain_code  VARCHAR(20)  NOT NULL UNIQUE COMMENT '편의점 코드(GS25/7-eleven/CU)',
                        chain_name  VARCHAR(100) NOT NULL COMMENT '편이점 이름'
);

-- 4) 카테고리
CREATE TABLE category (
                          category_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '카테고리 아이디',
                          category_code  VARCHAR(100) NOT NULL COMMENT '카테고리 코드',
                          category_name  VARCHAR(100) NOT NULL COMMENT '카테고리 이름',
                          use_yn         CHAR(1) DEFAULT 'Y' COMMENT '사용여부 Y,N'
);


-- 6) 크롤링 상품
CREATE TABLE craw_product (
                              crawl_id       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '크롤링 데이터 ID',
                              source_chain   VARCHAR(20)  NOT NULL COMMENT '출처 편의점 코드 (GS25 / CU / SEV 등)',
                              product_name   VARCHAR(200) NOT NULL COMMENT '상품명',
                              price          INT NOT NULL DEFAULT 0 COMMENT '상품 가격',
                              image_url      VARCHAR(500) NULL COMMENT '원본 페이지 이미지 URL',
                              promo_type     ENUM('ONE_PLUS_ONE','TWO_PLUS_ONE','GIFT','NONE','전체')
                NOT NULL DEFAULT 'NONE' COMMENT '행사 유형(1+1 / 2+1 / 덤증정 / 없음 / 전체)',
                              product_type   ENUM('DRINK','SNACK','FOOD','LIFE','NONE')
                NOT NULL DEFAULT 'NONE' COMMENT '상품 유형(음료/과자/음식/생활용품/없음)',
                              like_count     INT NOT NULL DEFAULT 0 COMMENT '좋아요 수',
                              crawled_at     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '수집 시각'
);
--제품 댓글
create table crawling_comment(
                                 comment_id int auto_increment primary key not null comment '댓글 아이디',
                                 crawl_id BIGINT not null comment '상품 아이디',
                                 user_id    VARCHAR(100) not null comment '사용자 아이디',
                                 content    text not null comment '댓글 내용',
                                 create_date  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '댓글 작성일',
                                 update_date  DATETIME      DEFAULT NULL COMMENT '댓글 수정일',
                                 CONSTRAINT fk_pc_product
                                     FOREIGN KEY (crawl_id) REFERENCES craw_product (crawl_id)
                                         ON DELETE CASCADE,
                                 CONSTRAINT fk_pc_user
                                     FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 7) 위시리스트 (users, craw_product FK, ON DELETE CASCADE)
CREATE TABLE wish_list (
                           user_id   VARCHAR(100) NOT NULL COMMENT '사용자 아이디',
                           crawl_id  BIGINT NOT NULL COMMENT '크롤링 데이터 ID',
                           PRIMARY KEY (user_id, crawl_id),
                           KEY fk_wish_user (user_id),
                           KEY fk_wish_crawl (crawl_id),
                           CONSTRAINT fk_wish_user  FOREIGN KEY (user_id)  REFERENCES users(user_id)          ON DELETE CASCADE,
                           CONSTRAINT fk_wish_crawl FOREIGN KEY (crawl_id) REFERENCES craw_product(crawl_id)  ON DELETE CASCADE
);

-- 8) 포인트 (update_date 추가 반영)
CREATE TABLE points (
                        id int auto_increment primary key comment '포인트 아이디',
                        user_id VARCHAR(100) NOT NULL COMMENT '사용자 ID',
                        source_type ENUM('ADMIN_GRANT','ATTENDANCE','COUPON_EXCHANGE') NOT NULL COMMENT '포인트 지급 유형',
                        amount      INT NOT NULL COMMENT '증감 포인트(+적립, -차감)',
                        reason      VARCHAR(200) NULL COMMENT '포인트 지급 사유',
                        create_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '포인트 지급 시각',
                        update_date DATETIME NULL ON UPDATE current_timestamp,
                        CONSTRAINT fk_points_user  FOREIGN KEY (user_id)  REFERENCES users(user_id)
);

-- 9) 게시판
CREATE TABLE board (
                       brd_id       INT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '게시판 아이디',
                       user_id      VARCHAR(100)  NOT NULL COMMENT '사용자 아이디',
                       title        VARCHAR(100) NOT NULL COMMENT '게시글 제목',
                       writer       VARCHAR(100) NOT NULL COMMENT '게시글 작성자',
                       contents     TEXT NOT NULL COMMENT '게시글 내용',
                       like_count   INT DEFAULT 0 COMMENT '추천수',
                       create_date  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                       update_date  DATETIME DEFAULT NULL COMMENT '수정일',
                       del_yn       CHAR(1) DEFAULT 'N' COMMENT '삭제여부 Y,N',
                       KEY fk_boards_author (writer),
                       CONSTRAINT fk_boards_author FOREIGN KEY (writer) REFERENCES users(user_id)
);

-- 10) 게시판 파일 (ON DELETE CASCADE)
CREATE TABLE board_files (
                             bf_id        INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '파일번호',
                             brd_id       INT NOT NULL COMMENT '게시글번호',
                             file_name    VARCHAR(255) NOT NULL COMMENT '파일 원본이름',
                             stored_name  VARCHAR(255) NOT NULL COMMENT '파일 저장이름',
                             file_path    VARCHAR(255) NOT NULL COMMENT '파일 경로',
                             file_size    BIGINT NOT NULL COMMENT '파일크기(byte)',
                             create_date  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                             KEY bf_fk (brd_id),
                             CONSTRAINT bf_fk FOREIGN KEY (brd_id) REFERENCES board(brd_id) ON DELETE CASCADE
);

-- 게시글 추천기능
CREATE TABLE board_like (
                            board_like_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 추천 ID',
                            brd_id        INT NOT NULL COMMENT '게시글 ID',
                            user_id       VARCHAR(100) NOT NULL COMMENT '추천한 사용자 ID',
                            create_date   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '추천 시각',
                            CONSTRAINT fk_board_like_board
                                FOREIGN KEY (brd_id) REFERENCES board(brd_id)
                                    ON DELETE CASCADE,
                            CONSTRAINT fk_board_like_user
                                FOREIGN KEY (user_id) REFERENCES users(user_id),
                            CONSTRAINT uk_board_like UNIQUE (brd_id, user_id)  -- 한 유저가 같은 글 두 번 못 누르게 유니크
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='게시글 추천 테이블';

-- 게시글 댓글
CREATE TABLE board_comment (
                               comment_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 아이디',
                               brd_id       INT NOT NULL COMMENT '게시글 아이디',
                               user_id      VARCHAR(100) NOT NULL COMMENT '작성자 아이디',
                               content      TEXT NOT NULL COMMENT '댓글 내용',
                               del_yn       CHAR(1) DEFAULT 'N' COMMENT '삭제 여부 (N: 사용, Y: 삭제)',
                               create_date  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '댓글 작성일',
                               update_date  DATETIME DEFAULT NULL COMMENT '댓글 수정일',
                               CONSTRAINT fk_bc_board
                                   FOREIGN KEY (brd_id) REFERENCES board(brd_id)
                                       ON DELETE CASCADE,
                               CONSTRAINT fk_bc_user
                                   FOREIGN KEY (user_id) REFERENCES users(user_id)
);


-- 11) 출석
CREATE TABLE attendance (
                            attendance_id   INT AUTO_INCREMENT PRIMARY KEY NOT NULL COMMENT '출석 아이디',
                            user_id         VARCHAR(100) NOT NULL COMMENT '사용자 아이디',
                            attendance_date DATE NOT NULL COMMENT '출석 일자',
                            points          INT NULL COMMENT '지급 포인트',
                            create_date     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                            KEY fk_att_user (user_id),
                            CONSTRAINT fk_att_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 12) 쿠폰
CREATE TABLE coupon (
                        coupon_id     INT AUTO_INCREMENT PRIMARY KEY COMMENT '쿠폰 아이디',
                        coupon_name   VARCHAR(200) NOT NULL COMMENT '쿠폰 이름',
                        description   TEXT NOT NULL COMMENT '쿠폰 설명',
                        required_point INT NOT NULL COMMENT '쿠폰 교환에 필요한 포인트',
                        create_date   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                        update_date   DATETIME DEFAULT NULL COMMENT '수정일'
);

-- 13) 쿠폰 파일 (update_date 컬럼 포함, ON DELETE CASCADE)
CREATE TABLE coupon_file (
                             cf_id        INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '쿠폰 파일 아이디',
                             coupon_id    INT NOT NULL COMMENT '쿠폰 아이디',
                             file_name    VARCHAR(255) NOT NULL COMMENT '원본 파일명',
                             stored_name  VARCHAR(255) NOT NULL COMMENT '저장 파일명(서버/UUID)',
                             file_path    VARCHAR(255) NOT NULL COMMENT '파일 경로(디렉터리)',
                             file_size    BIGINT NOT NULL COMMENT '파일 크기(byte)',
                             create_date  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                             update_date  DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
                             KEY fk_coupon_file_coupon (coupon_id),
                             CONSTRAINT fk_coupon_file_coupon FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id) ON DELETE CASCADE
);

-- 14) 사용자-쿠폰
CREATE TABLE user_coupon (
                             user_coupon_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 보유 쿠폰 ID',
                             user_id        VARCHAR(100) NOT NULL COMMENT '사용자 ID',
                             coupon_id      INT NOT NULL COMMENT '쿠폰 아이디',
                             acquired_at    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '교환한 시각->교환 내역 보기용',
                             KEY fk_user_coupon_user (user_id),
                             KEY fk_user_coupon_coupon (coupon_id),
                             CONSTRAINT fk_user_coupon_user   FOREIGN KEY (user_id)  REFERENCES users(user_id),
                             CONSTRAINT fk_user_coupon_coupon FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id)
);

-- 15) 배너
CREATE TABLE banner (
                        banner_id    VARCHAR(36) NOT null  PRIMARY KEY COMMENT '배너 아이디',
                        title        VARCHAR(200) NOT NULL COMMENT '배너 제목',
                        link_url     VARCHAR(500) NOT NULL COMMENT '클릭시 이동할 url',
                        use_yn       CHAR(1) DEFAULT 'Y' COMMENT '사용여부 Y/N',
                        img_url      VARCHAR(500) COMMENT '이미지 url',
                        banner_order int COMMENT '배너 순서',
                        create_date  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                        update_date  DATETIME DEFAULT NULL COMMENT '수정일'
);