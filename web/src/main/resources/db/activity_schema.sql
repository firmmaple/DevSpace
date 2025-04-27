-- 用户活动表
CREATE TABLE IF NOT EXISTS `user_activity` (
                                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                               `user_id` bigint NOT NULL COMMENT '用户ID',
                                               `activity_type` varchar(50) NOT NULL COMMENT '活动类型(CREATE_ARTICLE,EDIT_ARTICLE,VIEW_ARTICLE,LIKE_ARTICLE,COLLECT_ARTICLE,COMMENT)',
                                               `target_id` bigint NOT NULL COMMENT '目标ID(文章ID或评论ID)',
                                               `extra_data` JSON DEFAULT NULL COMMENT '额外数据(JSON格式)',
                                               `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               PRIMARY KEY (`id`),
                                               KEY `idx_user_id` (`user_id`),
                                               KEY `idx_target_id` (`target_id`),
                                               KEY `idx_activity_type` (`activity_type`),
                                               KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户活动表';

-- 文章每日统计表
CREATE TABLE IF NOT EXISTS `article_daily_stats` (
                                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                                     `article_id` bigint NOT NULL COMMENT '文章ID',
                                                     `stat_date` date NOT NULL COMMENT '统计日期',
                                                     `view_count` int NOT NULL DEFAULT 0 COMMENT '浏览量',
                                                     `like_count` int NOT NULL DEFAULT 0 COMMENT '点赞量',
                                                     `collect_count` int NOT NULL DEFAULT 0 COMMENT '收藏量',
                                                     `comment_count` int NOT NULL DEFAULT 0 COMMENT '评论量',
                                                     `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                     `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                     PRIMARY KEY (`id`),
                                                     UNIQUE KEY `uk_article_date` (`article_id`, `stat_date`),
                                                     KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章每日统计表';

-- 为user_activity表生成测试数据
CREATE PROCEDURE generate_user_activities()
proc_label: BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE article_count INT;
    DECLARE activity_types VARCHAR(255);
    DECLARE random_type VARCHAR(50);
    DECLARE random_user BIGINT;
    DECLARE random_article BIGINT;
    DECLARE random_date DATETIME;
    DECLARE random_extra JSON;

    -- 获取文章总数
    SELECT COUNT(*) INTO article_count FROM article WHERE status = 1;

    -- 如果没有文章，则退出
    IF article_count = 0 THEN
        SELECT 'No articles found. Exiting.' AS message;
        LEAVE proc_label;
    END IF;

    -- 定义活动类型数组
    SET activity_types = 'CREATE_ARTICLE,EDIT_ARTICLE,VIEW_ARTICLE,LIKE_ARTICLE,COLLECT_ARTICLE,COMMENT';

    -- 插入1000条随机活动记录
    WHILE i < 1000 DO
            -- 随机选择活动类型
            SET random_type = ELT(FLOOR(1 + RAND() * 6),
                                  'CREATE_ARTICLE', 'EDIT_ARTICLE', 'VIEW_ARTICLE',
                                  'LIKE_ARTICLE', 'COLLECT_ARTICLE', 'COMMENT');

            -- 随机选择用户ID (1-4)
            SET random_user = FLOOR(1 + RAND() * 4);

            -- 随机选择文章ID
            SELECT id INTO random_article FROM article WHERE status = 1 ORDER BY RAND() LIMIT 1;

            -- 随机日期 (过去30天内)
            SET random_date = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY);
            SET random_date = DATE_ADD(random_date, INTERVAL FLOOR(RAND() * 24) HOUR);
            SET random_date = DATE_ADD(random_date, INTERVAL FLOOR(RAND() * 60) MINUTE);

            -- 根据活动类型设置额外数据
            CASE random_type
                WHEN 'CREATE_ARTICLE' THEN SET random_extra = JSON_OBJECT('title', CONCAT('Generated Article ', FLOOR(RAND() * 1000)));
                WHEN 'EDIT_ARTICLE' THEN SET random_extra = JSON_OBJECT('title', CONCAT('Updated Article ', FLOOR(RAND() * 1000)));
                WHEN 'COMMENT' THEN SET random_extra = JSON_OBJECT('content', CONCAT('This is a sample comment ', FLOOR(RAND() * 100)));
                ELSE SET random_extra = NULL;
                END CASE;

            -- 插入活动记录
            INSERT INTO user_activity (user_id, activity_type, target_id, extra_data, created_at)
            VALUES (random_user, random_type, random_article, random_extra, random_date);

            SET i = i + 1;
        END WHILE;

    SELECT CONCAT('Generated ', i, ' user activities') AS message;
END;

-- 为article_daily_stats表生成测试数据
CREATE PROCEDURE generate_article_stats()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE article_id BIGINT;
    DECLARE curr_date DATE;
    DECLARE days_back INT;
    DECLARE random_views INT;
    DECLARE random_likes INT;
    DECLARE random_collects INT;
    DECLARE random_comments INT;

    -- 声明游标，获取所有已发布文章的ID
    DECLARE article_cursor CURSOR FOR
        SELECT id FROM article WHERE status = 1;

    -- 错误处理
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- 打开游标
    OPEN article_cursor;

    -- 开始循环处理每篇文章
    read_loop: LOOP
        -- 获取下一篇文章ID
        FETCH article_cursor INTO article_id;

        -- 如果没有更多文章，退出循环
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 为每篇文章生成过去30天的统计数据
        SET days_back = 0;
        WHILE days_back < 30 DO
                -- 设置当前日期
                SET curr_date = DATE_SUB(CURDATE(), INTERVAL days_back DAY);

                -- 生成随机统计数据
                SET random_views = FLOOR(10 + RAND() * 190);    -- 10-200 views
                SET random_likes = FLOOR(RAND() * (random_views / 10));  -- Likes are a fraction of views
                SET random_collects = FLOOR(RAND() * (random_likes / 2));  -- Collects are a fraction of likes
                SET random_comments = FLOOR(RAND() * (random_views / 20));  -- Comments are a fraction of views

                -- 添加一些趋势 - 越近的日期数据越多
                SET random_views = random_views + FLOOR((30 - days_back) * 2 * RAND());
                SET random_likes = random_likes + FLOOR((30 - days_back) * RAND());

                -- 插入统计数据
                INSERT INTO article_daily_stats
                (article_id, stat_date, view_count, like_count, collect_count, comment_count)
                VALUES
                    (article_id, curr_date, random_views, random_likes, random_collects, random_comments)
                ON DUPLICATE KEY UPDATE
                                     view_count = random_views,
                                     like_count = random_likes,
                                     collect_count = random_collects,
                                     comment_count = random_comments;

                SET days_back = days_back + 1;
            END WHILE;
    END LOOP;

    -- 关闭游标
    CLOSE article_cursor;

    SELECT 'Generated article statistics' AS message;
END;

-- 执行存储过程生成测试数据
CALL generate_user_activities();
CALL generate_article_stats();

-- 删除存储过程
DROP PROCEDURE IF EXISTS generate_user_activities;
DROP PROCEDURE IF EXISTS generate_article_stats;