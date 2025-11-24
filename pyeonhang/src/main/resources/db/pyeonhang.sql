CREATE TABLE `user_role` (
                             `role_id` varchar(255) NOT NULL COMMENT '아이디',
                             `role_name` varchar(255) NOT NULL COMMENT '이름',
                             `use_yn` char(1) DEFAULT NULL,
                             `create_date` datetime DEFAULT current_timestamp() COMMENT '생성일',
                             `update_date` datetime DEFAULT NULL COMMENT '수정일',
                             PRIMARY KEY (`role_id`)
);


CREATE TABLE `users` (
                         `user_id` varchar(100) NOT NULL COMMENT '사용자 아이디',
                         `passwd` varchar(255) NOT NULL COMMENT '패스워드',
                         `user_name` varchar(100) NOT NULL COMMENT '사용자 이름',
                         `nickname` varchar(100) NOT NULL COMMENT '닉네임',
                         `birth` varchar(100) NOT NULL COMMENT '생년월일',
                         `phone` varchar(100) NOT NULL COMMENT '전화번호',
                         `email` varchar(100) NOT NULL COMMENT '이메일',
                         `del_yn` char(1) DEFAULT 'N' COMMENT '삭제여부,Y,N',
                         `user_role` varchar(50) DEFAULT 'USER' COMMENT '권한',
                         `create_date` datetime DEFAULT current_timestamp() COMMENT '생성일',
                         `update_date` datetime DEFAULT NULL COMMENT '수정일',
                         `point_balance` int(11) DEFAULT 0 COMMENT '보유 포인트',
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `uk_users_email` (`email`),
                         KEY `user_rol_fk` (`user_role`),
                         CONSTRAINT `user_rol_fk` FOREIGN KEY (`user_role`) REFERENCES `user_role` (`role_id`)
);

CREATE TABLE `email` (
                         `id` bigint(20) NOT NULL AUTO_INCREMENT,
                         `code_hash` varchar(64) NOT NULL,
                         `created_at` datetime(6) DEFAULT NULL,
                         `email` varchar(200) NOT NULL,
                         `expires_at` datetime(6) NOT NULL,
                         `verified_yn` char(1) DEFAULT NULL,
                         `user_id` varchar(255) NOT NULL,
                         PRIMARY KEY (`id`),
                         KEY `FKah6v1juek8jb9ycg8cldv15d6` (`user_id`),
                         CONSTRAINT `FKah6v1juek8jb9ycg8cldv15d6` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `points` (
                          `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '포인트 아이디',
                          `user_id` varchar(100) NOT NULL COMMENT '사용자 ID',
                          `source_type` enum('ADMIN_GRANT','ATTENDANCE','COUPON_EXCHANGE') NOT NULL COMMENT '포인트 지급 유형',
                          `amount` int(11) NOT NULL COMMENT '증감 포인트(+적립, -차감)',
                          `reason` varchar(255) DEFAULT NULL,
                          `create_date` datetime DEFAULT current_timestamp() COMMENT '포인트 지급 시각',
                          `update_date` datetime DEFAULT NULL ON UPDATE current_timestamp(),
                          PRIMARY KEY (`id`),
                          KEY `fk_points_user` (`user_id`),
                          CONSTRAINT `fk_points_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);


CREATE TABLE `attendance` (
                              `attendance_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '출석 아이디',
                              `user_id` varchar(100) NOT NULL COMMENT '사용자 아이디',
                              `attendance_date` date NOT NULL COMMENT '출석 일자',
                              `points` int(11) DEFAULT NULL COMMENT '지급 포인트',
                              `create_date` datetime DEFAULT current_timestamp() COMMENT '생성일',
                              PRIMARY KEY (`attendance_id`),
                              KEY `fk_att_user` (`user_id`),
                              CONSTRAINT `fk_att_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);


CREATE TABLE `board` (
                         `brd_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '게시판 아이디',
                         `title` varchar(100) NOT NULL COMMENT '게시글 제목',
                         `contents` text NOT NULL COMMENT '게시글 내용',
                         `like_count` int(11) NOT NULL DEFAULT 0 COMMENT '좋아요 수',
                         `user_id` varchar(100) NOT NULL COMMENT '작성자 아이디',
                         `create_date` datetime DEFAULT current_timestamp() COMMENT '생성일',
                         `update_date` datetime DEFAULT NULL ON UPDATE current_timestamp(),
                         `best_yn` char(1) DEFAULT 'N' COMMENT '채택여부 Y,N',
                         `notice_yn` char(1) DEFAULT 'N' COMMENT '공지여부Y,N',
                         `temp_yn` CHAR(1) NOT NULL DEFAULT 'N' COMMENT '임시글 여부',
                         PRIMARY KEY (`brd_id`),
                         KEY `fk_board_writer` (`user_id`),
                         CONSTRAINT `fk_board_writer` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `board_cloudinary` (
                                    `cloudinary_id` varchar(255) NOT NULL,
                                    `img_url` varchar(500) DEFAULT NULL,
                                    `brd_id` int(11) NOT NULL,
                                    PRIMARY KEY (`cloudinary_id`),
                                    KEY `fk_board` (`brd_id`),
                                    CONSTRAINT `fk_board` FOREIGN KEY (`brd_id`) REFERENCES `board` (`brd_id`) ON DELETE CASCADE
);

CREATE TABLE `board_comment` (
                                 `comment_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '댓글 아이디',
                                 `brd_id` int(11) NOT NULL COMMENT '게시글 아이디',
                                 `user_id` varchar(100) NOT NULL COMMENT '사용자 아이디',
                                 `contents` text NOT NULL COMMENT '댓글 내용',
                                 `create_date` datetime DEFAULT current_timestamp() COMMENT '댓글 작성일',
                                 `update_date` datetime DEFAULT NULL COMMENT '댓글 수정일',
                                 PRIMARY KEY (`comment_id`),
                                 KEY `fk_board_comment_board_` (`brd_id`),
                                 KEY `fk_board_comment_user` (`user_id`),
                                 CONSTRAINT `fk_board_comment_board_` FOREIGN KEY (`brd_id`) REFERENCES `board` (`brd_id`) ON DELETE CASCADE,
                                 CONSTRAINT `fk_board_comment_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `board_like` (
                              `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                              `brd_id` INT(11) NOT NULL,
                              `user_id` VARCHAR(100) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uq_board_like` (`brd_id`, `user_id`),
                              KEY `fk_board_like_user` (`user_id`),
                              KEY `fk_board_like_board` (`brd_id`),
                              CONSTRAINT `fk_board_like_board`
                                  FOREIGN KEY (`brd_id`)
                                      REFERENCES `board` (`brd_id`)
                                      ON DELETE CASCADE,
                              CONSTRAINT `fk_board_like_user`
                                  FOREIGN KEY (`user_id`)
                                      REFERENCES `users` (`user_id`)
                                      ON DELETE CASCADE
);

CREATE TABLE `coupon` (
                          `coupon_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '쿠폰 아이디',
                          `coupon_name` varchar(200) NOT NULL COMMENT '쿠폰 이름',
                          `description` text NOT NULL COMMENT '쿠폰 설명',
                          `required_point` int(11) NOT NULL COMMENT '쿠폰 교환에 필요한 포인트',
                          `create_date` datetime DEFAULT current_timestamp() COMMENT '생성일',
                          `update_date` datetime DEFAULT NULL COMMENT '수정일',
                          `cloudinary_id` varchar(36) DEFAULT NULL COMMENT '이미지 업로드 아이디',
                          `img_url` varchar(500) DEFAULT NULL COMMENT '이미지 url',
                          PRIMARY KEY (`coupon_id`)
);
CREATE TABLE `user_coupon` (
                               `user_coupon_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '사용자 보유 쿠폰 ID',
                               `user_id` varchar(100) NOT NULL COMMENT '사용자 ID',
                               `coupon_id` int(11) NOT NULL COMMENT '쿠폰 아이디',
                               `acquired_at` datetime DEFAULT current_timestamp() COMMENT '교환한 시각->교환 내역 보기용',
                               PRIMARY KEY (`user_coupon_id`),
                               KEY `fk_user_coupon_user` (`user_id`),
                               KEY `fk_user_coupon_coupon` (`coupon_id`),
                               CONSTRAINT `fk_user_coupon_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`coupon_id`),
                               CONSTRAINT `fk_user_coupon_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);


CREATE TABLE `craw_product` (
                                `crawl_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '크롤링 데이터 ID',
                                `source_chain` varchar(255) DEFAULT NULL,
                                `product_name` varchar(255) DEFAULT NULL,
                                `price` int(11) NOT NULL DEFAULT 0 COMMENT '상품 가격',
                                `image_url` varchar(255) DEFAULT NULL,
                                `promo_type` enum('ONE_PLUS_ONE','TWO_PLUS_ONE','GIFT','NONE','전체') NOT NULL DEFAULT 'NONE' COMMENT '행사 유형(1+1 / 2+1 / 덤증정 / 없음 / 전체)',
                                `product_type` enum('DRINK','SNACK','FOOD','LIFE','NONE') NOT NULL DEFAULT 'NONE' COMMENT '상품 유형(음료/과자/음식/생활용품/없음)',
                                `like_count` int(11) NOT NULL DEFAULT 0 COMMENT '좋아요 수',
                                `crawled_at` datetime DEFAULT current_timestamp() COMMENT '수집 시각',
                                PRIMARY KEY (`crawl_id`)
);

CREATE TABLE `crawling_comment` (
                                    `comment_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '댓글 아이디',
                                    `crawl_id` bigint(20) NOT NULL COMMENT '상품 아이디',
                                    `user_id` varchar(100) NOT NULL COMMENT '사용자 아이디',
                                    `content` text NOT NULL COMMENT '댓글 내용',
                                    `create_date` datetime DEFAULT current_timestamp() COMMENT '댓글 작성일',
                                    `update_date` datetime DEFAULT NULL COMMENT '댓글 수정일',
                                    PRIMARY KEY (`comment_id`),
                                    KEY `fk_pc_product` (`crawl_id`),
                                    KEY `fk_pc_user` (`user_id`),
                                    CONSTRAINT `fk_pc_product` FOREIGN KEY (`crawl_id`) REFERENCES `craw_product` (`crawl_id`) ON DELETE CASCADE,
                                    CONSTRAINT `fk_pc_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `wish_list` (
                             `user_id` varchar(100) NOT NULL COMMENT '사용자 아이디',
                             `crawl_id` bigint(20) NOT NULL COMMENT '크롤링 데이터 ID',
                             PRIMARY KEY (`user_id`,`crawl_id`),
                             KEY `fk_wish_user` (`user_id`),
                             KEY `fk_wish_crawl` (`crawl_id`),
                             CONSTRAINT `fk_wish_crawl` FOREIGN KEY (`crawl_id`) REFERENCES `craw_product` (`crawl_id`) ON DELETE CASCADE,
                             CONSTRAINT `fk_wish_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
);


CREATE TABLE `banner` (
                          `banner_id` varchar(255) NOT NULL,
                          `title` varchar(255) DEFAULT NULL,
                          `link_url` text NOT NULL COMMENT '클릭시 이동할 url',
                          `use_yn` char(1) DEFAULT NULL,
                          `img_url` varchar(255) DEFAULT NULL,
                          `banner_order` int(11) DEFAULT NULL COMMENT '배너 순서',
                          `create_date` datetime DEFAULT current_timestamp() COMMENT '생성일',
                          `update_date` datetime DEFAULT NULL COMMENT '수정일',
                          PRIMARY KEY (`banner_id`)
);


CREATE TABLE `category` (
                            `category_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '카테고리 아이디',
                            `category_code` varchar(255) DEFAULT NULL,
                            `category_name` varchar(255) DEFAULT NULL,
                            `use_yn` char(1) DEFAULT NULL,
                            PRIMARY KEY (`category_id`)
);


INSERT IGNORE INTO users (user_id, passwd, user_name, nickname, birth, phone, email, del_yn, user_role, create_date, update_date)
VALUES ('admin', '$2a$10$l75Rleh6p5UdXuz6tcrYTeOaCzs8XdDcOvye/nyEL1a3NRx26CswO', '관리자', '관리자', '1997-07-26', '01011111111', 'admin@admin.com', 'N', 'ADMIN', '2025-11-11', '2025-11-11');

INSERT IGNORE INTO user_role (role_id, role_name) VALUES ('USER', 'USER');
INSERT IGNORE INTO user_role (role_id, role_name) VALUES ('ADMIN', 'ADMIN');
