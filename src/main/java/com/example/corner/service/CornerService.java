package com.example.corner.service;

import com.example.corner.dto.*;
import com.example.corner.entity.*;
import com.example.corner.repository.*;
import com.example.corner.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CornerService {
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Autowired
    private EmotionTagDictRepository emotionTagDictRepository;
    
    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;
    
    @Autowired
    private PlaceTagRelationRepository placeTagRelationRepository;
    
    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 手机号登录（自动注册）
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        UserInfo user = userInfoRepository.findByPhone(request.getPhone()).orElse(null);
        
        if (user == null) {
            user = new UserInfo();
            user.setPhone(request.getPhone());
            user.setNickname("用户" + request.getPhone().substring(7));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userInfoRepository.save(user);
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setToken(token);
        
        return response;
    }
    
    /**
     * 获取全部标签
     */
    public List<TagResponse> getAllTags() {
        List<EmotionTagDict> tags = emotionTagDictRepository.findAllByOrderByCategoryAscIdAsc();
        return tags.stream().map(tag -> {
            TagResponse response = new TagResponse();
            response.setId(tag.getId());
            response.setTagName(tag.getTagName());
            response.setCategory(tag.getCategory());
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * 核心推荐
     */
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
     * 推荐反馈
     */
    @Transactional
    public void feedback(Long userId, FeedbackRequest request) {
        UserPlaceMemory memory = userPlaceMemoryRepository
                .findByUserIdAndPlaceId(userId, request.getPlaceId())
                .orElse(new UserPlaceMemory());
        
        memory.setUserId(userId);
        memory.setPlaceId(request.getPlaceId());
        
        // 转换 action 到 interaction_type
        String interactionType;
        switch (request.getAction()) {
            case "liked":
                interactionType = "BOOKMARKED";
                break;
            case "disliked":
                interactionType = "DISLIKED";
                break;
            case "visited":
                interactionType = "VISITED";
                break;
            default:
                interactionType = "VISITED";
        }
        
        memory.setInteractionType(interactionType);
        memory.setRating(request.getRating());
        memory.setFeedback(request.getFeedback());
        memory.setVisitedAt(LocalDate.now());
        
        if (memory.getId() == null) {
            memory.setCreatedAt(LocalDateTime.now());
        }
        memory.setUpdatedAt(LocalDateTime.now());
        
        userPlaceMemoryRepository.save(memory);
    }
    
    /**
     * 我的记忆列表
     */
    public MemoryListResponse getMemoryList(Long userId, String type) {
        List<UserPlaceMemory> memories;
        
        if (type != null && !type.isEmpty()) {
            String interactionType;
            if ("visited".equals(type)) {
                interactionType = "VISITED";
            } else if ("bookmarked".equals(type)) {
                interactionType = "BOOKMARKED";
            } else {
                interactionType = null;
            }
            
            if (interactionType != null) {
                memories = userPlaceMemoryRepository.findByUserIdAndInteractionType(userId, interactionType);
            } else {
                memories = userPlaceMemoryRepository.findByUserId(userId);
            }
        } else {
            memories = userPlaceMemoryRepository.findByUserId(userId);
        }
        
        List<Long> placeIds = memories.stream()
                .map(UserPlaceMemory::getPlaceId)
                .collect(Collectors.toList());
        
        List<PlaceEmotionLibrary> places = placeEmotionLibraryRepository.findAllById(placeIds);
        Map<Long, PlaceEmotionLibrary> placeMap = places.stream()
                .collect(Collectors.toMap(PlaceEmotionLibrary::getId, p -> p));
        
        List<MemoryItem> items = memories.stream().map(memory -> {
            PlaceEmotionLibrary place = placeMap.get(memory.getPlaceId());
            MemoryItem item = new MemoryItem();
            item.setMemoryId(memory.getId());
            item.setPlaceId(memory.getPlaceId());
            item.setPlaceName(place != null ? place.getPlaceName() : "");
            item.setImageUrl(place != null ? place.getImageUrl() : "");
            item.setInteractionType(memory.getInteractionType());
            item.setRating(memory.getRating());
            item.setFeedback(memory.getFeedback());
            item.setVisitedAt(memory.getVisitedAt() != null ? memory.getVisitedAt().toString() : "");
            return item;
        }).collect(Collectors.toList());
        
        MemoryListResponse response = new MemoryListResponse();
        response.setList(items);
        
        return response;
    }
    
    /**
     * 地点详情
     */
    public PlaceDetailResponse getPlaceDetail(Long userId, Long placeId) {
        PlaceEmotionLibrary place = placeEmotionLibraryRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("地点不存在"));
        
        // 获取标签
        List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(placeId);
        List<Long> tagIds = relations.stream()
                .map(PlaceTagRelation::getTagId)
                .collect(Collectors.toList());
        List<EmotionTagDict> tags = emotionTagDictRepository.findAllById(tagIds);
        List<String> moodTags = tags.stream()
                .map(EmotionTagDict::getTagName)
                .collect(Collectors.toList());
        
        // 获取用户历史
        Optional<UserPlaceMemory> memoryOpt = userPlaceMemoryRepository.findByUserIdAndPlaceId(userId, placeId);
        UserHistory userHistory = new UserHistory();
        if (memoryOpt.isPresent()) {
            UserPlaceMemory memory = memoryOpt.get();
            userHistory.setHasVisited(true);
            userHistory.setVisitCount(1); // TODO: 统计访问次数
            userHistory.setLastVisited(memory.getVisitedAt() != null ? memory.getVisitedAt().toString() : "");
            userHistory.setYourRating(memory.getRating());
            userHistory.setYourFeedback(memory.getFeedback());
        } else {
            userHistory.setHasVisited(false);
            userHistory.setVisitCount(0);
        }
        
        PlaceDetailResponse response = new PlaceDetailResponse();
        response.setPlaceId(place.getId());
        response.setPlaceName(place.getPlaceName());
        response.setAddress(place.getAddress());
        response.setLatitude(place.getLatitude());
        response.setLongitude(place.getLongitude());
        response.setMoodTags(moodTags);
        response.setCrowdLevel(place.getCrowdLevel());
        response.setBestTime(place.getBestTime());
        response.setOneSentence(place.getOneSentence());
        response.setFullDescription(place.getFullDescription());
        response.setImageUrl(place.getImageUrl());
        response.setTips(place.getTips());
        response.setYourHistory(userHistory);
        
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
        card.setMoodTags(Arrays.asList("安静", "放空"));
        
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
