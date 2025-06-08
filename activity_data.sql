-- Use the target database
USE dev_space;

-- Ensure consistent character set for JSON data if needed
/*!40101 SET NAMES utf8mb4 */;

-- ------------------------------------------------------
-- Populating User Data (Using existing data from dump)
-- ------------------------------------------------------
-- No new inserts needed for `user` table as per the dump.
-- Assuming users with IDs 1, 2, 3, 4 already exist.

-- ------------------------------------------------------
-- Populating Article Data (Using existing data from dump)
-- ------------------------------------------------------
-- No new inserts needed for `article` table as per the dump.
-- Assuming articles with IDs 1-10 and 1916... exist.

-- ------------------------------------------------------
-- Populating Article View Counts (Using/Updating existing data)
-- ------------------------------------------------------
-- The dump already contains view counts. We'll add activities later
-- that conceptually lead to these counts. We can update a few for demonstration.
LOCK TABLES `article_viewcount` WRITE;
/*!40000 ALTER TABLE `article_viewcount` DISABLE KEYS */;
-- Let's assume some more views happened recently
UPDATE `article_viewcount` SET view_count = view_count + 5 WHERE article_id = 1;
UPDATE `article_viewcount` SET view_count = view_count + 10 WHERE article_id = 5;
UPDATE `article_viewcount` SET view_count = view_count + 2 WHERE article_id = 1916250064104296450; -- Stream article
UPDATE `article_viewcount` SET view_count = view_count + 1 WHERE article_id = 1916258575991279617; -- Sort article
/*!40000 ALTER TABLE `article_viewcount` ENABLE KEYS */;
UNLOCK TABLES;

-- ------------------------------------------------------
-- Populating Article Likes
-- ------------------------------------------------------
LOCK TABLES `article_like` WRITE;
/*!40000 ALTER TABLE `article_like` DISABLE KEYS */;
INSERT INTO `article_like` (`article_id`, `user_id`, `created_at`) VALUES
                                                                       (5, 2, '2025-04-26 10:15:00'),  -- User 2 likes React Hooks article
                                                                       (1, 3, '2025-04-26 11:00:00'),  -- User 3 likes Spring Boot 3 article
                                                                       (5, 3, '2025-04-26 11:05:00'),  -- User 3 also likes React Hooks article
                                                                       (10, 4, '2025-04-26 14:30:00'), -- User 4 likes Design Patterns article
                                                                       (1916250064104296450, 3, '2025-04-27 09:00:00'), -- User 3 likes Stream article
                                                                       (1916258575991279617, 2, '2025-04-27 10:00:00'); -- User 2 likes Sort article
/*!40000 ALTER TABLE `article_like` ENABLE KEYS */;
UNLOCK TABLES;

-- ------------------------------------------------------
-- Populating Article Collects
-- ------------------------------------------------------
LOCK TABLES `article_collect` WRITE;
/*!40000 ALTER TABLE `article_collect` DISABLE KEYS */;
INSERT INTO `article_collect` (`article_id`, `user_id`, `created_at`) VALUES
                                                                          (10, 2, '2025-04-26 09:30:00'), -- User 2 collects Design Patterns article
                                                                          (3, 3, '2025-04-26 11:10:00'),  -- User 3 collects Microservices article
                                                                          (1, 4, '2025-04-27 08:00:00'),  -- User 4 collects Spring Boot 3 article
                                                                          (1916250064104296450, 4, '2025-04-27 09:05:00'), -- User 4 collects Stream article
                                                                          (5, 2, '2025-04-27 12:00:00');  -- User 2 collects React Hooks article
/*!40000 ALTER TABLE `article_collect` ENABLE KEYS */;
UNLOCK TABLES;

-- ------------------------------------------------------
-- Populating Comments (Adding to existing data)
-- ------------------------------------------------------
-- Existing comments from dump:
-- 1916338063366262786 (Article 10, User 2, Top Level)
-- 1916338229494255617 (Article 10, User 3, Reply to above)
-- 1916383047704379394 (Article 1916258575991279617, User 3, Top Level)

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` (`id`, `article_id`, `user_id`, `content`, `parent_id`, `created_at`, `updated_at`) VALUES
                                                                                                              (1916400000000000001, 1, 4, '非常期待 Spring Boot 3 的新特性！感谢分享。', NULL, '2025-04-26 15:00:00', '2025-04-26 15:00:00'), -- User 4 comments on Spring Boot 3
                                                                                                              (1916400000000000002, 1, 2, '确实，Jakarta EE 的迁移是个大工程。', 1916400000000000001, '2025-04-26 15:30:00', '2025-04-26 15:30:00'), -- User 2 (author) replies to User 4
                                                                                                              (1916400000000000003, 1916250064104296450, 4, 'Stream API 真的让代码简洁了很多！Optional 也很有用。', NULL, '2025-04-27 09:10:00', '2025-04-27 09:10:00'), -- User 4 comments on Stream article
                                                                                                              (1916400000000000004, 1916258575991279617, 4, 'Comparable 和 Comparator 的区别讲得很清楚。', NULL, '2025-04-27 10:30:00', '2025-04-27 10:30:00'); -- User 4 comments on Sort article
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

-- ------------------------------------------------------
-- Populating User Activity (Reflecting Creates, Edits, Views, Likes, Collects, Comments)
-- ------------------------------------------------------
LOCK TABLES `user_activity` WRITE;
/*!40000 ALTER TABLE `user_activity` DISABLE KEYS */;
INSERT INTO `user_activity` (`user_id`, `activity_type`, `target_id`, `extra_data`, `created_at`) VALUES
-- Article Creations (Assuming from original dump times)
(2, 'CREATE_ARTICLE', 1, NULL, '2023-07-15 02:30:00'),
(3, 'CREATE_ARTICLE', 2, NULL, '2023-07-20 06:45:00'),
(4, 'CREATE_ARTICLE', 3, NULL, '2023-08-05 01:15:00'),
(2, 'CREATE_ARTICLE', 4, NULL, '2023-08-10 08:20:00'),
(3, 'CREATE_ARTICLE', 5, NULL, '2023-08-15 03:30:00'),
(4, 'CREATE_ARTICLE', 6, NULL, '2023-08-20 05:45:00'),
(2, 'CREATE_ARTICLE', 7, NULL, '2023-08-25 07:10:00'),
(3, 'CREATE_ARTICLE', 8, NULL, '2023-09-01 02:20:00'),
(2, 'CREATE_ARTICLE', 9, NULL, '2023-09-05 06:30:00'),
(3, 'CREATE_ARTICLE', 10, NULL, '2023-09-10 08:45:00'),
(2, 'CREATE_ARTICLE', 1916244097308397569, NULL, '2025-04-26 21:32:49'),
(2, 'CREATE_ARTICLE', 1916250064104296450, NULL, '2025-04-26 21:56:32'),
(2, 'CREATE_ARTICLE', 1916258575991279617, NULL, '2025-04-26 22:30:21'),
(2, 'CREATE_ARTICLE', 1916259734944583681, NULL, '2025-04-26 22:34:57'),

-- Article Edits (Example: Article 1 was updated)
(2, 'EDIT_ARTICLE', 1, '{\"previous_update_time\": \"2023-07-15 02:30:00\"}', '2025-04-27 04:09:36'),

-- Simulated Views (Spread across two days)
-- Day 1: 2025-04-26
(4, 'VIEW_ARTICLE', 10, NULL, '2025-04-26 09:00:00'),
(2, 'VIEW_ARTICLE', 10, NULL, '2025-04-26 09:25:00'),
(3, 'VIEW_ARTICLE', 10, NULL, '2025-04-26 09:40:00'),
(2, 'VIEW_ARTICLE', 5, NULL, '2025-04-26 10:10:00'),
(3, 'VIEW_ARTICLE', 5, NULL, '2025-04-26 11:02:00'),
(3, 'VIEW_ARTICLE', 1, NULL, '2025-04-26 10:58:00'),
(4, 'VIEW_ARTICLE', 1, NULL, '2025-04-26 14:55:00'),
(2, 'VIEW_ARTICLE', 1, NULL, '2025-04-26 15:25:00'),
(3, 'VIEW_ARTICLE', 3, NULL, '2025-04-26 11:08:00'),
-- Day 2: 2025-04-27
(4, 'VIEW_ARTICLE', 10, NULL, '2025-04-27 07:30:00'), -- Another view for article 10
(3, 'VIEW_ARTICLE', 10, NULL, '2025-04-27 11:45:00'), -- For the comment reply
(4, 'VIEW_ARTICLE', 1, NULL, '2025-04-27 07:55:00'), -- View before collecting
(3, 'VIEW_ARTICLE', 1916250064104296450, NULL, '2025-04-27 08:55:00'), -- View Stream before liking
(4, 'VIEW_ARTICLE', 1916250064104296450, NULL, '2025-04-27 09:02:00'), -- View Stream before collecting/commenting
(2, 'VIEW_ARTICLE', 1916258575991279617, NULL, '2025-04-27 09:58:00'), -- View Sort before liking
(4, 'VIEW_ARTICLE', 1916258575991279617, NULL, '2025-04-27 10:28:00'), -- View Sort before commenting
(3, 'VIEW_ARTICLE', 1916258575991279617, NULL, '2025-04-27 14:40:00'), -- View Sort for existing comment
(2, 'VIEW_ARTICLE', 5, NULL, '2025-04-27 11:55:00'), -- View React Hooks before collecting

-- Likes (Matching article_like inserts)
(2, 'LIKE_ARTICLE', 5, NULL, '2025-04-26 10:15:00'),
(3, 'LIKE_ARTICLE', 1, NULL, '2025-04-26 11:00:00'),
(3, 'LIKE_ARTICLE', 5, NULL, '2025-04-26 11:05:00'),
(4, 'LIKE_ARTICLE', 10, NULL, '2025-04-26 14:30:00'),
(3, 'LIKE_ARTICLE', 1916250064104296450, NULL, '2025-04-27 09:00:00'),
(2, 'LIKE_ARTICLE', 1916258575991279617, NULL, '2025-04-27 10:00:00'),

-- Collects (Matching article_collect inserts)
(2, 'COLLECT_ARTICLE', 10, NULL, '2025-04-26 09:30:00'),
(3, 'COLLECT_ARTICLE', 3, NULL, '2025-04-26 11:10:00'),
(4, 'COLLECT_ARTICLE', 1, NULL, '2025-04-27 08:00:00'),
(4, 'COLLECT_ARTICLE', 1916250064104296450, NULL, '2025-04-27 09:05:00'),
(2, 'COLLECT_ARTICLE', 5, NULL, '2025-04-27 12:00:00'),

-- Comments (Matching comment inserts, target_id = article_id, extra_data has comment_id)
(2, 'COMMENT', 10, '{\"comment_id\": 1916338063366262786, \"parent_id\": null}', '2025-04-27 11:46:13'), -- Existing comment 1
(3, 'COMMENT', 10, '{\"comment_id\": 1916338229494255617, \"parent_id\": 1916338063366262786}', '2025-04-27 11:46:52'), -- Existing comment 2 (Reply)
(3, 'COMMENT', 1916258575991279617, '{\"comment_id\": 1916383047704379394, \"parent_id\": null}', '2025-04-27 14:44:58'), -- Existing comment 3
(4, 'COMMENT', 1, '{\"comment_id\": 1916400000000000001, \"parent_id\": null}', '2025-04-26 15:00:00'), -- New comment 1
(2, 'COMMENT', 1, '{\"comment_id\": 1916400000000000002, \"parent_id\": 1916400000000000001}', '2025-04-26 15:30:00'), -- New comment 2 (Reply)
(4, 'COMMENT', 1916250064104296450, '{\"comment_id\": 1916400000000000003, \"parent_id\": null}', '2025-04-27 09:10:00'), -- New comment 3
(4, 'COMMENT', 1916258575991279617, '{\"comment_id\": 1916400000000000004, \"parent_id\": null}', '2025-04-27 10:30:00'); -- New comment 4

/*!40000 ALTER TABLE `user_activity` ENABLE KEYS */;
UNLOCK TABLES;

-- ------------------------------------------------------
-- Populating Article Daily Stats (Aggregated from simulated activity)
-- ------------------------------------------------------
LOCK TABLES `article_daily_stats` WRITE;
/*!40000 ALTER TABLE `article_daily_stats` DISABLE KEYS */;
INSERT INTO `article_daily_stats` (`article_id`, `stat_date`, `view_count`, `like_count`, `collect_count`, `comment_count`) VALUES
-- Stats for 2025-04-26
(1, '2025-04-26', 3, 1, 0, 2),   -- Article 1: 3 views (user 3, 4, 2), 1 like (user 3), 2 comments (user 4, user 2)
(3, '2025-04-26', 1, 0, 1, 0),   -- Article 3: 1 view (user 3), 1 collect (user 3)
(5, '2025-04-26', 2, 2, 0, 0),   -- Article 5: 2 views (user 2, 3), 2 likes (user 2, 3)
(10, '2025-04-26', 3, 1, 1, 0),  -- Article 10: 3 views (user 4, 2, 3), 1 like (user 4), 1 collect (user 2)

-- Stats for 2025-04-27
(1, '2025-04-27', 1, 0, 1, 0),   -- Article 1: 1 view (user 4), 1 collect (user 4)
(5, '2025-04-27', 1, 0, 1, 0),   -- Article 5: 1 view (user 2), 1 collect (user 2)
(10, '2025-04-27', 2, 0, 0, 2),  -- Article 10: 2 views (user 4, 3), 2 comments (existing user 2, existing user 3)
(1916250064104296450, '2025-04-27', 2, 1, 1, 1), -- Stream: 2 views (user 3, 4), 1 like (user 3), 1 collect (user 4), 1 comment (user 4)
(1916258575991279617, '2025-04-27', 3, 1, 0, 2); -- Sort: 3 views (user 2, 4, 3), 1 like (user 2), 2 comments (existing user 3, new user 4)

/*!40000 ALTER TABLE `article_daily_stats` ENABLE KEYS */;
UNLOCK TABLES;

-- ------------------------------------------------------
-- Dump Completed - Data Population Finished
-- ------------------------------------------------------