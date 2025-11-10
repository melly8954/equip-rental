SET NAMES utf8mb4;

CREATE TABLE `department_tbl` (
                                  `department_id` bigint NOT NULL AUTO_INCREMENT,
                                  `department_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
                                  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`department_id`),
                                  UNIQUE KEY `department_name` (`department_name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `member_tbl` (
                              `member_id` bigint NOT NULL AUTO_INCREMENT,
                              `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                              `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                              `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
                              `department_id` bigint DEFAULT NULL,
                              `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
                              `role` enum('ADMIN','MANAGER','USER') COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'USER',
                              `status` enum('PENDING','ACTIVE','DELETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
                              `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`member_id`),
                              UNIQUE KEY `username` (`username`),
                              UNIQUE KEY `email` (`email`),
                              KEY `fk_department` (`department_id`),
                              CONSTRAINT `fk_department` FOREIGN KEY (`department_id`) REFERENCES `department_tbl` (`department_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `category_tbl` (
                                `category_id` bigint NOT NULL AUTO_INCREMENT,
                                `category_code` varchar(100) NOT NULL,
                                `label` varchar(100) NOT NULL,
                                `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`category_id`),
                                UNIQUE KEY `category_code` (`category_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `sub_category_tbl` (
                                    `sub_category_id` bigint NOT NULL AUTO_INCREMENT,
                                    `category_id` bigint NOT NULL,
                                    `label` varchar(100) NOT NULL,
                                    `sub_category_code` varchar(10) NOT NULL,
                                    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`sub_category_id`),
                                    UNIQUE KEY `uk_sub_category_code` (`sub_category_code`),
                                    KEY `category_id` (`category_id`),
                                    CONSTRAINT `sub_category_tbl_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category_tbl` (`category_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `manager_scope_tbl` (
                                     `scope_id` bigint NOT NULL AUTO_INCREMENT,
                                     `manager_id` bigint NOT NULL,
                                     `category_id` bigint NOT NULL,
                                     PRIMARY KEY (`scope_id`),
                                     UNIQUE KEY `manager_id` (`manager_id`,`category_id`),
                                     KEY `category_id` (`category_id`),
                                     CONSTRAINT `manager_scope_tbl_ibfk_1` FOREIGN KEY (`manager_id`) REFERENCES `member_tbl` (`member_id`) ON DELETE CASCADE,
                                     CONSTRAINT `manager_scope_tbl_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `category_tbl` (`category_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `equipment_tbl` (
                                 `equipment_id` bigint NOT NULL AUTO_INCREMENT,
                                 `sub_category_id` bigint DEFAULT NULL,
                                 `model` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                 `model_code` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                 `model_sequence` bigint NOT NULL DEFAULT '0',
                                 `stock` int DEFAULT '0',
                                 `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
                                 `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `deleted_at` datetime DEFAULT NULL,
                                 PRIMARY KEY (`equipment_id`),
                                 KEY `fk_sub_category` (`sub_category_id`),
                                 CONSTRAINT `fk_sub_category` FOREIGN KEY (`sub_category_id`) REFERENCES `sub_category_tbl` (`sub_category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `equipment_item_tbl` (
                                      `equipment_item_id` bigint NOT NULL AUTO_INCREMENT,
                                      `equipment_id` bigint NOT NULL,
                                      `serial_number` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                      `sequence` bigint NOT NULL,
                                      `status` enum('AVAILABLE','RENTED','REPAIRING','OUT_OF_STOCK','LOST') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'AVAILABLE',
                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                      `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`equipment_item_id`),
                                      UNIQUE KEY `serial_number` (`serial_number`),
                                      KEY `fk_equipment` (`equipment_id`),
                                      CONSTRAINT `fk_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment_tbl` (`equipment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `equipment_item_history_tbl` (
                                              `history_id` bigint NOT NULL AUTO_INCREMENT,
                                              `equipment_item_id` bigint NOT NULL,
                                              `changed_by` bigint NOT NULL,
                                              `old_status` enum('AVAILABLE','RENTED','REPAIRING','OUT_OF_STOCK','LOST') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                              `new_status` enum('AVAILABLE','RENTED','REPAIRING','OUT_OF_STOCK','LOST') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                              `rental_start_date` date DEFAULT NULL,
                                              `actual_return_date` date DEFAULT NULL,
                                              `rented_user_id` bigint DEFAULT NULL,
                                              `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                              `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              PRIMARY KEY (`history_id`),
                                              KEY `fk_equipment_item` (`equipment_item_id`),
                                              KEY `fk_member_change` (`changed_by`),
                                              KEY `fk_rented_user` (`rented_user_id`),
                                              CONSTRAINT `fk_equipment_item` FOREIGN KEY (`equipment_item_id`) REFERENCES `equipment_item_tbl` (`equipment_item_id`),
                                              CONSTRAINT `fk_member_change` FOREIGN KEY (`changed_by`) REFERENCES `member_tbl` (`member_id`),
                                              CONSTRAINT `fk_rented_user` FOREIGN KEY (`rented_user_id`) REFERENCES `member_tbl` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `rental_tbl` (
                              `rental_id` bigint NOT NULL AUTO_INCREMENT,
                              `member_id` bigint NOT NULL,
                              `equipment_id` bigint NOT NULL,
                              `request_start_date` date NOT NULL,
                              `request_end_date` date NOT NULL,
                              `quantity` int unsigned NOT NULL,
                              `rental_reason` text COLLATE utf8mb4_general_ci,
                              `status` enum('PENDING','APPROVED','REJECTED','CANCELLED','COMPLETED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'PENDING',
                              `reject_reason` text COLLATE utf8mb4_general_ci,
                              `approved_at` timestamp NULL DEFAULT NULL,
                              `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`rental_id`),
                              KEY `fk_rental_member` (`member_id`),
                              KEY `fk_rental_equipment` (`equipment_id`),
                              CONSTRAINT `fk_rental_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment_tbl` (`equipment_id`),
                              CONSTRAINT `fk_rental_member` FOREIGN KEY (`member_id`) REFERENCES `member_tbl` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `rental_item_tbl` (
                                   `rental_item_id` bigint NOT NULL AUTO_INCREMENT,
                                   `rental_id` bigint NOT NULL,
                                   `equipment_item_id` bigint NOT NULL,
                                   `start_date` date NOT NULL,
                                   `end_date` date NOT NULL,
                                   `status` enum('RENTED','RETURNED','OVERDUE') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'RENTED',
                                   `actual_return_date` date DEFAULT NULL,
                                   `is_extended` tinyint(1) DEFAULT '0',
                                   `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                   `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`rental_item_id`),
                                   KEY `fk_rental_item_rental` (`rental_id`),
                                   KEY `fk_rental_item_equipment_item` (`equipment_item_id`),
                                   CONSTRAINT `fk_rental_item_equipment_item` FOREIGN KEY (`equipment_item_id`) REFERENCES `equipment_item_tbl` (`equipment_item_id`),
                                   CONSTRAINT `fk_rental_item_rental` FOREIGN KEY (`rental_id`) REFERENCES `rental_tbl` (`rental_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `rental_item_overdue_tbl` (
                                           `overdue_id` bigint NOT NULL AUTO_INCREMENT,
                                           `rental_item_id` bigint NOT NULL,
                                           `overdue_days` int NOT NULL,
                                           `planned_end_date` date NOT NULL,
                                           `actual_return_date` date DEFAULT NULL,
                                           `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                           `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                           PRIMARY KEY (`overdue_id`),
                                           KEY `fk_overdue_rental_item` (`rental_item_id`),
                                           CONSTRAINT `fk_overdue_rental_item` FOREIGN KEY (`rental_item_id`) REFERENCES `rental_item_tbl` (`rental_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `board_tbl` (
                             `board_id` bigint NOT NULL AUTO_INCREMENT,
                             `writer_id` bigint NOT NULL,
                             `board_type` enum('SUGGESTION','NOTICE') COLLATE utf8mb4_general_ci NOT NULL,
                             `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                             `content` text COLLATE utf8mb4_general_ci NOT NULL,
                             `status` enum('PENDING','RESOLVED') COLLATE utf8mb4_general_ci DEFAULT 'PENDING',
                             `is_deleted` tinyint(1) DEFAULT '0',
                             `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                             `deleted_at` timestamp NULL DEFAULT NULL,
                             PRIMARY KEY (`board_id`),
                             KEY `fk_board_writer` (`writer_id`),
                             CONSTRAINT `fk_board_writer` FOREIGN KEY (`writer_id`) REFERENCES `member_tbl` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `comment_tbl` (
                               `comment_id` bigint NOT NULL AUTO_INCREMENT,
                               `board_id` bigint NOT NULL,
                               `writer_id` bigint NOT NULL,
                               `parent_id` bigint DEFAULT NULL,
                               `is_official` tinyint(1) DEFAULT '0',
                               `content` text COLLATE utf8mb4_general_ci NOT NULL,
                               `is_deleted` tinyint(1) DEFAULT '0',
                               `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               `deleted_at` timestamp NULL DEFAULT NULL,
                               PRIMARY KEY (`comment_id`),
                               KEY `fk_comment_board` (`board_id`),
                               KEY `fk_comment_writer` (`writer_id`),
                               KEY `fk_comment_parent` (`parent_id`),
                               CONSTRAINT `fk_comment_board` FOREIGN KEY (`board_id`) REFERENCES `board_tbl` (`board_id`),
                               CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment_tbl` (`comment_id`),
                               CONSTRAINT `fk_comment_writer` FOREIGN KEY (`writer_id`) REFERENCES `member_tbl` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `file_tbl` (
                            `file_id` bigint NOT NULL AUTO_INCREMENT,
                            `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                            `related_id` bigint DEFAULT NULL,
                            `original_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                            `unique_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                            `file_order` int unsigned DEFAULT NULL,
                            `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                            `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                            `file_size` bigint DEFAULT NULL,
                            `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (`file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE `notification_tbl` (
                                    `notification_id` bigint NOT NULL AUTO_INCREMENT,
                                    `member_id` bigint DEFAULT NULL,
                                    `type` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
                                    `message` text COLLATE utf8mb4_general_ci NOT NULL,
                                    `link` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
                                    `status` enum('UNREAD','READ') COLLATE utf8mb4_general_ci DEFAULT 'UNREAD',
                                    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`notification_id`),
                                    KEY `fk_member` (`member_id`),
                                    CONSTRAINT `fk_member` FOREIGN KEY (`member_id`) REFERENCES `member_tbl` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


INSERT INTO department_tbl (department_id, department_name) VALUES
                                                                (1, '소프트웨어공학과'),
                                                                (2, '정보통신학과'),
                                                                (3, '전기공학과'),
                                                                (4, 'AI게임융합학과'),
                                                                (5, 'IoT전자공학과');

INSERT INTO category_tbl (category_id, category_code, label) VALUES
                                                                 (1, 'FURN', '가구'),
                                                                 (3, 'IT', 'IT'),
                                                                 (4, 'MEDIA', '멀티미디어'),
                                                                 (5, 'EDU', '교육/실습용 장비'),
                                                                 (6, 'MOBILE', '모바일 장치');

INSERT INTO sub_category_tbl (sub_category_id, category_id, label, sub_category_code) VALUES
                                                                                          (1, 1, '의자', 'CHAIR'),
                                                                                          (2, 1, '책상', 'DESK'),
                                                                                          (3, 1, '책장', 'CABINET'),
                                                                                          (4, 1, '책꽂이', 'BOOKSHELF'),
                                                                                          (5, 1, '수납장', 'DRAWER'),
                                                                                          (7, 1, '파티션', 'PARTITION'),
                                                                                          (8, 1, '사물함', 'LOCKER'),
                                                                                          (10, 1, '선반', 'SHELF'),
                                                                                          (20, 3, '컴퓨터 본체', 'PC'),
                                                                                          (21, 3, '노트북', 'LAPTOP'),
                                                                                          (22, 3, '모니터', 'MONITOR'),
                                                                                          (23, 3, '입출력 장치', 'IODEVICE'),
                                                                                          (25, 3, '저장장치', 'STORAGE'),
                                                                                          (26, 3, '그래픽카드', 'GPU'),
                                                                                          (29, 4, '빔프로젝터', 'PROJECTOR'),
                                                                                          (30, 4, '카메라', 'CAMERA'),
                                                                                          (32, 4, '게임 장비', 'GAMEDEV'),
                                                                                          (33, 5, '교육용 IoT 장비', 'IOT'),
                                                                                          (34, 5, '드론 장비', 'DRONE'),
                                                                                          (35, 5, 'AR VR 장비', 'ARVR_HW'),
                                                                                          (38, 6, '태블릿', 'TABLET'),
                                                                                          (39, 6, '스마트폰', 'SMART');

