package com.example.corner.service.impl;

import com.example.corner.dto.PlaceCard;
import com.example.corner.dto.RecommendRequest;
import com.example.corner.dto.RecommendResponse;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {
    
    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;
    
    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;
    
    /**
     * 核心推荐
     */
    @Override
    public RecommendResponse recommend(Long userId, RecommendRequest request) {
        RecommendResponse response = new RecommendResponse();
        
        // TODO: 调用 LLM 生成 understanding（暂时使用简单逻辑）
        response.setUnderstanding("懂了，你需要安静地待一会儿");
        
        // 查询用户记忆
        List<UserPlaceMemory> memories = userPlaceMemoryRepository.findByUserId(userId);
        List<Long> memoryPlaceIds = memories.stream()
                .map(UserPlaceMemory::getPlaceId)
                .collect(Collectors.toList());
        
        List<PlaceCard> memoryMatches = new ArrayList<>();
        if (!memoryPlaceIds.isEmpty()) {
            List<PlaceEmotionLibrary> places = placeEmotionLibraryRepository.findAllById(memoryPlaceIds);
            Map<Long, PlaceEmotionLibrary> placeMap = places.stream()
                    .collect(Collectors.toMap(PlaceEmotionLibrary::getId, p -> p));
            
            for (UserPlaceMemory memory : memories) {
                PlaceEmotionLibrary place = placeMap.get(memory.getPlaceId());
                if (place != null) {
                    PlaceCard card = buildPlaceCard(place, memory, request.getUserLat(), request.getUserLng(), "visited");
                    memoryMatches.add(card);
                }
            }
        }
        
        // TODO: 如果不够3条，从情绪地图补充
        List<PlaceCard> emotionMatches = new ArrayList<>();
        
        response.setMemoryMatches(memoryMatches);
        response.setEmotionMatches(emotionMatches);
        
        return response;
    }
    
    /**
     * 构建地点卡片
     */
    private PlaceCard buildPlaceCard(PlaceEmotionLibrary place, UserPlaceMemory memory, 
                                     BigDecimal userLat, BigDecimal userLng, String matchType) {
        PlaceCard card = new PlaceCard();
        card.setPlaceId(place.getId());
        card.setPlaceName(place.getPlaceName());
        card.setAddress(place.getAddress());
        card.setCrowdLevel(place.getCrowdLevel());
        card.setOneSentence(place.getOneSentence());
        card.setImageUrl(place.getImageUrl());
        card.setMatchType(matchType);
        
        // TODO: 获取标签
        card.setMoodTags(List.of("安静", "放空"));
        
        // TODO: 生成匹配原因
        card.setMatchReason(memory.getFeedback() != null ? memory.getFeedback() : "你上次说很放松");
        
        // 设置最后访问时间
        if (memory.getVisitedAt() != null) {
            card.setLastVisited(memory.getVisitedAt().toString());
        }
        
        // 计算距离
        if (userLat != null && userLng != null && place.getLatitude() != null && place.getLongitude() != null) {
            double distance = calculateDistance(userLat.doubleValue(), userLng.doubleValue(),
                    place.getLatitude().doubleValue(), place.getLongitude().doubleValue());
            card.setDistanceText(String.format("距你%.1f公里", distance));
        } else {
            card.setDistanceText("距离未知");
        }
        
        return card;
    }
    
    /**
     * 计算两点间距离（简化版，使用 Haversine 公式）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // 地球半径（公里）
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return BigDecimal.valueOf(R * c).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
