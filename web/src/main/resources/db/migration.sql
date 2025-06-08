-- 数据库迁移脚本：将热门文章改为推荐文章
-- 执行时间：请在系统维护期间执行

-- 1. 备份数据（可选，但强烈建议）
-- CREATE TABLE article_backup AS SELECT * FROM article;

-- 2. 重命名字段：将 is_hot 重命名为 is_recommended
ALTER TABLE article CHANGE COLUMN is_hot is_recommended TINYINT(1) DEFAULT 0 COMMENT '是否为推荐文章 (0:否, 1:是)';

-- 3. 重命名索引（如果存在）
-- 注意：根据实际数据库结构调整索引名称
-- DROP INDEX idx_is_hot ON article;
-- CREATE INDEX idx_is_recommended ON article (is_recommended, created_at);

-- 4. 验证迁移结果
-- SELECT * FROM article WHERE is_recommended = 1 LIMIT 10;

-- 迁移完成！
-- 数据库字段已从 is_hot 更改为 is_recommended
-- 请确保应用程序代码也已同步更新 