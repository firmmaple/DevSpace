# DevSpace 项目变更记录

## 2024-01-XX - 交互系统架构重构：简化为纯RabbitMQ模式

### 变更概述

重构了文章交互系统（点赞、收藏、评论）的架构，从原来的"Spring事件 + RabbitMQ"双层架构简化为纯RabbitMQ模式，提高了系统的简洁性和可维护性。

### 变更原因

原架构存在以下问题：
1. **过度复杂**：Spring事件 → RabbitMQ消息的转换层是冗余的
2. **调试困难**：链路过长，问题排查复杂
3. **性能损耗**：多层转换影响性能
4. **维护成本高**：需要维护两套事件机制

### 架构变更

#### 变更前（双层架构）
```
Controller → Service → Spring Event → EventListener → RabbitMQ → Consumer → Database
```

#### 变更后（纯RabbitMQ）
```
Controller → Service → RabbitMQ → Consumer → Database
```

### 具体变更内容

#### 1. 删除的文件
- `core/src/main/java/org/jeffrey/core/event/ArticleInteractionEvent.java` - Spring事件基类
- `core/src/main/java/org/jeffrey/core/event/ArticleLikeEvent.java` - 点赞事件
- `core/src/main/java/org/jeffrey/core/event/ArticleCollectEvent.java` - 收藏事件  
- `core/src/main/java/org/jeffrey/core/event/ArticleCommentEvent.java` - 评论事件
- `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleInteractionEventListener.java` - 事件监听器

#### 2. 修改的文件

**ArticleServiceImpl.java**
- 添加了 `MQPublisher` 依赖
- 修改点赞/取消点赞方法：直接发送RabbitMQ消息而非Spring事件
- 修改收藏/取消收藏方法：直接发送RabbitMQ消息而非Spring事件
- 删除了对已删除Spring事件类的导入

**CommentServiceImpl.java**  
- 将 `ApplicationEventPublisher` 替换为 `MQPublisher`
- 修改评论添加方法：直接发送RabbitMQ消息而非Spring事件
- 删除了对已删除Spring事件类的导入

**ArticleInteractionConsumer.java**
- 添加了 `UserActivityService` 和 `ApplicationEventPublisher` 依赖
- 在处理点赞、收藏、评论消息时，直接发布 `UserActivityEvent` 记录用户活动

**UserActivityEventListener.java**
- 删除了对已删除Spring事件的监听方法
- 保留了对 `UserActivityEvent` 的监听（用于活动记录）

### 技术优势

#### 性能提升
- **减少序列化开销**：消除了Spring事件到RabbitMQ消息的转换
- **降低内存使用**：减少了中间对象的创建
- **提高响应速度**：减少了一层异步处理

#### 架构简化
- **单一消息机制**：只使用RabbitMQ，架构更清晰
- **调试友好**：消息流向更直接，问题定位更容易
- **代码简洁**：删除了冗余的事件转换代码

#### 可靠性保持
- **消息持久化**：RabbitMQ确保消息不丢失
- **失败重试**：保持原有的消息重试机制
- **事务一致性**：通过消息队列保证最终一致性

### 用户活动记录

用户活动记录功能保持不变：
- 点赞、收藏、评论操作在RabbitMQ消费者中触发 `UserActivityEvent`
- `UserActivityEventListener` 继续处理用户活动记录
- 文章创建、编辑、浏览等活动记录保持原有机制

### 兼容性说明

- **API接口**：无变化，前端无需修改
- **数据库结构**：无变化
- **消息格式**：无变化，使用相同的DTO
- **配置文件**：无变化，继续使用RabbitMQ配置

### 测试建议

重构后需要测试以下功能：
1. 文章点赞/取消点赞
2. 文章收藏/取消收藏
3. 发表评论和回复
4. 用户活动记录是否正常
5. 消息队列的错误处理和重试机制

### 后续优化建议

1. **监控增强**：添加RabbitMQ消息处理的监控指标
2. **性能测试**：对比重构前后的性能数据
3. **错误处理**：完善消息处理失败的告警机制
4. **文档更新**：更新README.md和DOCUMENT.md中的架构描述

### 影响的文档

需要更新以下文档中的架构描述：
- `README.md` 第3.8节 "文章互动系统"
- `DOCUMENT.md` 第6节 "事件驱动系统 (交互功能)"

这些文档中关于"Spring事件 + RabbitMQ"双层架构的描述需要更新为纯RabbitMQ架构。 

## 2024-12-19 - 热门文章系统重构为推荐文章系统

### 变更背景
项目中原有的"热门文章"概念在实际使用中发现名称不够准确，更适合称为"推荐文章"。热门通常指基于数据统计的自动排序，而推荐更符合管理员手动精选文章的业务逻辑。

### 主要变更

#### 1. 数据库结构变更
- **字段重命名**: `article.is_hot` → `article.is_recommended`
- **字段注释**: "是否为热门文章" → "是否为推荐文章"
- **索引更新**: `idx_is_hot` → `idx_is_recommended`

#### 2. 后端代码变更
- **实体类**: `ArticleDO.isHot` → `ArticleDO.isRecommended`
- **VO类**: `ArticleVO.isHot` → `ArticleVO.isRecommended`
- **VO类**: `ArticleSummaryVO.isHot` → `ArticleSummaryVO.isRecommended`
- **服务接口**: 
  - `getHotArticles()` → `getRecommendedArticles()`
  - `toggleArticleHotStatus()` → `toggleArticleRecommendedStatus()`
- **服务实现**: 对应的实现方法和数据库查询条件更新
- **REST API**: `/api/articles/hot` → `/api/articles/recommended`
- **管理员API**: 新增 `/api/admin/articles/{id}/recommended`

#### 3. 前端变更
- **管理后台**: 热门状态按钮改为推荐状态按钮
- **UI文本**: "热门" → "推荐"，"设为热门" → "设为推荐"
- **JavaScript**: 相关事件处理函数和API调用更新

#### 4. 新增功能
- **管理员推荐管理**: 管理员可以在后台管理页面切换文章的推荐状态
- **数据库迁移脚本**: 提供安全的字段重命名迁移方案

### 技术影响

#### 兼容性
- **数据库**: 需要执行迁移脚本 `web/src/main/resources/db/migration.sql`
- **API**: 旧的 `/api/articles/hot` 端点已移除，请使用 `/api/articles/recommended`
- **前端**: 无需前端代码变更，UI会自动适配

#### 性能影响
- **查询性能**: 索引重建后查询性能保持不变
- **功能性能**: 推荐文章获取逻辑保持一致

### 部署指导

1. **数据库迁移**:
   ```sql
   -- 建议在维护窗口执行
   ALTER TABLE article CHANGE COLUMN is_hot is_recommended TINYINT(1) DEFAULT 0 COMMENT '是否为推荐文章 (0:否, 1:是)';
   ```

2. **应用部署**: 正常部署新版本代码即可

3. **验证**:
   - 确认管理后台推荐功能正常
   - 确认首页推荐文章显示正常
   - 确认 API `/api/articles/recommended` 可用

### 相关文件变更清单
- `init.sql`: 数据库初始化脚本更新
- `ArticleDO.java`: 实体字段重命名
- `ArticleVO.java`, `ArticleSummaryVO.java`: 视图对象更新
- `ArticleService.java`: 服务接口方法重命名
- `ArticleServiceImpl.java`: 服务实现更新
- `ArticleRestController.java`: REST API 端点更新
- `AdminRestController.java`: 新增管理员推荐管理 API
- `HomeController.java`: 首页控制器注释更新
- `articles-management.html`: 管理后台模板更新
- `migration.sql`: 数据库迁移脚本

### 下一步计划
- 考虑基于浏览量、点赞数等数据实现真正的"热门文章"自动排序功能
- 可与现有的"推荐文章"形成互补，提供多种文章发现机制

---

## 2024-12-18 - 文章交互系统架构简化

### 变更背景
原有的文章交互系统采用了 Spring 事件 + RabbitMQ 的双层架构，存在不必要的复杂性和性能开销。为了简化系统架构并提高性能，决定重构为纯 RabbitMQ 架构。

### 架构对比

#### 原架构（Spring事件 + RabbitMQ）
```
Controller → Service → Spring Event → EventListener → RabbitMQ → Consumer → Database
```

#### 新架构（纯RabbitMQ）
```
Controller → Service → RabbitMQ → Consumer → Database
```

### 主要变更

#### 1. 删除的组件
- `ArticleInteractionEvent.java` - 基础交互事件类
- `ArticleLikeEvent.java` - 点赞事件类  
- `ArticleCollectEvent.java` - 收藏事件类
- `ArticleCommentEvent.java` - 评论事件类
- `ArticleInteractionEventListener.java` - 事件监听器（负责将Spring事件转换为RabbitMQ消息）

#### 2. 修改的组件

**ArticleServiceImpl.java**:
- 移除 `ApplicationEventPublisher` 依赖，添加 `MQPublisher` 依赖
- `likeArticle()` 和 `unlikeArticle()`: 直接发送 RabbitMQ 消息而非 Spring 事件
- `collectArticle()` 和 `uncollectArticle()`: 直接发送 RabbitMQ 消息而非 Spring 事件

**CommentServiceImpl.java**:
- 将 `ApplicationEventPublisher` 替换为 `MQPublisher`
- `createComment()`: 直接发送 RabbitMQ 消息到评论队列

**ArticleInteractionConsumer.java**:
- 添加 `UserActivityService` 依赖
- 在处理完数据库操作后，发布 `UserActivityEvent` 用于活动记录

**UserActivityEventListener.java**:
- 移除已删除事件的监听器方法
- 保留 `UserActivityEvent` 的处理逻辑

### 技术优势

#### 1. 架构简化
- **单一消息机制**: 只使用 RabbitMQ，避免了 Spring 事件到消息队列的转换层
- **调试友好**: 消息流向更直接，问题定位更容易
- **代码简洁**: 删除了冗余的事件转换代码

#### 2. 性能提升
- **减少序列化开销**: 消除了 Spring 事件到 RabbitMQ 消息的转换
- **降低内存使用**: 减少了中间对象的创建
- **提高响应速度**: 减少了一层异步处理

#### 3. 可靠性保持
- **消息持久化**: RabbitMQ 确保消息不丢失
- **失败重试**: 保持原有的消息重试机制
- **事务一致性**: 通过消息队列保证最终一致性

### 兼容性说明
- **API 接口**: 无变化，保持完全兼容
- **前端代码**: 无需修改
- **数据库结构**: 无变化
- **用户体验**: 功能行为完全一致

### 测试建议
1. **功能测试**: 验证点赞、收藏、评论功能正常
2. **性能测试**: 对比重构前后的响应时间
3. **消息队列**: 确认 RabbitMQ 消息正常处理
4. **活动记录**: 验证用户活动正常记录

### 部署注意事项
- 确保 RabbitMQ 服务正常运行
- 验证消息队列配置（交换机、队列、绑定关系）
- 监控消息消费情况，确保无消息积压

---

此记录将持续更新，记录 DevSpace 项目的重要功能变更和技术演进。 