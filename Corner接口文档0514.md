---
title: 默认模块
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---



# TagController

## GET 查询全部标签

GET /api/tags

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "tags": [
      {
        "id": 0,
        "tagName": "string",
        "category": "string"
      }
    ]
  },
  "timestamp": 0,
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultMapObject](#schemaresultmapobject)|



----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# UserController

## POST 手机号登录

POST /api/user/login

> Body 请求参数

```json
{
    "phone": "13532009336"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|
|body|body|[LoginRequest](#schemaloginrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "userId": 0,
    "nickname": "string",
    "token": "string"
  },
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultLoginResponse](#schemaresultloginresponse)|

## POST 上传头像接口

POST /api/user/avatar/upload

> Body 请求参数

```yaml
file: string

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|
|body|body|object| 否 |none|
|» file|body|string(binary)| 是 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "avatarUrl": "string"
  },
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultAvatarResponse](#schemaresultavatarresponse)|

## POST 退出登录

POST /api/user/logout

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": null,
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 统计用户数据

GET /api/user/stats

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "visitedPlacesCount": 0,
    "moodStats": {
      "key": 0
    }
  },
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultUserStatsResponse](#schemaresultuserstatsresponse)|



-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# PlaceController

## GET 地点详情

GET /api/place/detail/{placeId}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|placeId|path|integer| 是 |none|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "placeId": 0,
    "placeName": "string",
    "address": "string",
    "latitude": 0,
    "longitude": 0,
    "moodTags": [
      "string"
    ],
    "crowdLevel": "string",
    "bestTime": "string",
    "oneSentence": "string",
    "fullDescription": "string",
    "imageUrl": "string",
    "tips": "string",
    "yourHistory": {
      "hasVisited": true,
      "visitCount": 0,
      "lastVisited": "string",
      "yourRating": 0,
      "yourFeedback": "string"
    }
  },
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultPlaceDetailResponse](#schemaresultplacedetailresponse)|

## POST 生成出行温馨提示

POST /api/place/{placeId}/travel-tips

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|placeId|path|integer| 是 |none|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "placeId": 0,
    "placeName": "string",
    "address": "string",
    "weatherTip": "string",
    "preparationTip": "string",
    "aiMessage": "string",
    "distanceText": "string"
  },
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultTravelTipCard](#schemaresulttraveltipcard)|



-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# LocationController

## POST 获取用户实时位置

POST /api/location/update

> Body 请求参数

```json
{
  "key": {}
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|
|body|body|[MapObject](#schemamapobject)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": "string",
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultString](#schemaresultstring)|



-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# RecommendController

## POST 推荐接口

POST /api/recommend

> Body 请求参数

```json
{
  "mood": "string",
  "energyLevel": 0,
  "socialLevel": 0,
  "userInput": "string",
  "userLat": 0,
  "userLng": 0,
  "enableStream": true
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|
|body|body|[RecommendRequest](#schemarecommendrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "understanding": "string",
    "emotionMatches": [
      {
        "placeId": 0,
        "placeName": "string",
        "address": "string",
        "moodTags": [
          "string"
        ],
        "crowdLevel": "string",
        "oneSentence": "string",
        "imageUrl": "string",
        "distanceText": "string",
        "matchType": "string",
        "matchReason": "string",
        "lastVisited": "string"
      }
    ]
  },
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultRecommendResponse](#schemaresultrecommendresponse)|

## POST AI 对话接口（流式输出）

POST /api/chat

> Body 请求参数

```json
{
  "mood": "string",
  "energyLevel": 0,
  "socialLevel": 0,
  "userInput": "string",
  "userLat": 0,
  "userLng": 0,
  "enableStream": true
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|
|body|body|[RecommendRequest](#schemarecommendrequest)| 否 |none|

> 返回示例

> 200 Response

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

*流式响应*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|



-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# MemoryController

## POST 切换地点收藏状态（收藏/取消收藏）

POST /api/memory/{placeId}/bookmark

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|placeId|path|integer| 是 |none|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": null,
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultVoid](#schemaresultvoid)|

## GET 获取用户收藏的地点列表

GET /api/memory/bookmarks

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "placeId": 0,
      "placeName": "string",
      "address": "string",
      "moodTags": [
        "string"
      ],
      "crowdLevel": "string",
      "oneSentence": "string",
      "imageUrl": "string",
      "distanceText": "string",
      "matchType": "string",
      "matchReason": "string",
      "lastVisited": "string"
    }
  ],
  "timestamp": 0
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListPlaceCard](#schemaresultlistplacecard)|

## GET 获取用户去过的地点及对应心情（用于地图展示）

GET /api/memory/visited-with-mood

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "key": {}
    }
  ],
  "timestamp": 0,
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[ResultListMapObject](#schemaresultlistmapobject)|

# 数据模型

<h2 id="tocS_tags">tags</h2>

<a id="schematags"></a>
<a id="schema_tags"></a>
<a id="tocStags"></a>
<a id="tocstags"></a>

```json
{
  "id": 0,
  "tagName": "string",
  "category": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||none|
|tagName|string|false|none||none|
|category|string|false|none||none|

<h2 id="tocS_ResultMapObject">ResultMapObject</h2>

<a id="schemaresultmapobject"></a>
<a id="schema_ResultMapObject"></a>
<a id="tocSresultmapobject"></a>
<a id="tocsresultmapobject"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "tags": [
      {
        "id": 0,
        "tagName": "string",
        "category": "string"
      }
    ]
  },
  "timestamp": 0,
  "success": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|object|false|none||响应数据|
|» tags|[[tags](#schematags)]|false|none||none|
|timestamp|integer(int64)|false|none||时间戳|
|success|boolean|false|none||判断是否成功|

<h2 id="tocS_ChatRequest">ChatRequest</h2>

<a id="schemachatrequest"></a>
<a id="schema_ChatRequest"></a>
<a id="tocSchatrequest"></a>
<a id="tocschatrequest"></a>

```json
{
  "message": "string",
  "userLat": 0,
  "userLng": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|message|string|false|none||用户消息|
|userLat|number|false|none||用户纬度（可选，用于位置相关对话）|
|userLng|number|false|none||用户经度（可选，用于位置相关对话）|

<h2 id="tocS_LoginResponse">LoginResponse</h2>

<a id="schemaloginresponse"></a>
<a id="schema_LoginResponse"></a>
<a id="tocSloginresponse"></a>
<a id="tocsloginresponse"></a>

```json
{
  "userId": 0,
  "nickname": "string",
  "token": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userId|integer(int64)|false|none||none|
|nickname|string|false|none||none|
|token|string|false|none||none|

<h2 id="tocS_ResultLoginResponse">ResultLoginResponse</h2>

<a id="schemaresultloginresponse"></a>
<a id="schema_ResultLoginResponse"></a>
<a id="tocSresultloginresponse"></a>
<a id="tocsresultloginresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "userId": 0,
    "nickname": "string",
    "token": "string"
  },
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[LoginResponse](#schemaloginresponse)|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_LoginRequest">LoginRequest</h2>

<a id="schemaloginrequest"></a>
<a id="schema_LoginRequest"></a>
<a id="tocSloginrequest"></a>
<a id="tocsloginrequest"></a>

```json
{
  "phone": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|phone|string|false|none||none|

<h2 id="tocS_AvatarResponse">AvatarResponse</h2>

<a id="schemaavatarresponse"></a>
<a id="schema_AvatarResponse"></a>
<a id="tocSavatarresponse"></a>
<a id="tocsavatarresponse"></a>

```json
{
  "avatarUrl": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|avatarUrl|string|false|none||none|

<h2 id="tocS_ResultAvatarResponse">ResultAvatarResponse</h2>

<a id="schemaresultavatarresponse"></a>
<a id="schema_ResultAvatarResponse"></a>
<a id="tocSresultavatarresponse"></a>
<a id="tocsresultavatarresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "avatarUrl": "string"
  },
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[AvatarResponse](#schemaavatarresponse)|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_ResultVoid">ResultVoid</h2>

<a id="schemaresultvoid"></a>
<a id="schema_ResultVoid"></a>
<a id="tocSresultvoid"></a>
<a id="tocsresultvoid"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": null,
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|null|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_MapInteger">MapInteger</h2>

<a id="schemamapinteger"></a>
<a id="schema_MapInteger"></a>
<a id="tocSmapinteger"></a>
<a id="tocsmapinteger"></a>

```json
{
  "key": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|key|integer|false|none||none|

<h2 id="tocS_UserStatsResponse">UserStatsResponse</h2>

<a id="schemauserstatsresponse"></a>
<a id="schema_UserStatsResponse"></a>
<a id="tocSuserstatsresponse"></a>
<a id="tocsuserstatsresponse"></a>

```json
{
  "visitedPlacesCount": 0,
  "moodStats": {
    "key": 0
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|visitedPlacesCount|integer(int64)|false|none||去过的不同地点数量|
|moodStats|[MapInteger](#schemamapinteger)|false|none||各情绪标签使用次数 {心情: 次数}|

<h2 id="tocS_ResultUserStatsResponse">ResultUserStatsResponse</h2>

<a id="schemaresultuserstatsresponse"></a>
<a id="schema_ResultUserStatsResponse"></a>
<a id="tocSresultuserstatsresponse"></a>
<a id="tocsresultuserstatsresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "visitedPlacesCount": 0,
    "moodStats": {
      "key": 0
    }
  },
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[UserStatsResponse](#schemauserstatsresponse)|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_UserHistory">UserHistory</h2>

<a id="schemauserhistory"></a>
<a id="schema_UserHistory"></a>
<a id="tocSuserhistory"></a>
<a id="tocsuserhistory"></a>

```json
{
  "hasVisited": true,
  "visitCount": 0,
  "lastVisited": "string",
  "yourRating": 0,
  "yourFeedback": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|hasVisited|boolean|false|none||是否访问过|
|visitCount|integer|false|none||访问次数|
|lastVisited|string|false|none||最后访问时间|
|yourRating|integer|false|none||你的评分|
|yourFeedback|string|false|none||你的反馈|

<h2 id="tocS_FeedbackRequest">FeedbackRequest</h2>

<a id="schemafeedbackrequest"></a>
<a id="schema_FeedbackRequest"></a>
<a id="tocSfeedbackrequest"></a>
<a id="tocsfeedbackrequest"></a>

```json
{
  "placeId": 0,
  "action": "string",
  "feedback": "string",
  "rating": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|placeId|integer(int64)|false|none||none|
|action|string|false|none||none|
|feedback|string|false|none||none|
|rating|integer|false|none||none|

<h2 id="tocS_PlaceDetailResponse">PlaceDetailResponse</h2>

<a id="schemaplacedetailresponse"></a>
<a id="schema_PlaceDetailResponse"></a>
<a id="tocSplacedetailresponse"></a>
<a id="tocsplacedetailresponse"></a>

```json
{
  "placeId": 0,
  "placeName": "string",
  "address": "string",
  "latitude": 0,
  "longitude": 0,
  "moodTags": [
    "string"
  ],
  "crowdLevel": "string",
  "bestTime": "string",
  "oneSentence": "string",
  "fullDescription": "string",
  "imageUrl": "string",
  "tips": "string",
  "yourHistory": {
    "hasVisited": true,
    "visitCount": 0,
    "lastVisited": "string",
    "yourRating": 0,
    "yourFeedback": "string"
  }
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|placeId|integer(int64)|false|none||none|
|placeName|string|false|none||none|
|address|string|false|none||none|
|latitude|number|false|none||none|
|longitude|number|false|none||none|
|moodTags|[string]|false|none||none|
|crowdLevel|string|false|none||none|
|bestTime|string|false|none||none|
|oneSentence|string|false|none||none|
|fullDescription|string|false|none||none|
|imageUrl|string|false|none||none|
|tips|string|false|none||none|
|yourHistory|[UserHistory](#schemauserhistory)|false|none||none|

<h2 id="tocS_MemoryItem">MemoryItem</h2>

<a id="schemamemoryitem"></a>
<a id="schema_MemoryItem"></a>
<a id="tocSmemoryitem"></a>
<a id="tocsmemoryitem"></a>

```json
{
  "memoryId": 0,
  "placeId": 0,
  "placeName": "string",
  "imageUrl": "string",
  "interactionType": "string",
  "rating": 0,
  "feedback": "string",
  "visitedAt": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|memoryId|integer(int64)|false|none||none|
|placeId|integer(int64)|false|none||none|
|placeName|string|false|none||none|
|imageUrl|string|false|none||none|
|interactionType|string|false|none||none|
|rating|integer|false|none||none|
|feedback|string|false|none||none|
|visitedAt|string|false|none||none|

<h2 id="tocS_ResultPlaceDetailResponse">ResultPlaceDetailResponse</h2>

<a id="schemaresultplacedetailresponse"></a>
<a id="schema_ResultPlaceDetailResponse"></a>
<a id="tocSresultplacedetailresponse"></a>
<a id="tocsresultplacedetailresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "placeId": 0,
    "placeName": "string",
    "address": "string",
    "latitude": 0,
    "longitude": 0,
    "moodTags": [
      "string"
    ],
    "crowdLevel": "string",
    "bestTime": "string",
    "oneSentence": "string",
    "fullDescription": "string",
    "imageUrl": "string",
    "tips": "string",
    "yourHistory": {
      "hasVisited": true,
      "visitCount": 0,
      "lastVisited": "string",
      "yourRating": 0,
      "yourFeedback": "string"
    }
  },
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[PlaceDetailResponse](#schemaplacedetailresponse)|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_MemoryListResponse">MemoryListResponse</h2>

<a id="schemamemorylistresponse"></a>
<a id="schema_MemoryListResponse"></a>
<a id="tocSmemorylistresponse"></a>
<a id="tocsmemorylistresponse"></a>

```json
{
  "list": [
    {
      "memoryId": 0,
      "placeId": 0,
      "placeName": "string",
      "imageUrl": "string",
      "interactionType": "string",
      "rating": 0,
      "feedback": "string",
      "visitedAt": "string"
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|list|[[MemoryItem](#schemamemoryitem)]|false|none||none|

<h2 id="tocS_ResultMemoryListResponse">ResultMemoryListResponse</h2>

<a id="schemaresultmemorylistresponse"></a>
<a id="schema_ResultMemoryListResponse"></a>
<a id="tocSresultmemorylistresponse"></a>
<a id="tocsresultmemorylistresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "list": [
      {
        "memoryId": 0,
        "placeId": 0,
        "placeName": "string",
        "imageUrl": "string",
        "interactionType": "string",
        "rating": 0,
        "feedback": "string",
        "visitedAt": "string"
      }
    ]
  },
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[MemoryListResponse](#schemamemorylistresponse)|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_TravelTipCard">TravelTipCard</h2>

<a id="schematraveltipcard"></a>
<a id="schema_TravelTipCard"></a>
<a id="tocStraveltipcard"></a>
<a id="tocstraveltipcard"></a>

```json
{
  "placeId": 0,
  "placeName": "string",
  "address": "string",
  "weatherTip": "string",
  "preparationTip": "string",
  "aiMessage": "string",
  "distanceText": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|placeId|integer(int64)|false|none||地点ID|
|placeName|string|false|none||地点名称|
|address|string|false|none||地址|
|weatherTip|string|false|none||天气提示|
|preparationTip|string|false|none||准备事项|
|aiMessage|string|false|none||AI温馨寄语|
|distanceText|string|false|none||距离文本|

<h2 id="tocS_ResultTravelTipCard">ResultTravelTipCard</h2>

<a id="schemaresulttraveltipcard"></a>
<a id="schema_ResultTravelTipCard"></a>
<a id="tocSresulttraveltipcard"></a>
<a id="tocsresulttraveltipcard"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "placeId": 0,
    "placeName": "string",
    "address": "string",
    "weatherTip": "string",
    "preparationTip": "string",
    "aiMessage": "string",
    "distanceText": "string"
  },
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[TravelTipCard](#schematraveltipcard)|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_PlaceCard">PlaceCard</h2>

<a id="schemaplacecard"></a>
<a id="schema_PlaceCard"></a>
<a id="tocSplacecard"></a>
<a id="tocsplacecard"></a>

```json
{
  "placeId": 0,
  "placeName": "string",
  "address": "string",
  "moodTags": [
    "string"
  ],
  "crowdLevel": "string",
  "oneSentence": "string",
  "imageUrl": "string",
  "distanceText": "string",
  "matchType": "string",
  "matchReason": "string",
  "lastVisited": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|placeId|integer(int64)|false|none||地点ID|
|placeName|string|false|none||地点名称|
|address|string|false|none||地点地址|
|moodTags|[string]|false|none||心情标签|
|crowdLevel|string|false|none||人流量|
|oneSentence|string|false|none||一句话推荐|
|imageUrl|string|false|none||图片URL|
|distanceText|string|false|none||距离文本|
|matchType|string|false|none||匹配类型 （收藏/历史/不感兴趣）|
|matchReason|string|false|none||匹配原因|
|lastVisited|string|false|none||最后访问时间|

<h2 id="tocS_ResultListPlaceCard">ResultListPlaceCard</h2>

<a id="schemaresultlistplacecard"></a>
<a id="schema_ResultListPlaceCard"></a>
<a id="tocSresultlistplacecard"></a>
<a id="tocsresultlistplacecard"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "placeId": 0,
      "placeName": "string",
      "address": "string",
      "moodTags": [
        "string"
      ],
      "crowdLevel": "string",
      "oneSentence": "string",
      "imageUrl": "string",
      "distanceText": "string",
      "matchType": "string",
      "matchReason": "string",
      "lastVisited": "string"
    }
  ],
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[[PlaceCard](#schemaplacecard)]|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_key">key</h2>

<a id="schemakey"></a>
<a id="schema_key"></a>
<a id="tocSkey"></a>
<a id="tocskey"></a>

```json
{}

```

### 属性

*None*

<h2 id="tocS_MapObject">MapObject</h2>

<a id="schemamapobject"></a>
<a id="schema_MapObject"></a>
<a id="tocSmapobject"></a>
<a id="tocsmapobject"></a>

```json
{
  "key": {}
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|key|object|false|none||none|

<h2 id="tocS_ResultListMapObject">ResultListMapObject</h2>

<a id="schemaresultlistmapobject"></a>
<a id="schema_ResultListMapObject"></a>
<a id="tocSresultlistmapobject"></a>
<a id="tocsresultlistmapobject"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": [
    {
      "key": {}
    }
  ],
  "timestamp": 0,
  "success": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[[MapObject](#schemamapobject)]|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|
|success|boolean|false|none||判断是否成功|

<h2 id="tocS_ResultString">ResultString</h2>

<a id="schemaresultstring"></a>
<a id="schema_ResultString"></a>
<a id="tocSresultstring"></a>
<a id="tocsresultstring"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": "string",
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|string|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_RecommendResponse">RecommendResponse</h2>

<a id="schemarecommendresponse"></a>
<a id="schema_RecommendResponse"></a>
<a id="tocSrecommendresponse"></a>
<a id="tocsrecommendresponse"></a>

```json
{
  "understanding": "string",
  "emotionMatches": [
    {
      "placeId": 0,
      "placeName": "string",
      "address": "string",
      "moodTags": [
        "string"
      ],
      "crowdLevel": "string",
      "oneSentence": "string",
      "imageUrl": "string",
      "distanceText": "string",
      "matchType": "string",
      "matchReason": "string",
      "lastVisited": "string"
    }
  ]
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|understanding|string|false|none||大模型输出的理解和分析|
|emotionMatches|[[PlaceCard](#schemaplacecard)]|false|none||心情匹配的地点|

<h2 id="tocS_ResultRecommendResponse">ResultRecommendResponse</h2>

<a id="schemaresultrecommendresponse"></a>
<a id="schema_ResultRecommendResponse"></a>
<a id="tocSresultrecommendresponse"></a>
<a id="tocsresultrecommendresponse"></a>

```json
{
  "code": 0,
  "message": "string",
  "data": {
    "understanding": "string",
    "emotionMatches": [
      {
        "placeId": 0,
        "placeName": "string",
        "address": "string",
        "moodTags": [
          "string"
        ],
        "crowdLevel": "string",
        "oneSentence": "string",
        "imageUrl": "string",
        "distanceText": "string",
        "matchType": "string",
        "matchReason": "string",
        "lastVisited": "string"
      }
    ]
  },
  "timestamp": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||状态码|
|message|string|false|none||响应消息|
|data|[RecommendResponse](#schemarecommendresponse)|false|none||响应数据|
|timestamp|integer(int64)|false|none||时间戳|

<h2 id="tocS_RecommendRequest">RecommendRequest</h2>

<a id="schemarecommendrequest"></a>
<a id="schema_RecommendRequest"></a>
<a id="tocSrecommendrequest"></a>
<a id="tocsrecommendrequest"></a>

```json
{
  "mood": "string",
  "energyLevel": 0,
  "socialLevel": 0,
  "userInput": "string",
  "userLat": 0,
  "userLng": 0,
  "enableStream": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|mood|string|false|none||心情|
|energyLevel|integer|false|none||精力条|
|socialLevel|integer|false|none||IE值|
|userInput|string|false|none||用户输入|
|userLat|number|false|none||用户纬度|
|userLng|number|false|none||用户精度|
|enableStream|boolean|false|none||是否启用流式输出（用于对话模式）|

