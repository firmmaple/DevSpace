# 功能变更记录

## GitHub OAuth2 集成 (2024-09-08)

### 概述

DevSpace 项目集成了 GitHub OAuth2 认证功能，允许用户通过 GitHub 账号登录系统，无需单独注册。此功能使用 Spring Security OAuth2 Client 实现，并与现有的基于 JWT 的认证系统无缝集成。

### 技术实现

1. **依赖添加**
   - 添加了 `spring-boot-starter-oauth2-client` 依赖，提供 OAuth2 客户端支持

2. **配置更新**
   - 在 `application.yml` 中配置了 GitHub OAuth 参数，包括 client ID、client secret 和回调 URL
   - 更新了 `SecurityConfig` 以支持 OAuth2 登录
   - 扩展了 `SecurityMatchersConfig` 以允许访问 OAuth2 相关 URL

3. **核心组件**
   - 创建 `OAuth2LoginSuccessHandler` 处理 GitHub 认证成功后的逻辑：
     - 提取 GitHub 用户信息（用户名、ID、头像）
     - 验证或创建本地用户
     - 生成 JWT 令牌并设置为 HTTP-only cookie
     - 设置 `user_info` cookie 以保持与传统登录流程一致
     - 重定向到首页

4. **用户服务扩展**
   - 在 `UserService` 中添加 `processOAuth2User` 方法，处理 OAuth2 用户登录
   - 实现了用户信息同步（如电子邮件和头像更新）

5. **前端集成**
   - 在登录页面添加"使用 GitHub 账号登录"按钮，链接到 OAuth2 授权流程

### 用户体验流程

1. 用户点击登录页面上的"使用 GitHub 账号登录"按钮
2. 系统重定向到 GitHub 授权页面
3. 用户授权 DevSpace 应用访问其 GitHub 账号信息
4. GitHub 重定向回应用的回调 URL (`/login/oauth2/code/github`)
5. `OAuth2LoginSuccessHandler` 处理用户信息并执行 JWT 认证
6. 用户被重定向到首页，已完成登录

### 代码变更

1. 添加依赖：
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-oauth2-client</artifactId>
   </dependency>
   ```

2. OAuth2 配置：
   ```yaml
   spring:
     security:
       oauth2:
         client:
           registration:
             github:
               client-id: Ov23liGWGv4l57FpNEAP
               client-secret: 18acc79b882a9297729f70c2522be790484cae5b
               redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
               scope:
                 - user:email
                 - read:user
   ```

3. 实现 OAuth2 成功处理器和用户服务扩展：
   - `OAuth2LoginSuccessHandler.java`
   - `UserService.processOAuth2User()` 方法

4. 前端添加 GitHub 登录按钮：
   ```html
   <div class="mt-3">
       <a href="/oauth2/authorization/github" class="btn btn-dark w-100 d-flex justify-content-center align-items-center">
           <i class="fab fa-github me-2"></i>
           <span>使用 GitHub 账号登录</span>
       </a>
   </div>
   ```

### 问题修复

1. **2024-09-08 修复**: 完善了OAuth2登录流程，解决了使用GitHub登录后未正确设置`user_info` cookie的问题：
   - 问题描述：传统登录流程通过返回JSON响应设置`user_info` cookie，而OAuth2登录直接重定向，导致前端无法识别登录状态
   - 解决方案：在 `OAuth2LoginSuccessHandler` 中直接设置非HTTP-only的`user_info` cookie，保持与传统登录流程的一致性
   - 代码实现：
     ```java
     // 设置user_info cookie
     UserDTO userDTO = userDetails.toUserDTO();
     String userInfoJson = URLEncoder.encode(objectMapper.writeValueAsString(userDTO), StandardCharsets.UTF_8);
     Cookie userInfoCookie = new Cookie("user_info", userInfoJson);
     userInfoCookie.setHttpOnly(false); // 允许JavaScript访问
     userInfoCookie.setPath("/");
     userInfoCookie.setMaxAge(86400); // 1天
     response.addCookie(userInfoCookie);
     ```

### 注意事项

1. GitHub OAuth 应用配置中需确保回调 URL 正确设置为：`http://localhost:8088/login/oauth2/code/github`
2. 生产环境部署时应更新 client ID 和 client secret，并确保使用 HTTPS
3. `OAuth2LoginSuccessHandler` 确保与现有的 JWT cookie 认证机制兼容
