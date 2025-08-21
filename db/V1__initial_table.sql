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
                                title VARCHAR(255) NULL,
                                content LONGTEXT NULL,
                                summary TEXT NULL,
                                category ENUM('TECH','LIFE','NOTE') NULL,
                                cover_url VARCHAR(255) NULL,
                                tags_json JSON NULL,

    -- 弹窗相关
                                prompt_dismissed BOOLEAN DEFAULT FALSE,   -- 用户拒绝“调出”后置 true
                                is_deleted BOOLEAN DEFAULT FALSE,         -- 发布成功后置 true（软删）
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                last_edit_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                INDEX idx_user_latest (user_id, is_deleted, last_edit_time),
                                INDEX idx_user_not_deleted (user_id, is_deleted)
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

-- AI摘要表
CREATE TABLE ai_article_summaries (
                                      article_id BIGINT PRIMARY KEY,
                                      ai_summary TEXT NOT NULL,
                                      model VARCHAR(64) NOT NULL,
                                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE comments (
                          id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                          article_id   BIGINT NOT NULL,
                          user_id      BIGINT NOT NULL,
                          content      TEXT NOT NULL,
                          parent_id    BIGINT DEFAULT NULL,
                          is_deleted   BOOLEAN NOT NULL DEFAULT FALSE,   -- 软删除标记
                          is_pinned    BOOLEAN NOT NULL DEFAULT FALSE,   -- 是否置顶
                          create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 索引
                          INDEX idx_article_time (article_id, create_time),
                          INDEX idx_parent (parent_id),
                          INDEX idx_article_pinned (article_id, is_pinned, create_time) -- 方便查置顶+排序
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