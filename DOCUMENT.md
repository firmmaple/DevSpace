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