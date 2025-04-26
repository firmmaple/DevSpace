# 功能变更记录

## 2023-10-11: 文章浏览量统计功能

### 新增功能
1. 实现基于Redis的文章浏览量统计功能
   - 使用Redis Hash结构存储文章ID和对应的浏览量
   - 每次访问文章详情页时自动增加浏览量并返回最新数值
   - 定时同步Redis中的浏览量数据到MySQL数据库

### 技术实现
1. 新增数据表
   - 创建`article_viewcount`表专门存储文章浏览量数据
   - 使用`articleId`作为唯一索引，支持高效查询
   - 增加针对`view_count`的降序索引以支持热门文章排序

2. 缓存设计
   - 使用Redis Hash结构 `article_views` 存储所有文章ID及其浏览量
   - 读取优先从Redis获取，Redis不存在则从数据库加载并缓存

3. 定时同步
   - 使用Spring的`@Scheduled`注解实现定时任务
   - 每5分钟同步一次Redis中的浏览量数据到MySQL
   - 记录详细的同步日志，包括更新和新增记录数

### 代码目录
- 数据表定义: `web/src/main/resources/db/init.sql`
- 实体类: `service/src/main/java/org/jeffrey/service/article/repository/entity/ArticleViewCountDO.java`
- Mapper: `service/src/main/java/org/jeffrey/service/article/repository/mapper/ArticleViewCountMapper.java`
- 服务接口: `service/src/main/java/org/jeffrey/service/article/service/ArticleViewCountService.java`
- 服务实现: `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleViewCountServiceImpl.java`
- 业务集成: 在`ArticleServiceImpl`中集成浏览量功能

### 后续优化计划
1. 引入布隆过滤器防止重复计数
2. 考虑添加基于IP或用户ID的去重逻辑
3. 实现热门文章推荐功能
4. 增加文章浏览量趋势统计

## 2023-10-12: 文章浏览量功能优化

### 改进内容
1. 修复Redis key常量使用
   - 定义并使用统一的常量`ARTICLE_VIEWS_KEY`管理Redis缓存键
   - 确保跨方法一致性，避免硬编码字符串

2. 完善浏览量数据聚合
   - 在文章列表页中显示真实浏览量，不再使用占位符
   - 改进`convertToSummaryVO`方法获取实时浏览量

3. 确保定时任务正常运行
   - 验证`@EnableScheduling`注解正确配置
   - 优化定时任务执行频率，保证数据同步效率

### 变更影响
- 提高浏览量统计的准确性和数据一致性
- 改进文章列表页的数据展示，反映实际浏览情况
- 加强缓存与数据库之间的数据同步可靠性

## 2023-10-13: 文章浏览量同步调度优化

### 改进内容
1. 创建专用调度器类
   - 新增`ArticleViewCountSyncScheduler`专门负责浏览量同步调度
   - 将定时任务与业务逻辑分离，实现关注点分离
   - 从`ArticleViewCountServiceImpl`移除`@Scheduled`注解

2. 增强调度策略
   - 实现两种同步策略：
     - 每5分钟执行一次增量同步，保证数据及时更新
     - 每天凌晨2点执行一次全量同步，保证数据完整性
   - 增加异常处理，提高系统健壮性

3. 改进日志记录
   - 添加详细的开始和完成日志
   - 记录异常信息，方便问题排查

### 代码目录
- 调度器: `service/src/main/java/org/jeffrey/service/scheduler/ArticleViewCountSyncScheduler.java`
- 服务实现: `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleViewCountServiceImpl.java`

### 变更影响
- 提高系统设计的模块化，便于维护和扩展
- 增强数据同步的可靠性和完整性
- 优化系统性能，通过错峰同步减少对数据库的影响
