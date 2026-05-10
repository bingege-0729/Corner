CREATE DATABASE IF NOT EXISTS corner_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE corner_db;

-- 用户信息表
CREATE TABLE IF NOT EXISTS user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 情绪标签字典表
CREATE TABLE IF NOT EXISTS emotion_tag_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    category VARCHAR(20) COMMENT '分类：氛围/功能/场景',
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情绪标签字典表';

-- 地点情绪库表
CREATE TABLE IF NOT EXISTS place_emotion_library (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地点ID',
    place_name VARCHAR(100) NOT NULL COMMENT '地点名称',
    address VARCHAR(200) COMMENT '地址',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    longitude DECIMAL(10, 7) COMMENT '经度',
    crowd_level VARCHAR(20) COMMENT '人流程度：低/中/高',
    best_time VARCHAR(50) COMMENT '最佳时间',
    one_sentence TEXT COMMENT '一句话描述',
    full_description TEXT COMMENT '完整描述',
    image_url VARCHAR(200) COMMENT '图片URL',
    tips TEXT COMMENT '小贴士'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点情绪库表';

-- 地点标签关联表
CREATE TABLE IF NOT EXISTS place_tag_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    place_id BIGINT NOT NULL COMMENT '地点ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    INDEX idx_place_id (place_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点标签关联表';

-- 用户地点记忆表
CREATE TABLE IF NOT EXISTS user_place_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记忆ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    place_id BIGINT NOT NULL COMMENT '地点ID',
    interaction_type VARCHAR(20) COMMENT '互动类型：VISITED/BOOKMARKED/DISLIKED',
    rating INT COMMENT '评分（1-5）',
    feedback TEXT COMMENT '反馈内容',
    visited_at DATE COMMENT '访问日期',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_place_id (place_id),
    UNIQUE KEY uk_user_place (user_id, place_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地点记忆表';

-- 插入示例标签数据
INSERT INTO emotion_tag_dict (tag_name, category) VALUES
('安静', '氛围'),
('放松', '氛围'),
('独处', '氛围'),
('放空', '功能'),
('发呆', '功能'),
('阅读', '功能'),
('思考', '功能'),
('湖边', '场景'),
('公园', '场景'),
('咖啡馆', '场景');

-- 插入示例地点数据
INSERT INTO place_emotion_library (place_name, address, latitude, longitude, crowd_level, best_time, one_sentence, full_description, image_url, tips) VALUES
('沙河公园湖边长椅', '南山区沙河西路', 22.5532, 113.9456, '低', '工作日下午', '下午三点有阳光，通常没人', '位于沙河公园北侧湖边，环境清幽，适合独自发呆或阅读。', '/images/place/shahe_changyi.jpg', '蚊虫较多，建议带驱蚊水'),
('南山图书馆角落', '南山区常兴路176号', 22.5331, 113.9231, '中', '全天', '靠窗位置能看到绿树', '图书馆三楼东侧有个安静的角落，靠窗可以看到外面的绿树，非常适合静心阅读。', '/images/place/nanshan_library.jpg', '需要保持安静，手机调静音'),
('华侨城创意园咖啡座', '南山区侨城东路', 22.5445, 113.9678, '中', '周末下午', '露天座位很惬意', '创意园内的露天咖啡座，周围都是绿植和艺术装置，适合放松心情。', '/images/place/oct_coffee.jpg', '周末人较多，建议早点去');

-- 插入示例地点标签关联
INSERT INTO place_tag_relation (place_id, tag_id) VALUES
(1, 1), -- 沙河公园-安静
(1, 2), -- 沙河公园-放松
(1, 4), -- 沙河公园-放空
(2, 1), -- 图书馆-安静
(2, 5), -- 图书馆-阅读
(2, 6), -- 图书馆-思考
(3, 2), -- 创意园-放松
(3, 7), -- 创意园-咖啡馆
(3, 9); -- 创意园-公园
