# 1. 项目总览

## 项目介绍

DevSpace是一个基于SpringBoot的博客社区，面向互联网开发者的技术内容分享欲交流平台 ，拥有完整的文章发布，评论，点赞，收藏，搜索，统计等功能，
前端使用Thymeleaf(后期可能会使用React实现) ，后端使用SpringBoot+MyBatis-Plus+MySQL+Redis+RabbitMQ。

主要目标为通过这个项目学习SpringBoot的使用，写入CV作为个人项目。

## 项目模块

DevSpace
├── api -- 定义一些通用的枚举、实体类，定义 DO\DTO\VO 等
├── core -- 核心工具/组件相关模块，如工具包 util， 通用的组件都放在这个模块（以包路径对模块功能进行拆分，如搜索、缓存、推荐等）
├── service -- 服务模块，业务相关的主要逻辑，DB 的操作都在这里
├── ui -- HTML 前端资源（包括 JavaScript、CSS、Thymeleaf 等）
├── web -- Web模块、HTTP入口、项目启动入口，包括权限身份校验、全局异常处理等

## 所用技术栈

- Spring Boot 3.44
- Thymeleaf + Bootstrap 5.3 (用于前端页面渲染)
- Spring Security 6.0 (计划使用 JWT以及身份认证和授权)
- MyBatis, MyBatis-Plus(计划使用)
- Redis(计划使用，用于存储在线用户以及计数统计)
- RabbitMQ(计划使用，用于评论、点赞、收藏等异步处理)
- HandlerExceptionResolver(计划使用，用于全局异常处理)
- AOP + TraceID(计划使用，用于日志记录，实现任务追踪)

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

使用 ResVo 进行前后端数据传输，ResVo 是一个通用的响应对象，包含了状态码、消息和数据等字段。它可以用于统一处理 API 的响应格式。

```java
public class ResVo<T> {
    public class ResVo<T> implements Serializable {
        private Status status;

        private T result;
    }
}
```

Status 类用于表示响应的状态码和消息。

```java
public class Status {
    @Schema(description = "状态码, 0表示成功返回，其他异常返回", required = true, example = "0")
    private int code;

    @Schema(description = "正确返回时为ok，异常时为描述文案", required = true, example = "ok")
    private String msg;

    public static Status newStatus(int code, String msg) {
        return new Status(code, msg);
    }
}
```

下面为Status code的定义，其中code 0表示成功，其他值表示异常。

```
异常码规范：
xxx - xxx - xxx
业务 - 状态 - code
  <p>
  业务取值
  - 100 全局
  - 200 文章相关
  - 300 评论相关
  - 400 用户相关
  <p>
  状态：基于http status的含义
  - 4xx 调用方使用姿势问题
  - 5xx 服务内部问题
  <p>
  code: 具体的业务code
  
示例：
SUCCESS(0, "OK"),
ILLEGAL_ARGUMENTS(100_400_001, "参数异常"),
```
