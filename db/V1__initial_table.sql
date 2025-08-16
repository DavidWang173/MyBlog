-- 用户表
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL,
                       nickname VARCHAR(50) NOT NULL,
                       avatar VARCHAR(255),
                       signature VARCHAR(255),
                       role ENUM('USER', 'ADMIN') DEFAULT 'USER',
                       register_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 文章表
CREATE TABLE articles (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          title VARCHAR(255) NOT NULL,
                          content TEXT NOT NULL,
                          summary TEXT,
                          category ENUM('TECH', 'LIFE', 'NOTE') NOT NULL,
                          cover_url VARCHAR(255),
                          is_top BOOLEAN DEFAULT FALSE,
                          is_recommend BOOLEAN DEFAULT FALSE,
                          view_count BIGINT DEFAULT 0,
                          like_count BIGINT DEFAULT 0,
                          comment_count BIGINT DEFAULT 0,
                          status ENUM('PUBLISHED', 'DELETED') DEFAULT 'PUBLISHED',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 文章草稿表
CREATE TABLE article_drafts (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                title VARCHAR(255),
                                content TEXT,
                                summary TEXT,
                                category ENUM('TECH','LIFE','NOTE'),
                                cover_url VARCHAR(255),
                                tags_json JSON,                 -- ["Java","Redis"]
                                is_auto_saved BOOLEAN DEFAULT FALSE, -- 最近一次保存方式，仅做标记
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                last_edit_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                UNIQUE KEY uk_user (user_id),
                                INDEX idx_user_edit (user_id, last_edit_time DESC)
);

-- 标签表
CREATE TABLE tags (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(50) NOT NULL UNIQUE,
                      is_system BOOLEAN DEFAULT TRUE COMMENT '是否系统内置标签',
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 文章-标签多对多关联
CREATE TABLE article_tags (
                              article_id BIGINT NOT NULL,
                              tag_id BIGINT NOT NULL,
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (article_id, tag_id),
                              KEY idx_article_tags_article (article_id),
                              KEY idx_article_tags_tag (tag_id)
);

-- 文章点赞表
CREATE TABLE article_likes (
                               user_id BIGINT NOT NULL,
                               article_id BIGINT NOT NULL,
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (user_id, article_id)
);

-- 文章收藏表
CREATE TABLE article_favorites (
                                   user_id BIGINT NOT NULL,
                                   article_id BIGINT NOT NULL,
                                   is_deleted BOOLEAN DEFAULT FALSE,
                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   PRIMARY KEY (user_id, article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE comments (
                          id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                          article_id   BIGINT NOT NULL,
                          user_id      BIGINT NOT NULL,
                          content      TEXT NOT NULL,
                          parent_id    BIGINT DEFAULT NULL,
                          create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 索引：仅你要求的两个
                          INDEX idx_article_time (article_id, create_time),
                          INDEX idx_parent (parent_id)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- 评论点赞表
CREATE TABLE comment_likes (
                               user_id BIGINT NOT NULL,
                               comment_id BIGINT NOT NULL,
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (user_id, comment_id)
);

-- 用户操作表
CREATE TABLE user_operations (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 user_id BIGINT NOT NULL,
                                 operation_type VARCHAR(50) NOT NULL,
                                 target_type VARCHAR(50),
                                 target_id BIGINT,
                                 ip_address VARCHAR(50),
                                 user_agent VARCHAR(255),
                                 operation_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户关注关系表
CREATE TABLE user_follows (
                              follower_id BIGINT NOT NULL,
                              following_id BIGINT NOT NULL,
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (follower_id, following_id)
);

-- 用户浏览记录表
CREATE TABLE user_history (
                              user_id BIGINT NOT NULL,
                              article_id BIGINT NOT NULL,
                              view_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (user_id, article_id)
);

-- 用户扩展信息表
CREATE TABLE user_profiles (
                               user_id BIGINT PRIMARY KEY,
                               article_count INT DEFAULT 0,
                               comment_count INT DEFAULT 0,
                               fan_count INT DEFAULT 0,
                               follow_count INT DEFAULT 0,
                               level INT DEFAULT 1,
                               experience BIGINT DEFAULT 0,
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);