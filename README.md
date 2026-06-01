<p align="center">
  <h1 align="center">� Corner</h1>
  <p align="center">
    <strong>情绪驱动的智能地点推荐系统</strong>
  </p>
  <p align="center">
    根据你的心情，找到属于你的"治愈角落"
  </p>
</p>

---

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.14-green?logo=springboot&logoColor=white)
![Vue.js](https://img.shields.io/badge/Vue.js-3.5.32-4FC08D?logo=vuedotjs&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-6.0+-DC3828?logo=redis&logoColor=white)
![LangChain4j](https://img.shields.io/badge/LangChain4j-AI-blue)
![Qwen](https://img.shields.io/badge/Qwen-LLM-purple)

[功能特性](#-核心特性) · [快速开始](#-快速开始) · [技术架构](#-技术架构) · [API 文档](#-api-接口) · [项目结构](#-项目结构)

</div>

---

## ✨ 核心特性

### 🧠 AI 情绪理解
- 基于大语言模型（Qwen/DeepSeek）深度解析用户情绪状态
- 支持自然语言输入，智能提取情绪关键词
- 温暖贴心的 AI 安慰话术生成

### � 三层推荐引擎
```
本地数据库匹配 → 向量语义搜索 → 实时联网搜索
     ↓               ↓                ↓
 (标签+距离)    (Embedding相似度)   (Tavily API)
```
- **第一层**：基于情绪标签 + 地理距离的精准匹配
- **第二层**：向量数据库语义相似度检索
- **第三层**：实时联网搜索补充更多选择

### 💾 智能记忆系统
| 类型 | 说明 |
|:---:|:---|
| ✅ 已访问 | 记录心情与评分，形成个人足迹 |
| ⭐ 已收藏 | 优先推荐，个性化体验升级 |
| ❌ 不喜欢 | 自动排除，持续优化推荐质量 |

### 📍 LBS 位置服务
- GPS 定位 + 百度地图地理编码
- 智能距离计算（Haversine 公式）
- 一键导航跳转

### 🌤️ 天气感知
- 集成 AccuWeather API 实时天气
- 结合天气条件优化推荐策略

---

## 🚀 快速开始

### 环境要求

| 组件 | 版本要求 |
|:---:|:---|
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |

### 一键启动

<details>
<summary><b>📦 克隆 & 安装依赖</b></summary>

```bash
git clone https://github.com/your-username/corner.git
cd corner
```

**后端**
```bash
cd backend
mvn clean install -DskipTests
```

**前端**
```bash
cd frontend
npm install
```

</details>

<details>
<summary><b>⚙️ 配置环境变量</b></summary>

创建 `.env` 文件：

```env
OPENAI_API_KEY=your_dashscope_api_key
TAVILY_API_KEY=your_tavily_api_key
BAIDU_MAP_API_KEY=your_baidu_map_api_key
ACCUWEATHER_API_KEY=your_accuweather_api_key
```

修改 `backend/src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/corner_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm
```

</details>

<details>
<summary><b>🗄️ 初始化数据库</b></summary>

```bash
mysql -u root -p < backend/src/main/resources/db/init.sql
```

启动 Redis：
```bash
docker run -d -p 6379:6379 --name corner-redis redis
```

</details>

<details>
<summary><b>▶️ 启动服务</b></summary>

**终端 1 - 后端**
```bash
cd backend
mvn spring-boot:run
```
后端运行在 `http://localhost:8080`

**终端 2 - 前端**
```bash
cd frontend
npm run dev
```
前端运行在 `http://localhost:5173`

</details>

---

## 🏗️ 技术架构

### 系统架构图

```
┌──────────────────────────────────────────────────────────────┐
│                         用户层                                │
│                    Vue 3 SPA (Mobile First)                   │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP / SSE
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                       API 网关层                              │
│              Spring Boot (:8080) + Vite Proxy                 │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────────────────┐ │
│  │ Auth       │  │ Recommend  │  │ Chat (SSE Stream)      │ │
│  │ Interceptor│  │ Controller│  │ Controller             │ │
│  └─────┬──────┘  └─────┬──────┘  └───────────┬────────────┘ │
│        │               │                      │              │
│  ┌─────▼──────┐  ┌─────▼──────────────────────▼────────────┐ │
│  │           Service Layer (Business Logic)                  │ │
│  │  ┌─────────────────┐  ┌─────────────────────────────┐   │ │
│  │  │ RecommendService│  │ LangChain4j AI Service      │   │ │
│  │  │ MemoryService   │  │ ├─ Tool: Local DB Search    │   │ │
│  │  │ PlaceService    │  │ ├─ Tool: Vector Search      │   │ │
│  │  │ LocationService │  │ └─ Tool: Web Search         │   │ │
│  │  └─────────────────┘  └─────────────────────────────┘   │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────┬───────────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
│   MySQL 8.0  │  │   Redis 6.0  │  │  External APIs   │
│  (地点数据)   │  │              │  │                  │
│  (用户数据)   │  │ ├─ 向量存储   │  │ ├─ 阿里云 Qwen   │
│  (标签字典)   │  │ ├─ 会话记忆   │  │ ├─ Tavily 搜索   │
│              │  │ └─ 缓存       │  │ ├─ 百度地图      │
│              │  │              │  │ └─ AccuWeather   │
└──────────────┘  └──────────────┘  └──────────────────┘
```

### 推荐流程时序图

```
用户          前端            后端              AI服务           工具链
 │             │               │                │                │
 │ 选择情绪     │               │                │                │
 │ 点击推荐     │               │                │                │
 │────────────→│ POST /recommend│                │                │
 │             │──────────────→│                │                │
 │             │               │ 解析请求        │                │
 │             │               │──────────────→ │                │
 │             │               │  getRecommend() │                │
 │             │               │                │──────────────→ │
 │             │               │                │ Step1: 本地DB  │
 │             │               │                │ ←───────────── │
 │             │               │                │ 返回 1-2 个结果 │
 │             │               │                │──────────────→ │
 │             │               │                │ Step2: 向量搜索│
 │             │               │                │ ←───────────── │
 │             │               │                │ 返回相似地点   │
 │             │               │                │──────────────→ │
 │             │               │                │ Step3: 联网搜索│
 │             │               │                │ ←───────────── │
 │             │               │                │ 返回在线地点   │
 │             │               │                │ 合并去重 ≥3个  │
 │             │               │←───────────────│                │
 │             │ JSON Response │                │                │
 │←────────────│               │                │                │
 │ 展示结果     │               │                │                │
```

---

## 📡 API 接口

### 认证接口

#### 手机号登录
```http
POST /api/user/login
Content-Type: application/json

{
  "phone": "13800138001"
}
```

**响应**
```json
{
  "code": 200,
  "data": {
    "user_id": 1,
    "nickname": "小明",
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### 推荐接口

#### 核心推荐
```http
POST /api/recommend
Authorization: Bearer <token>
Content-Type: application/json

{
  "mood": "烦闷",
  "energy_level": 3,
  "social_level": 2,
  "user_input": "我想找个安静的地方一个人待着",
  "user_lat": 22.5431,
  "user_lng": 113.9526
}
```

**响应**
```json
{
  "code": 200,
  "data": {
    "understanding": "听起来你今天有点累呢，找个安静的地方放松一下吧～",
    "emotionMatches": [
      {
        "placeId": 1,
        "name": "静谧咖啡馆",
        "description": "...",
        "tags": ["安静", "治愈"],
        "distanceKm": 1.2,
        "imageUrl": "..."
      }
    ]
  }
}
```

#### 流式对话
```http
POST /api/chat
Authorization: Bearer <token>
Content-Type: application/json

{
  "message": "还有更近的吗？",
  "memoryId": "user_1"
}
```

**响应**: Server-Sent Events (SSE) 流式输出

---

### 数据接口

| 方法 | 路径 | 说明 | 认证 |
|:---:|:---|:---|:---:|
| GET | `/api/tags` | 获取全部情绪标签 | ❌ |
| GET | `/api/memory/list` | 我的记忆列表 | ✅ |
| POST | `/api/place/feedback` | 地点反馈 | ✅ |
| GET | `/api/place/detail/{id}` | 地点详情 | ✅ |

---

## 📁 项目结构

```
Corner/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/example/corner/
│   │   ├── config/                   # 配置类
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── WebConfig.java
│   │   │   └── PlaceVectorLoader.java
│   │   ├── controller/               # REST 控制器
│   │   │   ├── RecommendController.java
│   │   │   ├── UserController.java
│   │   │   └── ...
│   │   ├── service/
│   │   │   ├── aiService/           # AI 服务层
│   │   │   │   └── RecommendAIService.java
│   │   │   └── impl/                # 业务实现
│   │   ├── tools/                   # AI 工具定义
│   │   │   └── RecommendAITools.java
│   │   ├── entity/                  # JPA 实体
│   │   ├── repository/              # 数据访问层
│   │   ├── vo/                      # 视图对象
│   │   └── util/                    # 工具类
│   └── src/main/resources/
│       ├── db/init.sql              # 数据库初始化脚本
│       └── application.yaml         # 应用配置
│
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── api/index.js             # API 封装
│   │   ├── components/              # 公共组件
│   │   │   ├── BottomNav.vue
│   │   │   └── TopBar.vue
│   │   ├── views/                   # 页面视图
│   │   │   ├── Recommend.vue        # 推荐（首页）
│   │   │   ├── Result.vue           # 结果页
│   │   │   ├── Detail.vue           # 详情页
│   │   │   ├── Login.vue            # 登录
│   │   │   ├── Memory.vue           # 记忆
│   │   │   ├── Discover.vue         # 发现
│   │   │   └── Profile.vue          # 个人中心
│   │   └── App.vue                  # 根组件
│   └── vite.config.js
│
├── .env                             # 环境变量
└── README.md                        # 本文件
```

---

## 🔧 技术栈详解

### 后端技术栈

| 技术 | 用途 | 版本 |
|:---:|:---|:---:|
| **Spring Boot** | 应用框架 | 3.5.14 |
| **Spring Data JPA** | ORM / 数据访问 | - |
| **Spring Security** | JWT 认证 | - |
| **Redis** | 向量存储 / 缓存 / 会话记忆 | 6.0+ |
| **MySQL** | 关系型数据库 | 8.0+ |
| **LangChain4j** | AI 集成框架 | latest |
| **Qwen / DeepSeek** | 大语言模型 | - |
| **Tavily API** | 联网搜索 | - |
| **Baidu Map API** | 地理位置 / 导航 | - |

### 前端技术栈

| 技术 | 用途 | 版本 |
|:---:|:---|:---:|
| **Vue 3** | 渐进式框架 | 3.5.32 |
| **Vite** | 构建工具 | 5.4.10 |
| **Axios** | HTTP 客户端 | 1.16.0 |
| **Fetch API** | SSE 流式请求 | Native |

---

## 📊 核心模块说明

### 1️⃣ 用户认证模块
- 手机号一键登录/注册
- JWT Token 无状态认证
- 基于拦截器的统一权限控制

### 2️⃣ 智能推荐模块
- 多维度匹配算法（情绪 × 距离 × 历史）
- 三层递进式搜索策略
- 强制工具调用机制（防止 AI 幻觉）

### 3️⃣ 向量检索模块
- 基于 Redis 的向量数据库
- text-embedding-v3 模型（1024 维）
- 应用启动自动初始化向量数据
- 余弦相似度语义搜索

### 4️⃣ 记忆系统模块
- 用户行为追踪（访问/收藏/不喜欢）
- 基于 Redis 的会话级聊天记忆
- 长期偏好学习

### 5️⃣ 位置服务模块
- GPS / IP 双模式定位
- 地理编码与逆地理编码
- Haversine 距离计算

---

## ⚠️ 重要注意事项

| 项目 | 说明 |
|:---|:---|
| **Redis 必需** | 应用无法在无 Redis 环境下启动（向量存储 + 会话记忆） |
| **API 密钥** | 所有密钥需在 `.env` 文件中配置，不可提交至代码仓库 |
| **JWT 密钥** | 生产环境必须使用至少 256 位的强随机密钥 |
| **向量维度** | embedding dimension 必须与模型匹配（text-embedding-v3 = 1024） |
| **CORS** | 开发环境已配置跨域，生产环境需调整允许的域名 |

---

## 🗺️ 开发路线图

### ✅ 已完成
- [x] DeepSeek / Qwen LLM 集成
- [x] 三层推荐引擎实现
- [x] 百度地图位置服务
- [x] AccuWeather 天气集成
- [x] Vue 3 前端完整页面
- [x] JWT 认证系统
- [x] 向量数据库检索
- [x] SSE 流式对话

### 🚧 进行中
- [ ] 推荐结果数量稳定性优化
- [ ] 用户画像长期分析

### 📋 待规划
- [ ] 推荐算法多维度扩展（时间、天气、季节）
- [ ] Redis 缓存层性能优化
- [ ] 单元测试与集成测试覆盖
- [ ] 用户画像系统深化
- [ ] PWA 离线支持
- [ ] 实时消息推送
- [ ] UI/UX 设计升级

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Made with ❤️ by <a href="#">Your Team</a>
</p>
