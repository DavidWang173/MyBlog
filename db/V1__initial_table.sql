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
                          category_id BIGINT,
                          cover_url VARCHAR(255),
                          is_top BOOLEAN DEFAULT FALSE,
                          is_recommend BOOLEAN DEFAULT FALSE,
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
                                cover_url VARCHAR(255),
                                is_auto_saved BOOLEAN DEFAULT FALSE,
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                last_edit_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 文章种类表
CREATE TABLE article_categories (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    name VARCHAR(50) NOT NULL UNIQUE,
                                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 标签表
CREATE TABLE tags (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(50) NOT NULL UNIQUE,
                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 文章标签表
CREATE TABLE article_tags (
                              article_id BIGINT NOT NULL,
                              tag_id BIGINT NOT NULL,
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (article_id, tag_id)
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
                                   create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (user_id, article_id)
);

-- 文章浏览量表
CREATE TABLE article_views (
                               article_id BIGINT NOT NULL,
                               view_count BIGINT DEFAULT 0,
                               last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (article_id)
);

-- 评论表
CREATE TABLE comments (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          article_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL,
                          content TEXT NOT NULL,
                          parent_id BIGINT DEFAULT NULL,
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

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