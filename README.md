# Corner 项目说明

## 项目简介
Corner 是一个基于情绪推荐的地点推荐系统，根据用户当前的心情和状态，推荐适合的情绪地点。

## 技术栈
- Spring Boot 4.0.6
- Java 17
- MySQL 8.0+
- JPA/Hibernate
- JWT (jjwt 0.12.5)
- Lombok
- Redis (用于会话记忆存储)
- LangChain4j (AI集成框架)

## 项目结构
```
backend/src/main/java/com/example/corner/
├── common/              # 通用类
│   ├── RedisConstant.java    # Redis常量定义
│   ├── Result.java           # 统一响应格式
│   └── ResultCode.java       # 响应状态码
├── config/              # 配置类
│   ├── CommonConfig.java     # 通用配置（Bean定义）
│   └── WebConfig.java        # Web配置（拦截器）
├── controller/          # 控制器
│   ├── MemoryController.java # 记忆相关接口
│   ├── PlaceController.java  # 地点相关接口
│   ├── RecommendController.java # 推荐相关接口
│   ├── TagController.java    # 标签相关接口
│   └── UserController.java   # 用户相关接口
├── dto/                 # 数据传输对象
│   ├── FeedbackRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── MemoryItem.java
│   ├── MemoryListResponse.java
│   ├── PlaceCard.java
│   ├── PlaceDetailResponse.java
│   ├── RecommendRequest.java
│   ├── RecommendResponse.java
│   ├── TagResponse.java
│   └── UserHistory.java
├── entity/              # 实体类
│   ├── EmotionTagDict.java
│   ├── PlaceEmotionLibrary.java
│   ├── PlaceTagRelation.java
│   ├── UserInfo.java
│   └── UserPlaceMemory.java
├── interceptor/         # 拦截器
│   └── AuthInterceptor.java # JWT认证拦截器
├── repository/          # 数据访问层
│   ├── EmotionTagDictRepository.java
│   ├── PlaceEmotionLibraryRepository.java
│   ├── PlaceTagRelationRepository.java
│   ├── RedisChatMemoryRepository.java # Redis聊天记忆仓库
│   ├── UserInfoRepository.java
│   └── UserPlaceMemoryRepository.java
├── service/             # 业务逻辑层
│   ├── aiService/            # AI服务
│   │   └── RecommendAIService.java # AI推荐服务
│   ├── impl/                 # 服务实现
│   │   ├── MemoryServiceImpl.java
│   │   ├── PlaceServiceImpl.java
│   │   ├── RecommendServiceImpl.java
│   │   ├── TagServiceImpl.java
│   │   └── UserServiceImpl.java
│   ├── MemoryService.java
│   ├── PlaceService.java
│   ├── RecommendService.java
│   ├── TagService.java
│   └── UserService.java
├── tools/               # 工具类
│   └── RecommendAITools.java # AI推荐工具类
└── util/                # 工具类
    └── JwtUtil.java     # JWT工具

backend/src/main/resources/
├── db/
│   └── init.sql         # 数据库初始化脚本
└── application.yaml     # 应用配置
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
- Redis 6.0+（用于会话记忆存储）

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

3. 修改 JWT 密钥（至少32字符）：
```yaml
jwt:
  secret: your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm
```

### 3. 运行项目
```bash
cd backend
mvn spring-boot:run
```

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

### 2. 智能推荐模块
- 基于情绪标签的地点推荐
- 集成 LangChain4j AI 服务
- 结合用户历史记忆的个性化推荐
- 多维度匹配算法（情绪、距离、历史行为）

### 3. 地点管理模块
- 地点详情查询
- 用户反馈收集（喜欢/不喜欢/已访问）
- 地点与情绪标签关联

### 4. 记忆系统模块
- 用户访问记录管理
- 收藏地点管理
- 基于 Redis 的会话记忆存储

### 5. 标签系统模块
- 情绪标签字典管理
- 地点标签关联查询

## 注意事项

1. **JWT 密钥**: 生产环境务必修改为强密钥（至少32字符）
2. **数据库密码**: 根据实际情况修改数据库密码
3. **Redis 配置**: 确保 Redis 服务正常运行，用于会话记忆存储
4. **LLM 集成**: 当前版本集成了 LangChain4j 框架，需要配置相应的 LLM API 密钥
5. **距离计算**: 使用了简化的 Haversine 公式计算距离
6. **图片资源**: 需要自行准备图片资源或修改为外部图片链接

## 后续优化方向

1. 完善 LangChain4j LLM 集成，实现更精准的情绪解析和文案生成
2. 优化推荐算法，增加更多维度的匹配（时间、天气、用户偏好等）
3. 添加缓存机制提升性能（地点信息、标签数据等）
4. 增加日志记录和异常处理
5. 添加单元测试和集成测试
6. 实现更精确的距离计算和地图服务集成（高德/百度地图API）
7. 增加用户画像系统，提供更个性化的推荐
8. 实现实时消息推送功能
