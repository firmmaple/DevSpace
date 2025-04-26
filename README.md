# 1. 项目总览

## 项目介绍

DevSpace是一个基于SpringBoot的博客社区，面向互联网开发者的技术内容分享与交流平台，拥有完整的文章发布、评论、点赞、收藏、搜索、统计、用户资料管理等功能。
前端使用Thymeleaf + Bootstrap 5.x，后端使用SpringBoot + MyBatis-Plus + MySQL + Redis + RabbitMQ。

主要目标为通过这个项目学习SpringBoot的使用，写入CV作为个人项目。

[项目文档](DOCUMENT.md) 中描述了项目的详细设计和相关规定，当AI生成代码需要参考。

## 项目模块

```
DevSpace
├── api -- 定义一些通用的枚举、实体类，定义 DO\DTO\VO 等
├── core -- 核心工具/组件相关模块，如工具包 util， 通用的组件都放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）
├── service -- 服务模块，业务相关的主要逻辑，DB 的操作都在这里（包含文件服务）
├── ui -- HTML 前端资源（包括 JavaScript、CSS、Thymeleaf 等）
├── web -- Web模块、HTTP入口、项目启动入口，包括权限身份校验、全局异常处理等
```

## 所用技术栈

- Spring Boot 3.x
- Thymeleaf + Bootstrap 5.x (用于前端页面渲染)
- Spring Security 6.x (使用 JWT Cookie 进行认证和授权，支持 OAuth2)
- MyBatis, MyBatis-Plus (已使用)
- MySQL
- Redis (已使用，用于存储在线用户、文章浏览量缓存)
- RabbitMQ (计划使用，用于评论、点赞、收藏等异步处理)
- HandlerExceptionResolver + @RestControllerAdvice (已使用，用于全局异常处理)
- AOP + TraceID (已实现，用于日志记录)
- Spring Scheduling (已使用，用于定时任务)

## 项目结构

```
.                    # 项目根目录
├── pom.xml          # 父Maven配置
├── api/             # 通用接口、DTO、VO定义
│   └── src/main/java/org/jeffrey/api/
│       ├── dto/         # 数据传输对象 (请求体)
│       ├── vo/          # 视图对象 (响应体)
│       └── exception/   # 自定义异常
├── core/            # 核心工具/组件
│   └── src/main/java/org/jeffrey/core/
│       ├── security/    # 安全相关工具 (JWTUtil, Matchers)
│       ├── cache/       # 缓存相关 (RedisClient)
│       ├── trace/       # 日志追踪 (AOP, Filter, Util)
│       └── util/        # 通用工具类
├── service/         # 业务逻辑、数据库交互
│   └── src/main/java/org/jeffrey/service/
│       ├── security/    # Spring Security 配置、过滤器、服务、处理器
│       ├── user/        # 用户服务、仓库 (Mapper/Entity)
│       ├── article/     # 文章服务、仓库 (Mapper/Entity, ViewCountDO/Mapper)
│       ├── file/        # 文件服务 (LocalFileServiceImpl)
│       ├── scheduler/   # 定时任务 (ArticleViewCountSyncScheduler)
│       └── *.java       # 配置类 (MybatisPlusConfig, ServiceAutoConfig)
├── ui/              # 前端资源
│   └── src/main/resources/
│       ├── templates/   # Thymeleaf模板 (layout/, articles/, user/, *.html)
│       └── static/      # 静态资源 (js/, css/, images/)
├── web/             # Web入口、控制器、全局异常处理
│   └── src/main/java/org/jeffrey/web/
│       ├── login/       # 认证控制器 (AuthController)
│       ├── article/     # 文章控制器 (ArticleController, ArticleRestController)
│       ├── user/        # 用户相关控制器 (ProfileController, UserProfileRestController)
│       ├── file/        # 文件访问控制器 (FileController)
│       ├── home/        # 首页控制器
│       ├── exception/   # 全局异常处理器 (GlobalExceptionHandler)
│       └── DevSpaceApplication.java # Spring Boot启动类
│   └── src/main/resources/
│       ├── application.yml # 主配置文件
│       ├── application-dal.yml # 数据访问层配置
│       ├── init.sql        # 数据库初始化脚本
│       └── logback.xml     # 日志配置
├── logs/            # 日志文件目录 (运行时生成)
├── DOCUMENT.md      # 项目详细设计文档
├── CHANGE.md        # 功能变更记录
└── README.md        # 本文件
```

# 2. 如何开始 (Getting Started)

*目前不需要任何其他内容*

项目启动后，默认访问地址为 `http://localhost:8088` (或您在 `application.yml` 中配置的端口)。


# 3. 功能模块说明

## 3.1 AOP+TraceID日志追踪系统

DevSpace实现了基于AOP和TraceID的接口访问日志记录系统，用于请求追踪、监控和诊断。

### 已实现功能

1. **TraceID生成与传递**
   - 通过过滤器为每个HTTP请求自动生成唯一的TraceID
   - 支持从请求头中读取已有TraceID，便于跨服务调用追踪
   - 通过MDC(Mapped Diagnostic Context)将TraceID注入日志系统

2. **AOP切面日志记录**
   - 基于`@TraceLog`注解标记需要记录日志的方法
   - 自动记录方法的请求参数、执行时间和返回结果
   - 记录异常信息，便于问题排查

3. **灵活的日志配置**
   - 自定义的日志格式，包含TraceID
   - 支持控制台和文件日志输出
   - 可配置的日志轮转策略

### 技术实现

1. **核心组件**:
   - `TraceLog` - 自定义注解，用于标记需要记录日志的方法
   - `TraceIdFilter` - Servlet过滤器，为每个请求生成和管理TraceID
   - `TraceLogAspect` - AOP切面，实现方法执行前后的日志记录
   - `TraceUtil` - 工具类，提供TraceID的生成和获取方法

2. **日志格式**:
   - 包含时间戳、日志级别、线程信息、类名和TraceID
   - 示例: `2023-08-01 12:34:56.789 INFO [thread-1] o.j.web.Controller [TraceID: abc123] - 日志内容`

3. **使用方式**:
   - 在方法或类上添加`@TraceLog`注解即可启用日志记录
   - 可通过注解参数控制是否记录请求参数和返回结果

### 代码位置

- **核心实现**:
  - `core/src/main/java/org/jeffrey/core/trace/TraceLog.java` - 注解定义
  - `core/src/main/java/org/jeffrey/core/trace/TraceIdFilter.java` - 过滤器实现
  - `core/src/main/java/org/jeffrey/core/trace/TraceLogAspect.java` - AOP切面实现
  - `core/src/main/java/org/jeffrey/core/trace/TraceUtil.java` - 工具类

- **配置文件**:
  - `web/src/main/resources/logback.xml` - 日志配置文件

### 使用示例

在Controller层使用:
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/{id}")
    @TraceLog("获取用户信息")
    public User getUser(@PathVariable Long id) {
        return userService.getById(id);
    }
}
```

在Service层使用:
```java
@Service
public class UserServiceImpl implements UserService {
    @Override
    @TraceLog(value = "根据ID查询用户", recordResult = true)
    public User getById(Long id) {
        // 业务逻辑
        return userRepository.findById(id).orElse(null);
    }
}
```

## 3.2 文章管理系统

DevSpace 实现了完整的文章发布和管理系统，支持文章的创建、阅读、编辑和删除功能。

### 已实现功能

1. **文章列表**
   - 分页展示所有已发布的文章
   - 文章搜索和筛选
   - 按状态过滤（已发布/草稿）
   - 显示文章摘要、作者、发布时间和统计数据

2. **文章详情**
   - 完整展示文章内容
   - 作者信息展示
   - 文章目录导航（自动生成章节目录）
   - 文章互动功能（点赞、收藏、评论）
   - 相关文章推荐
   - 标签展示和导航

3. **文章创建与编辑**
   - Markdown编辑器（使用 [marked.js](https://marked.js.org/) + [DOMPurify](https://github.com/cure53/DOMPurify) 实现实时预览）
   - 文章标题、内容和标签管理
   - 摘要将自动生成（从内容提取），无需手动输入
   - 草稿保存功能
   - 表单验证

4. **文章删除**
   - 作者可删除自己的文章
   - 删除前确认机制

### 技术实现

1. **前端实现**:
   - 响应式UI设计，基于Bootstrap 5
   - 使用Thymeleaf模板引擎渲染页面
   - JavaScript动态加载和渲染内容
   - Fetch API进行前后端数据交互
   - 使用 marked.js + DOMPurify 处理 Markdown 内容及预览
   - HTML/JavaScript 分离 (`article/detail.js` 用于详情页逻辑, `create.html` 内嵌脚本用于创建/编辑页)

2. **后端实现**:
   - `ArticleController` 处理页面路由
   - `ArticleRestController` 提供RESTful API
   - `ArticleService` 处理业务逻辑
   - JWT验证用户身份和权限

3. **数据模型**:
   - `ArticleVO` 视图对象展示文章详情
   - `ArticleSummaryVO` 视图对象展示文章摘要
   - `ArticleCreateDTO` 数据传输对象处理文章创建 (不含 `summary`)
   - `ArticleUpdateDTO` 数据传输对象处理文章更新 (不含 `summary`)

### 代码位置

- **UI 模板**:
   - `ui/src/main/resources/templates/articles/list.html` - 文章列表页
   - `ui/src/main/resources/templates/articles/detail.html` - 文章详情页
   - `ui/src/main/resources/templates/articles/create.html` - 文章创建/编辑页

- **控制器**:
   - `web/src/main/java/org/jeffrey/web/article/ArticleController.java` - 页面控制器
   - `web/src/main/java/org/jeffrey/web/article/ArticleRestController.java` - REST API控制器

- **服务**:
   - `service/src/main/java/org/jeffrey/service/article/service/ArticleService.java` - 服务接口
   - `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleServiceImpl.java` - 服务实现

### 下一步计划

- 添加文章标签管理系统
- 实现文章分类功能
- 添加文章搜索（全文检索）
- 实现文章统计分析

## 3.3 用户认证与注册

DevSpace 实现了用户认证和注册功能，支持**传统用户名/密码登录**和**GitHub OAuth2登录**。认证状态通过安全的 **HTTP-only JWT Cookie** (`jwt_token`) 和**标准 UserInfo Cookie** (`user_info`) 进行管理。

### 已实现功能

1. **用户注册**
    - 用户注册表单页面 (`/register`)
    - 客户端和服务器端表单验证
    - 密码加密存储
    - 重复用户名检查

2. **用户认证**
    - **传统登录**:
        - 用户登录表单 (`/login`)
        - 成功后，服务器返回用户信息 (`UserDTO`)，前端 `AuthUtils` 设置 `user_info` Cookie；服务器设置 `jwt_token` HttpOnly Cookie。
    - **GitHub OAuth2 登录**:
        - 登录页面提供"使用 GitHub 账号登录"按钮 (`/oauth2/authorization/github`)
        - 通过 Spring Security OAuth2 Client 处理认证流程。
        - `OAuth2LoginSuccessHandler` 在认证成功后：
            - 获取或创建本地用户。
            - 设置 `jwt_token` HttpOnly Cookie。
            - **设置 `user_info` 标准 Cookie** (修复了之前未设置此 Cookie 的问题)。
            - 重定向到首页。
    - **JWT 验证**: 后续请求通过 `JWTAuthenticationFilter` 自动验证 `jwt_token` Cookie。
    - **认证失败处理**: `GlobalExceptionHandler` 和 `CustomAuthenticationEntryPoint` 统一处理。
    - **访问控制**: 通过 `SecurityConfig` 保护路由。

3. **Cookie-based 认证管理**
    - **JWT 令牌 (`jwt_token`)**: 通过 **HTTP-Only Cookie** 存储，防止 XSS 攻击。
    - **用户信息 (`user_info`)**: 通过**标准 Cookie** 存储，允许前端 JavaScript (`AuthUtils.js`) 读取以更新 UI 和判断登录状态。
    - **统一认证机制**: 无论哪种登录方式，最终都依赖这两个 Cookie 进行状态管理。
    - **自动凭证传输**: `AuthUtils.authenticatedFetch` 使用 `credentials: 'include'` 确保 Cookie 自动随请求发送。

### 技术实现

1. **前端实现** (`AuthUtils.js`, `login.html`):
    - Thymeleaf 渲染的登录/注册页面，包含 GitHub 登录按钮。
    - **统一认证工具 (`AuthUtils`)**: 封装 `user_info` Cookie 操作 (`setUserInfo`, `getUserInfo`, `isAuthenticated`, `logout`)。
    - `authenticatedFetch`: 包装 `fetch` API，自动处理凭证发送和基本响应/错误处理。

2. **后端实现**:
    - `AuthController`: 处理传统登录和注册请求。
    - `UserService`: 处理用户数据，包含 `processOAuth2User` 方法。
    - `SecurityConfig`: 配置 Spring Security，启用 JWT 和 OAuth2 登录。
    - `JWTAuthenticationFilter`: 验证 `jwt_token` Cookie。
    - `OAuth2LoginSuccessHandler`: 处理 OAuth2 登录成功后的逻辑，设置两个 Cookie。
    - `CustomAuthenticationEntryPoint`, `GlobalExceptionHandler`: 处理认证/授权异常。

3. **数据模型**: `UserDO`, `UserDTO`, `RegisterDTO`, `ResVo`。

### 前端请求最佳实践

为确保API请求的安全性和一致性，DevSpace遵循以下前端请求规范：

1. **强制使用 `AuthUtils.authenticatedFetch`**: 所有需要认证的API请求**必须**使用`AuthUtils.authenticatedFetch(url, options)`。此函数会自动：
   * 设置 `credentials: 'include'`，指示浏览器发送 Cookie（包括 `jwt_token`）。
   * 调用 `fetch` 并解析响应为 JSON。
   * 调用 `AuthUtils.handleApiResponse(jsonData)` 进行初步处理：
     * 处理未登录 (`100_403_003`) 和其他 API 错误。
     * 成功时返回完整的原始 JSON 响应对象。

2. **处理 `authenticatedFetch` 的结果**: 你的 `.then()` 回调函数将直接收到**完整的 JSON 响应对象**。你需要从中提取 `result` 部分来获取业务数据。**不要**再次调用 `.json()`。

   ```javascript
   // 正确使用 authenticatedFetch (JWT 通过 Cookie 自动发送)
   AuthUtils.authenticatedFetch('/api/user/profile')
     .then(response => {
       // 'response' 是完整的 JSON 对象, 例如 { status: { code: 0, msg: 'OK' }, result: { username: '...', ... } }
       // 不需要再调用 response.json()
       if (response.status.code === 0) {
           const userData = response.result;
           console.log(userData.username);
           renderUserProfile(userData);
       } else {
           console.error("获取用户信息失败:", response.status.msg);
           showError(response.status.msg);
       }
     })
     .catch(error => {
       // 处理由 handleApiResponse 或 fetch 本身抛出的错误
       console.error("获取用户信息失败:", error.message);
       showError(error.message);
     });
   ```

3. **统一错误处理**: 在 `.catch()` 块中处理特定于该 API 调用的错误。通用错误（如未登录）已由 `authenticatedFetch` 内部处理。

4. **用户信息存储**: 用户信息（如用户名、ID、头像URL）在登录成功后存储在 Cookie (`user_info`) 中（通过 `AuthUtils.setUserInfo`)，用于 UI 显示。JWT 令牌本身存储在 HTTP-only Cookie (`jwt_token`) 中。

- **UI 模板**:
    - `ui/src/main/resources/templates/register.html`
    - `ui/src/main/resources/templates/login.html`

- **后端实现**:
    - `web/src/main/java/org/jeffrey/web/login/AuthController.java`
    - `service/src/main/java/org/jeffrey/service/user/service/impl/UserServiceImpl.java`
    - `service/src/main/java/org/jeffrey/service/security/SecurityConfig.java`
    - `service/src/main/java/org/jeffrey/service/security/OAuth2LoginSuccessHandler.java`

- **DTO**:
    - `api/src/main/java/org/jeffrey/api/dto/RegisterDTO.java`
    - `api/src/main/java/org/jeffrey/api/dto/user/UserDTO.java`

### 下一步计划

- 添加邮箱验证
- 实现密码重置功能
- 增强密码策略
- 添加其他 OAuth2 提供商（如 Google）

## 3.4 用户资料管理

DevSpace 允许用户查看和编辑自己的个人资料，并上传头像。

### 已实现功能

1.  **个人资料页面 (`/profile`)**: 显示用户的基本信息（用户名、邮箱、简介、加入日期）和头像。
2.  **资料编辑**: 用户可以修改用户名、邮箱和个人简介。
3.  **头像上传**: 用户可以点击头像区域上传新的头像图片 (JPG/PNG, < 2MB)。
4.  **实时更新**: 编辑资料或上传头像后，页面和 Header 中的用户信息会实时更新。
5.  **用户名变更**: 如果用户名被修改，系统会重新生成 JWT 令牌并更新 Cookie，以维持登录状态。

### 技术实现

1.  **前端 (`profile.js`)**: 
    *   使用 `AuthUtils` 获取和更新用户信息 Cookie。
    *   通过 `authenticatedFetch` 调用后端 API 获取和提交资料。
    *   使用 `FormData` 上传头像文件。
    *   触发 `userInfoUpdated` 自定义事件通知 Header 更新。
2.  **后端 (`ProfileController`, `UserProfileRestController`)**: 
    *   提供 `/profile` 页面路由。
    *   提供 `/api/user/profile` (GET/POST) 和 `/api/user/avatar` (POST) API 端点。
    *   使用 `UserService` 处理业务逻辑。
    *   使用 `FileService` (本地存储实现 `LocalFileServiceImpl`) 处理文件上传。
    *   `FileController` 提供头像文件的访问端点 (`/api/file/avatars/{filename}`).
3.  **服务 (`UserServiceImpl`, `LocalFileServiceImpl`)**: 
    *   `UserService` 负责更新用户数据库记录。
    *   `FileService` 负责文件验证、存储和 URL 生成。

### 代码位置

-   **UI 模板**: `ui/src/main/resources/templates/profile.html`
-   **前端脚本**: `ui/src/main/resources/static/js/profile.js`
-   **控制器**: 
    -   `web/src/main/java/org/jeffrey/web/user/ProfileController.java`
    -   `web/src/main/java/org/jeffrey/web/user/UserProfileRestController.java`
    -   `web/src/main/java/org/jeffrey/web/file/FileController.java`
-   **服务**: 
    -   `service/src/main/java/org/jeffrey/service/user/service/impl/UserServiceImpl.java`
    -   `service/src/main/java/org/jeffrey/service/file/impl/LocalFileServiceImpl.java`
-   **DTO/VO**: `UserUpdateDTO`, `UserVO`

## 3.5 前端布局系统

DevSpace 使用 Thymeleaf 模板引擎构建了一个模块化的前端布局系统，使页面结构统一、代码复用性高。

### 布局文件结构

```
ui/src/main/resources/templates/
├── layout/
│   ├── main.html    # 主布局文件，所有页面的基础模板
│   └── header.html  # 页面头部布局，包含导航栏和用户菜单
├── articles/        # 文章相关页面
│   ├── list.html    # 文章列表页
│   ├── detail.html  # 文章详情页
│   └── create.html  # 文章创建页
├── user/
│   └── profile.html # 用户资料页
├── index.html       # 首页内容片段
├── login.html       # 登录页面
└── register.html    # 注册页面
```

### 布局工作原理

1. **主布局文件 (main.html)**
    - 定义了页面的基础结构，包括头部、内容区域和页脚
    - 通过 `th:replace` 引入 header.html 中的导航栏
    - 使用 `th:replace="~{__${viewName}__ :: content}"` 动态引入各个页面的内容片段
    - 在底部加载必要的脚本，包括 Bootstrap、认证和用户界面脚本

2. **头部布局 (header.html)**
    - 包含两个主要片段：`headerFragment`（页面头部元数据）和 `navbar`（导航栏）
    - 定义了导航菜单、搜索框和用户界面元素
    - 包含认证状态相关的两种视图：用户菜单（已登录）和登录/注册按钮（未登录）
    - 加载全局 CSS 样式表和图标库

3. **内容页面**
    - 每个页面使用 `th:fragment="content"` 定义自己的内容片段
    - 控制器设置 `viewName` 属性来决定加载哪个内容片段
    - 可以通过 `th:fragment="scripts"` 定义页面特定的脚本

### HTML 与 JavaScript 分离

为了提高代码的可维护性和复用性，DevSpace 实现了 HTML 与 JavaScript 的分离：

1. **结构与行为分离**:
    - HTML 文件仅包含页面结构和静态元素
    - JavaScript 逻辑放在单独的 .js 文件中
    - 通过 `th:src` 属性引用外部 JavaScript 文件

2. **文件组织**:
    ```
    ui/src/main/resources/static/js/
    ├── auth.js       # 认证相关工具和函数
    ├── header.js     # 全局头部交互逻辑
    ├── profile.js    # 用户资料页面逻辑
    └── article/
        └── detail.js # 文章详情页脚本
    ```

3. **脚本引用方式**:
    ```html
    <!-- 在页面的 scripts 片段中引用对应的 JS 文件 (e.g., article/detail.html) -->
    <th:block th:fragment="scripts">
        <script th:src="@{/js/article/detail.js}"></script>
    </th:block>
    ```

### 认证状态切换

用户界面会根据认证状态动态变化：

1. **已登录状态**
    - 显示用户头像和用户名
    - 显示用户下拉菜单（包含发布文章、我的文章、个人资料等选项）
    - 隐藏登录/注册按钮

2. **未登录状态**
    - 显示登录/注册按钮
    - 隐藏用户菜单

实现方式：

- 使用 CSS 类（force-show/force-hide）控制元素显示/隐藏
- 使用 JavaScript（header.js）检测用户认证状态
- 根据Cookie中的用户信息 (`user_info`) 判断登录状态

### 控制器集成

```java

@GetMapping("/")
public String index(Model model) {
    // 设置页面属性
    model.addAttribute("title", "DevSpace - Home");
    model.addAttribute("currentPage", "home");
    model.addAttribute("viewName", "index");

    // ... 其他数据 ...

    return "layout/main"; // 使用主布局作为视图
}
```

## 3.6 全局异常处理系统

DevSpace 实现了一个基于 `@RestControllerAdvice` 的全局异常处理系统，用于统一处理 Web 层（特别是 REST API）抛出的异常，并返回标准化的 `ResVo` 响应。

### 核心组件

1.  **`GlobalExceptionHandler`**: 
    *   位于 `web/src/main/java/org/jeffrey/web/exception/GlobalExceptionHandler.java`。
    *   使用 `@RestControllerAdvice` 注解，自动拦截 Controller 层抛出的异常。
    *   为不同类型的异常（如 `ConstraintViolationException`, `MethodArgumentNotValidException`, `BindException`, `AccessDeniedException`, `AuthenticationException`, 以及通用的 `Exception`）定义了 `@ExceptionHandler` 方法。
    *   所有处理器方法都返回 `ResVo<String>` 对象，包含来自 `StatusEnum` 的标准错误码和消息。
    *   记录带有 TraceID 的错误日志。

2.  **`CustomAuthenticationEntryPoint` 交互**: 
    *   对于需要认证的 API 请求，如果认证失败 (`AuthenticationException`)，`CustomAuthenticationEntryPoint` 会将异常委托给 `GlobalExceptionHandler` 处理，最终返回 `FORBID_NOTLOGIN` 的 JSON 响应。

### 工作流程

1.  当 Controller 中的方法（或 Spring Security 过滤器链中的认证/授权环节）抛出异常时。
2.  如果异常是 `AuthenticationException` 且请求是 API 请求，`CustomAuthenticationEntryPoint` 将其交给 `GlobalExceptionHandler`。
3.  `GlobalExceptionHandler` 中匹配的 `@ExceptionHandler` 方法被触发。
4.  该方法构造一个包含相应 `StatusEnum` 错误码的 `ResVo` 对象。
5.  将 `ResVo` 对象序列化为 JSON 并返回给客户端。

### 使用示例

在 Service 层抛出业务异常：
```java
@Service
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            // 抛出业务异常，会被 GlobalExceptionHandler 捕获
            throw new BusinessException(StatusEnum.USER_NOT_EXISTS, id);
        }
        return user;
    }
}
```

前端通过 `authenticatedFetch` 接收并处理：
```javascript
AuthUtils.authenticatedFetch('/api/users/' + userId)
  .then(response => { 
    // response 是完整的 ResVo 对象 
    if (response.status.code === 0) {
       const userData = response.result;
       // 使用 userData
    }
  })
  .catch(error => { 
    // 处理特定于此调用的错误，或显示通用错误信息
    console.error("获取用户信息失败:", error.message);
    showError(error.message);
  });
```

## 3.7 管理后台 (Admin Management)

DevSpace 包含一个管理后台，仅限具有 `ROLE_ADMIN` 角色的用户访问。

### 功能

1.  **用户管理**: 
    *   查看所有注册用户的列表 (`/admin/users`)。
    *   显示用户的 ID、用户名和是否为管理员。
2.  **文章管理**:
    *   查看所有文章的列表 (`/admin/articles`)。
    *   显示文章的 ID、标题、作者、状态和创建日期。

### 技术实现

1.  **访问控制**: 
    *   使用 Spring Security 的 `@PreAuthorize("hasRole('ADMIN')")` 注解保护 `AdminController` 中的方法。
    *   `SecurityConfig` 配置 `/admin/**` 路径需要 `ROLE_ADMIN` 权限。
    *   `CustomUserDetails` 根据用户的 `isAdmin` 字段授予 `ROLE_ADMIN`。
2.  **前端**: 
    *   管理后台的导航链接 (`/admin`) 只在用户是管理员时在页眉中显示 (通过 `header.js` 控制)。
    *   使用 Thymeleaf 模板 (`admin/users-management.html`, `admin/articles-management.html`) 显示管理数据。
3.  **后端**: 
    *   `AdminController` 处理 `/admin` 路径下的请求。
    *   `UserService` 和 `ArticleService` 提供获取用户和文章列表的数据。

### 代码位置

-   **控制器**: `web/src/main/java/org/jeffrey/web/admin/AdminController.java`
-   **视图模板**: `ui/src/main/resources/templates/admin/`
-   **安全配置**: `service/src/main/java/org/jeffrey/service/security/SecurityConfig.java`
-   **用户详情**: `service/src/main/java/org/jeffrey/service/security/CustomUserDetails.java`
-   **前端脚本**: `ui/src/main/resources/static/js/header.js`

## 3.8 文章互动系统

DevSpace 采用事件驱动架构和消息队列实现文章交互功能（点赞、收藏、评论），提高系统性能和用户体验。

#### 3.8.1 核心特性

- **异步处理**：使用Spring事件 + RabbitMQ实现异步处理，减轻主业务线程负担
- **乐观UI**：前端实现乐观更新策略，提供即时反馈
- **可靠性**：消息队列确保互动数据最终一致性
- **扩展性**：事件驱动设计便于添加新的互动类型

#### 3.8.2 评论系统改进

- **用户头像展示**：评论和回复中显示用户的头像。
- **无层级限制**：支持任意层级的评论嵌套回复。
- **优化加载**：提高了单次加载的评论数量。
- **代码分离**：评论相关的前端逻辑已整合到 `article/detail.js`。

#### 3.8.3 技术实现

- Spring事件：用于应用内部事件发布与监听
- RabbitMQ：实现可靠的异步消息处理
- 消息持久化：确保系统重启后消息不丢失
- 失败重试：支持消息处理失败后的自动重试
- `CommentVO` 扩展：增加了 `avatarUrl` 字段。
- `CommentService` 优化：批量获取用户信息以提高效率。

#### 3.8.4 性能优势

- 提高响应速度：主线程不等待互动处理完成即可返回
- 削峰填谷：消息队列缓冲高峰期的互动请求
- 资源隔离：交互处理失败不影响主业务流程
- 简化维护：便于监控和排查互动相关问题

## 3.9 大整数ID处理

DevSpace 使用 Java 的 Long 类型作为实体 ID，采用特定策略处理 JavaScript 中的大整数精度问题。

#### 3.9.1 问题与解决方案

- **问题**：Long 类型 ID 可能超出 JavaScript 安全整数范围 (2^53-1)，导致精度丢失
- **解决方案**：
  - 后端：使用 `@JsonSerialize(using = ToStringSerializer.class)` 将 ID 序列化为字符串
  - 前端：始终以字符串形式处理 ID，避免数值转换和运算 (例如使用 `dataset.originalId` 存储原始字符串ID)

#### 3.9.2 最佳实践

- 所有实体 ID 字段在 API 响应中都以字符串形式返回
- 前端发送请求时保持 ID 的字符串形式
- 避免使用 `parseInt()` 或 `Number()` 等转换 ID
- DOM 属性中保持 ID 的字符串形式
- 必要时使用 `BigInt` 类型处理大整数运算

## 3.10 文章浏览量统计系统 (Article View Count System)

DevSpace 实现了一个基于 Redis 缓存和 MySQL 持久化的文章浏览量统计系统。

### 已实现功能

1.  **浏览量计数**:
    *   每次成功访问文章详情页 (`/api/articles/{id}`) 时，自动增加该文章的浏览量。
    *   使用 Redis Hash 结构 (`article_views`) 实时存储和更新浏览量，性能高。
2.  **数据展示**:
    *   文章详情页和文章列表页均显示准确的文章浏览量。
3.  **持久化与同步**:
    *   新增 `article_viewcount` 表用于持久化存储浏览量。
    *   通过 `ArticleViewCountSyncScheduler` 定时任务：
        *   每 5 分钟将 Redis 中的浏览量增量同步到 MySQL 数据库。
        *   每天凌晨 2 点执行一次全量同步，确保数据最终一致性。
4.  **缓存策略**:
    *   读取浏览量时优先从 Redis 获取。
    *   若 Redis 中不存在，则从数据库加载并回填到 Redis 缓存。

### 技术实现

1.  **Redis 缓存 (`RedisClient`)**:
    *   使用 Hash (`article_views`) 存储 `articleId -> viewCount` 映射。
    *   利用 `hIncr` 原子操作增加浏览量。
2.  **MySQL 持久化**:
    *   `article_viewcount` 表存储最终的浏览量数据。
    *   `ArticleViewCountMapper` 提供数据库操作。
3.  **服务层 (`ArticleViewCountService`)**:
    *   封装浏览量获取 (`getViewCount`) 和增加 (`incrementViewCount`) 逻辑。
    *   提供数据同步方法 (`syncViewCountsToDatabase`)。
4.  **定时调度 (`ArticleViewCountSyncScheduler`)**:
    *   使用 Spring `@Scheduled` 注解配置定时任务。
    *   分离调度逻辑，提高模块化。
5.  **业务集成 (`ArticleServiceImpl`)**:
    *   在 `getArticleById` 方法中调用 `incrementViewCount`。
    *   在 `convertToVO` 和 `convertToSummaryVO` 中调用 `getViewCount` 获取浏览量。

### 代码位置

-   **调度器**: `service/src/main/java/org/jeffrey/service/scheduler/ArticleViewCountSyncScheduler.java`
-   **服务**: `service/src/main/java/org/jeffrey/service/article/service/impl/ArticleViewCountServiceImpl.java`
-   **实体**: `service/src/main/java/org/jeffrey/service/article/repository/entity/ArticleViewCountDO.java`
-   **Mapper**: `service/src/main/java/org/jeffrey/service/article/repository/mapper/ArticleViewCountMapper.java`
-   **数据库脚本**: `web/src/main/resources/db/init.sql`
-   **Redis 客户端**: `core/src/main/java/org/jeffrey/core/cache/RedisClient.java`

### 后续计划

-   引入布隆过滤器等机制防止恶意刷量。
-   实现热门文章排行功能。




