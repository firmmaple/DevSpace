# 1. 项目总览

## 项目介绍

DevSpace是一个基于SpringBoot的博客社区，面向互联网开发者的技术内容分享欲交流平台 ，拥有完整的文章发布，评论，点赞，收藏，搜索，统计等功能，
前端使用Thymeleaf(后期可能会使用React实现) ，后端使用SpringBoot+MyBatis-Plus+MySQL+Redis+RabbitMQ。

主要目标为通过这个项目学习SpringBoot的使用，写入CV作为个人项目。

[项目文档](.DOCUMENT.md) 中描述了项目的详细设计和相关规定，当AI生成代码需要参考。

## 项目模块

```
DevSpace
├── api -- 定义一些通用的枚举、实体类，定义 DO\DTO\VO 等
├── core -- 核心工具/组件相关模块，如工具包 util， 通用的组件都放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）
├── service -- 服务模块，业务相关的主要逻辑，DB 的操作都在这里
├── ui -- HTML 前端资源（包括 JavaScript、CSS、Thymeleaf 等）
├── web -- Web模块、HTTP入口、项目启动入口，包括权限身份校验、全局异常处理等
```

## 所用技术栈

- Spring Boot 3.x
- Thymeleaf + Bootstrap 5.x (用于前端页面渲染)
- Spring Security 6.x (计划使用 JWT以及身份认证和授权)
- MyBatis, MyBatis-Plus (计划使用)
- MySQL
- Redis (计划使用，用于存储在线用户以及计数统计)
- RabbitMQ (计划使用，用于评论、点赞、收藏等异步处理)
- HandlerExceptionResolver (计划使用，用于全局异常处理)
- AOP + TraceID (已实现，用于日志记录，实现任务追踪、监控和诊断)

## 项目结构

```
.
├── pom.xml                          # 父项目的 Maven 配置
├── api/
│   ├── pom.xml
│   └── src/main/java/org/jeffrey   # 公共接口或 DTO 定义位置（示例）
├── core/
│   ├── pom.xml
│   └── src/main/java/org/jeffrey/core/security/SecurityConfig.java
├── service/
│   ├── pom.xml
│   └── src/main/java/org/jeffrey/service/
│       ├── LoginService.java
│       ├── ServiceAutoConfig.java
│       └── user/
│           ├── repository/entity/UserDO.java
│           ├── repository/mapper/UserMapper.java
│           └── service/
│               ├── UserService.java
│               └── impl/UserServiceImpl.java
├── ui/
│   ├── pom.xml
│   └── src/main/resources/
│       ├── templates/index.html         # Thymeleaf 模板
│       └── static/                      # 静态资源（如 CSS, 图片等）
│           └── images/icon.png
├── web/
│   ├── pom.xml
│   ├── src/main/java/org/jeffrey/web/
│   │   ├── DevSpaceApplication.java     # 应用启动类
│   │   ├── TestController.java          # 示例控制器
│   │   └── home/HomeController.java     # 带 @PreAuthorize 的页面控制器
│   └── src/main/resources/
│       ├── application.yml              # 核心配置文件
│       └── logback-spring.xml           # 日志配置
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
   - 文章互动功能（点赞、收藏）
   - 相关文章推荐
   - 标签展示和导航

3. **文章创建与编辑**
   - 富文本编辑器（集成Quill编辑器）
   - 文章标题、摘要、内容和标签管理
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
   - Quill富文本编辑器处理文章内容

2. **后端实现**:
   - `ArticleController` 处理页面路由
   - `ArticleRestController` 提供RESTful API
   - `ArticleService` 处理业务逻辑
   - JWT验证用户身份和权限

3. **数据模型**:
   - `ArticleVO` 视图对象展示文章详情
   - `ArticleSummaryVO` 视图对象展示文章摘要
   - `ArticleCreateDTO` 数据传输对象处理文章创建
   - `ArticleUpdateDTO` 数据传输对象处理文章更新

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

- 实现文章评论功能
- 添加文章标签管理系统
- 实现文章分类功能
- 添加文章搜索（全文检索）
- 实现文章统计分析

## 3.3 用户认证与注册

DevSpace 实现了完整的用户认证和注册功能，使用 Spring Security 和 JWT 令牌进行安全认证。

### 已实现功能

1. **用户注册**
    - 用户注册表单页面 (`/register`)
    - 客户端表单验证（用户名、密码格式检查）
    - 服务端验证和用户创建
    - 密码加密存储
    - 重复用户名检查

2. **用户认证**
    - 用户登录表单 (`/login`)
    - JWT 令牌生成与验证
    - 认证失败处理 (`GlobalExceptionHandler` 和 `CustomAuthenticationEntryPoint`)
    - 安全路由保护 (`SecurityConfig`)
    - 页面访问未登录重定向: 直接访问需要登录的页面时，`CustomAuthenticationEntryPoint` 会自动重定向到 `/login` 并附带原始URL作为 `redirect` 参数。
    - API访问未登录处理: 请求需要认证的 API 时，如果未提供有效 Token，`CustomAuthenticationEntryPoint` 会委托 `GlobalExceptionHandler` 返回 `StatusEnum.FORBID_NOTLOGIN` (100_403_003) 的 JSON 响应。

### 技术实现

1. **前端实现**:
    - Thymeleaf 模板引擎渲染的注册和登录页面
    - Bootstrap 5.3 提供的样式和布局
    - 客户端 JavaScript 验证
    - **强制使用 `AuthUtils.authenticatedFetch`**: 替代原生 `fetch` 发送所有需要认证的 AJAX 请求。

2. **后端实现**:
    - `AuthController` 处理认证和注册请求
    - `UserService` 接口定义和 `UserServiceImpl` 实现
    - Spring Security 配置在 `SecurityConfig` 中设置
    - 使用 `PasswordEncoder` 进行密码加密
    - JWT 认证过滤器 `JWTAuthenticationFilter`
    - **认证入口点 `CustomAuthenticationEntryPoint`**: 区分页面请求（重定向到登录页）和 API 请求（返回 JSON 错误）。
    - **全局异常处理器 `GlobalExceptionHandler`**: 统一处理包括认证/授权在内的各种 API 异常。

3. **数据模型**:
    - `UserDO` 实体类映射到数据库
    - `RegisterDTO` 用于注册请求数据传输
    - `ResVo` 通用响应对象封装 API 响应

### 前端请求最佳实践

为确保API请求的安全性和一致性，DevSpace遵循以下前端请求规范：

1.  **强制使用 `AuthUtils.authenticatedFetch`**: 所有需要认证的API请求**必须**使用`AuthUtils.authenticatedFetch(url, options)`而非原生`fetch`。此函数会自动处理：
    *   添加 `Authorization: Bearer <token>` 请求头。
    *   调用 `fetch` 并解析响应为 JSON (`.then(res => res.json())`)。
    *   调用 `AuthUtils.handleApiResponse(jsonData)` 进行初步处理：
        *   如果响应状态码为 `100_403_003` (未登录), 它会自动保存当前 URL 到 `localStorage` 并重定向到 `/login` 页面，然后 `reject` Promise。
        *   如果响应状态码为其他错误码, 它会 `reject` Promise 并附带错误信息。
        *   如果响应成功 (状态码为 0), 它会返回**完整的原始 JSON 响应对象** (包含 `status` 和 `result` 属性)。

2.  **处理 `authenticatedFetch` 的结果**: 由于 `authenticatedFetch` 内部已经解析了 JSON 并处理了通用错误 (如未登录重定向)，你的 `.then()` 回调函数将直接收到**完整的 JSON 响应对象**。你需要从中提取 `result` 部分来获取业务数据。**不要**再次调用 `.json()`。

    ```javascript
    // 正确使用 authenticatedFetch
    AuthUtils.authenticatedFetch('/api/user/profile')
      .then(response => {
        // 'response' 是完整的 JSON 对象, 例如 { status: { code: 0, msg: 'OK' }, result: { username: '...', ... } }
        // 不需要再调用 response.json()
        if (response.status.code === 0) {
            const userData = response.result;
            console.log(userData.username);
            renderUserProfile(userData);
        } else {
            // 理论上非 0 code 应该在 handleApiResponse 中被 reject，但可以加一层防护
            console.error("获取用户信息失败:", response.status.msg);
            showError(response.status.msg);
        }
      })
      .catch(error => {
        // 处理特定于此调用的错误，或显示通用错误信息
        // 注意：未登录错误已经被 handleApiResponse 处理并重定向，一般不会在这里捕获到
        console.error("获取用户信息失败:", error.message);
        showError(error.message);
      });
    ```

3.  **统一错误处理**: 在 `.catch()` 块中处理特定于该 API 调用的错误。通用错误（如未登录）已由 `authenticatedFetch` 内部处理。

- **UI 模板**:
    - `ui/src/main/resources/templates/register.html` - 注册表单
    - `ui/src/main/resources/templates/login.html` - 登录表单

- **后端实现**:
    - `web/src/main/java/org/jeffrey/web/login/AuthController.java` - 控制器
    - `service/src/main/java/org/jeffrey/service/user/service/UserService.java` - 服务接口
    - `service/src/main/java/org/jeffrey/service/user/service/impl/UserServiceImpl.java` - 服务实现
    - `core/src/main/java/org/jeffrey/core/security/SecurityConfig.java` - 安全配置

- **DTO**:
    - `api/src/main/java/org/jeffrey/api/dto/RegisterDTO.java` - 注册数据传输对象

### 下一步计划

- 添加用户个人资料功能
- 添加邮箱验证
- 实现密码重置功能
- 增强密码策略
- 添加第三方登录（如 GitHub, Google）

## 3.4 前端布局系统

DevSpace 使用 Thymeleaf 模板引擎构建了一个模块化的前端布局系统，使页面结构统一、代码复用性高。

### 布局文件结构

```
ui/src/main/resources/templates/
├── layout/
│   ├── main.html    # 主布局文件，所有页面的基础模板
│   └── header.html  # 页面头部布局，包含导航栏和用户菜单
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

### 认证状态切换

用户界面会根据认证状态动态变化：

1. **已登录状态**
    - 显示用户头像和用户名
    - 显示用户下拉菜单（包含发布文章、我的文章等选项）
    - 隐藏登录/注册按钮

2. **未登录状态**
    - 显示登录/注册按钮
    - 隐藏用户菜单

实现方式：

- 使用 CSS 类（force-show/force-hide）控制元素显示/隐藏
- 通过 JavaScript（header.js）检测用户认证状态
- 根据localStorage中的JWT令牌判断登录状态

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

## 3.5 全局异常处理系统

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




