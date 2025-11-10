CREATE TABLE department_tbl (
                                department_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                department_name VARCHAR(100) NOT NULL UNIQUE,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE member_tbl (
                            member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(50) NOT NULL UNIQUE,
                            password VARCHAR(255) NOT NULL,
                            name VARCHAR(100) NOT NULL,
                            department_id BIGINT,
                            email VARCHAR(100) UNIQUE,
                            role ENUM('ADMIN','MANAGER','USER') NOT NULL DEFAULT 'USER',
                            status ENUM('PENDING','ACTIVE','REJECTED','DELETED') NOT NULL DEFAULT 'PENDING',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            CONSTRAINT fk_department FOREIGN KEY (department_id) REFERENCES department_tbl(department_id)
);

CREATE TABLE category_tbl (
                              category_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                              category_code VARCHAR(50) NOT NULL UNIQUE,
                              label VARCHAR(100) NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sub_category_tbl (
                                  sub_category_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                  category_id BIGINT NOT NULL,
                                  label VARCHAR(100) NOT NULL,
                                  sub_category_code VARCHAR(20) NOT NULL,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  UNIQUE KEY uk_sub_category_code (sub_category_code),
                                  FOREIGN KEY (category_id) REFERENCES category_tbl(category_id)
                                      ON UPDATE CASCADE
                                      ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE equipment_tbl (
                               equipment_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                               sub_category_id BIGINT NOT NULL,
                               model VARCHAR(100) DEFAULT NULL,
                               model_code VARCHAR(100) DEFAULT NULL,
                               model_sequence BIGINT NOT NULL DEFAULT '0',
                               stock INT DEFAULT 0,
                               is_deleted TINYINT(1) DEFAULT 0,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               deleted_at DEFAULT NULL,
                               FOREIGN KEY (sub_category_id) REFERENCES sub_category_tbl(sub_category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `equipment_item_tbl` (
                                      `equipment_item_id` bigint NOT NULL AUTO_INCREMENT,
                                      `equipment_id` bigint NOT NULL,
                                      `serial_number` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
                                      `sequence` bigint NOT NULL,
                                      `status` enum('AVAILABLE','RENTED','REPAIRING','OUT_OF_STOCK','LOST') COLLATE utf8mb4_general_ci DEFAULT 'AVAILABLE',
                                      `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                      `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`equipment_item_id`),
                                      UNIQUE KEY `serial_number` (`serial_number`),
                                      KEY `fk_equipment` (`equipment_id`),
                                      CONSTRAINT `fk_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment_tbl` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `equipment_item_history_tbl` (
                                              `history_id` BIGINT NOT NULL AUTO_INCREMENT,
                                              `equipment_item_id` BIGINT NOT NULL,
                                              `changed_by` BIGINT NOT NULL,
                                              `old_status` ENUM('AVAILABLE','RENTED','REPAIRING','OUT_OF_STOCK','LOST') DEFAULT NULL,
                                              `new_status` ENUM('AVAILABLE','RENTED','REPAIRING','OUT_OF_STOCK','LOST') DEFAULT NULL,
                                              `rental_start_date` DATE DEFAULT NULL,
                                              `actual_return_date` DATE DEFAULT NULL,
                                              `rented_user_id` BIGINT DEFAULT NULL,
                                              `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              PRIMARY KEY (`history_id`),
                                              KEY `fk_equipment_item` (`equipment_item_id`),
                                              KEY `fk_member_change` (`changed_by`),
                                              KEY `fk_rented_user` (`rented_user_id`),
                                              CONSTRAINT `fk_equipment_item` FOREIGN KEY (`equipment_item_id`) REFERENCES `equipment_item_tbl` (`equipment_item_id`),
                                              CONSTRAINT `fk_member_change` FOREIGN KEY (`changed_by`) REFERENCES `member_tbl` (`member_id`),
                                              CONSTRAINT `fk_rented_user` FOREIGN KEY (`rented_user_id`) REFERENCES `member_tbl` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE rental_tbl (
                            rental_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            member_id BIGINT NOT NULL,
                            equipment_id BIGINT NOT NULL,
                            quantity INT UNSIGNED NOT NULL,
                            rental_reason TEXT,
                            status ENUM('PENDING','APPROVED','REJECTED','CANCELLED','COMPLETED') DEFAULT 'PENDING',
                            reject_reason TEXT,
                            approved_at TIMESTAMP NULL DEFAULT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            CONSTRAINT fk_rental_member FOREIGN KEY (member_id) REFERENCES member_tbl(member_id),
                            CONSTRAINT fk_rental_equipment FOREIGN KEY (equipment_id) REFERENCES equipment_tbl(equipment_id)
);

CREATE TABLE rental_item_tbl (
                                 rental_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 rental_id BIGINT NOT NULL,
                                 equipment_item_id BIGINT NOT NULL,
                                 start_date DATE NOT NULL,
                                 end_date DATE NOT NULL,
                                 status ENUM('RENTED', 'RETURNED', 'OVERDUE') DEFAULT 'RENTED',
                                 actual_return_date DATE,
                                 is_extended BOOLEAN DEFAULT FALSE,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_rental_item_rental FOREIGN KEY (rental_id) REFERENCES rental_tbl(rental_id),
                                 CONSTRAINT fk_rental_item_equipment_item FOREIGN KEY (equipment_item_id) REFERENCES equipment_item_tbl(equipment_item_id)
);

CREATE TABLE rental_item_overdue_tbl (
                                         overdue_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                         rental_item_id BIGINT NOT NULL,          -- 연체된 개별 대여 아이템
                                         overdue_days INT NOT NULL,               -- 연체 일수
                                         planned_end_date DATE NOT NULL,          -- 원래 반납 예정일
                                         actual_return_date DATE NOT NULL,        -- 실제 반납일
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         CONSTRAINT fk_overdue_rental_item
                                             FOREIGN KEY (rental_item_id) REFERENCES rental_item_tbl(rental_item_id)
);

CREATE TABLE `manager_scope_tbl` (
                                     `scope_id` bigint NOT NULL AUTO_INCREMENT,
                                     `manager_id` bigint NOT NULL,
                                     `category_id` bigint NOT NULL,
                                     PRIMARY KEY (`scope_id`),
                                     UNIQUE KEY `manager_id` (`manager_id`,`category_id`),
                                     KEY `category_id` (`category_id`),
                                     CONSTRAINT `manager_scope_tbl_ibfk_1` FOREIGN KEY (`manager_id`) REFERENCES `member_tbl` (`member_id`) ON DELETE CASCADE,
                                     CONSTRAINT `manager_scope_tbl_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `category_tbl` (`category_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE board_tbl (
                           board_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           writer_id BIGINT NOT NULL,
                           board_type ENUM('SUGGESTION','NOTICE') NOT NULL,
                           title VARCHAR(255) NOT NULL,
                           content TEXT NOT NULL,
                           status ENUM('PENDING','RESOLVED') DEFAULT 'PENDING',
                           is_deleted TINYINT(1) DEFAULT '0',
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           deleted_at TIMESTAMP NULL DEFAULT NULL,
                           CONSTRAINT fk_board_writer FOREIGN KEY (writer_id) REFERENCES member_tbl(member_id)
);

CREATE TABLE comment_tbl (
                             comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             board_id BIGINT NOT NULL,
                             writer_id BIGINT NOT NULL,
                             parent_id BIGINT DEFAULT NULL,
                             is_official BOOLEAN DEFAULT FALSE,
                             content TEXT NOT NULL,
                             is_deleted TINYINT(1) DEFAULT '0',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             deleted_at TIMESTAMP NULL DEFAULT NULL,
                             CONSTRAINT fk_comment_board FOREIGN KEY (board_id) REFERENCES board_tbl(board_id),
                             CONSTRAINT fk_comment_writer FOREIGN KEY (writer_id) REFERENCES member_tbl(member_id),
                             CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment_tbl(comment_id)
);

CREATE TABLE notification (
                              notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              member_id BIGINT NULL,
                              type VARCHAR(50) NOT NULL,
                              message TEXT NOT NULL,
                              link VARCHAR(255),
                              status ENUM('UNREAD','READ') DEFAULT 'UNREAD',
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES member(id)
);

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;