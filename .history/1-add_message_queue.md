CHANGE.md是一个用于记录项目更新内容的文件。
存放最新更改，但是尚未合并到README.md和DOCUMENT.md的更新内容。

# 项目变更记录：Spring事件 + RabbitMQ 异步文章交互系统

## 1. 变更概述

本次更新为 DevSpace 项目添加了基于 Spring 事件和 RabbitMQ 消息队列的异步处理系统，以支持文章点赞、收藏和评论功能。该实现采用了事件驱动架构，将交互操作与业务逻辑解耦，提高系统响应速度和可扩展性。

## 2. 架构设计

### 2.1 事件驱动流程

1. **交互触发**: 用户在前端进行点赞、收藏或评论操作
2. **事件发布**: 相应的 Controller 调用 Service 方法，Service 发布相应的 Spring 事件
3. **事件监听**: 事件监听器捕获事件并将其转换为消息发送到 RabbitMQ
4. **消息消费**: 消息消费者异步处理消息，执行数据库操作
5. **结果更新**: 下次用户加载文章时，从数据库获取最新交互状态

### 2.2 主要组件

- **Spring 事件**: 用于应用内事件通知
- **RabbitMQ**: 实现可靠的异步消息处理
- **事件监听器**: 将事件转换为消息
- **消息消费者**: 异步处理交互逻辑

## 3. 实现细节

### 3.1 Spring 事件

创建了基础的交互事件类和具体实现：

- `ArticleInteractionEvent`: 交互事件基类
- `ArticleLikeEvent`: 点赞事件
- `ArticleCollectEvent`: 收藏事件
- `ArticleCommentEvent`: 评论事件

### 3.2 消息队列配置

配置了 RabbitMQ 相关的交换机、队列和绑定：

- 交换机: `interaction.exchange`
- 队列:
  - `article.like.queue`: 文章点赞队列
  - `article.collect.queue`: 文章收藏队列
  - `article.comment.queue`: 文章评论队列

### 3.3 数据库设计

添加了三个表来存储交互数据：

- `article_like`: 存储用户点赞记录
- `article_collect`: 存储用户收藏记录
- `comment`: 存储用户评论，支持嵌套回复

### 3.4 API 设计

新增了以下 REST API 端点：

- 点赞相关:
  - `POST /api/articles/{id}/like`: 点赞文章
  - `DELETE /api/articles/{id}/like`: 取消点赞

- 收藏相关:
  - `POST /api/articles/{id}/collect`: 收藏文章
  - `DELETE /api/articles/{id}/collect`: 取消收藏

- 评论相关:
  - `POST /api/comments`: 发表评论
  - `GET /api/comments/article/{articleId}`: 获取文章评论
  - `DELETE /api/comments/{id}`: 删除评论

## 4. 文件变更

### 4.1 配置文件

- `web/src/main/resources/application.yml`: 添加 RabbitMQ 配置
- `core/pom.xml`: 添加 RabbitMQ 相关依赖
- `web/src/main/resources/init.sql`: 添加新表结构

### 4.2 新增文件

- **消息队列相关**:
  - `core/src/main/java/org/jeffrey/core/mq/RabbitMQConfig.java`: RabbitMQ 配置
  - `core/src/main/java/org/jeffrey/core/mq/MQPublisher.java`: 消息发布服务

- **事件相关**:
  - `core/src/main/java/org/jeffrey/core/event/ArticleInteractionEvent.java`: 交互事件基类
  - `core/src/main/java/org/jeffrey/core/event/ArticleLikeEvent.java`: 点赞事件
  - `core/src/main/java/org/jeffrey/core/event/ArticleCollectEvent.java`: 收藏事件
  - `core/src/main/java/org/jeffrey/core/event/ArticleCommentEvent.java`: 评论事件

- **DTO/VO**:
  - `api/src/main/java/org/jeffrey/api/dto/interaction/LikeDTO.java`: 点赞数据传输对象
  - `api/src/main/java/org/jeffrey/api/dto/interaction/CollectDTO.java`: 收藏数据传输对象
  - `api/src/main/java/org/jeffrey/api/dto/interaction/CommentDTO.java`: 评论数据传输对象
  - `api/src/main/java/org/jeffrey/api/vo/comment/CommentVO.java`: 评论视图对象

- **实体类**:
  - `service/src/main/java/org/jeffrey/service/article/repository/entity/ArticleLikeDO.java`: 点赞实体
  - `service/src/main/java/org/jeffrey/service/article/repository/entity/ArticleCollectDO.java`: 收藏实体
  - `service/src/main/java/org/jeffrey/service/article/repository/entity/CommentDO.java`: 评论实体

- **Mapper**:
  - `service/src/main/java/org/jeffrey/service/article/repository/mapper/ArticleLikeMapper.java`: 点赞Mapper
  - `service/src/main/java/org/jeffrey/service/article/repository/mapper/ArticleCollectMapper.java`: 收藏Mapper
  - `service/src/main/java/org/jeffrey/service/article/repository/mapper/CommentMapper.java`: 评论Mapper
  - `service/src/main/resources/mapper/ArticleLikeMapper.xml`: 点赞SQL映射
  - `service/src/main/resources/mapper/ArticleCollectMapper.xml`: 收藏SQL映射
  - `service/src/main/resources/mapper/CommentMapper.xml`: 评论SQL映射

- **服务相关**:
  - `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleInteractionEventListener.java`: 事件监听器
  - `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleInteractionConsumer.java`: 消息消费者
  - `service/src/main/java/org/jeffrey/service/article/service/CommentService.java`: 评论服务接口
  - `service/src/main/java/org/jeffrey/service/article/service/impl/CommentServiceImpl.java`: 评论服务实现

- **控制器**:
  - `web/src/main/java/org/jeffrey/web/article/CommentRestController.java`: 评论REST控制器

### 4.3 修改文件

- `service/src/main/java/org/jeffrey/service/article/service/ArticleService.java`: 添加交互相关方法
- `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleServiceImpl.java`: 实现交互相关方法
- `service/src/main/java/org/jeffrey/service/user/service/UserService.java`: 添加批量获取用户信息方法
- `service/src/main/java/org/jeffrey/service/user/service/impl/UserServiceImpl.java`: 实现批量获取用户信息方法
- `web/src/main/java/org/jeffrey/web/article/ArticleRestController.java`: 添加交互相关端点

## 5. 运行和测试

### 5.1 运行要求

- 需要一个运行中的 RabbitMQ 服务器，默认配置为 localhost:5672，用户名/密码: guest/guest
- 启动应用前需确保数据库中已创建相应表结构

### 5.2 测试示例

#### 点赞文章

```bash
# 点赞文章
curl -X POST http://localhost:8088/api/articles/1/like \
  -H "Content-Type: application/json" \
  -b "jwt_token=<your_token>"

# 取消点赞
curl -X DELETE http://localhost:8088/api/articles/1/like \
  -H "Content-Type: application/json" \
  -b "jwt_token=<your_token>"
```

#### 发表评论

```bash
# 发表评论
curl -X POST http://localhost:8088/api/comments \
  -H "Content-Type: application/json" \
  -b "jwt_token=<your_token>" \
  -d '{"articleId": 1, "content": "这是一条评论", "parentId": null}'
```

## 6. 未来改进

1. 添加针对点赞、收藏和评论的单元测试和集成测试
2. 实现评论内容过滤功能（敏感词过滤等）
3. 增加用户通知系统，通知被点赞或评论的作者
4. 添加缓存层，减少数据库访问提高性能
5. 为消息队列添加死信队列和重试机制

## 7. 前端实现与改进

### 7.1 异步处理与实时UI更新

因为后端采用了事件驱动和消息队列的异步架构，前端需要采用特殊策略来提供良好的用户体验：

#### 乐观UI更新 (Optimistic UI Updates)

为解决用户执行交互操作（点赞、评论等）后需要等待异步处理完成的问题，前端实现了"乐观UI更新"策略：

1. **即时反馈**：用户执行操作后，UI立即更新，无需等待服务器响应
2. **临时状态**：新创建的内容（如评论）显示"发布中"状态
3. **后台同步**：同时发送请求到服务器进行实际处理
4. **延迟验证**：服务器处理完成后，前端重新加载数据以确保显示的是最新状态
5. **错误处理**：如果服务器处理失败，撤销乐观更新并显示错误信息

这种方式大大提升了用户体验，使异步系统在前端表现得如同同步系统一样响应迅速。

### 7.2 安全增强

前端实现了多项安全增强措施：

1. **移除显式Token处理**：利用 `AuthUtils.authenticatedFetch` 统一处理认证，无需在请求中手动添加 JWT token
2. **权限检查**：根据当前用户状态动态显示/隐藏操作按钮
3. **错误反馈**：为用户操作提供清晰的成功/失败反馈
4. **防止重复提交**：在操作处理过程中禁用相关按钮
5. **数据验证**：在客户端进行基本数据验证，减少无效请求

## 8. 已知问题与限制

1. **异步延迟**：由于使用消息队列，操作结果可能需要短暂延迟才能在系统中完全生效
2. **无离线支持**：当前实现不支持离线操作，用户需要保持网络连接
3. **内存占用**：大量评论的文章可能导致前端内存占用较高，需考虑分页加载优化
4. **复杂嵌套回复**：当前支持一级回复，但多层级嵌套回复可能导致UI布局问题

## 9. 部署注意事项

1. **消息队列依赖**：确保RabbitMQ服务正常运行，否则交互功能将无法正常工作
2. **数据库索引**：为 `article_like`、`article_collect` 和 `comment` 表创建适当索引以提高查询性能
3. **缓存配置**：考虑为热门文章配置缓存以减轻数据库负担
4. **前端资源**：确保正确部署前端静态资源，特别是JS文件

## 10. 未来计划

1. **实时通知**：通过WebSocket向用户推送实时通知
2. **高级评论功能**：支持评论编辑、富文本格式、@用户、表情等
3. **缓存层**：添加Redis缓存以提高高流量场景下的性能
4. **用户体验优化**：实现无限滚动、虚拟列表等技术以优化大量数据的展示
5. **统计分析**：为文章互动数据提供统计分析功能

## 11. 问题修复记录

### 11.1 评论回复功能修复 (2025-04-24)

修复了用户无法回复评论的问题：

1. **问题描述**：
   - 用户点击评论的"回复"按钮，填写内容并提交后，系统返回错误：`Parent comment not found with ID: XXXXXXX`
   - 服务器日志中显示 `ResourceNotFoundException: Parent comment not found with ID`
   - 导致所有评论回复功能无法使用

2. **原因分析**：
   - 前端传递父评论ID时使用了`parseInt()`函数解析ID
   - 评论ID可能是超出JavaScript `parseInt()`安全整数范围的长整型数值
   - 导致ID在传输过程中精度丢失或转换不正确

3. **解决方案**：
   - 将`parseInt(parentId)`改为`Number(parentId)`处理评论ID
   - `Number()`函数可以更好地处理大整数，保持数值精度
   - 保证前端传递的ID格式与后端期望的格式保持一致

4. **实施细节**：
   - 修改 `ui/src/main/resources/templates/articles/detail.html` 中的 `submitReply` 函数
   - 更新API请求参数处理逻辑

5. **防止类似问题**：
   - 对于可能包含大整数ID的API请求，应避免使用`parseInt()`函数
   - 考虑使用字符串类型传递ID，或在后端接收后进行适当的类型转换
   - 在开发新功能时注意JavaScript中处理大整数的限制

### 11.2 评论回复ID精度问题修复 (2025-04-24)

进一步修复了评论回复功能中的ID精度问题：

1. **问题深入分析**：
   - 在尝试回复评论时，即使使用 `Number()` 函数处理评论ID，仍然出现父评论找不到的错误
   - 发现从服务器返回的评论ID (如：1915372463303286785) 在前端JavaScript中被显示为近似值 (1915372463303286800)
   - 这是因为JavaScript的Number类型对于超过16位的整数无法保持精确值，会出现精度丢失

2. **根本原因**：
   - JavaScript的Number类型基于IEEE 754标准的双精度浮点数
   - 能够精确表示的最大整数是`Number.MAX_SAFE_INTEGER` (9007199254740991，即2^53-1)
   - 评论ID使用的长整型值(Long)超出了这个范围，导致在JavaScript处理过程中精度丢失

3. **解决方案**：
   - 将所有涉及评论ID的处理从数值类型改为字符串类型
   - 在提交回复请求时，使用`String()`确保ID不会因为数值转换而丢失精度
   - 在DOM中保存原始ID字符串，避免数值转换

4. **实施细节**：
   - 修改`showReplyForm`函数，在元素的dataset中保存原始评论ID
   - 修改`submitReply`函数，从dataset中获取原始ID并使用`String()`转换
   - 将API请求中的`parentId`参数显式设置为字符串类型

5. **后续建议**：
   - 对于大整数ID，始终在前端以字符串形式处理
   - 考虑在后端API响应中直接将大整数ID序列化为字符串
   - 如需在前端进行ID比较，应转换为字符串后再比较
   - 长期解决方案可考虑使用BigInt类型（需注意浏览器兼容性）

### 11.3 Long类型ID的JSON序列化修复 (2025-04-24)

对后端代码进行修改，解决前端JavaScript处理大整数ID时的精度丢失问题：

1. **问题根本原因**：
   - JavaScript的Number类型基于IEEE 754双精度浮点数，最大安全整数为2^53-1（约9007兆）
   - 后端使用的Long类型ID（如雪花算法生成的ID）通常超过16位数，超出了JavaScript安全整数范围
   - 前端尝试处理这些大整数时会发生精度丢失，最后几位数字可能会被四舍五入

2. **综合解决方案**：
   - 在后端为所有包含Long类型ID的响应对象添加`@JsonSerialize(using = ToStringSerializer.class)`注解
   - 这确保所有大整数ID在JSON序列化时自动转换为字符串类型
   - 前端接收到的ID将始终保持完整精度，不会发生失真

3. **具体修改**：
   - 修改`CommentVO`：为`id`、`articleId`、`userId`和`parentId`添加序列化注解
   - 修改`CommentDTO`：为`articleId`、`userId`和`parentId`添加序列化注解
   - 修改`ArticleVO`：为`id`、`authorId`、`viewCount`、`likeCount`和`collectCount`添加序列化注解
   - 修改`ArticleSummaryVO`：为`id`、`authorId`、`viewCount`、`likeCount`和`collectCount`添加序列化注解

4. **前端处理**：
   - 前端接收到的ID现在是字符串类型，无需特殊处理即可保持精度
   - 在需要进行ID比较时，应始终使用字符串比较方法

5. **最佳实践**：
   - 对于所有可能超出JavaScript安全整数范围的ID字段，一律使用`ToStringSerializer`序列化
   - 确保前端代码在处理这些ID时将其视为字符串，避免使用数值操作
   - 需要进行数值操作时，考虑使用`BigInt`类型（需要考虑浏览器兼容性）