# 1. 项目总览

## 项目介绍

DevSpace是一个基于SpringBoot的博客社区，面向互联网开发者的技术内容分享欲交流平台 ，拥有完整的文章发布，评论，点赞，收藏，搜索，统计等功能，
前端使用Thymeleaf(后期可能会使用React实现) ，后端使用SpringBoot+MyBatis-Plus+MySQL+Redis+RabbitMQ。

主要目标为通过这个项目学习SpringBoot的使用，写入CV作为个人项目。

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

- Spring Boot 3.44
- Thymeleaf + Bootstrap 5.3 (用于前端页面渲染)
- Spring Security 6.0 (计划使用 JWT以及身份认证和授权)
- MyBatis, MyBatis-Plus(计划使用)
- Redis(计划使用，用于存储在线用户以及计数统计)
- RabbitMQ(计划使用，用于评论、点赞、收藏等异步处理)
- HandlerExceptionResolver(计划使用，用于全局异常处理)
- AOP + TraceID(已实现，用于日志记录，实现任务追踪、监控和诊断)

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

# 功能模块说明

## 用户认证与注册

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
    - 认证失败处理
    - 安全路由保护

### 技术实现

1. **前端实现**:
    - Thymeleaf 模板引擎渲染的注册和登录页面
    - Bootstrap 5.3 提供的样式和布局
    - 客户端 JavaScript 验证
    - Fetch API 进行 AJAX 请求

2. **后端实现**:
    - `AuthController` 处理认证和注册请求
    - `UserService` 接口定义和 `UserServiceImpl` 实现
    - Spring Security 配置在 `SecurityConfig` 中设置
    - 使用 `PasswordEncoder` 进行密码加密
    - JWT 认证过滤器 `JWTAuthenticationFilter`

3. **数据模型**:
    - `UserDO` 实体类映射到数据库
    - `RegisterDTO` 用于注册请求数据传输
    - `ResVo` 通用响应对象封装 API 响应

### 代码位置

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

## 前端布局系统

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

# 前后端数据传输

DevSpace采用统一的数据传输模型，确保API响应格式一致性和可预测性，便于前端处理各种请求结果。

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

## 使用示例

### 成功响应

```java
// 返回带数据的成功响应
User user = userService.getUserById(userId);
return ResVo.ok(user);

// 返回无数据的成功响应(仅操作状态)
userService.updatePassword(userId, newPassword);
return ResVo.ok();
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

### 前端处理

```javascript
// 发送请求并处理统一响应
fetch('/api/user/profile')
  .then(response => response.json())
  .then(data => {
    if (data.status.code === 0) {
      // 处理成功响应
      renderUserProfile(data.result);
    } else {
      // 处理错误
      showError(data.status.msg);
    }
  });
```

## 错误码示例

| 错误码 | 描述 | 场景 |
|--------|------|------|
| 0 | 成功 | 操作成功完成 |
| 100_400_001 | 参数异常 | 请求参数不符合要求 |
| 100_403_003 | 未登录 | 未授权访问需要登录的资源 |
| 200_404_001 | 文章不存在 | 请求不存在的文章资源 |
| 400_403_002 | 用户名or密码错误 | 登录验证失败 |
| 400_405_002 | 用户已存在 | 注册时用户名已被占用 |

# AOP+TraceID日志追踪系统

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
