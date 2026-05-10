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

## 项目结构
```
src/main/java/com/example/corner/
├── common/              # 通用类
│   └── ApiResponse.java # 统一响应格式
├── config/              # 配置类
│   └── WebConfig.java   # Web配置（拦截器）
├── controller/          # 控制器
│   └── CornerController.java # API接口
├── dto/                 # 数据传输对象
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── TagResponse.java
│   ├── RecommendRequest.java
│   ├── RecommendResponse.java
│   ├── PlaceCard.java
│   ├── FeedbackRequest.java
│   ├── MemoryItem.java
│   ├── MemoryListResponse.java
│   ├── PlaceDetailResponse.java
│   └── UserHistory.java
├── entity/              # 实体类
│   ├── UserInfo.java
│   ├── EmotionTagDict.java
│   ├── PlaceEmotionLibrary.java
│   ├── PlaceTagRelation.java
│   └── UserPlaceMemory.java
├── interceptor/         # 拦截器
│   └── AuthInterceptor.java # JWT认证拦截器
├── repository/          # 数据访问层
│   ├── UserInfoRepository.java
│   ├── EmotionTagDictRepository.java
│   ├── PlaceEmotionLibraryRepository.java
│   ├── PlaceTagRelationRepository.java
│   └── UserPlaceMemoryRepository.java
├── service/             # 业务逻辑层
│   └── CornerService.java
└── util/                # 工具类
    └── JwtUtil.java     # JWT工具

src/main/resources/
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

### 2. 数据库配置
1. 创建数据库并导入初始数据：
```bash
mysql -u root -p < src/main/resources/db/init.sql
```

2. 修改 `application.yaml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/corner_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

3. 修改 JWT 密钥（至少32字符）：
```yaml
jwt:
  secret: your-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm
```

### 3. 运行项目
```bash
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

## 注意事项

1. **JWT 密钥**: 生产环境务必修改为强密钥
2. **数据库密码**: 根据实际情况修改数据库密码
3. **LLM 集成**: 当前版本未集成真实的 LLM，相关功能使用了简单逻辑替代
4. **距离计算**: 使用了简化的 Haversine 公式计算距离
5. **图片资源**: 需要自行准备图片资源或修改为外部图片链接

## 后续优化方向

1. 集成 LangChain4j 实现真实的情绪解析和文案生成
2. 完善推荐算法，增加更多维度的匹配
3. 添加缓存机制提升性能
4. 增加日志记录和异常处理
5. 添加单元测试和集成测试
6. 实现更精确的距离计算和地图服务集成
