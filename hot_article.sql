ALTER TABLE article ADD COLUMN is_hot TINYINT(1) DEFAULT 0 COMMENT '是否为热门文章 (0:否, 1:是)';
