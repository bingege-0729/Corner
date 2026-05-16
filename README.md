# Corner 项目说明

## 项目简介
Corner 是一个基于情绪推荐的地点推荐系统，根据用户当前的心情和状态，推荐适合的情绪地点。

## 技术栈
### 后端
- Spring Boot 3.5.14
- Java 17
- MySQL 8.0+
- JPA/Hibernate
- JWT (jjwt 0.12.5)
- Lombok
- Redis (用于会话记忆存储和向量数据库)
- LangChain4j (AI集成框架)
- DeepSeek/Qwen (大语言模型)
- Baidu Map API (地理位置服务)
- AccuWeather API (天气服务)

### 前端
- Vue 3.5.32
- Vite 5.4.10
- Axios 1.16.0

## 项目结构
```
Corner/
├── backend/                    # 后端项目
│   ├── src/main/java/com/example/corner/
│   │   ├── common/              # 通用类
│   │   │   ├── RedisConstant.java    # Redis常量定义
│   │   │   ├── Result.java           # 统一响应格式
│   │   │   └── ResultCode.java       # 响应状态码
│   │   ├── config/              # 配置类
│   │   │   ├── CommonConfig.java     # 通用配置（Bean定义）
│   │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   │   ├── PlaceVectorLoader.java  # 地点向量加载器
│   │   │   └── WebConfig.java        # Web配置（拦截器）
│   │   ├── controller/          # 控制器
│   │   │   ├── LocationController.java # 位置相关接口
│   │   │   ├── MemoryController.java   # 记忆相关接口
│   │   │   ├── PlaceController.java    # 地点相关接口
│   │   │   ├── RecommendController.java # 推荐相关接口
│   │   │   ├── TagController.java      # 标签相关接口
│   │   │   └── UserController.java     # 用户相关接口
│   │   ├── dto/                 # 数据传输对象
│   │   │   ├── LoginRequest.java
│   │   │   └── RecommendRequest.java
│   │   ├── entity/              # 实体类
│   │   │   ├── EmotionTagDict.java
│   │   │   ├── PlaceEmotionLibrary.java
│   │   │   ├── PlaceTagRelation.java
│   │   │   ├── UserInfo.java
│   │   │   ├── UserMoodRecord.java
│   │   │   └── UserPlaceMemory.java
│   │   ├── interceptor/         # 拦截器
│   │   │   └── AuthInterceptor.java # JWT认证拦截器
│   │   ├── repository/          # 数据访问层
│   │   │   ├── EmotionTagDictRepository.java
│   │   │   ├── PlaceEmotionLibraryRepository.java
│   │   │   ├── PlaceTagRelationRepository.java
│   │   │   ├── RedisChatMemoryRepository.java # Redis聊天记忆仓库
│   │   │   ├── UserInfoRepository.java
│   │   │   ├── UserMoodRecordRepository.java
│   │   │   └── UserPlaceMemoryRepository.java
│   │   ├── service/             # 业务逻辑层
│   │   │   ├── aiService/            # AI服务
│   │   │   │   └── RecommendAIService.java # AI推荐服务
│   │   │   ├── impl/                 # 服务实现
│   │   │   │   ├── BaiduMapServiceImpl.java
│   │   │   │   ├── ChatServiceImpl.java
│   │   │   │   ├── LocationServiceImpl.java
│   │   │   │   ├── MemoryServiceImpl.java
│   │   │   │   ├── PlaceServiceImpl.java
│   │   │   │   ├── RecommendServiceImpl.java
│   │   │   │   ├── TagServiceImpl.java
│   │   │   │   └── UserServiceImpl.java
│   │   │   ├── BaiduMapService.java
│   │   │   ├── ChatService.java
│   │   │   ├── LocationService.java
│   │   │   ├── MemoryService.java
│   │   │   ├── PlaceService.java
│   │   │   ├── PlaceVectorData.java
│   │   │   ├── RecommendService.java
│   │   │   ├── TagService.java
│   │   │   └── UserService.java
│   │   ├── tools/               # AI工具类
│   │   │   └── RecommendAITools.java # AI推荐工具类
│   │   ├── util/                # 工具类
│   │   │   └── JwtUtil.java     # JWT工具
│   │   └── vo/                  # 视图对象
│   │       ├── AvatarResponse.java
│   │       ├── LoginResponse.java
│   │       ├── PlaceCard.java
│   │       ├── PlaceDetailResponse.java
│   │       ├── RecommendResponse.java
│   │       ├── TagResponse.java
│   │       ├── TravelTipCard.java
│   │       ├── UserHistory.java
│   │       └── UserStatsResponse.java
│   └── src/main/resources/
│       ├── db/
│       │   └── init.sql         # 数据库初始化脚本
│       └── application.yaml     # 应用配置
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   │   └── index.js
│   │   ├── assets/             # 静态资源
│   │   │   ├── css/
│   │   │   │   └── main.css
│   │   │   └── img/
│   │   │       └── bg.png
│   │   ├── components/         # 公共组件
│   │   │   ├── BottomNav.vue   # 底部导航
│   │   │   └── TopBar.vue      # 顶部栏
│   │   ├── views/              # 页面视图
│   │   │   ├── Detail.vue      # 地点详情页
│   │   │   ├── Discover.vue    # 发现页
│   │   │   ├── GoTo.vue        # 前往页面
│   │   │   ├── Login.vue       # 登录页
│   │   │   ├── Memory.vue      # 记忆页
│   │   │   ├── Profile.vue     # 个人中心
│   │   │   ├── Recommend.vue   # 推荐页
│   │   │   └── Result.vue      # 结果页
│   │   ├── App.vue             # 根组件
│   │   ├── axios.js            # Axios配置
│   │   └── main.js             # 入口文件
│   ├── public/                 # 公共资源
│   │   ├── images/
│   │   │   └── default-place.jpg
│   │   └── favicon.ico
│   ├── package.json            # 前端依赖配置
│   └── vite.config.js          # Vite配置
├── uploads/                    # 上传文件目录
│   └── avatars/                # 头像上传目录
├── .env                        # 环境变量配置
├── Corner接口文档0514.md       # 接口文档
└── 修改日志.md                 # 修改日志
```

## API 接口列表

### 1. 手机号登录
- **路径**: `POST /api/user/login`
- **说明**: 不需要登录，自动注册或登录
- **请求体**:
```json
{
  "phone": "13800138001"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "user_id": 1,
    "nickname": "小明",
    "token": "xxx"
  }
}
```

### 2. 获取全部标签
- **路径**: `GET /api/tags`
- **说明**: 不需要登录
- **响应**:
```json
{
  "code": 200,
  "data": {
    "tags": [
      { "id": 1, "tag_name": "安静", "category": "氛围" }
    ]
  }
}
```

### 3. 核心推荐
- **路径**: `POST /api/recommend`
- **说明**: 需要登录，Header 带 `Authorization: Bearer <token>`
- **请求体**:
```json
{
  "mood": "烦闷",
  "energy_level": 3,
  "social_level": 2,
  "user_input": null,
  "user_lat": 22.5431,
  "user_lng": 113.9526
}
```
- **响应**:
```json
{
  "code": 200,
  "data": {
    "understanding": "懂了，你需要安静地待一会儿",
    "memory_matches": [...],
    "emotion_matches": [...]
  }
}
```

### 4. 推荐反馈
- **路径**: `POST /api/place/feedback`
- **说明**: 需要登录
- **请求体**:
```json
{
  "place_id": 1,
  "action": "liked",
  "feedback": "真的很放松",
  "rating": 5
}
```
- **action 值映射**:
  - `liked` → `BOOKMARKED`
  - `disliked` → `DISLIKED`
  - `visited` → `VISITED`

### 5. 我的记忆列表
- **路径**: `GET /api/memory/list?type=visited`
- **说明**: 需要登录
- **参数**: `type` 可选，值为 `visited` / `bookmarked`，不传返回全部

### 6. 地点详情
- **路径**: `GET /api/place/detail/{place_id}`
- **说明**: 需要登录

## 快速开始

### 1. 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+（用于会话记忆存储和向量数据库）

### 2. 数据库和Redis配置
1. 创建数据库并导入初始数据：
```bash
mysql -u root -p < backend/src/main/resources/db/init.sql
```

2. 修改 `application.yaml` 中的数据库配置：
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
```

3. **启动 Redis 服务**（必需）：

**使用 Docker（推荐）**：
```bash
docker run -d -p 6379:6379 --name redis redis
```

**或本地启动**：
```bash
redis-server
```

验证 Redis 是否运行：
```bash
redis-cli ping
# 应返回 PONG
```

4. 配置 API 密钥（在 `.env` 文件中）：
```env
OPENAI_API_KEY=your_openai_api_key_here
TAVILY_API_KEY=your_tavily_api_key_here
BAIDU_MAP_API_KEY=your_baidu_map_api_key_here
ACCUWEATHER_API_KEY=your_accuweather_api_key_here
UNSPLASH_ACCESS_KEY=your_unsplash_access_key_here
PEXELS_API_KEY=your_pexels_api_key_here
```

5. 修改 JWT 密钥（至少32字符）：
```yaml
jwt:
  secret: your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm
```

### 3. 运行项目
#### 后端启动
```bash
cd backend
mvn spring-boot:run
```

**注意**：首次启动时，应用会自动初始化地点向量数据到 Redis，请确保 Redis 服务已启动。

#### 前端启动
```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 http://localhost:5173

### 4. 测试接口
使用 Postman 或其他 API 测试工具测试接口。

**登录示例**:
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138001"}'
```

**获取标签示例**:
```bash
curl http://localhost:8080/api/tags
```

**推荐示例**（需要先登录获取 token）:
```bash
curl -X POST http://localhost:8080/api/recommend \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "mood": "烦闷",
    "user_lat": 22.5431,
    "user_lng": 113.9526
  }'
```

## 核心功能模块

### 1. 用户认证模块
- 手机号一键登录/注册
- JWT Token 认证
- 基于拦截器的权限控制
- 用户信息管理（头像、昵称等）

### 2. 智能推荐模块
- 基于情绪标签的地点推荐
- 集成 LangChain4j AI 服务（DeepSeek/Qwen模型）
- 结合用户历史记忆的个性化推荐
- 多维度匹配算法（情绪、距离、历史行为）
- 支持自然语言输入理解

### 3. 地点管理模块
- 地点详情查询
- 用户反馈收集（喜欢/不喜欢/已访问）
- 地点与情绪标签关联
- 地点图片展示

### 4. 记忆系统模块
- 用户访问记录管理
- 收藏地点管理
- 基于 Redis 的会话记忆存储
- 用户心情记录

### 5. 标签系统模块
- 情绪标签字典管理
- 地点标签关联查询
- 标签分类管理

### 6. 向量检索模块
- 基于 Redis 的向量数据库存储地点描述向量
- 使用 text-embedding-v3 模型生成 1024 维向量
- 应用启动时自动初始化官方地点向量数据
- 支持语义相似度搜索，实现智能地点推荐

### 7. 位置服务模块
- 集成百度地图API
- IP定位功能
- 地理编码/逆地理编码
- 距离计算

### 8. 天气服务模块
- 集成 AccuWeather API
- 实时天气信息查询
- 基于位置的天气推荐

### 9. 前端功能模块
- 响应式移动端界面设计
- 底部导航栏组件
- 顶部栏组件
- 多个页面视图（登录、发现、推荐、记忆、个人中心等）
- Axios HTTP客户端封装

## 注意事项

1. **API 密钥配置**: 在 `.env` 文件中配置所有必需的 API 密钥：
   - `OPENAI_API_KEY`: OpenAI/DashScope API密钥
   - `TAVILY_API_KEY`: Tavily搜索API密钥
   - `BAIDU_MAP_API_KEY`: 百度地图API密钥
   - `ACCUWEATHER_API_KEY`: AccuWeather天气API密钥
   - `UNSPLASH_ACCESS_KEY`: Unsplash图片API密钥（可选）
   - `PEXELS_API_KEY`: Pexels图片API密钥（可选）
2. **数据库密码**: 根据实际情况修改数据库密码
3. **Redis 服务**: 
   - **必须启动 Redis 服务**，否则应用无法启动
   - Redis 用于两部分功能：
     - 会话记忆存储（ChatMemory）
     - 向量数据库（EmbeddingStore）存储地点向量
4. **JWT 密钥**: 生产环境务必修改为强密钥（至少32字符）
5. **向量维度配置**: `application.yaml` 中 `langchain4j.openai.embedding-model.dimension` 需与使用的 embedding 模型匹配（text-embedding-v3 为 1024 维）
6. **距离计算**: 使用了简化的 Haversine 公式计算距离
7. **图片资源**: 需要自行准备图片资源或配置外部图片API密钥
8. **前后端分离**: 项目采用前后端分离架构，需分别启动后端和前端服务
9. **CORS配置**: 开发环境下已配置跨域支持，生产环境需根据实际域名调整

## 后续优化方向

1. ✅ 已完成 DeepSeek/Qwen LLM 集成，实现情绪解析和文案生成
2. ✅ 已完成百度地图API集成，提供位置服务
3. ✅ 已完成AccuWeather API集成，提供天气信息
4. ✅ 已完成前端Vue3项目搭建，实现主要页面功能
5. 优化推荐算法，增加更多维度的匹配（时间、天气、用户偏好等）
6. 添加缓存机制提升性能（地点信息、标签数据等）
7. 增加日志记录和异常处理
8. 添加单元测试和集成测试
9. 实现更精确的距离计算和地图服务功能
10. 增加用户画像系统，提供更个性化的推荐
11. 实现实时消息推送功能
12. 优化前端UI/UX设计，提升用户体验
13. 增加离线功能和PWA支持
