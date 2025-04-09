# 1. 项目总览

## 项目介绍

DevSpace是一个基于SpringBoot的博客社区，面向互联网开发者的技术内容分享欲交流平台 ，拥有完整的文章发布，评论，点赞，收藏，搜索，统计等功能，
前端使用Thymeleaf(后期可能会使用React实现) ，后端使用SpringBoot+MyBatis-Plus+MySQL+Redis+RabbitMQ。

主要目标为通过这个项目学习SpringBoot的使用，写入CV作为个人项目。

## 项目结构

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