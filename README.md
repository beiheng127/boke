架构概览

- 前后端分离：后端为 Spring Boot 3（Java 17）REST API，前端为 Vue 3 + Vite 的单页应用
- 目录布局：后端根目录下包含 frontend 子项目；运行时前端通过 Vite 代理与后端交互
- 通信方式：Axios 携带 JWT Bearer Token 调用 /api/* 接口；跨域通过后端 CORS 与 Vite 代理共同处理
后端技术栈

- 框架与依赖：Spring Boot Web、Spring Data JPA、Validation、Spring Security、Mail、Redis、JJWT
  - 依赖清单与 Java 版本见 pom.xml
- 数据库：MySQL（默认），支持切换 H2（注释配置用于开发/测试）
  - 数据源与 JPA 配置见 application.properties
- 鉴权与权限：
  - JWT 发放与校验：见 TokenService.java
  - 请求过滤链：自定义 JWT 过滤器 AuthFilter.java 注入 SecurityContext
  - 访问控制：基于角色 BLOGGER/VIEWER 的路径授权，见 SecurityConfig.java
  - CORS：允许域名通过 app.cors.allowed-origins 配置，见 SecurityConfig.java 与 application.properties
- 缓存与会话：
  - Redis 缓存序列化与 CacheManager 定义见 RedisConfig.java
  - 通用封装工具见 RedisUtil.java
  - 用户会话与信息缓存位于 Auth 流程中，见 AuthController.java
- 文件上传与静态资源：
  - 本地文件存储见 FileStorageService.java
  - 将 /uploads/** 映射到项目 uploads 目录，见 WebConfig.java
- 邮件与验证码：基于 Spring Mail 发送注册/重置密码验证码（敏感配置请用环境变量），流程见 AuthController.java
- 管理员初始化：应用启动自动创建博主账号，见 AdminInitializer.java
后端领域模型与核心模块

- 核心实体：User、Article、Comment、Favorite、ArticleLike、Follow、Message、Conversation、VerificationCode（位于 domain/）
  - 示例：文章模型与作者关联、发布状态、浏览量见 Article.java
- 业务服务：
  - 文章：检索/热度评分/发布-草稿切换/浏览量，见 ArticleService.java
  - 消息：私信与会话，见 MessageService.java 与 MessageController.java
  - 用户：注册、登录、密码重置、资料
- API 分层：
  - Auth：/api/auth 登录、注册、验证码、重置、会话校验，见 AuthController.java
  - 文章：/api/articles 列表、详情、创建、更新、删除、上传、统计、草稿、发布切换，见 ArticleController.java
  - 互动：点赞、收藏、评论（对应 repository 与 controller）
  - 社交：关注与粉丝
  - 消息：/api/messages 文本/文件消息、会话、未读计数、已读标记
前端技术栈

- 框架与构建：Vue 3、Vite、Pinia、Vue Router、Axios
  - 依赖见 package.json
- 富文本/Markdown：@wangeditor-next、marked + dompurify
- UI：Element Plus（在组件/视图中使用）
- 状态与路由：
  - 认证态与会话持久化见 auth.js
  - 路由守卫与权限控制（requiresAuth、requiresBlogger）见 router/index.js
- 网络层：
  - Axios 实例与拦截器，公开 API 白名单与 Bearer 注入见 api/http.js
  - Vite 代理将 /api 与 /uploads 转发到后端 8080，见 vite.config.js
- 页面与组件：
  - 视图：Home、ArticleDetail、Editor、Drafts、Favorites、Profile、Search、AdminPage、FollowPage、Messages、UserHome 等（见 frontend/src/views）
  - 组件：Navbar、ArticleCard、CommentList、SliderCaptcha、MessageChat、FollowButton 等（见 frontend/src/components）
  - 全局装配见 App.vue
请求流简述

- 登录：/api/auth/login 返回 JWT 与用户信息 → 前端保存到 Pinia + localStorage → Axios 拦截器为受保护接口加 Authorization 头
- 授权：Spring Security 放行公开端点，其他受 JWT 保护；角色 BLOGGER 可进行文章管理等操作
- 缓存：文章详情与列表使用 Redis 分别按键 “article:{id}”、“article_list:*” 缓存；登录后用户信息与会话缓存到 Redis
运行与开发（Windows）

- 后端
  - 启动（使用 Maven Wrapper）：
    
    ```
    .\mvnw.cmd spring-boot:run
    ```
  - 或打包运行：
    
    ```
    .\mvnw.cmd clean package -DskipTests
    java -jar target\boke-0.0.1-SNAPSHOT.jar
    ```
  - 准备：MySQL 创建库 boke，配置 application.properties 中的数据库地址与账号；配置 Redis；将 JWT 秘钥与邮箱密码改为环境变量或外部化配置（避免明文）
- 前端
  - 开发模式：
    
    ```
    npm install
    npm run dev
    ```
  - 默认端口 5173，代理 /api 与 /uploads 到 http://localhost:8080
安全与实践要点

- 将 JWT secret、邮箱账号与密码迁移到环境变量或独立配置文件，避免提交明文
- 生产环境请细化 CORS 允许来源，仅开放必要域名
- 上传目录 /uploads 建议配合反向代理与访问控制，保护私有文件路径
