# DevSpace 项目详细设计文档

本文档旨在提供 DevSpace 项目更深入的技术细节、设计决策和规范，作为开发过程中的主要参考。

# 1. 前后端数据传输与认证

DevSpace采用统一的数据传输模型，确保API响应格式一致性和可预测性。认证机制主要基于 JWT (JSON Web Token)，并通过安全的 Cookie 进行管理。同时支持 GitHub OAuth2 登录。

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

### 认证方式

DevSpace 提供两种认证方式：

1.  **传统用户名/密码认证**:
    - **JWT 令牌存储**: 认证成功后，服务器生成 JWT 并将其设置在名为 `jwt_token` 的 **HTTP-only** Cookie 中。HttpOnly 属性可防止客户端 JavaScript 访问令牌，增强安全性。
    - **用户信息存储**: 用户基本信息（如用户名、ID、头像URL）通过接口响应返回给前端，前端通过 `AuthUtils.js` 将其存储在名为 `user_info` 的**标准 Cookie** 中。此 Cookie **非** HTTP-only，允许前端 JavaScript 访问以更新 UI。
    - **验证**: 服务器端的 `JWTAuthenticationFilter` 负责从请求 Cookie 中提取并验证 `jwt_token`。

2.  **GitHub OAuth2 认证**:
    - **流程**: 用户点击"使用 GitHub 账号登录"按钮，重定向至 GitHub 授权，授权后 GitHub 重定向回应用 (`/login/oauth2/code/github`)。
    - **成功处理 (`OAuth2LoginSuccessHandler`)**: 
        - 服务器获取 GitHub 用户信息。
        - 在本地数据库中查找或创建用户。
        - 生成 JWT 并设置 `jwt_token` **HTTP-only** Cookie。
        - 将用户信息（UserDTO）序列化为 JSON，并设置 `user_info` **标准 Cookie**。
        - 重定向到首页。
    - **JWT 验证**: 同传统认证，后续请求通过 `JWTAuthenticationFilter` 验证 `jwt_token` Cookie。

### 统一的 Cookie 管理 (`AuthUtils.js`)

- 前端使用 `AuthUtils` 统一管理 `user_info` Cookie，提供 `setUserInfo`, `getUserInfo`, `isAuthenticated`, `logout` 等方法。
- 无论使用哪种登录方式，前端都通过 `AuthUtils.getUserInfo()` 读取 `user_info` Cookie 来判断登录状态和获取用户信息。
- `AuthUtils.authenticatedFetch` 自动包含 `credentials: 'include'` 选项，确保浏览器自动发送 `jwt_token` 和 `user_info` Cookies。

## 使用示例

### 成功响应 (传统登录)

```java
// AuthController.java - 登录成功
// ...认证逻辑...
String token = jwtUtil.generateToken(userDetails);
onlineUserService.save(userDetails.getUsername(), token);

// 设置 JWT HttpOnly Cookie
Cookie jwtCookie = new Cookie("jwt_token", token);
jwtCookie.setHttpOnly(true);
jwtCookie.setPath("/");
jwtCookie.setMaxAge(86400); // 1 day
response.addCookie(jwtCookie);

// 获取并准备 UserDTO
UserDTO userDTO = userDetails.toUserDTO();

// 返回 UserDTO 给前端，前端 AuthUtils 会将其存入 user_info Cookie
Map<String, Object> authInfo = new HashMap<>();
authInfo.put("user", userDTO);
return ResVo.ok(authInfo);
```

### 成功响应 (GitHub OAuth2 登录 - 后端处理)

```java
// OAuth2LoginSuccessHandler.java - 登录成功
// ... 获取 OAuth2User ...
UserDO user = userService.processOAuth2User(username, githubId, email, avatarUrl);
CustomUserDetails userDetails = new CustomUserDetails(user);
String token = jwtUtil.generateToken(userDetails);
onlineUserService.save(userDetails.getUsername(), token);

// 设置 JWT HttpOnly Cookie
Cookie jwtCookie = new Cookie("jwt_token", token);
jwtCookie.setHttpOnly(true);
// ... 设置 path, maxAge ...
response.addCookie(jwtCookie);

// 设置 User Info 标准 Cookie (供前端读取)
UserDTO userDTO = userDetails.toUserDTO();
String userInfoJson = URLEncoder.encode(objectMapper.writeValueAsString(userDTO), StandardCharsets.UTF_8);
Cookie userInfoCookie = new Cookie("user_info", userInfoJson);
userInfoCookie.setHttpOnly(false);
// ... 设置 path, maxAge ...
response.addCookie(userInfoCookie);

// 重定向
getRedirectStrategy().sendRedirect(request, response, "/");
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

```javascript
// 使用 authenticatedFetch (JWT 和 user_info Cookie 自动发送)
AuthUtils.authenticatedFetch('/api/user/profile', { method: 'GET' })
  .then(response => { // 'response' 是完整的 ResVo 对象
    if (response.status.code === 0) {
      // 处理成功响应
      const userData = response.result; // userData 是 UserVO
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

客户端 JavaScript 通过 `AuthUtils` 管理 `user_info` Cookie，但**不需要也无法**直接管理 `jwt_token` Cookie。

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

用于封装从后端返回给前端的数据，通常不包含敏感信息。

```java
// 文章详情VO
public class ArticleVO implements Serializable { 
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String summary;
    private String content; // HTML for display
    private String rawContent; // Markdown for editing
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;
    private String authorUsername; 
    private String authorAvatarUrl;
    private String authorBio;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long viewCount; // From Redis/DB ViewCount Service
    @JsonSerialize(using = ToStringSerializer.class)
    private Long likeCount; // From Like Service/DB
    @JsonSerialize(using = ToStringSerializer.class)
    private Long collectCount; // From Collect Service/DB
    @JsonSerialize(using = ToStringSerializer.class)
    private Long commentCount; // From Comment Service/DB
    private Boolean likedByCurrentUser;
    private Boolean collectedByCurrentUser;
    private List<String> tags;
}

// 文章摘要VO (用于列表)
public class ArticleSummaryVO implements Serializable { 
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private String summary;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;
    private String authorUsername;
    private Integer status;
    private LocalDateTime createdAt;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long viewCount; // From Redis/DB ViewCount Service
    @JsonSerialize(using = ToStringSerializer.class)
    private Long likeCount; // From Like Service/DB
    @JsonSerialize(using = ToStringSerializer.class)
    private Long collectCount; // From Collect Service/DB
    private List<String> tags;
}

// 评论VO
public class CommentVO implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long articleId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String username;
    private String avatarUrl; // 新增：用户头像URL
    private String content;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;  // null 表示顶层评论，否则为回复
    private LocalDateTime createdAt;
    private List<CommentVO> replies;  // 嵌套回复
}

// 用户信息VO (用于Profile页和登录响应)
public class UserVO implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private Boolean isAdmin;
}
```

## 2.2 数据传输对象 (DTO)

用于封装从前端发送到后端的数据。

```java
// 文章创建DTO
public class ArticleCreateDTO implements Serializable {
    private String title;        // 文章标题
    private String content;      // 文章内容
    private List<String> tags;   // 文章标签
    private Integer status;      // 文章状态，1-已发布，0-草稿
}

// 文章更新DTO
public class ArticleUpdateDTO implements Serializable {
    private String title;        // 文章标题
    private String content;      // 文章内容
    private List<String> tags;   // 文章标签
    private Integer status;      // 文章状态
}

// 用户注册DTO
public class RegisterDTO implements Serializable {
    private String username;
    private String password;
    private String email;
}

// 用户登录DTO
public class LoginDTO implements Serializable {
    private String username;
    private String password;
}

// 用户资料更新DTO
public class UserUpdateDTO implements Serializable {
    private String username;
    private String email;
    private String bio;
    // avatarUrl 不在此处，通过单独接口上传
}

```

# 3. API 文档

## 3.1 认证接口 (`/auth`, `/oauth2`)
    - `POST /auth/register`: 用户注册
        - Request Body: `RegisterDTO`
        - Response: `ResVo<Void>`
    - `POST /auth/login`: 用户登录 (传统方式)
        - Request Body: Form parameters: `username`, `password`
        - Response: `ResVo<Map<String, Object>>` (包含 `user` (UserDTO))
        - **Side Effect**: Sets `jwt_token` HttpOnly Cookie. 前端 `AuthUtils` 会将返回的 `user` 存入 `user_info` Cookie。
    - `GET /oauth2/authorization/github`: GitHub OAuth2 登录入口
        - **Action**: 重定向到 GitHub 授权页面。
    - `/login/oauth2/code/github`: GitHub OAuth2 回调地址 (由 Spring Security 处理)
        - **Action**: 处理 GitHub 返回的授权码，验证用户，`OAuth2LoginSuccessHandler` 执行以下操作：
          - Sets `jwt_token` HttpOnly Cookie.
          - Sets `user_info` Standard Cookie.
          - 重定向到 `/`.
    - `POST /auth/logout`: 用户登出
        - Response: `ResVo<String>`
        - **Side Effect**: Clears `jwt_token` and `user_info` Cookies.
## 3.2 用户接口 (`/api/user`)
    - `GET /api/user/profile`: 获取当前登录用户资料
        - Response: `ResVo<UserVO>`
    - `POST /api/user/profile`: 更新当前登录用户资料
        - Request Body: `UserUpdateDTO`
        - Response: `ResVo<UserVO>`
        - **Side Effect**: 如果用户名更改，会重新生成并设置 `jwt_token` Cookie。
    - `POST /api/user/avatar`: 上传/更新用户头像
        - Request Body: `multipart/form-data` with field `avatar` (File)
        - Response: `ResVo<String>` (返回新的头像 URL)
        - **Side Effect**: 更新用户数据库中的 `avatarUrl`。
## 3.3 文章接口 (`/api/articles`)
    - `GET /api/articles`: 获取文章列表 (分页)
        - Parameters:
            - `pageNum` (int): 页码，默认1
            - `pageSize` (int): 每页数量，默认10
            - `authorId` (Long, optional): 按作者筛选
            - `status` (Integer, optional): 按状态筛选 (1: 已发布, 0: 草稿)
            - `tag` (String, optional): 按标签筛选
            - `keyword` (String, optional): 按标题或内容搜索
        - Response: `ResVo<IPage<ArticleSummaryVO>>` (Includes accurate `viewCount`)
    - `GET /api/articles/{id}`: 获取文章详情
        - Parameters:
            - `id` (Long): 文章ID (字符串形式)
        - Response: `ResVo<ArticleVO>` (Includes accurate `viewCount`)
        - **Side Effect**: Increments the view count for the article in Redis.
    - `POST /api/articles`: 创建文章
        - Request Body: `ArticleCreateDTO` (不包含 `summary`)
        - Response: `ResVo<ArticleVO>`
    - `PUT /api/articles/{id}`: 更新文章
        - Parameters:
            - `id` (Long): 文章ID (字符串形式)
        - Request Body: `ArticleUpdateDTO` (不包含 `summary`)
        - Response: `ResVo<ArticleVO>`
    - `DELETE /api/articles/{id}`: 删除文章
        - Parameters:
            - `id` (Long): 文章ID (字符串形式)
        - Response: `ResVo<String>`
    - `POST /api/articles/{id}/like`: 点赞文章
        - Parameters:
            - `id` (Long): 文章ID (字符串形式)
        - Response: `ResVo<Void>`
    - `DELETE /api/articles/{id}/like`: 取消点赞
        - Parameters:
            - `id` (Long): 文章ID (字符串形式)
        - Response: `ResVo<Void>`
    - `POST /api/articles/{id}/collect`: 收藏文章
        - Parameters:
            - `id` (Long): 文章ID (字符串形式)
        - Response: `ResVo<Void>`
    - `DELETE /api/articles/{id}/collect`: 取消收藏
        - Parameters:
            - `id` (Long): 文章ID (字符串形式)
        - Response: `ResVo<Void>`
## 3.4 评论接口 (`/api/comments`)
    - `POST /api/comments`: 发表评论
        - Request Body: `CommentCreateDTO` (包含 `articleId`, `content`, `parentId`)
        - Response: `ResVo<CommentVO>` (包含新评论信息)
    - `GET /api/comments/article/{articleId}`: 获取文章的评论列表 (包含嵌套回复和用户头像)
        - Parameters:
            - `articleId` (Long): 文章ID (字符串形式)
            - `pageNum` (int): 页码，默认1
            - `pageSize` (int): 每页数量，默认50
        - Response: `ResVo<IPage<CommentVO>>`
    - `DELETE /api/comments/{id}`: 删除评论 (仅作者或管理员)
        - Parameters:
            - `id` (Long): 评论ID (字符串形式)
        - Response: `ResVo<String>`
## 3.5 文件访问接口 (`/api/file`)
    - `GET /api/file/avatars/{filename:.+}`: 获取用户头像文件
        - Parameters: `filename` (String)
        - Response: Image file
## 3.6 AI 聊天接口 (`/api/ai`)
    - `POST /api/ai/chat`: 与 AI 模型聊天 (简单模式)
        - Request Body: JSON `{"prompt": "Your question here"}`
        - Response: `ResVo<String>` (包含 AI 的回复)
        - **Authentication**: Required (用户必须登录)
        - **Side Effect**: 调用配置的第三方 AI API (e.g., Sambanova).
    - `POST /api/ai/chat/advanced`: 与 AI 模型聊天 (高级模式)
        - Request Body: JSON `[{"role": "system", "content": "..."}, {"role": "user", "content": "..."}]`
        - Response: `ResVo<String>` (包含 AI 的回复)
        - **Authentication**: Required (用户必须登录)
    - `POST /api/ai/chat/stream`: 与 AI 模型聊天 (简单模式, 流式响应)
        - Request Body: JSON `{"prompt": "Your question here"}`
        - Response: `text/event-stream` (SSE 格式)
        - **Authentication**: Required (用户必须登录)
    - `POST /api/ai/chat/stream/advanced`: 与 AI 模型聊天 (高级模式, 流式响应)
        - Request Body: JSON `[{"role": "system", "content": "..."}, {"role": "user", "content": "..."}]`
        - Response: `text/event-stream` (SSE 格式)
        - **Authentication**: Required (用户必须登录)
    - `POST /api/ai/summary`: 生成文章摘要
        - Request Body: JSON `{"content": "Article content here..."}`
        - Response: `ResVo<String>` (包含 AI 生成的摘要)
        - **Authentication**: Required (用户必须登录)

# 4. 数据库设计

- **用户表 (`t_user`)**
    - `id` (BIGINT, PK)
    - `username` (VARCHAR, UNIQUE)
    - `password` (VARCHAR) - 加密存储
    - `email` (VARCHAR, UNIQUE)
    - `bio` (TEXT, nullable) - 用户简介
    - `avatar_url` (VARCHAR, nullable) - 头像文件URL
    - `is_admin` (BOOLEAN, default: false) - 是否为管理员
    - `create_time` (DATETIME)
    - `update_time` (DATETIME)
    - `deleted` (BOOLEAN, default: false) - 逻辑删除标记

- **文章表 (`t_article`)**
    - `id` (BIGINT, PK)
    - `title` (VARCHAR) - 文章标题
    - `summary` (VARCHAR) - 文章摘要 (由后端根据内容自动生成)
    - `content` (TEXT) - 文章内容
    - `author_id` (BIGINT, FK) - 作者ID，关联用户表
    - `status` (TINYINT) - 状态，1-已发布，0-草稿
    - `view_count` (BIGINT, default: 0)
    - `like_count` (BIGINT, default: 0)
    - `collect_count` (BIGINT, default: 0)
    - `comment_count` (BIGINT, default: 0)
    - `create_time` (DATETIME)
    - `update_time` (DATETIME)
    - `deleted` (BOOLEAN, default: false)

- **文章标签关联表 (`t_article_tag`)**
    - `id` (BIGINT, PK)
    - `article_id` (BIGINT, FK) - 文章ID
    - `tag_name` (VARCHAR) - 标签名称
    - `create_time` (DATETIME)
    - *建议*: 添加 `UNIQUE(article_id, tag_name)` 约束

- **标签表 (`t_tag`)** (可选，如果需要管理标签本身)
    - `tag_name` (VARCHAR, PK)
    - `create_time` (DATETIME)

- **文章点赞表 (`t_article_like`)**
    - `id` (BIGINT, PK)
    - `article_id` (BIGINT, FK)
    - `user_id` (BIGINT, FK)
    - `create_time` (DATETIME)
    - *建议*: 添加 `UNIQUE(article_id, user_id)` 约束

- **文章收藏表 (`t_article_collect`)**
    - `id` (BIGINT, PK)
    - `article_id` (BIGINT, FK)
    - `user_id` (BIGINT, FK)
    - `create_time` (DATETIME)
    - *建议*: 添加 `UNIQUE(article_id, user_id)` 约束

- **评论表 (`t_comment`)**
    - `id` (BIGINT, PK)
    - `article_id` (BIGINT, FK) - 关联文章ID
    - `user_id` (BIGINT, FK) - 评论用户ID
    - `content` (TEXT) - 评论内容
    - `parent_id` (BIGINT, nullable, FK) - 父评论ID，用于嵌套回复
    - `reply_count` (BIGINT, default: 0) - 子回复数
    - `like_count` (BIGINT, default: 0) - 评论点赞数 (待实现)
    - `create_time` (DATETIME)
    - `update_time` (DATETIME)
    - `deleted` (BOOLEAN, default: false)

- **文章浏览量表 (`t_article_viewcount`)**
    - `id` (BIGINT, PK)
    - `article_id` (BIGINT, UK) - 文章ID
    - `view_count` (BIGINT, default: 0) - 浏览量
    - `updated_at` (DATETIME) - 最后更新时间
    - *建议*: 添加 `INDEX(view_count DESC)` 支持按热度排序

### 后续计划

-   引入布隆过滤器等机制防止恶意刷量。
-   实现热门文章排行功能。

# 5. 用户资料管理设计

## 5.1 概述

用户资料管理模块允许用户查看、编辑自己的信息，并上传头像。

## 5.2 功能点

- **查看资料 (`/profile`)**: 显示当前用户信息 (用户名、邮箱、简介、头像、加入时间)。
- **编辑资料 (`POST /api/user/profile`)**: 允许修改用户名、邮箱、简介。
- **头像上传 (`POST /api/user/avatar`)**: 允许上传 JPG/PNG 格式，小于 2MB 的图片作为头像。
- **实时更新**: 资料或头像更新后，前端 UI (包括 Header) 应实时反映变化。

## 5.3 技术实现

### 5.3.1 前端 (`profile.js`, `AuthUtils.js`)

- **页面逻辑 (`profile.js`)**: 
    - 通过 `AuthUtils.getUserInfo()` 获取当前用户数据填充页面。
    - 调用 `/api/user/profile` (GET) 获取最新数据进行编辑。
    - 提交表单时，调用 `/api/user/profile` (POST) 发送 `UserUpdateDTO`。
    - 点击头像区域触发文件选择，使用 `FormData` 调用 `/api/user/avatar` (POST) 上传文件。
    - 上传成功或编辑成功后，调用 `AuthUtils.updateUserInfo()` 更新 `user_info` Cookie。
    - 触发 `userInfoUpdated` 自定义事件，通知其他组件（如 Header）更新。
- **认证工具 (`AuthUtils.js`)**: 
    - `updateUserInfo()` 方法用于更新 `user_info` Cookie 中的用户信息。

### 5.3.2 后端

- **控制器**:
    - `ProfileController`: 处理 `/profile` 页面请求。
    - `UserProfileRestController`: 处理 `/api/user/profile` (GET/POST) 和 `/api/user/avatar` (POST) 请求。
    - `FileController`: 处理 `/api/file/avatars/{filename}` 头像文件访问请求。
- **服务**:
    - `UserService`: 实现 `updateUserProfile` (处理 DTO 更新) 和 `updateUserAvatar` (处理文件上传并更新 `avatarUrl`)。
    - `FileService` (`LocalFileServiceImpl`): 实现文件验证、存储到本地 (`<uploadDir>/avatars/`)、生成可访问 URL (`<baseUrl>/api/file/avatars/...`)。
- **配置**: 
    - `application.yml`: 配置 `app.file.upload-dir` (文件存储根目录) 和 `app.file.base-url` (文件访问基础URL)。

### 5.3.3 用户名变更处理

- 当 `POST /api/user/profile` 检测到用户名发生变化时：
    1. 更新数据库中的用户名。
    2. 使用新的用户信息重新生成 JWT 令牌。
    3. 创建一个新的 `jwt_token` HttpOnly Cookie 并设置到响应中，覆盖旧 Cookie。
    4. 更新 Redis 中存储的在线用户令牌信息。
    5. 返回更新后的 `UserVO`。
    *注意*: 前端 `AuthUtils.updateUserInfo()` 会根据返回的 `UserVO` 更新 `user_info` Cookie。

# 6. 事件驱动系统 (交互功能)

## 6.1 变更概述

DevSpace 项目添加了基于 Spring 事件和 RabbitMQ 消息队列的异步处理系统，以支持文章点赞、收藏和评论功能。该实现采用了事件驱动架构，将交互操作与业务逻辑解耦，提高系统响应速度和可扩展性。

## 6.2 架构设计

### 6.2.1 事件驱动流程

1. **交互触发**: 用户在前端进行点赞、收藏或评论操作。
2. **API 调用**: 前端调用相应的 REST API (e.g., `/api/articles/{id}/like`, `/api/comments`)。
3. **同步操作 & 事件发布**: Controller 调用 Service。Service 执行**必要的同步验证** (如检查用户权限)，然后发布相应的 Spring 事件 (e.g., `ArticleLikeEvent`, `ArticleCommentEvent`)。 **API 立即返回成功响应**给前端（乐观响应）。
4. **事件监听 & 消息发送**: 事件监听器 (`ArticleInteractionListener`) 捕获 Spring 事件，将其转换为消息，并通过 `RabbitTemplate` 发送到 RabbitMQ 的指定队列。
5. **消息消费 & 异步处理**: 消息消费者 (e.g., `ArticleLikeConsumer`, `CommentConsumer`) 监听队列，接收消息并执行**实际的数据库操作**（如更新点赞/收藏/评论表，更新文章统计计数）。
6. **结果最终一致**: 数据库状态最终会与用户的操作一致。前端通过后续加载数据或 WebSocket (可选) 获取最新状态。

### 6.2.2 主要组件

- **Spring 事件**: 用于应用内事件通知 (`ArticleInteractionEvent` 及其子类)。
- **RabbitMQ**: 实现可靠的异步消息处理 (交换机 `interaction.exchange`, 队列 `article.like.queue`, `article.collect.queue`, `article.comment.queue`)。
- **事件监听器 (`ArticleInteractionListener`)**: 将 Spring 事件转发到 MQ。
- **消息消费者 (e.g., `ArticleLikeConsumer`)**: 异步处理数据库逻辑。

## 6.3 实现细节

### 6.3.1 Spring 事件

- `ArticleInteractionEvent`: 交互事件基类 (含 `articleId`, `userId`)。
- `ArticleLikeEvent`: 点赞/取消事件 (含 `isLike` 标志)。
- `ArticleCollectEvent`: 收藏/取消事件 (含 `isCollect` 标志)。
- `ArticleCommentEvent`: 发表评论事件 (含 `content`, `parentId`)。

### 6.3.2 消息队列配置

配置了 RabbitMQ 相关的交换机 (`interaction.exchange` - topic)、队列和绑定。

### 6.3.3 数据库设计

- `t_article_like`: 存储用户点赞记录。
- `t_article_collect`: 存储用户收藏记录。
- `t_comment`: 存储用户评论，支持嵌套回复 (通过 `parent_id`)。
- `t_article`: 包含 `like_count`, `collect_count`, `comment_count` 统计字段，由消息消费者更新。

### 6.3.4 API 设计

交互相关的 API 端点设计为快速响应，将耗时操作交给后台异步处理。

## 6.4 前端实现 (乐观 UI)

为提供流畅的用户体验，前端实现了"乐观 UI 更新"策略：

1. **即时反馈**：用户执行操作（点赞、评论等）后，UI 立即更新（如点赞按钮状态切换、评论直接显示），无需等待服务器确认。
2. **API 调用**: 同时，前端调用相应的 API 将操作发送到后端。
3. **后台同步**：后端 API 接收请求，发布事件，并立即返回成功。
4. **错误处理**：如果 API 调用失败（网络错误或同步验证失败），前端需要撤销 UI 更改并显示错误信息。
5. **最终一致性**: 异步处理完成后，数据库状态会更新。下次加载页面或通过其他机制（如 WebSocket）刷新数据时，会显示最终的、准确的状态。

# 7. 大整数ID处理规范

## 7.1 问题描述

在DevSpace项目中，实体ID (如用户ID、文章ID、评论ID) 使用Java的Long类型，可能超出JavaScript安全整数范围 (`Number.MAX_SAFE_INTEGER`, 2^53-1)。这会导致前端处理这些ID时出现精度丢失，影响功能正常运行。

## 7.2 解决方案

为解决此问题，DevSpace采用以下规范：

1. **后端序列化**：
   - 所有包含 `Long` 类型ID的 DTO 和 VO 都应在 ID 字段上使用 `@JsonSerialize(using = ToStringSerializer.class)` 注解。
   - 这确保大整数ID在JSON序列化时自动转换为字符串类型。

2. **前端处理**：
   - 前端代码应始终将从API接收的ID视为**字符串类型**。
   - 在页面URL、API请求参数和 DOM 数据属性 (如 `dataset.originalId`) 中始终使用字符串形式的ID。
   - **严禁**对ID进行数值运算或使用 `parseInt()`, `Number()` 等可能导致精度丢失的操作。

## 7.3 实现示例

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
    
    private String username;
    private String avatarUrl;
    // ... 其他字段
}
```

**前端正确处理ID**：
```javascript
// 获取评论数据 (假设 comment.id 是后端返回的字符串 "9007199254740992")
const commentIdStr = comment.id; 

// 正确：在URL中使用字符串ID
const url = `/api/comments/${commentIdStr}`; 

// 正确：在API请求体或参数中使用字符串ID
AuthUtils.authenticatedFetch(`/api/comments/${commentIdStr}`, { method: 'DELETE' });

// 正确：存储在 DOM dataset 中
const commentElement = document.createElement('div');
commentElement.dataset.originalId = commentIdStr; 

// --- 错误做法 --- 
// const numericId = Number(commentIdStr); // 错误! 结果可能为 9007199254740990 (精度丢失)
// const numericId = parseInt(commentIdStr); // 错误! 结果可能不准确或溢出
```

## 7.4 类型检查规范

1. 所有包含 `Long` 类型ID的实体类、DTO和VO都**必须**在其 ID 字段上使用 `ToStringSerializer` 序列化注解。
2. 前端代码在处理从API接收的ID时，必须将其视为**字符串**而非数值。
3. 使用大整数ID进行比较时，应使用字符串比较方法。
4. **禁止**对这些ID进行任何数值运算（如递增、相加等）。
5. 如需在前端进行大整数运算（极少情况），必须使用 `BigInt` 类型，并评估浏览器兼容性。

# 8. 前端架构与代码组织

## 8.1 HTML/JavaScript 分离模式

为提高代码可维护性和复用性，DevSpace 前端实现了 HTML 结构与 JavaScript 行为的分离：

### 8.1.1 核心原则

- **关注点分离**：HTML 文件 (`*.html`) 仅包含页面结构和 Thymeleaf 模板逻辑。JavaScript 文件 (`*.js`) 仅包含交互逻辑和 DOM 操作。
- **模块化组织**：按功能领域划分 JavaScript 文件 (e.g., `auth.js`, `header.js`, `profile.js`, `article/detail.js`)。
- **统一引用方式**：通过 Thymeleaf 的 `th:src` 属性在 `scripts` 片段中引用页面特定的 JavaScript 文件。

### 8.1.2 文件组织结构

```
ui/src/main/resources/
├── templates/                 # Thymeleaf 模板
│   ├── layout/                # 布局模板 (main.html, header.html)
│   ├── articles/              # 文章相关页面 (list.html, detail.html, create.html)
│   ├── user/                  # 用户相关页面 (profile.html)
│   ├── admin/                 # 管理后台页面
│   ├── index.html             # 首页片段
│   ├── login.html, register.html # 登录注册页
│   └── ...
└── static/                   # 静态资源
    ├── js/                    # JavaScript 文件
    │   ├── auth.js            # 认证工具 (含 Cookie 操作)
    │   ├── header.js          # 头部交互 (导航栏、用户菜单、头像)
    │   ├── profile.js         # 个人资料页脚本
    │   └── article/           
    │       └── detail.js      # 文章详情页脚本 (含评论、点赞等)
    ├── css/                   # 样式文件
    └── images/                # 图片资源
```

### 8.1.3 脚本集成方式

在具体的页面模板 (e.g., `articles/detail.html`) 中定义 `scripts` 片段：

```html
<!-- 在页面模板中定义 scripts 片段 -->
<th:block th:fragment="scripts">
    <script th:src="@{/js/article/detail.js}"></script>
    <!-- 可以包含其他页面特定的脚本 -->
</th:block>
```

主布局文件 (`layout/main.html`) 通过 Thymeleaf 表达式动态包含这些脚本：

```html
<!-- 在 layout/main.html 底部 -->
<script th:src="@{/js/auth.js}"></script> <!-- 全局认证工具 -->
<script th:src="@{/js/header.js}"></script> <!-- 全局头部逻辑 -->
<!-- ... 其他全局脚本 ... -->
<!-- 动态包含页面特定脚本 -->
<th:block th:if="${viewName != null}" th:replace="~{${viewName} :: scripts}"></th:block>
```

## 8.2 评论系统架构 (`article/detail.js`)

DevSpace 实现了一个可扩展的嵌套评论系统，其前端逻辑主要位于 `article/detail.js`。

### 8.2.1 数据模型 (`CommentVO`)

评论系统支持无限层级的嵌套结构，通过 `parentId` 和 `replies` 列表实现。`CommentVO` 包含评论者 `username` 和 `avatarUrl`。

### 8.2.2 前端实现 (`article/detail.js`)

- **模块化**: 评论相关的函数 (加载、渲染、提交、删除) 集中管理。
- **HTML/JS 分离**: HTML 结构在 `detail.html`，所有交互逻辑在 `detail.js`。
- **动态渲染**: 使用 `renderComment()` 和 `renderReply()` 函数动态创建评论和回复的 DOM 元素。
- **用户头像**: 在渲染评论和回复时，使用 `avatarUrl` 显示用户头像，提供占位符作为备选。
- **无限嵌套**: 通过递归或迭代方式处理和渲染任意层级的回复。
- **事件委托**: 为动态创建的评论/回复元素（如回复按钮、删除按钮）添加事件监听器。
- **乐观更新**: 提交评论/回复后，立即在 UI 上显示（可能带有"发布中"状态），并同时发送 API 请求。
- **大整数 ID 处理**: 使用 `dataset.originalId` 存储评论 ID 的原始字符串形式，并在 API 调用中使用该字符串。
- **加载优化**: 调整了 `pageSize` 以一次加载更多评论。

### 8.2.3 性能与健壮性

- **缓存 DOM 引用**：减少重复的 `document.getElementById` 或 `querySelector` 调用。
- **错误处理**：增强了 API 调用失败时的错误处理和用户反馈。
- **条件检查**: 增加了对空值和边界情况的检查。

## 8.3 文章目录导航 (TOC) 实现

DevSpace 实现了一个动态生成的文章目录导航功能，用于提升长文章的阅读体验。

### 8.3.1 功能概述

- **自动生成**：基于文章内容中的标题标签（h1-h6）自动生成结构化目录。
- **导航定位**：点击目录项可平滑滚动到对应的文章章节。
- **响应式设计**：在大屏幕设备上在侧边栏显示，小屏幕设备上提供可折叠的内联目录。
- **当前位置指示**：滚动阅读时自动高亮当前阅读的章节目录项。

### 8.3.2 技术实现

- **前端逻辑**：在 `article/detail.js` 中实现了 TOC 的动态生成和交互控制。
- **DOM 结构**：TOC 容器添加在文章侧边栏中，作者信息卡片下方。
- **事件监听**：使用 `IntersectionObserver` API 监测阅读位置，更新当前目录项高亮状态。
- **平滑滚动**：使用 `scrollIntoView({ behavior: 'smooth' })` 实现点击目录项后的平滑滚动。
- **响应式适配**：根据屏幕大小动态调整 TOC 的显示方式和位置。

### 8.3.3 实现细节

- **标题解析**：递归处理文章中的 h1-h6 标签，生成嵌套层级的目录结构。
- **唯一标识**：为文章中没有 id 的标题元素自动添加唯一标识，以支持锚点导航。
- **目录层级**：支持多达六级的目录层级结构，使用适当的缩进和样式区分不同级别。
- **动态更新**：仅在页面初始加载时生成目录，无需在阅读过程中重新计算。
- **小屏适配**：在移动设备上提供内联折叠式目录，点击后展开，再次点击折叠。
- **交互优化**：添加了目录项的悬停效果和当前位置指示，提升用户体验。

# 9. 文章浏览量统计系统设计

## 9.1 概述

为跟踪文章的受欢迎程度，系统实现了基于Redis缓存和MySQL持久化的浏览量统计功能。

## 9.2 功能点

- **实时计数**: 访问文章详情页时，通过Redis `HINCRBY` 原子操作增加浏览量。
- **高效读取**: API响应（文章详情、列表）直接从Redis或数据库获取最新浏览量。
- **数据持久化**: 定期将Redis中的浏览量同步到MySQL的`t_article_viewcount`表。
- **最终一致性**: 通过定时任务确保缓存与数据库数据最终保持一致。

## 9.3 技术实现

### 9.3.1 缓存层 (Redis)

- **数据结构**: 使用Redis Hash结构，Key为`article_views`。
- **字段**: Hash的Field为文章ID (字符串)，Value为对应的浏览量 (数值)。
- **操作**: 
    - 增加: `HINCRBY article_views <articleId> 1`
    - 获取单个: `HGET article_views <articleId>`
    - 获取所有: `HGETALL article_views` (用于同步)
- **客户端**: `core/src/main/java/org/jeffrey/core/cache/RedisClient.java`

### 9.3.2 持久化层 (MySQL)

- **数据表**: `t_article_viewcount` (字段见 #4 数据库设计)。
- **Mapper**: `service/src/main/java/org/jeffrey/service/article/repository/mapper/ArticleViewCountMapper.java` (基于MyBatis-Plus)

### 9.3.3 服务层 (`ArticleViewCountService`)

- **接口**: `service/src/main/java/org/jeffrey/service/article/service/ArticleViewCountService.java`
- **实现**: `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleViewCountServiceImpl.java`
    - `incrementViewCount(articleId)`: 调用Redis `hIncr`。
    - `getViewCount(articleId)`: 优先读Redis，失败则读DB并回填Redis。
    - `syncViewCountsToDatabase()`: 核心同步逻辑，由调度器调用。

### 9.3.4 定时调度 (`ArticleViewCountSyncScheduler`)

- **类**: `service/src/main/java/org/jeffrey/service/scheduler/ArticleViewCountSyncScheduler.java`
- **职责**: 负责按计划调用`ArticleViewCountService.syncViewCountsToDatabase()`。
- **策略**: 
    - `@Scheduled(fixedRate = 300000)`: 每5分钟执行一次。
    - `@Scheduled(cron = "0 0 2 * * *")`: 每天凌晨2点执行一次。
- **配置**: 需要在配置类 (如 `ServiceAutoConfig`) 上添加 `@EnableScheduling`。

### 9.3.5 业务集成 (`ArticleServiceImpl`)

- `getArticleById()`: 在返回文章详情前调用 `incrementViewCount()`。
- `convertToVO()` / `convertToSummaryVO()`: 调用 `getViewCount()` 填充VO中的 `viewCount` 字段。

## 9.4 数据同步流程

1. `ArticleViewCountSyncScheduler` 按计划触发。
2. 调用 `ArticleViewCountService.syncViewCountsToDatabase()`。
3. 服务层调用 `RedisClient.hGetAll("article_views")` 获取Redis中所有文章的浏览量。
4. 遍历获取到的Map。
5. 对每个 `articleId`：
    a. 查询 `t_article_viewcount` 表中是否存在记录。
    b. 如果存在，更新 `view_count` 字段。
    c. 如果不存在，插入新记录。
6. 记录同步日志。

## 9.5 注意事项

- Redis Key (`article_views`) 应保持一致。
- 定时任务的执行频率应根据系统负载和数据新鲜度要求调整。
- 异常处理：同步过程中单个文章失败不应中断整个任务。

# 10. AI 聊天服务集成

## 10.1 概述

DevSpace 集成了一个 AI 聊天服务，允许登录用户通过 REST API 与配置的 AI 模型进行交互。该功能旨在提供一个基础的 AI 对话能力，未来可扩展用于内容辅助、问答等场景。

## 10.2 功能点

- **API 驱动**: 
    - 通过 `POST /api/ai/chat` (简单模式) 和 `POST /api/ai/chat/advanced` (高级模式) 端点接收用户提问。
    - 通过 `POST /api/ai/chat/stream` 和 `POST /api/ai/chat/stream/advanced` 端点提供流式响应。
    - 通过 `POST /api/ai/summary` 端点提供文章摘要生成功能。
- **认证**: 要求用户必须登录才能使用此功能。
- **后端服务**: `AIService` 抽象了与 AI 模型的交互，当前实现 (`SambanovaAIServiceImpl`) 使用 Sambanova API。
    - 支持 `List<ChatMessageDTO>` 输入，允许更复杂的对话。
    - 包含专门的 `getArticleSummary` 方法。
- **配置**: AI 服务的相关配置（如 API Key、模型名称、基础 URL、是否启用流式响应）通过 `application.yml` 和环境变量管理。
- **标准响应**: 对于非流式请求，使用统一的 `ResVo` 格式返回 AI 的回答或错误信息。
- **异常处理**: 使用自定义运行时异常 (`AIServiceException`, `AIConfigurationException`) 进行错误管理。

## 10.3 技术实现

- **Controller**: `AIChatController` 处理 API 请求，进行权限验证，调用服务。
- **Service**: `AIService` 接口和 `SambanovaAIServiceImpl` 实现，使用 `RestTemplate` 调用外部 API。
- **Configuration**: `SambanovaProperties` 类加载 `ai.sambanova.*` 配置。
- **DTOs**: 定义了 `ChatRequestDTO`, `ChatMessageDTO`, `ChatResponseDTO` 等用于 API 交互。
- **Exceptions**: 定义了 `AIServiceException` 和 `AIConfigurationException` 运行时异常。

## 10.4 注意事项

- **API Key 安全**: Sambanova API Key 需要通过环境变量 `SAMBANOVA_API_KEY` 设置，不应硬编码或提交到版本控制。
- **成本与限制**: 使用第三方 AI API 可能涉及成本和使用限制，需要注意监控。
- **错误处理**: 服务实现和控制器包含对 API 调用失败和配置错误的健壮处理。控制器层捕获自定义运行时异常并返回适当的错误响应。