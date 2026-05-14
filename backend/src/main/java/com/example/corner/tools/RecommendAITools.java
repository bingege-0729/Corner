package com.example.corner.tools;

import com.example.corner.entity.EmotionTagDict;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.PlaceTagRelation;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.EmotionTagDictRepository;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.PlaceTagRelationRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.vo.PlaceCard;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecommendAITools {

    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;
    
    @Autowired
    private EmotionTagDictRepository emotionTagDictRepository;
    
    @Autowired
    private PlaceTagRelationRepository placeTagRelationRepository;
    
    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;
    
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Value("${langchain4j.web-search-engine.tavily.api-key}")
    private String tavilyApiKey;
    
    @Value("${baidu.map.api-key:Fj18RcqBdN8w1lbYY7Rs32Kx6pW1Heru}")
    private String baiduMapApiKey;
    
    @Value("${image.unsplash.access-key:your_unsplash_access_key}")
    private String unsplashAccessKey;
    
    @Value("${image.pexels.api-key:your_pexels_api_key}")
    private String pexelsApiKey;
    
    @Autowired
    private OpenAiChatModel chatModel;
    
    // 推荐结果缓存Key前缀
    private static final String RECOMMEND_CACHE_KEY = "recommend_cache:";

    /**
     * 根据情绪+用户收藏+位置推荐地点（三层策略）
     * 第一层：用户去过(VISITED)和喜欢(BOOKMARKED)的地点，排除不喜欢(DISLIKED)，计算匹配分数
     * 第二层：若不足3个，从全量地点按情绪标签+距离匹配补充
     * @param mood 用户情绪
     * @param userId 用户ID
     * @param latitude 用户位置纬度
     * @param longitude 用户位置经度
     * @return 符合条件的地点列表（按优先级和距离排序，最多3个）
     */
    @Tool("getSuitablePlaceByMoodAndLocation")
    public List<PlaceCard> getSuitablePlaceBymoodAndsave(
            @P("用户情绪标签：如安静、治愈") String mood,
            @P("用户ID") Long userId,
            @P("用户位置纬度") BigDecimal latitude,
            @P("用户位置经度") BigDecimal longitude
            ){

        // 1. 查询用户所有记忆，排除DISLIKED
        List<UserPlaceMemory> allMemories = userPlaceMemoryRepository.findByUserId(userId);
        List<UserPlaceMemory> validMemories = allMemories.stream()
                .filter(memory -> !"DISLIKED".equals(memory.getInteractionType()))
                .toList();

        // 获取用户去过的地点ID列表
        List<Long> visitedPlaceIds = validMemories.stream()
                .map(UserPlaceMemory::getPlaceId)
                .distinct()
                .collect(Collectors.toList());

        List<PlaceCard> result = new ArrayList<>();

        // 2. 第一层：如果用户有记忆，计算匹配分数
        if (!validMemories.isEmpty()) {
            List<PlaceEmotionLibrary> memoryPlaces = placeEmotionLibraryRepository.findAllById(visitedPlaceIds);
            List<ScoredPlace> scoredPlaces = new ArrayList<>();

            for (UserPlaceMemory memory : validMemories) {
                PlaceEmotionLibrary place = memoryPlaces.stream()
                        .filter(p -> p.getId().equals(memory.getPlaceId()))
                        .findFirst()
                        .orElse(null);

                if (place != null) {
                    double score = calculateMatchScore(memory, place, mood);
                    scoredPlaces.add(new ScoredPlace(place, memory, score));
                }
            }

            // 按分数降序排序，取前5个
            scoredPlaces.sort((a, b) -> Double.compare(b.score, a.score));
            List<ScoredPlace> topMemories = scoredPlaces.stream()
                    .limit(5)
                    .toList();

            // 构建第一层结果（在5公里内的）
            for (ScoredPlace sp : topMemories) {
                PlaceCard card = buildPlaceCardIfNearby(sp.place, latitude, longitude, mood, true);
                if (card != null) {
                    result.add(card);
                }
            }
        }

        if (result.size() < 3) {
            // 获取所有匹配该情绪标签的地点ID
            List<EmotionTagDict> matchingTags = emotionTagDictRepository.findAll().stream()
                    .filter(tag -> tag.getTagName().equals(mood))
                    .collect(Collectors.toList());

            if (!matchingTags.isEmpty()) {
                // 获取除了去过的地点ID
                List<Long> moodMatchedPlaceIds = matchingTags.stream()
                        .flatMap(tag -> placeTagRelationRepository.findByTagId(tag.getId()).stream())
                        .map(PlaceTagRelation::getPlaceId)
                        .distinct()
                        .filter(id -> !visitedPlaceIds.contains(id)) // 排除已经去过的
                        .collect(Collectors.toList());

                // 获取该标签匹配的地点
                if (!moodMatchedPlaceIds.isEmpty()) {
                    List<PlaceEmotionLibrary> moodPlaces = placeEmotionLibraryRepository.findAllById(moodMatchedPlaceIds);

                    // 按距离排序
                    for (PlaceEmotionLibrary place : moodPlaces) {
                        PlaceCard card = buildPlaceCardIfNearby(place, latitude, longitude, mood, false);
                        if (card != null) {
                            result.add(card);
                            if (result.size() >= 3) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 5. 最终按距离排序
        result.sort((a, b) -> {
            double distA = Double.parseDouble(a.getDistanceText().replaceAll("[^0-9.]", ""));
            double distB = Double.parseDouble(b.getDistanceText().replaceAll("[^0-9.]", ""));
            return Double.compare(distA, distB);
        });


        return result;
    }
    
    /**
     * 计算匹配分数
     * - 用户评分权重 40% (1-5 分)
     * - 互动类型权重 30% (BOOKMARKED=1.0, VISITED=0.7)
     * - 标签匹配度 30% (情绪标签匹配)
     */
    private double calculateMatchScore(UserPlaceMemory memory, PlaceEmotionLibrary place, String mood) {
        // 1. 用户评分 (40%)
        double ratingScore = 0;
        if (memory.getRating() != null && memory.getRating() > 0) {
            ratingScore = (memory.getRating() / 5.0) * 40;
        }
        
        // 2. 互动类型 (30%)
        double interactionScore = 0;
        if ("BOOKMARKED".equals(memory.getInteractionType())) {
            interactionScore = 30;
        } else if ("VISITED".equals(memory.getInteractionType())) {
            interactionScore = 21; // 30 * 0.7
        }
        
        // 3. 标签匹配度 (30%)
        double tagMatchScore = 0;
        // TODO: 需要查询地点的标签，判断是否包含当前情绪标签
        List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(place.getId());
        if (!relations.isEmpty()) {
            for (PlaceTagRelation relation : relations) {
                EmotionTagDict tag = emotionTagDictRepository.findById(relation.getTagId()).orElse(null);
                if (tag != null && tag.getTagName().equals(mood)) {
                    tagMatchScore = 15;
                    break;
                }
            }
        }
        
        return ratingScore + interactionScore + tagMatchScore;
    }
    
    /**
     * 内部类：带分数的地点
     */
    private static class ScoredPlace {
        PlaceEmotionLibrary place;
        UserPlaceMemory memory;
        double score;
        
        ScoredPlace(PlaceEmotionLibrary place, UserPlaceMemory memory, double score) {
            this.place = place;
            this.memory = memory;
            this.score = score;
        }
    }
    
    /**
     * 构建地点卡片（如果在5公里内）
     * @param place 地点信息
     * @param userLat 用户纬度
     * @param userLng 用户经度
     * @param mood 情绪标签
     * @param isFromMemory 是否来自用户记忆
     * @return 地点卡片，如果超过5公里则返回null
     */
    private PlaceCard buildPlaceCardIfNearby(PlaceEmotionLibrary place, 
                                              BigDecimal userLat, 
                                              BigDecimal userLng,
                                              String mood,
                                              boolean isFromMemory) {
        if (place.getLatitude() == null || place.getLongitude() == null) {
            return null;
        }
        
        double distance = calculateDistance(
                userLat.doubleValue(), userLng.doubleValue(),
                place.getLatitude().doubleValue(), place.getLongitude().doubleValue()
        );
        
        // 只返回5000米以内的地点
        if (distance > 5.0) {
            return null;
        }
        
        PlaceCard card = getPlaceCard(mood, place, distance);
        card.setMatchType(isFromMemory ? "memory_match" : "emotion_match");
        if (isFromMemory) {
            card.setMatchReason("你去过且喜欢的地方");
        } else {
            card.setMatchReason("符合你的" + mood + "心情");
        }
        
        return card;
    }

    /**
     * 获取地点卡片信息
     * @param mood 用户情绪
     * @param place 地点信息
     * @param distance 地点距离（米）
     */
    private static @NonNull PlaceCard getPlaceCard(String mood, PlaceEmotionLibrary place, double distance) {
        PlaceCard card = new PlaceCard();
        card.setPlaceId(place.getId());
        card.setPlaceName(place.getPlaceName());
        card.setAddress(place.getAddress());
        card.setCrowdLevel(place.getCrowdLevel());
        card.setOneSentence(place.getOneSentence());
        
        // 确保图片不为空，如果数据库中没有则使用 Picsum 随机图
        String imageUrl = place.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            int seed = Math.abs(place.getPlaceName().hashCode());
            imageUrl = String.format("https://picsum.photos/seed/%d/400/300", seed);
        }
        card.setImageUrl(imageUrl);
        
        card.setDistanceText(String.format("距你%.1f公里", distance));
        card.setMatchReason("符合你的" + mood + "心情");
        return card;
    }

    /**
     * 计算两点间距离（Haversine公式）
     * @param lat1 纬度1
     * @return 距离（公里）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    @Tool("searchByVector")
    public List<PlaceCard> searchByVector(
            @P("用户输入的查询文本") String query,
            @P("用户ID")Long userId,
            @P("用户纬度") BigDecimal latitude,
            @P("用户经度") BigDecimal longitude
    ){
        //1.query转向
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        //2.向量相似度度搜索
        // 2. 向量相似度搜索 ← 这里是 search，不是 findRelevant
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(10)
                        .build()
        );
        List<EmbeddingMatch<TextSegment>> matches = result.matches();
        if(matches.isEmpty()){
            return List.of();

        }
        List<Long> placeIds = matches.stream()
                .map(m-> Long.valueOf(m.embedded().metadata().getString("placeId")))
                .collect(Collectors.toList());

        List<PlaceEmotionLibrary> places = placeEmotionLibraryRepository.findAllById(placeIds);

        //按向量相似度顺序进行排列
        Map<Long,Double> similarityMap = new LinkedHashMap<>();
        for(EmbeddingMatch<TextSegment> match:matches){
            Long placeId = Long.valueOf(match.embedded().metadata().getString("placeId"));
            similarityMap.putIfAbsent(placeId,match.score());
        }

        //构建PlaceCard
        List<PlaceCard> cards = new ArrayList<>();
        for(Long placeId: similarityMap.keySet()){
            PlaceEmotionLibrary place = places.stream()
                    .filter(p->p.getId().equals(placeId))
                    .findFirst()
                    .orElse(null);

            if(place!=null){
                double distance = calculateDistance(
                        latitude.doubleValue(),
                        longitude.doubleValue(),place.getLatitude().doubleValue(),
                        place.getLongitude().doubleValue()
                );
                if(distance<=5.0){
                    PlaceCard card = getPlaceCard(query,place,distance);
                    card.setMatchType("vector_match");
                    card.setMatchReason("与描述的相似");
                    cards.add(card);
                }
            }
        }
        return cards.stream().limit(3).collect(Collectors.toList());
    }



    @Tool("联网搜索地点，在其他工具无法满足用户需求时使用")
    public List<PlaceCard> searchWeb(
            @P("搜索关键词，应包含地点/城市信息，如'深圳安静的书店'或'北京咖啡馆'") String query,
            @P("用户纬度") BigDecimal latitude,
            @P("用户经度") BigDecimal longitude) {

        org.springframework.web.client.RestClient client = org.springframework.web.client.RestClient.create("https://api.tavily.com");

        // 构建搜索查询，确保包含地理位置信息
        String searchQuery = query;
        // 如果 query 中不包含常见城市关键词，可以添加提示
        if (!query.matches(".*[北上广深京津沪渝港澳台苏浙鲁粤].*")) {
            searchQuery = query + " 附近";
        }

        Map<String, Object> body = Map.of(
                "api_key", tavilyApiKey,
                "query", searchQuery + " 推荐 地点",
                "search_depth", "basic",
                "max_results", 3
        );

        Map<String, Object> resp = client.post()
                .uri("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<PlaceCard> cards = new ArrayList<>();
        List<Map<String, String>> results = (List<Map<String, String>>) resp.get("results");

        if (results != null) {
            for (Map<String, String> item : results) {
                PlaceCard card = new PlaceCard();
                card.setPlaceId(-1L); // 网络搜索结果无数据库ID
                
                // 提取并清理标题（转换为简体）
                String title = item.get("title");
                card.setPlaceName(title);
                
                String url = item.get("url");
                card.setAddress(url);
                
                // 优化内容摘要
                String content = item.get("content");
                card.setOneSentence(content != null && content.length() > 100 ? 
                    content.substring(0, 100) + "..." : content);
                
                card.setMatchType("web_search");
                
                // 尝试从多个来源获取图片
                String imageUrl = extractImageUrl(item, title);
                card.setImageUrl(imageUrl);
                
                // 尝试通过百度地图 Geocoding 获取距离和坐标
                GeocodingResult geoResult = geocodePlace(item.get("title"));
                if (geoResult != null) {
                    card.setDistanceText(geoResult.distanceText);
                    card.setLatitude(geoResult.latitude);
                    card.setLongitude(geoResult.longitude);
                } else {
                    card.setDistanceText("距离需自行确认");
                }
                
                // 联网查询地点注意事项
                String tips = searchPlaceTips(item.get("title"), url);
                card.setTips(tips);
                
                cards.add(card);
            }
            
            // LLM 后处理：生成个性化推荐理由
            cards = enhanceWithLLM(cards, query, latitude, longitude);
        }
        return cards;
    }
    
    /**
     * Geocoding 结果类
     */
    private static class GeocodingResult {
        String distanceText;
        BigDecimal latitude;
        BigDecimal longitude;
        
        GeocodingResult(String distanceText, BigDecimal latitude, BigDecimal longitude) {
            this.distanceText = distanceText;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
    
    /**
     * 通过百度地图 Geocoding API 获取地点坐标和距离
     */
    private GeocodingResult geocodePlace(String placeName) {
        if (placeName == null || placeName.isEmpty()) {
            return null;
        }
        
        try {
            // 调用百度地图 Geocoding API（地址解析）
            RestClient client = RestClient.create("https://api.map.baidu.com");
            String url = String.format(
                "/geocoding/v3/?address=%s&output=json&ak=%s",
                java.net.URLEncoder.encode(placeName, "UTF-8"),
                baiduMapApiKey
            );
            
            Map<String, Object> response = client.get()
                .uri(url)
                .retrieve()
                .body(Map.class);
            
            if (response != null && "0".equals(String.valueOf(response.get("status")))) {
                Map<String, Object> result = (Map<String, Object>) response.get("result");
                Map<String, Object> location = (Map<String, Object>) result.get("location");
                
                if (location != null) {
                    double lat = ((Number) location.get("lat")).doubleValue();
                    double lng = ((Number) location.get("lng")).doubleValue();
                    
                    return new GeocodingResult(
                        "距你需计算",  // 距离会在 Service 层计算
                        BigDecimal.valueOf(lat),
                        BigDecimal.valueOf(lng)
                    );
                }
            } else {
                // Geocoding 失败，静默处理
            }
        } catch (Exception e) {
            // Geocoding 异常，静默处理
        }
        
        return null;
    }
    
    /**
     * 使用 LLM 对网络搜索结果进行后处理，生成个性化推荐理由
     */
    private List<PlaceCard> enhanceWithLLM(List<PlaceCard> cards, String userQuery, 
                                            BigDecimal latitude, BigDecimal longitude) {
        if (cards.isEmpty()) {
            return cards;
        }
        
        try {
            // 构建 LLM 请求
            StringBuilder sb = new StringBuilder();
            sb.append("你是一个贴心的地点推荐助手。基于以下网络搜索结果，为每个地点生成个性化的推荐理由。\n\n");
            sb.append("【重要要求】\n");
            sb.append("- 必须使用简体中文输出，不要使用繁体中文\n");
            sb.append("- 不要出现乱码或特殊符号\n");
            sb.append("- 保持语言简洁、温暖、有亲和力\n\n");
            sb.append("用户需求：").append(userQuery).append("\n");
            sb.append("用户位置：纬度").append(latitude).append(", 经度").append(longitude).append("\n\n");
            sb.append("搜索结果：\n");
            
            for (int i = 0; i < cards.size(); i++) {
                PlaceCard card = cards.get(i);
                sb.append(i + 1).append(". ").append(card.getPlaceName())
                  .append(" - ").append(card.getOneSentence()).append("\n");
            }
            
            sb.append("\n请为每个地点生成一个简短的推荐理由（30字以内），说明为什么这个地点适合用户的需求。");
            sb.append("\n返回格式：每行一个推荐理由，与上述地点顺序对应。\n");
            
            String prompt = sb.toString();
            
            // 调用 LLM
            String response = chatModel.chat(prompt);
            
            // 解析 LLM 返回的推荐理由
            String[] reasons = response.split("\n");
            for (int i = 0; i < Math.min(reasons.length, cards.size()); i++) {
                String reason = reasons[i].trim();
                if (!reason.isEmpty()) {
                    cards.get(i).setMatchReason(reason);
                }
            }
            
        } catch (Exception e) {
            // LLM 处理失败，使用默认推荐理由
            for (PlaceCard card : cards) {
                if (card.getMatchReason() == null || card.getMatchReason().isEmpty()) {
                    card.setMatchReason("根据您的需求和位置，从网络搜索到的推荐");
                }
            }
        }
        
        return cards;
    }
    
    /**
     * 联网搜索地点注意事项/贴士
     */
    private String searchPlaceTips(String placeName, String url) {
        try {
            RestClient client = RestClient.create("https://api.tavily.com");
            
            // 构建搜索查询，强调当地实际情况和实用信息
            String query = placeName + " 实地游玩攻略 注意事项 当地特色 温馨提示 简体中文";
            
            Map<String, Object> body = Map.of(
                "api_key", tavilyApiKey,
                "query", query,
                "search_depth", "advanced", // 使用深度搜索获取更详细的信息
                "max_results", 3
            );
            
            Map<String, Object> resp = client.post()
                .uri("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
            
            List<Map<String, String>> results = (List<Map<String, String>>) resp.get("results");
            
            if (results != null && !results.isEmpty()) {
                // 合并前2个结果的内容，获取更全面的信息
                StringBuilder combinedContent = new StringBuilder();
                int count = 0;
                for (Map<String, String> result : results) {
                    String content = result.get("content");
                    if (content != null && !content.isEmpty()) {
                        combinedContent.append(content).append(" ");
                        count++;
                        if (count >= 2) break; // 只取前2个
                    }
                }
                
                if (combinedContent.length() > 0) {
                    String fullContent = combinedContent.toString();
                    // 截取前200字作为提示（稍微长一点，包含更多实用信息）
                    return fullContent.length() > 200 ? 
                        fullContent.substring(0, 200) + "..." : fullContent;
                }
            }
        } catch (Exception e) {
            // 搜索失败，返回默认提示
        }
        
        return "建议提前了解开放时间和相关规定，祝您旅途愉快 🌟";
    }
    
    /**
     * 从多个来源提取地点图片 URL
     * 优先级：Tavily > Unsplash > Pexels > Picsum 随机图
     * 保证每个地点都有图片返回
     */
    private String extractImageUrl(Map<String, String> item, String placeName) {
        // 1. 尝试从 Tavily 结果中获取 img_src（最稳定）
        String imageUrl = item.get("img_src");
        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            return imageUrl;
        }
        
        // 2. 尝试从 Tavily 结果的其他字段获取图片
        imageUrl = item.get("image");
        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            return imageUrl;
        }
        
        // 3. 尝试 Unsplash API（高质量免费图片）
        imageUrl = searchImageFromUnsplash(placeName);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        
        // 4. 尝试 Pexels API（另一个高质量图片源）
        imageUrl = searchImageFromPexels(placeName);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        
        // 5. 使用 Picsum Photos 生成稳定的随机图片（保证有图片）
        return generateFallbackImage(placeName);
    }
    
    /**
     * 生成兜底图片（使用 Picsum Photos）
     * 保证每个地点都有图片返回
     */
    private String generateFallbackImage(String placeName) {
        try {
            // 使用地点名称的 hashCode 作为种子，确保同一地点总是返回相同图片
            int seed = Math.abs(placeName.hashCode());
            return String.format("https://picsum.photos/seed/%d/400/300", seed);
        } catch (Exception e) {
            // 极端情况下，返回一个固定的默认图片 URL
            return "https://via.placeholder.com/400x300/4A90E2/FFFFFF?text=地点图片";
        }
    }
    
    /**
     * 从网页 meta 标签中提取 Open Graph 图片
     */
    private String extractImageFromUrl(String url) {
        try {
            // 不直接获取整个网页，避免响应头过大问题
            // 这个方法暂时跳过，直接使用其他图片源
            return null;
        } catch (Exception e) {
            // 提取失败，返回 null
        }
        return null;
    }
    
    /**
     * 从 Unsplash 搜索图片（高质量免费图片）
     * 需要申请 API Key: https://unsplash.com/developers
     */
    private String searchImageFromUnsplash(String placeName) {
        // 检查 API Key 是否配置
        if ("your_unsplash_access_key".equals(unsplashAccessKey)) {
            return null; // 未配置，跳过
        }
        
        try {
            RestClient client = RestClient.create("https://api.unsplash.com");
            String url = String.format(
                "/search/photos?query=%s&per_page=1&orientation=landscape",
                java.net.URLEncoder.encode(placeName + " place location", "UTF-8")
            );
            
            Map<String, Object> response = client.get()
                .uri(url)
                .header("Authorization", "Client-ID " + unsplashAccessKey)
                .retrieve()
                .body(Map.class);
            
            if (response != null) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                if (results != null && !results.isEmpty()) {
                    Map<String, Object> firstResult = results.get(0);
                    Map<String, Object> urls = (Map<String, Object>) firstResult.get("urls");
                    if (urls != null) {
                        return (String) urls.get("regular");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Unsplash 搜索失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 从 Pexels 搜索图片（另一个高质量免费图片源）
     * 需要申请 API Key: https://www.pexels.com/api/
     */
    private String searchImageFromPexels(String placeName) {
        // 检查 API Key 是否配置
        if ("your_pexels_api_key".equals(pexelsApiKey)) {
            return null; // 未配置，跳过
        }
        
        try {
            RestClient client = RestClient.create("https://api.pexels.com");
            String url = String.format(
                "/v1/search?query=%s&per_page=1&orientation=landscape",
                java.net.URLEncoder.encode(placeName + " place", "UTF-8")
            );
            
            Map<String, Object> response = client.get()
                .uri(url)
                .header("Authorization", pexelsApiKey)
                .retrieve()
                .body(Map.class);
            
            if (response != null) {
                List<Map<String, Object>> photos = (List<Map<String, Object>>) response.get("photos");
                if (photos != null && !photos.isEmpty()) {
                    Map<String, Object> firstPhoto = photos.get(0);
                    Map<String, Object> src = (Map<String, Object>) firstPhoto.get("src");
                    if (src != null) {
                        return (String) src.get("medium");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Pexels 搜索失败: " + e.getMessage());
        }
        return null;
    }
}
