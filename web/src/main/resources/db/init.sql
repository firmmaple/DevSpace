-- 创建用户表
CREATE TABLE `user` (
                        `id` INT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(64) NOT NULL COMMENT '用户名',
                        `password` VARCHAR(128) NOT NULL COMMENT '密码',
                        `is_admin` TINYINT(1) DEFAULT '0' COMMENT '是否为管理员',
                        `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '用户头像URL',
                        `email` VARCHAR(255) DEFAULT NULL COMMENT '用户邮箱',
                        `bio` TEXT DEFAULT NULL COMMENT '个人简介',
                        `join_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


INSERT INTO `user` (`id`, `username`, `password`, `is_admin`) VALUES (1, 'admin', '123', 1);
INSERT INTO `user` (`id`, `username`, `password`, `avatar_url`, `bio`, `is_admin`) VALUES (2, 'meat', '123456', 'http://localhost:8088/api/file/avatars/White-Maltese.jpg', 'I am a meat', 1);
INSERT INTO `user` (`id`, `username`, `password`, `avatar_url`, `bio`, `is_admin`) VALUES (3, 'jeffrey', '123', 'http://localhost:8088/api/file/avatars/Golden-Maltese.jpg', '喜欢捡垃圾！', 1);
INSERT INTO `user` (`id`, `username`, `password`, `is_admin`) VALUES (4, 'user', '123', 0);

CREATE TABLE `article` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文章主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
  `summary` VARCHAR(512) DEFAULT NULL COMMENT '文章摘要',
  `content` LONGTEXT NOT NULL COMMENT '文章内容 (Markdown or HTML)',
  `image_url` VARCHAR(512) DEFAULT NULL COMMENT '文章封面图片URL',
  `author_id` BIGINT NOT NULL COMMENT '作者用户ID',
  `status` TINYINT DEFAULT 0 COMMENT '文章状态 (0:草稿, 1:已发布, 2:已删除)',
  `is_hot` TINYINT(1) DEFAULT 0 COMMENT '是否为热门文章 (0:否, 1:是)',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_author_id` (`author_id`),
  INDEX `idx_status_created` (`status`, `created_at`), -- For fetching published articles sorted by time
  INDEX `idx_is_hot` (`is_hot`, `created_at`) -- For fetching hot articles
  -- Maybe add FULLTEXT index later for basic search: FULLTEXT KEY `idx_title_content` (`title`,`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 插入测试文章数据
INSERT INTO `article` (`title`, `summary`, `content`, `author_id`, `status`, `created_at`) VALUES 
('Spring Boot 3.0 新特性解析', 
 'Spring Boot 3.0带来了哪些重大变化？本文详细解析其新特性及升级指南。', 
 '# Spring Boot 3.0 新特性解析\n\n## 1. 概述\n\nSpring Boot 3.0是一个重大版本更新，带来了许多激动人心的变化。本文将详细介绍这些新特性。\n\n## 2. Java 17基础支持\n\nSpring Boot 3.0要求Java 17作为最低版本，充分利用了Java新特性如记录类、密封类等。\n\n## 3. 迁移到Jakarta EE\n\n所有API包名从javax.*迁移到jakarta.*，这是一个破坏性变更。\n\n## 4. 原生镜像支持\n\n通过GraalVM，Spring Boot 3.0提供了更好的原生镜像支持，大幅提升启动时间和减少内存占用。\n\n## 5. 性能优化\n\n框架核心组件经过重写，提供更好的性能表现。\n\n## 6. 总结\n\nSpring Boot 3.0是一次重大升级，虽然迁移需要一定工作量，但带来的性能提升和新特性值得投入。',
 2, 1, '2023-07-15 10:30:00'),
 
('深入理解Java虚拟机', 
 'JVM是Java平台的核心，本文深入剖析JVM内存模型、垃圾回收机制及性能调优技巧。', 
 '# 深入理解Java虚拟机\n\n## 1. JVM内存结构\n\nJava虚拟机在执行Java程序时会把它管理的内存划分为若干个不同的数据区域：\n\n- 堆区（Heap）\n- 方法区（Method Area）\n- 虚拟机栈（VM Stack）\n- 本地方法栈（Native Method Stack）\n- 程序计数器（Program Counter Register）\n\n## 2. 垃圾回收机制\n\nJVM的GC主要有以下几种算法：\n\n- 标记-清除算法\n- 复制算法\n- 标记-整理算法\n- 分代收集算法\n\n## 3. JVM性能调优\n\n性能调优主要从以下几个方面入手：\n\n- 内存分配\n- GC策略选择\n- JIT编译器优化\n- 线程优化\n\n## 4. 总结\n\n深入理解JVM对于解决Java应用的性能问题至关重要。',
 3, 1, '2023-07-20 14:45:00'),
 
('微服务架构实践指南', 
 '从单体应用到微服务架构的演进，本文分享微服务设计、实现和运维的最佳实践。', 
 '# 微服务架构实践指南\n\n## 1. 微服务简介\n\n微服务是一种将应用程序构建为一系列小型服务的架构风格，每个服务运行在自己的进程中，通过轻量级机制通信。\n\n## 2. 服务拆分原则\n\n- 业务功能边界\n- 数据自治\n- 团队组织结构\n- 技术异构性\n\n## 3. 服务通信\n\n- 同步通信：REST、gRPC\n- 异步通信：消息队列\n- API网关\n\n## 4. 服务治理\n\n- 服务注册与发现\n- 负载均衡\n- 熔断与降级\n- 配置中心\n\n## 5. 微服务监控\n\n- 分布式追踪\n- 日志聚合\n- 指标监控\n- 告警系统\n\n## 6. 总结\n\n微服务架构带来了很多好处，但也增加了系统复杂性，需要谨慎选择和实施。',
 4, 1, '2023-08-05 09:15:00'),
 
('Docker容器化应用实战', 
 '容器化正在改变应用的开发和部署方式，本文介绍Docker的核心概念和最佳实践。', 
 '# Docker容器化应用实战\n\n## 1. Docker基础\n\nDocker是一个开源的应用容器引擎，让开发者可以打包他们的应用以及依赖包到一个可移植的容器中。\n\n## 2. Dockerfile最佳实践\n\n```dockerfile\nFROM openjdk:17-jdk-slim\nWORKDIR /app\nCOPY target/*.jar app.jar\nENTRYPOINT ["java","-jar","/app/app.jar"]\n```\n\n## 3. Docker Compose\n\n使用Docker Compose编排多容器应用：\n\n```yaml\nversion: "3"\nservices:\n  app:\n    build: .\n    ports:\n      - "8080:8080"\n    depends_on:\n      - db\n  db:\n    image: mysql:8.0\n    environment:\n      MYSQL_ROOT_PASSWORD: root\n      MYSQL_DATABASE: testdb\n```\n\n## 4. 容器编排与管理\n\n在生产环境中，通常使用Kubernetes等工具进行容器编排。\n\n## 5. 总结\n\nDocker极大地简化了应用的打包、分发和部署过程，是现代DevOps不可或缺的工具。',
 2, 1, '2023-08-10 16:20:00'),
 
('React Hooks深度剖析', 
 'React Hooks改变了函数组件的能力边界，本文深入分析Hooks的工作原理和使用技巧。', 
 '# React Hooks深度剖析\n\n## 1. Hooks简介\n\nReact Hooks是React 16.8引入的新特性，它可以让你在不编写class的情况下使用state以及其他的React特性。\n\n## 2. 常用Hooks\n\n### useState\n\n```jsx\nconst [count, setCount] = useState(0);\n```\n\n### useEffect\n\n```jsx\nuseEffect(() => {\n  document.title = `You clicked ${count} times`;\n  return () => {\n    // cleanup\n  };\n}, [count]);\n```\n\n### useContext\n\n```jsx\nconst value = useContext(MyContext);\n```\n\n## 3. 自定义Hooks\n\n```jsx\nfunction useWindowSize() {\n  const [size, setSize] = useState({ width: 0, height: 0 });\n  \n  useEffect(() => {\n    const handleResize = () => {\n      setSize({ width: window.innerWidth, height: window.innerHeight });\n    };\n    \n    window.addEventListener("resize", handleResize);\n    handleResize();\n    \n    return () => window.removeEventListener("resize", handleResize);\n  }, []);\n  \n  return size;\n}\n```\n\n## 4. Hooks规则\n\n- 只在最顶层使用Hooks\n- 只在React函数中调用Hooks\n\n## 5. 总结\n\nHooks使React组件更加简洁、可复用，是现代React开发的核心。',
 3, 1, '2023-08-15 11:30:00'),
 
('MySQL性能优化实战', 
 '数据库性能直接影响应用体验，本文分享MySQL索引设计、查询优化和服务器调优的实用技巧。', 
 '# MySQL性能优化实战\n\n## 1. 索引优化\n\n### 1.1 索引基础\n\nMySQL主要使用B+树索引，了解其结构有助于优化。\n\n### 1.2 索引设计原则\n\n- 最左前缀匹配原则\n- 选择区分度高的列建索引\n- 控制索引数量\n- 覆盖索引优化\n\n## 2. 查询优化\n\n### 2.1 EXPLAIN分析\n\n```sql\nEXPLAIN SELECT * FROM users WHERE name = "John";\n```\n\n### 2.2 常见优化手段\n\n- 避免SELECT *\n- 使用合适的WHERE条件\n- 优化JOIN操作\n- 使用适当的分页方式\n\n## 3. 服务器参数调优\n\n- innodb_buffer_pool_size\n- innodb_log_file_size\n- max_connections\n- query_cache_size\n\n## 4. 总结\n\nMySQL优化是一个系统工程，需要从应用设计、SQL编写、索引设计和服务器配置多方面入手。',
 4, 1, '2023-08-20 13:45:00'),
 
('Git工作流最佳实践', 
 '高效的Git工作流可以提升团队协作效率，本文介绍几种流行的Git工作流模型及实践经验。', 
 '# Git工作流最佳实践\n\n## 1. 常见Git工作流\n\n### 1.1 Git Flow\n\n适合有计划发布周期的项目，包含以下分支：\n\n- master: 生产环境代码\n- develop: 开发环境代码\n- feature/*: 新功能分支\n- release/*: 发布准备分支\n- hotfix/*: 紧急修复分支\n\n### 1.2 GitHub Flow\n\n简化的工作流，适合持续部署：\n\n- main: 随时可部署的代码\n- feature/*: 新功能分支\n\n### 1.3 GitLab Flow\n\n结合了上述两种流的优点，增加了环境分支。\n\n## 2. 提交规范\n\n```\n<type>(<scope>): <subject>\n\n<body>\n\n<footer>\n```\n\n常见type：feat, fix, docs, style, refactor, test, chore\n\n## 3. 分支管理策略\n\n- 功能分支要小而专注\n- 定期从主分支同步更新\n- 使用Pull Request进行代码审查\n\n## 4. 总结\n\n选择合适的Git工作流并严格执行，可以显著提高团队协作效率。',
 2, 1, '2023-08-25 15:10:00'),
 
('Kubernetes入门到实践', 
 'Kubernetes已成为容器编排的事实标准，本文从基础概念到实际部署，全面介绍K8s的使用。', 
 '# Kubernetes入门到实践\n\n## 1. Kubernetes基础概念\n\n- Pod: 最小部署单元，包含一个或多个容器\n- Service: 为Pod提供稳定的网络访问方式\n- Deployment: 声明式更新Pod和ReplicaSet\n- ConfigMap/Secret: 配置管理\n- Namespace: 资源隔离\n\n## 2. 核心组件\n\n- kube-apiserver: API服务器，资源操作入口\n- etcd: 键值数据库，存储集群状态\n- kube-scheduler: 调度器，分配Pod到Node\n- kube-controller-manager: 控制器管理器\n- kubelet: 节点代理，管理容器\n- kube-proxy: 网络代理\n\n## 3. 部署应用示例\n\n```yaml\napiVersion: apps/v1\nkind: Deployment\nmetadata:\n  name: nginx-deployment\nspec:\n  replicas: 3\n  selector:\n    matchLabels:\n      app: nginx\n  template:\n    metadata:\n      labels:\n        app: nginx\n    spec:\n      containers:\n      - name: nginx\n        image: nginx:1.14.2\n        ports:\n        - containerPort: 80\n```\n\n## 4. Kubernetes生态\n\n- Helm: 包管理器\n- Istio: 服务网格\n- Prometheus: 监控系统\n- Fluentd: 日志收集\n\n## 5. 总结\n\nKubernetes虽然有一定学习曲线，但其强大的容器编排能力使其成为云原生应用的基础设施。',
 3, 1, '2023-09-01 10:20:00'),
 
('RESTful API设计指南', 
 'API设计直接影响开发体验，本文总结RESTful API的设计原则和最佳实践。', 
 '# RESTful API设计指南\n\n## 1. REST基础原则\n\n- 资源导向\n- 使用HTTP方法表达语义\n- 无状态\n- 统一接口\n\n## 2. URL设计\n\n- 使用名词表示资源: `/users`而非`/getUsers`\n- 资源集合用复数: `/articles`而非`/article`\n- 层级关系用嵌套表示: `/users/123/posts`\n- 过滤、排序、分页通过查询参数: `/users?role=admin&sort=created_at`\n\n## 3. HTTP方法使用\n\n- GET: 获取资源\n- POST: 创建资源\n- PUT: 全量更新资源\n- PATCH: 部分更新资源\n- DELETE: 删除资源\n\n## 4. 状态码使用\n\n- 2xx: 成功\n  - 200 OK: 请求成功\n  - 201 Created: 资源创建成功\n  - 204 No Content: 请求成功但无返回内容\n- 4xx: 客户端错误\n  - 400 Bad Request: 请求参数错误\n  - 401 Unauthorized: 未认证\n  - 403 Forbidden: 无权限\n  - 404 Not Found: 资源不存在\n- 5xx: 服务器错误\n\n## 5. 版本控制\n\n```\n/api/v1/users\n```\n\n## 6. 总结\n\n良好设计的API可以提高开发效率，降低沟通成本，是系统成功的关键因素。',
 2, 1, '2023-09-05 14:30:00'),
 
('深入浅出设计模式', 
 '设计模式是解决软件设计中常见问题的可复用方案，本文介绍常用设计模式及其应用场景。', 
 '# 深入浅出设计模式\n\n## 1. 创建型模式\n\n### 1.1 单例模式\n\n```java\npublic class Singleton {\n    private static volatile Singleton instance;\n    \n    private Singleton() {}\n    \n    public static Singleton getInstance() {\n        if (instance == null) {\n            synchronized (Singleton.class) {\n                if (instance == null) {\n                    instance = new Singleton();\n                }\n            }\n        }\n        return instance;\n    }\n}\n```\n\n### 1.2 工厂方法模式\n\n### 1.3 抽象工厂模式\n\n### 1.4 建造者模式\n\n## 2. 结构型模式\n\n### 2.1 适配器模式\n\n### 2.2 装饰器模式\n\n### 2.3 代理模式\n\n## 3. 行为型模式\n\n### 3.1 观察者模式\n\n### 3.2 策略模式\n\n### 3.3 命令模式\n\n## 4. 设计模式的应用\n\n- Spring框架中的设计模式\n- Java标准库中的设计模式\n\n## 5. 总结\n\n设计模式不是银弹，应根据实际情况选择合适的模式，避免过度设计。',
 3, 1, '2023-09-10 16:45:00');

-- Article Like Table
CREATE TABLE IF NOT EXISTS `article_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`,`user_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章点赞表';

-- Article Collect Table
CREATE TABLE IF NOT EXISTS `article_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`,`user_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章收藏表';

-- Comment Table
CREATE TABLE IF NOT EXISTS `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，顶层评论为NULL',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';

-- Article View Count Table
CREATE TABLE IF NOT EXISTS `article_viewcount` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `view_count` bigint NOT NULL DEFAULT 0 COMMENT '浏览量',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_id` (`article_id`),
  KEY `idx_view_count` (`view_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文章浏览量表';