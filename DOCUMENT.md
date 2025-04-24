# DevSpace 项目详细设计文档

本文档旨在提供 DevSpace 项目更深入的技术细节、设计决策和规范，作为开发过程中的主要参考。

# 1. 前后端数据传输

DevSpace采用统一的数据传输模型，确保API响应格式一致性和可预测性，便于前端处理各种请求结果。
JWT (JSON Web Token) 通过 HTTP-only Cookie 进行传输和验证。

## 核心组件

### ResVo - 统一响应对象

`ResVo<T>`是系统核心的响应封装类，泛型设计允许封装任意类型的业务数据。

```java
public class ResVo<T> implements Serializable {
    // 状态信息，包含状态码和消息
    private Status status;
    
    // 业务数据，可以是任何类型
    private T result;
    
    // 便捷的静态构造方法
    public static <T> ResVo<T> ok(T result) {...}
    public static ResVo<String> ok() {...}
    public static <T> ResVo<T> fail(StatusEnum status, Object... args) {...}
    public static <T> ResVo<T> fail(Status status) {...}
}
```

### Status - 状态描述

`Status`类封装了API响应的状态信息，包含状态码和消息文本。

```java
public class Status {
    // 状态码：0表示成功，其他值表示特定错误
    private int code;
    
    // 状态消息：成功为"OK"，失败时提供错误描述
    private String msg;
}
```

### StatusEnum - 错误码体系

系统定义了规范化的错误码体系，采用三段式设计：`业务-状态-具体错误`

```
异常码规范：xxx - xxx - xxx
           业务 - 状态 - code

业务码：
- 100: 全局通用错误
- 200: 文章相关错误
- 300: 评论相关错误
- 400: 用户相关错误

状态码：(基于HTTP状态码设计)
- 4xx: 客户端错误(参数错误、权限不足等)
- 5xx: 服务器错误(内部异常等)
```

### JWT 认证与 Cookie

- **存储**: 认证成功后，服务器生成 JWT 并将其设置在名为 `jwt_token` 的 HTTP-only Cookie 中。HttpOnly 属性可防止客户端 JavaScript 访问令牌，增强安全性。
- **传输**: 浏览器会自动将此 Cookie 附加到后续对同一域的请求中。
- **验证**: 服务器端的 `JWTAuthenticationFilter` 负责从请求 Cookie 中提取并验证 JWT。

## 使用示例

### 成功响应 (登录)

```java
// AuthController.java - 登录成功
// ...认证逻辑...
String token = jwtUtil.generateToken(userDetails);
onlineUserService.save(userDetails.getUsername(), token);

// 设置 JWT Cookie
Cookie jwtCookie = new Cookie("jwt_token", token);
jwtCookie.setHttpOnly(true);
jwtCookie.setPath("/");
jwtCookie.setMaxAge(86400); // 1 day
// jwtCookie.setSecure(true); // 生产环境建议启用
response.addCookie(jwtCookie);

// 返回用户信息，不直接返回 token
UserDTO userDTO = userDetails.toUserDTO();
Map<String, Object> authInfo = new HashMap<>();
authInfo.put("user", userDTO);
return ResVo.ok(authInfo);
```

### 错误响应

```java
// 使用预定义错误类型
if (user == null) {
    return ResVo.fail(StatusEnum.USER_NOT_EXISTS, userId);
}

// 登录失败示例
if (!passwordEncoder.matches(password, user.getPassword())) {
    return ResVo.fail(StatusEnum.USER_PWD_ERROR);
}
```

### 前端使用 authenticatedFetch

前端应使用 `AuthUtils.authenticatedFetch` 发送需要认证的请求。该方法确保请求包含必要的 `credentials` 选项，以便浏览器发送 `jwt_token` Cookie。

```javascript
// 使用 authenticatedFetch (JWT 通过 Cookie 自动发送)
AuthUtils.authenticatedFetch('/api/user/profile', { method: 'GET' })
  .then(response => { // 'response' 是完整的 ResVo 对象
    if (response.status.code === 0) {
      // 处理成功响应
      const userData = response.result;
      renderUserProfile(userData);
    } else {
      // 处理错误
      showError(response.status.msg);
    }
  })
  .catch(error => {
      console.error("请求失败:", error.message);
      showError(error.message);
  });
```

客户端 JavaScript **不需要也无法**直接管理 `jwt_token` Cookie。浏览器和服务器负责其传输和验证。

## 错误码示例

| 错误码 | 描述 | 场景 |
|--------|------|------|
| 0 | 成功 | 操作成功完成 |
| 100_400_001 | 参数异常 | 请求参数不符合要求 |
| 100_403_003 | 未登录 | 未授权访问需要登录的资源 |
| 200_404_001 | 文章不存在 | 请求不存在的文章资源 |
| 400_403_002 | 用户名or密码错误 | 登录验证失败 |
| 400_405_002 | 用户已存在 | 注册时用户名已被占用 |

# 2. 数据模型 (Data Models)

## 2.1 视图对象 (VO)

``` java
public class ArticleVO implements Serializable { // For detailed view
    private Long id;
    private String title;
    private String summary;
    private String content; 
    private Long authorId;
    private String authorUsername; 
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long viewCount;
    private Long likeCount;
    private Long collectCount;
    private Boolean likedByCurrentUser;
    private Boolean collectedByCurrentUser;
}
```

## 2.2 数据传输对象 (DTO)

```java
public class ArticleCreateDTO implements Serializable {
    private String title;        // 文章标题
    private String summary;      // 文章摘要
    private String content;      // 文章内容
    private List<String> tags;   // 文章标签
    private Integer status;      // 文章状态，1-已发布，0-草稿
}

public class ArticleUpdateDTO implements Serializable {
    private String title;        // 文章标题
    private String summary;      // 文章摘要
    private String content;      // 文章内容
    private List<String> tags;   // 文章标签
    private Integer status;      // 文章状态
}

public class ArticleSummaryVO implements Serializable { // For list view
    private Long id;
    private String title;
    private String summary;
    private Long authorId;
    private String authorUsername;
    private Integer status;
    private LocalDateTime createdAt;
    private Long viewCount;
    private Long likeCount;
    private Long collectCount;
    private List<String> tags;
}
```

# 3. API 文档

## 3.1 认证接口 (`/auth`)
    - `POST /auth/register`: 用户注册
        - Request Body: `RegisterDTO`
        - Response: `ResVo<Void>`
    - `POST /auth/login`: 用户登录
        - Request Body: `LoginDTO` (Form parameters: username, password)
        - Response: `ResVo<Map<String, Object>>` (包含 `user` DTO)
        - **Side Effect**: Sets `jwt_token` HttpOnly Cookie.
    - `POST /auth/logout`: 用户登出
        - Response: `ResVo<String>`
        - **Side Effect**: Clears `jwt_token` Cookie.
## 3.2 用户接口 (`/api/user`)
## 3.3 文章接口 (`/api/articles`)
    - `GET /api/articles`: 获取文章列表
        - Parameters: 
            - `pageNum` (int): 页码，默认1
            - `pageSize` (int): 每页数量，默认10
            - `authorId` (Long, optional): 按作者筛选
            - `status` (Integer, optional): 按状态筛选
        - Response: `ResVo<IPage<ArticleSummaryVO>>`
    - `GET /api/articles/{id}`: 获取文章详情
        - Parameters: 
            - `id` (Long): 文章ID
        - Response: `ResVo<ArticleVO>`
    - `POST /api/articles`: 创建文章
        - Request Body: `ArticleCreateDTO`
        - Response: `ResVo<ArticleVO>`
    - `PUT /api/articles/{id}`: 更新文章
        - Parameters: 
            - `id` (Long): 文章ID
        - Request Body: `ArticleUpdateDTO`
        - Response: `ResVo<ArticleVO>`
    - `DELETE /api/articles/{id}`: 删除文章
        - Parameters: 
            - `id` (Long): 文章ID
        - Response: `ResVo<String>`
    - `POST /api/articles/{id}/like`: 点赞文章
        - Parameters: 
            - `id` (Long): 文章ID
        - Response: `ResVo<Void>`
    - `DELETE /api/articles/{id}/like`: 取消点赞
        - Parameters: 
            - `id` (Long): 文章ID
        - Response: `ResVo<Void>`
    - `POST /api/articles/{id}/collect`: 收藏文章
        - Parameters: 
            - `id` (Long): 文章ID
        - Response: `ResVo<Void>`
    - `DELETE /api/articles/{id}/collect`: 取消收藏
        - Parameters: 
            - `id` (Long): 文章ID
        - Response: `ResVo<Void>`
## 3.4 评论接口 (`/api/comment`)

# 4. 数据库设计

- **用户表 (`t_user`)**
    - `id` (BIGINT, PK)
    - `username` (VARCHAR, UNIQUE)
    - `password` (VARCHAR)
    - `email` (VARCHAR, UNIQUE)
    - `create_time` (DATETIME)
    - `update_time` (DATETIME)
    - ...

- **文章表 (`t_article`)**
    - `id` (BIGINT, PK)
    - `title` (VARCHAR) - 文章标题
    - `summary` (VARCHAR) - 文章摘要
    - `content` (TEXT) - 文章内容
    - `author_id` (BIGINT, FK) - 作者ID，关联用户表
    - `status` (TINYINT) - 状态，1-已发布，0-草稿
    - `view_count` (BIGINT) - 浏览数
    - `like_count` (BIGINT) - 点赞数
    - `collect_count` (BIGINT) - 收藏数
    - `create_time` (DATETIME) - 创建时间
    - `update_time` (DATETIME) - 更新时间
    - `deleted` (BOOLEAN) - 逻辑删除标记

- **文章标签表 (`t_article_tag`)**
    - `id` (BIGINT, PK)
    - `article_id` (BIGINT, FK) - 文章ID
    - `tag_name` (VARCHAR) - 标签名称
    - `create_time` (DATETIME)

- **文章点赞表 (`t_article_like`)**
    - `id` (BIGINT, PK)
    - `article_id` (BIGINT, FK) - 文章ID
    - `user_id` (BIGINT, FK) - 用户ID
    - `create_time` (DATETIME) - 点赞时间

- **文章收藏表 (`t_article_collect`)**
    - `id` (BIGINT, PK)
    - `article_id` (BIGINT, FK) - 文章ID
    - `user_id`

# 5. 事件驱动系统

## 5.1 变更概述

DevSpace 项目添加了基于 Spring 事件和 RabbitMQ 消息队列的异步处理系统，以支持文章点赞、收藏和评论功能。该实现采用了事件驱动架构，将交互操作与业务逻辑解耦，提高系统响应速度和可扩展性。

## 5.2 架构设计

### 5.2.1 事件驱动流程

1. **交互触发**: 用户在前端进行点赞、收藏或评论操作
2. **事件发布**: 相应的 Controller 调用 Service 方法，Service 发布相应的 Spring 事件
3. **事件监听**: 事件监听器捕获事件并将其转换为消息发送到 RabbitMQ
4. **消息消费**: 消息消费者异步处理消息，执行数据库操作
5. **结果更新**: 下次用户加载文章时，从数据库获取最新交互状态

### 5.2.2 主要组件

- **Spring 事件**: 用于应用内事件通知
- **RabbitMQ**: 实现可靠的异步消息处理
- **事件监听器**: 将事件转换为消息
- **消息消费者**: 异步处理交互逻辑

## 5.3 实现细节

### 5.3.1 Spring 事件

创建了基础的交互事件类和具体实现：

- `ArticleInteractionEvent`: 交互事件基类
- `ArticleLikeEvent`: 点赞事件
- `ArticleCollectEvent`: 收藏事件
- `ArticleCommentEvent`: 评论事件

### 5.3.2 消息队列配置

配置了 RabbitMQ 相关的交换机、队列和绑定：

- 交换机: `interaction.exchange`
- 队列:
  - `article.like.queue`: 文章点赞队列
  - `article.collect.queue`: 文章收藏队列
  - `article.comment.queue`: 文章评论队列

### 5.3.3 数据库设计

添加了三个表来存储交互数据：

- `article_like`: 存储用户点赞记录
- `article_collect`: 存储用户收藏记录
- `comment`: 存储用户评论，支持嵌套回复

### 5.3.4 API 设计

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

## 5.4 前端实现

### 5.4.1 乐观UI更新

为解决用户执行交互操作（点赞、评论等）后需要等待异步处理完成的问题，前端实现了"乐观UI更新"策略：

1. **即时反馈**：用户执行操作后，UI立即更新，无需等待服务器响应
2. **临时状态**：新创建的内容（如评论）显示"发布中"状态
3. **后台同步**：同时发送请求到服务器进行实际处理
4. **延迟验证**：服务器处理完成后，前端重新加载数据以确保显示的是最新状态
5. **错误处理**：如果服务器处理失败，撤销乐观更新并显示错误信息

# 8. 事件驱动架构 (Event-Driven Architecture)

## 8.1 文章互动系统

DevSpace 使用事件驱动架构来处理文章互动功能（点赞、收藏、评论），这种架构能够提高系统的扩展性和响应性。

### 8.1.1 架构概述

1. **事件流程**：
   - 用户在前端执行交互操作（点赞、收藏、评论）
   - Controller 接收请求并调用相应的 Service 方法
   - Service 方法执行必要的验证，然后发布相应的 Spring 事件
   - 事件监听器捕获事件并将其转换为消息发送至 RabbitMQ
   - 消息消费者异步处理消息，执行实际的数据库操作

2. **核心组件**：
   - **Spring 事件**：用于应用内部事件通知
   - **RabbitMQ**：实现可靠的异步消息处理
   - **事件监听器**：将事件转换为消息
   - **消息消费者**：异步处理交互逻辑

### 8.1.2 消息队列配置

- **交换机**：`interaction.exchange` (topic类型)
- **队列**：
  - `article.like.queue`：文章点赞队列
  - `article.collect.queue`：文章收藏队列
  - `article.comment.queue`：文章评论队列
- **路由键**：
  - 点赞事件：`article.like`
  - 收藏事件：`article.collect`
  - 评论事件：`article.comment`

### 8.1.3 事件类型

- `ArticleInteractionEvent`：交互事件基类
- `ArticleLikeEvent`：点赞事件
- `ArticleCollectEvent`：收藏事件
- `ArticleCommentEvent`：评论事件

## 8.2 前端优化策略

由于后端采用异步处理，为提供良好的用户体验，前端实现了乐观UI更新策略：

1. **即时反馈**：用户执行操作后，UI立即更新，无需等待服务器响应
2. **临时状态**：新创建的内容（如评论）显示"发布中"状态
3. **后台同步**：同时发送请求到服务器进行实际处理
4. **延迟验证**：服务器处理完成后，前端重新加载数据以确保显示的是最新状态
5. **错误处理**：如果服务器处理失败，撤销乐观更新并显示错误信息

# 9. 大整数ID处理规范

## 9.1 问题描述

在DevSpace项目中，实体ID (如用户ID、文章ID、评论ID) 使用Java的Long类型，可能超出JavaScript安全整数范围。这会导致前端处理这些ID时出现精度丢失，影响功能正常运行。

- **JavaScript数值限制**：
  - 能够精确表示的最大整数是 `Number.MAX_SAFE_INTEGER` (9007199254740991，即2^53-1)
  - 超过此范围的整数在JavaScript中会失去精度

## 9.2 解决方案

为解决此问题，DevSpace采用以下规范：

1. **后端序列化**：
   - 所有包含Long类型ID的响应对象都应使用`@JsonSerialize(using = ToStringSerializer.class)`注解
   - 这确保大整数ID在JSON序列化时自动转换为字符串类型

2. **前端处理**：
   - 前端代码应将从API接收的ID视为字符串类型
   - 在页面URL、API请求和DOM数据属性中始终使用字符串形式的ID
   - 避免对ID进行数值运算或使用`parseInt()`等可能导致精度丢失的操作

## 9.3 实现示例

**后端实体序列化**：
```java
public class CommentVO implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
    
    // 其他字段...
}
```

**前端正确处理ID**：
```javascript
// 正确：将ID作为字符串处理
const commentId = comment.id; // 已是字符串，不需转换
// 正确：在URL中使用ID
const url = `/api/comments/${commentId}`;
// 正确：在API请求中使用ID
fetch(`/api/comments/${commentId}`);

// 错误：将ID转换为数值
const numericId = Number(commentId); // 可能导致精度丢失!
const numericId = parseInt(commentId); // 可能导致精度丢失!
```

## 9.4 类型检查规范

为避免精度问题，项目开发中应遵循以下规范：

1. 所有包含Long类型ID的实体类、DTO和VO都必须使用`ToStringSerializer`序列化注解
2. 前端代码在处理从API接收的ID时，应将其视为字符串而非数值
3. 使用大整数ID进行比较时，应使用字符串比较方法
4. 不要对这些ID进行数值运算（如递增、相加等）
5. 如需在前端进行大整数运算，考虑使用`BigInt`类型（需要评估浏览器兼容性）

# 10. 文章交互系统设计

## 10.1 系统概述

DevSpace的文章交互系统采用事件驱动架构和消息队列技术，实现点赞、收藏和评论等功能，以提高系统性能和用户体验。

### 10.1.1 核心设计目标

- **响应速度**：通过异步处理减少用户等待时间
- **系统解耦**：交互处理与核心业务逻辑分离
- **可扩展性**：支持新交互类型的便捷添加
- **可靠性**：确保交互数据的准确性和一致性

## 10.2 技术架构

### 10.2.1 架构组件

- **Spring事件系统**：用于应用内部的事件发布与监听
- **RabbitMQ消息队列**：实现可靠的异步消息处理
- **事件处理器**：处理特定类型的交互事件
- **乐观UI更新**：前端实现即时反馈的交互体验

### 10.2.2 事件流程

1. 用户在前端发起交互操作（点赞、收藏、评论）
2. 后端接收请求并返回成功响应（无需等待实际处理）
3. 同时发布相应的Spring事件
4. 事件监听器捕获事件并发送到RabbitMQ队列
5. 消息消费者异步处理交互逻辑（更新数据库等）
6. 处理结果通过WebSocket通知前端（可选）

## 10.3 技术实现

### 10.3.1 事件模型

系统定义了多种交互事件类型：

```java
public abstract class ArticleInteractionEvent extends ApplicationEvent {
    private Long articleId;
    private Long userId;
    // 其他公共属性和方法
}

public class ArticleLikeEvent extends ArticleInteractionEvent {
    private boolean isLike; // true为点赞，false为取消点赞
    // 构造器和其他方法
}

public class ArticleCollectEvent extends ArticleInteractionEvent {
    private boolean isCollect; // true为收藏，false为取消收藏
    // 构造器和其他方法
}

public class ArticleCommentEvent extends ArticleInteractionEvent {
    private String content;
    // 构造器和其他方法
}
```

### 10.3.2 RabbitMQ配置

```java
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue articleLikeQueue() {
        return new Queue("article.like", true);
    }
    
    @Bean
    public Queue articleCollectQueue() {
        return new Queue("article.collect", true);
    }
    
    @Bean
    public Queue articleCommentQueue() {
        return new Queue("article.comment", true);
    }
    
    // 交换机和绑定配置
}
```

### 10.3.3 事件监听与处理

```java
@Component
public class ArticleInteractionListener {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @EventListener
    public void handleArticleLikeEvent(ArticleLikeEvent event) {
        // 将事件转换为消息并发送到RabbitMQ队列
        rabbitTemplate.convertAndSend("article.like", event);
    }
    
    // 其他事件处理方法
}

@Component
public class ArticleLikeConsumer {
    @Autowired
    private ArticleService articleService;
    
    @RabbitListener(queues = "article.like")
    public void consumeArticleLikeMessage(ArticleLikeEvent event) {
        // 处理点赞/取消点赞的业务逻辑
        articleService.processArticleLike(event.getArticleId(), event.getUserId(), event.isLike());
    }
}
```

## 10.4 API设计

### 10.4.1 交互端点

系统提供以下REST API端点用于文章交互：

```
POST /api/articles/{articleId}/like        # 点赞文章
DELETE /api/articles/{articleId}/like      # 取消点赞
POST /api/articles/{articleId}/collect     # 收藏文章
DELETE /api/articles/{articleId}/collect   # 取消收藏
POST /api/articles/{articleId}/comments    # 发表评论
GET /api/articles/{articleId}/comments     # 获取评论列表
```

### 10.4.2 响应格式

所有交互API返回标准化的`ResVo`响应：

```json
{
  "status": {
    "code": 0,
    "message": "success"
  },
  "result": true
}
```

## 10.5 前端实现

### 10.5.1 乐观UI更新

前端采用乐观更新策略，在用户操作后立即反映UI变化：

```javascript
function handleLike() {
  // 立即更新UI状态
  const newLikeState = !isLiked;
  setIsLiked(newLikeState);
  if (newLikeState) {
    setLikeCount(prevCount => prevCount + 1);
  } else {
    setLikeCount(prevCount => prevCount - 1);
  }
  
  // 发送API请求
  const endpoint = newLikeState ? 
    `/api/articles/${articleId}/like` : 
    `/api/articles/${articleId}/like`;
  const method = newLikeState ? 'POST' : 'DELETE';
  
  AuthUtils.authenticatedFetch(endpoint, { method })
    .then(response => {
      // 处理响应
    })
    .catch(error => {
      // 如果请求失败，回滚UI状态
      setIsLiked(!newLikeState);
      if (newLikeState) {
        setLikeCount(prevCount => prevCount - 1);
      } else {
        setLikeCount(prevCount => prevCount + 1);
      }
      // 显示错误信息
    });
}
```

## 10.6 性能与可靠性

### 10.6.1 性能优化

- 消息队列缓冲高峰期请求，避免数据库过载
- 避免主线程阻塞，提高API响应速度
- 独立的消费者处理互动逻辑，不影响主业务流程

### 10.6.2 可靠性保证

- 消息持久化确保系统重启后不丢失数据
- 失败重试机制处理暂时性错误
- 死信队列捕获无法处理的消息
- 事务保证数据一致性

## 10.7 监控与维护

- 集成RabbitMQ管理插件，监控队列状态
- 记录详细的消息处理日志
- 提供交互统计数据的管理界面
- 定期清理过期交互数据