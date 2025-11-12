-- 기본 권한 삽입
INSERT IGNORE INTO user_role (role_id, role_name) VALUES ('USER', 'USER');
INSERT IGNORE INTO user_role (role_id, role_name) VALUES ('ADMIN', 'ADMIN');

INSERT IGNORE INTO users (user_id, passwd, user_name, nickname, birth, phone, email, use_yn, del_yn, user_role, create_date, update_date)
VALUES ('admin', '$2a$10$l75Rleh6p5UdXuz6tcrYTeOaCzs8XdDcOvye/nyEL1a3NRx26CswO', '관리자', '관리자', '1997-07-26', '01011111111', 'admin@admin.com', 'Y',   'N', 'ADMIN', '2025-11-11', '2025-11-11');