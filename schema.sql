CREATE DATABASE IF NOT EXISTS recruit DEFAULT CHARACTER SET utf8mb4;
USE recruit;

-- 候选人
CREATE TABLE IF NOT EXISTS candidate (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    email VARCHAR(128),
    resume_text LONGTEXT,
    file_path VARCHAR(512),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
    );

-- 岗位
CREATE TABLE IF NOT EXISTS job (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   title VARCHAR(128) NOT NULL,
    description TEXT,
    requirements TEXT,
    status TINYINT DEFAULT 1, -- 1 开放 0 关闭
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
    );

-- 投递记录
CREATE TABLE IF NOT EXISTS application_record (
                                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                  candidate_id BIGINT NOT NULL,
                                                  job_id BIGINT NOT NULL,
                                                  status VARCHAR(32) DEFAULT 'PENDING', -- PENDING/SCREENED/INTERVIEW/REJECTED
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
    );

-- 面试安排
CREATE TABLE IF NOT EXISTS interview (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                         candidate_id BIGINT NOT NULL,
                                         job_id BIGINT NOT NULL,
                                         interview_time DATETIME,
                                         interviewer VARCHAR(64),
    status VARCHAR(32) DEFAULT 'SCHEDULED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
    );
