# 🎯 个人博客系统 - Boke Blog Platform

![License](https://img.shields.io/badge/License-MIT-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.5-green.svg)
![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Status](https://img.shields.io/badge/Status-Active-success.svg)

## 📋 项目简介

**Boke** 是一个现代化的全栈博客平台，基于 **Vue3 + Spring Boot** 构建。本项目实现了前后端分离架构，包含文章发布、用户认证、实时消息、互动评论等核心功能，展示微服务雏形设计。

### ✨ 核心特性

- 📝 **富文本编辑器** - 支持 Markdown 和 WYSIWYG 编辑
- 🔐 **JWT 认证系统** - 安全的用户登录和权限控制
- 💬 **实时私聊系统** - 用户间实时消息通信
- 🌟 **社交功能** - 关注、收藏、点赞、评论互动
- ⚡ **性能优化** - Redis 缓存、数据库索引优化
- 📱 **响应式设计** - 完美适配各设备屏幕
- 🎨 **现代 UI** - Element Plus 组件库支持

---

## 📖 目录

- [项目简介](#项目简介)
- [屏幕截图](#屏幕截图)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [核心功能模块](#核心功能模块)
- [快速开始](#快速开始)
- [安全建议](#安全建议)
- [开源协议](#开源协议)

---

## 🖼️ 屏幕截图

### 主页和导航
![主页面](https://github.com/user-attachments/assets/76a4d328-a740-4efb-90af-f69452a9e206)

### 文章编辑和管理
![文章编辑器功能](https://github.com/user-attachments/assets/65ff527e-0c67-4fec-91c3-b043227523fd)
![草稿箱功能](https://github.com/user-attachments/assets/bd028795-20ad-440c-9bb1-a512257bfdce)

### 用户交互功能
![私聊功能](https://github.com/user-attachments/assets/214040f3-9648-4c23-8ea4-fd48f6163ebe)
![个人资料页面](https://github.com/user-attachments/assets/e58c27fe-589a-4d5e-983f-19b8c6563bc5)
![关注列表](https://github.com/user-attachments/assets/90dd0dd4-f805-49cf-abe0-48a26a602ae4)

### 后台管理
![后台管理功能](https://github.com/user-attachments/assets/1a780252-fc80-4adf-ac98-bf61e750253e)
![我的收藏功能](https://github.com/user-attachments/assets/61f81760-008b-4008-9241-fb3678cb31b7)

### 用户界面
![个人主页页面](https://github.com/user-attachments/assets/6d5f0d11-a649-4947-ab75-74546f56260e)
![登录界面](https://github.com/user-attachments/assets/88a53f24-8cc2-4af7-949b-499ed11e93b4)
![一般用户登录](https://github.com/user-attachments/assets/28f0c638-66d2-4f74-ac0c-fa3e10b3d761)

---

## 🛠️ 技术栈

### 📊 语言分布

| 语言 | 占比 | 用途 |
|------|------|------|
| Vue | 62.2% | 前端框架 |
| Java | 33.2% | ��端框架 |
| JavaScript | 4.5% | 脚本和构建 |
| HTML | 0.1% | 模板标记 |

### 🎨 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.5+ | 渐进式 JavaScript 框架 |
| **Vite** | Latest | 极速 ESM 构建工具 |
| **Pinia** | Latest | 状态管理库 |
| **Vue Router** | Latest | 路由管理 |
| **Axios** | Latest | HTTP 客户端库（二次封装） |
| **Element Plus** | Latest | UI 组件库 |
| **@wangeditor-next** | Latest | 富文本编辑器 |
| **marked + dompurify** | Latest | Markdown 解析和 XSS 防护 |

**特性**: 请求拦截、响应拦截、公开 API 白名单、Bearer Token 注入、懒加载优化

### 🔧 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 3.5+ | 微服务框架 |
| **Java** | 17+ | 编程语言 |
| **Spring Data JPA** | Latest | ORM 框架 |
| **Spring Security** | Latest | 安全认证框架 |
| **MySQL** | 5.7+ | 关系型数据库（默认） |
| **Redis** | 6.0+ | 缓存和会话存储 |
| **JWT (JJWT)** | Latest | Token 认证 |
| **Spring Mail** | Latest | 邮件服务 |
| **Lombok** | Latest | 代码生成工具 |

**特性**: H2 数据库支持开发/测试、Maven Wrapper 项目管理

---

## 🏗️ 系统架构

### 架构设计图

```
┌─────────────────────────────────────────────────────────────┐
│                        浏览器 / 客户端                        │
└────────────┬────────────────────────────────────┬───────────┘
             │ HTTP/HTTPS                         │
             ▼                                     ▼
┌─────────────────────────────┐      ┌─────────────────────────┐
│   前端应用 (Vue 3 + Vite)    │      │  Nginx 反向代理          │
│  ┌──────────────────────────┤      │  - 负载均衡             │
│  │ 📄 Pages                  │      │  - 静态资源服务         │
│  │ - Home 首页               │      │  - SSL 终止             │
│  │ - Editor 编辑器           │      └─────────────┬──────────┘
│  │ - Profile 个人资料        │                    │
│  │ - Messages 私聊           │                    ▼
│  ├──────────────────────────┤      ┌──────────────────────────────┐
│  │ 🔧 核心模块               │      │   Spring Boot 后端           │
│  │ - Pinia 状态管理          │      │ ┌──────────────────────────┤
│  │ - Vue Router 路由         │      │ │ 🔐 Security Layer        │
│  │ - Axios 网络请求          │      │ │ - JWT 认证               │
│  │ - 请求拦截器             │      │ │ - 权限控制               │
│  └───────────────────────���──┘      │ │ - CORS 处理              │
│                                     │ ├──────────────────────────┤
│                                     │ │ 🎯 API 层 (REST)         │
│                                     │ │ - /api/auth              │
│  Vite Dev Server                   │ │ - /api/articles          │
│  Proxy: /api -> :8080              │ │ - /api/messages          │
│  Proxy: /uploads -> :8080          │ │ - /api/users             │
│                                     │ ├──────────────────────────┤
│                                     │ │ 📦 Service 层            │
│                                     │ │ - ArticleService         │
│                                     │ │ - AuthService            │
│                                     │ │ - MessageService         │
│                                     │ ├──────────────────────────┤
│                                     │ │ 💾 Data Access 层        │
│                                     │ │ - JPA Repository         │
│                                     │ │ - Hibernate ORM          │
│                                     │ └──────────────────────────┘
└─────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┼─────────────┐
                │             │             │
                ▼             ▼             ▼
        ┌───────────┐  ┌───────────┐  ┌─────────┐
        │ MySQL DB  │  │  Redis    │  │ 文件系统 │
        │ 关系数据库 │  │ 缓存&会话  │  │ /uploads│
        └───────────┘  └───────────┘  └─────────┘
```

### 核心交互流程

#### 前后端分离

- **后端**: Spring Boot 3（Java 17）提供 REST API
- **前端**: Vue 3 + Vite 单页应用
- **通信**: Axios 携带 JWT Bearer Token 调用 `/api/*` 接口

#### 目录布局

```
boke/
├── src/                          # 后端源代码
│   ├── main/java/.../
│   │   ├── config/               # 配置类
│   │   ├── controller/           # 控制层
│   │   ├── service/              # 业务逻辑层
│   │   ├── repository/           # 数据访问层
│   │   └── domain/               # 实体模型
│   └── main/resources/
│       └── application.properties # 应用配置
├── frontend/                      # 前端项目
│   ├── src/
│   │   ├── views/                # 页面组件
│   │   ├── components/           # 通用组件
│   │   ├── stores/               # Pinia 状态
│   │   ├── router/               # 路由配置
│   │   ├── api/                  # API 调用
│   │   └── App.vue               # 根组件
│   ├── vite.config.js            # Vite 配置
│   └── package.json              # 前端依赖
├── pom.xml                        # Maven 配置
└── README.md                      # 项目文档
```

#### 跨域处理

- **后端 CORS**: 通过 `app.cors.allowed-origins` 配置允许的域名
- **Vite 代理**: 开发时 `/api` 和 `/uploads` 代理到后端 8080 端口

---

## 🔐 核心功能模块

### 1️⃣ 认证与授权（Auth）

**端点**: `/api/auth`

| 功能 | 描述 |
|------|------|
| 登录 | 使用用户名密码登录，返回 JWT Token |
| 注册 | 新用户注册，发送验证码到邮箱 |
| 验证码 | 生成和验证邮件验证码 |
| 密码重置 | 通过邮箱验证码重置密码 |
| 会话校验 | 验证当前 Token 的有效性 |

**技术实现**:
- JWT 发放与校验 → `TokenService.java`
- 请求过滤链 → `AuthFilter.java` 注入 SecurityContext
- 访问控制 → 基于角色 **BLOGGER/VIEWER** 的路径授权（`SecurityConfig.java`）

### 2️⃣ 文章管理（Articles）

**端点**: `/api/articles`

| 功能 | 描述 |
|------|------|
| 列表查询 | 分页获取文章列表，支持搜索和排序 |
| 详情获取 | 获取单篇文章完整内容 |
| 创建文章 | 新建草稿或直接发布 |
| 编辑文章 | 修改已有文章内容 |
| 删除文章 | 删除指定文章 |
| 图片上传 | 上传文章内联图片 |
| 浏览统计 | 统计文章浏览量 |
| 草稿管理 | 草稿与发布状态切换 |

**核心实体**:
```
Article (文章)
├── id                  # 文章 ID
├── title               # 标题
├── content             # 内容
├── author              # 作者（关联 User）
├── viewCount           # 浏览量
├── publishedAt         # 发布时间
├── isDraft             # 草稿标记
└── createdAt           # 创建时间
```

**服务实现** → `ArticleService.java`

### 3️⃣ 互动功能���Interactions）

#### 点赞（Likes）
- 文章点赞
- 评论点赞
- 实体: `ArticleLike`

#### 收藏（Favorites）
- 收藏文章
- 实体: `Favorite`
- 获取方式: `/api/favorites`

#### 评论（Comments）
- 发表评论
- 嵌套回复
- 实体: `Comment`
- 获取方式: `/api/comments`

### 4️⃣ 社交功能（Social）

**关注与粉丝**:
- 关注用户 → `Follow` 实体
- 查看粉丝列表
- 查看关注列表
- API: `/api/users/{userId}/followers`, `/api/users/{userId}/following`

### 5️⃣ 实时消息（Messages）

**端点**: `/api/messages`

| 功能 | 描述 |
|------|------|
| 发送消息 | 支持文本和文件消息 |
| 会话管理 | 创建和查询消息会话 |
| 未读计数 | 获取未读消息数量 |
| 已读标记 | 标记消息为已读 |

**核心实体**:
```
Message (消息)
├── id
├── sender              # 发送者
├── receiver            # 接收者
├── content             # 消息内容
├── type                # 文本/文件
├── isRead              # 已读标记
└── createdAt

Conversation (会话)
├── id
├── participants        # 参与者列表
├── lastMessage         # 最后一条消息
└── updatedAt
```

### 6️⃣ 缓存与会话

**Redis 存储**:
- **文章缓存**: `article:{id}` 存储单篇文章
- **列表缓存**: `article_list:*` 存储文章列表
- **用户会话**: `user:{userId}` 存储用户信息
- **认证信息**: JWT 黑名单管理

**配置实现** → `RedisConfig.java` & `RedisUtil.java`

### 7️⃣ 文件管理

**本地存储** → `FileStorageService.java`
- 文件上传目录: `/uploads/**`
- 映射配置 → `WebConfig.java`
- 支持图片和文档上传

### 8️⃣ 邮件服务

**功能**:
- 注册验证码发送
- 密码重置链接发送
- 评论通知邮件

**配置**: Spring Mail，敏感信息通过环境变量注入

### 9️⃣ 后台管理

**博主账号初始化** → `AdminInitializer.java`
- 应用启动自动创建博主账号
- 首次运行需配置默认密码

---

## 🚀 快速开始

### 📋 前置要求

- **Node.js** >= 16.0
- **Java** >= 17
- **MySQL** >= 5.7
- **Redis** >= 6.0
- **Maven** >= 3.6（或使用 Maven Wrapper）

### 💾 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE boke CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入 SQL 脚本（如果提供）
mysql -u root -p boke < schema.sql
```

### 🔧 后端配置

#### 1. 修改数据库配置

编辑 `src/main/resources/application.properties`:

```properties
# MySQL 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/boke?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

#### 2. 配置 Redis

```properties
# Redis 配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.timeout=2000ms
```

#### 3. 配置 JWT 和邮件（使用环境变量）

```bash
# 设置环境变量（推荐）
export JWT_SECRET=your_very_secure_secret_key_here
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_app_password
```

或在 `application.properties` 中配置（**不推荐用于生产**）:

```properties
app.jwt.secret=${JWT_SECRET:default_secret_key}
app.jwt.expiration=86400000

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

#### 4. CORS 配置

```properties
# CORS 允许的来源
app.cors.allowed-origins=http://localhost:5173,https://yourdomain.com
```

### ▶️ 后端启动

**方式1: 使用 Maven Wrapper**

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**方式2: 编译后运行**

```bash
# 编译打包
.\mvnw.cmd clean package -DskipTests

# 运行 JAR 文件
java -jar target/boke-0.0.1-SNAPSHOT.jar
```

后端将在 `http://localhost:8080` 启动

### 🎨 前端配置

#### 1. 安装依赖

```bash
cd frontend
npm install
```

#### 2. 修改 API 配置（可选）

编辑 `frontend/src/api/http.js`:

```javascript
const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8080';
```

#### 3. 运行开发服务器

```bash
npm run dev
```

前端将在 `http://localhost:5173` 启动，自动代理 API 到后端 8080

#### 4. 构建生产版本

```bash
npm run build
```

生成的静态文件在 `dist/` 目录

---

## 🔒 安全建议

### 🚨 敏感信息管理

**❌ 不安全做法**（不要在代码中硬编码）:
```properties
# 不要这样做！
spring.datasource.password=root123
app.jwt.secret=mysecretkey
```

**✅ 安全做法**（使用环境变量）:
```bash
# 设置环境变量
export SPRING_DATASOURCE_PASSWORD=secure_password
export APP_JWT_SECRET=very_long_random_secure_key
export MAIL_PASSWORD=app_specific_password
```

### 🛡️ 生产环境建议

#### 1. CORS 配置细化

```properties
# 仅允许必要的域名
app.cors.allowed-origins=https://yourblog.com,https://www.yourblog.com
```

#### 2. JWT 安全

```properties
# 使用长强密钥（至少 32 个字符）
app.jwt.secret=aVeryLongRandomSecureKeyWithUpperLowerNumbersAndSymbols!@#$%^&*()
# 设置合理的过期时间（单位毫秒）
app.jwt.expiration=3600000  # 1小时
```

#### 3. 文件上传保护

```properties
# 上传目录安全配置
# - 配合反向代理（Nginx）进行访问控制
# - 验证文件类型和大小
# - 限制上传频率
app.upload.max-file-size=5242880  # 5MB
app.upload.allowed-types=jpg,jpeg,png,gif,pdf,doc,docx
```

#### 4. 数据库安全

- 使用强密码
- 启用 SSL 连接
- 定期备份
- 限制访问 IP

#### 5. 密码策略

```java
// 建议的密码强度要求
- 长度 >= 8 位
- 包含大小写字母、数字、特殊符号
- 使用 BCrypt 加密存储
```

#### 6. API 速率限制

```properties
# 防止暴力破解和 DDoS
app.ratelimit.enabled=true
app.ratelimit.requests-per-minute=60
```

### 📝 部署检查清单

- [ ] 所有敏感信息使用环境变量
- [ ] 启用 HTTPS/SSL
- [ ] 配置防火墙规则
- [ ] 设置数据库备份计划
- [ ] 启用 Redis 密码保护
- [ ] 配置日志监控
- [ ] 启用 CSRF 防护
- [ ] 验证 JWT 黑名单机制

---

## 📦 依赖清单

### 后端依赖（Maven）

关键依赖见 `pom.xml`:

```xml
<!-- Spring Boot -->
<spring-boot-starter-web/>
<spring-boot-starter-data-jpa/>
<spring-boot-starter-security/>

<!-- 数据库 -->
<mysql-connector-java/>
<h2/> <!-- 开发/测试 -->

<!-- 缓存 -->
<spring-boot-starter-data-redis/>

<!-- 认证 -->
<jjwt/> <!-- JWT -->

<!-- 邮件 -->
<spring-boot-starter-mail/>

<!-- 工具 -->
<lombok/>
```

### 前端依赖（NPM）

关键依赖见 `frontend/package.json`:

```json
{
  "dependencies": {
    "vue": "^3.5",
    "vite": "latest",
    "pinia": "latest",
    "vue-router": "latest",
    "axios": "latest",
    "element-plus": "latest",
    "@wangeditor-next/editor": "latest",
    "marked": "latest",
    "dompurify": "latest"
  }
}
```

---

## 📊 API 文档示例

### 登录

**请求**:
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "blogger",
  "password": "password123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "blogger",
      "email": "blogger@example.com",
      "role": "BLOGGER"
    }
  }
}
```

### 获取文章列表

**请求**:
```http
GET /api/articles?page=1&size=10&sort=createdAt,desc
Authorization: Bearer {token}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "文章标题",
        "content": "文章内容摘要...",
        "author": "blogger",
        "viewCount": 100,
        "isDraft": false,
        "createdAt": "2024-01-01T12:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "currentPage": 1
  }
}
```

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发工作流

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交变更 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 后端: 遵循 Google Java 代码规范
- 前端: 遵循 Airbnb JavaScript 代码规范
- 提交信息使用英文，清晰描述变更内容

---

## 📄 开源协议

本项目采用 **MIT License** 开源协议。

```
MIT License

Copyright (c) 2024 Beiheng

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

详见 [LICENSE](LICENSE) 文件。

---

## 📞 联系方式

- GitHub: [@beiheng127](https://github.com/beiheng127)
- Issues: [GitHub Issues](https://github.com/beiheng127/boke/issues)

---

## 🙏 致谢

感谢以下优秀项目的支持：

- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架
- [Spring Boot](https://spring.io/projects/spring-boot) - Java 快速开发框架
- [Element Plus](https://element-plus.org/) - 企业级 UI 组件库
- [WangEditor](https://www.wangeditor.com/) - 开源富文本编辑器

---

<div align="center">

**⭐ 如果觉得有帮助，请给个 Star 支持一下！**

Made with ❤️ by [Beiheng](https://github.com/beiheng127)

</div>
