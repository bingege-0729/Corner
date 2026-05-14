package com.example.corner.tools;

import com.example.corner.dto.PlaceCard;
import com.example.corner.entity.EmotionTagDict;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.PlaceTagRelation;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.EmotionTagDictRepository;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.PlaceTagRelationRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
        
        // 6. 限制最多返回3个
        return result.stream().limit(3).collect(Collectors.toList());
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
        card.setImageUrl(place.getImageUrl());
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

}
