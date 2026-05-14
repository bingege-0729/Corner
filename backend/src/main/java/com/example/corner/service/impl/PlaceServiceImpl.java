package com.example.corner.service.impl;

import com.example.corner.entity.EmotionTagDict;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.PlaceTagRelation;
import com.example.corner.entity.UserInfo;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.EmotionTagDictRepository;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.PlaceTagRelationRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.repository.UserInfoRepository;
import com.example.corner.service.PlaceService;
import com.example.corner.vo.PlaceCard;
import com.example.corner.vo.PlaceDetailResponse;
import com.example.corner.vo.TravelTipCard;
import com.example.corner.vo.UserHistory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlaceServiceImpl implements PlaceService {
    
    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;
    
    @Autowired
    private PlaceTagRelationRepository placeTagRelationRepository;
    
    @Autowired
    private EmotionTagDictRepository emotionTagDictRepository;
    
    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Value("${qweather.api.key}")
    private String qWeatherApiKey;
    
    @Value("${qweather.api.weather-url}")
    private String qWeatherUrl;
    
    @Value("${qweather.api.geo-api-url}")
    private String qGeoApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 地点详情
     **/
    @Override
    public PlaceDetailResponse getPlaceDetail(Long userId, Long placeId) {
        // 处理网络搜索地点（无数据库ID）
        if (placeId == null || placeId == -1L) {
            throw new RuntimeException("该地点为网络搜索结果，暂无详细信息");
        }

        PlaceEmotionLibrary place = placeEmotionLibraryRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("地点不存在，ID: " + placeId));

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
     * 生成出行温馨提示
     */
    @Override
    public TravelTipCard generateTravelTips(Long userId, Long placeId) {
        // 处理网络搜索地点（无数据库ID）
        if (placeId == null || placeId == -1L) {
            throw new RuntimeException("该地点为网络搜索结果，无法生成出行提示");
        }

        // 1. 获取地点信息
        PlaceEmotionLibrary place = placeEmotionLibraryRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("地点不存在，ID: " + placeId));
        
        // 2. 获取用户当前位置
        UserInfo user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 3. 计算距离
        String distanceText = "未知";
        if (user.getLatitude() != null && user.getLongitude() != null 
                && place.getLatitude() != null && place.getLongitude() != null) {
            double distance = calculateDistance(
                    user.getLatitude().doubleValue(), user.getLongitude().doubleValue(),
                    place.getLatitude().doubleValue(), place.getLongitude().doubleValue()
            );
            distanceText = String.format("%.1f公里", distance);
        }
        
        // 4. 构建提示卡片
        TravelTipCard tipCard = new TravelTipCard();
        tipCard.setPlaceId(place.getId());
        tipCard.setPlaceName(place.getPlaceName());
        tipCard.setAddress(place.getAddress());
        tipCard.setDistanceText(distanceText);
        
        // 5. 生成天气提示（调用和风天气API）
        tipCard.setWeatherTip(generateWeatherTip(user));
        
        // 6. 生成准备事项提示
        tipCard.setPreparationTip(generatePreparationTip(place.getTips()));
        
        // 7. 生成AI温馨寄语
        tipCard.setAiMessage(generateAiMessage(place, LocalDateTime.now()));
        
        return tipCard;
    }
    
    /**
     * 生成天气提示（调用和风天气API）
     */
    private String generateWeatherTip(UserInfo user) {
        try {
            // 获取用户所在城市
            String city = null;
            if (user.getLatitude() != null && user.getLongitude() != null) {
                // 通过经纬度获取城市名称
                city = getCityByLocation(user.getLatitude().doubleValue(), user.getLongitude().doubleValue());
            }
            
            // 如果无法获取城市，返回默认提示
            if (city == null || city.isEmpty()) {
                return "建议出发前查看天气预报，做好相应准备 ☀️";
            }
            
            // 调用和风天气API获取实时天气
            Map<String, Object> weatherInfo = getWeatherByCity(city);
            
            if (Boolean.TRUE.equals(weatherInfo.get("success"))) {
                String weather = (String) weatherInfo.get("weather");
                String temperature = (String) weatherInfo.get("temperature");
                String windDir = (String) weatherInfo.get("windDir");
                String windScale = (String) weatherInfo.get("windScale");
                
                // 构建天气提示
                StringBuilder tip = new StringBuilder();
                tip.append(String.format("%s今天%s，气温%s°C，%s%s级。", city, weather, temperature, windDir, windScale));
                
                // 根据天气给出建议
                if (weather.contains("雨")) {
                    tip.append("记得带伞哦 ☔");
                } else if (weather.contains("雪")) {
                    tip.append("注意保暖防滑 ❄️");
                } else if (weather.contains("晴")) {
                    tip.append("适合出行，注意防晒 ☀️");
                } else if (weather.contains("云")) {
                    tip.append("天气舒适，愉快出行吧 🌤️");
                } else if (weather.contains("雷")) {
                    tip.append("雷雨天气，注意安全 ⚡");
                } else if (weather.contains("雾")) {
                    tip.append("能见度较低，小心慢行 🌫️");
                } else {
                    tip.append("祝您旅途愉快 🌟");
                }
                
                return tip.toString();
            } else {
                // API调用失败，返回默认提示
                return "建议出发前查看天气预报，做好相应准备 ☀️";
            }
        } catch (Exception e) {
            // 异常情况下返回默认提示
            return "建议出发前查看天气预报，做好相应准备 ☀️";
        }
    }
    
    /**
     * 根据经纬度获取城市名称
     */
    private String getCityByLocation(double latitude, double longitude) {
        try {
            String url = String.format("%s?key=%s&location=%s,%s",
                    qGeoApiUrl, qWeatherApiKey, longitude, latitude);
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            
            String code = rootNode.get("code").asText();
            if ("200".equals(code)) {
                JsonNode locationNode = rootNode.get("location");
                if (locationNode != null && locationNode.isArray() && locationNode.size() > 0) {
                    return locationNode.get(0).get("name").asText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据城市名称获取天气信息
     */
    private Map<String, Object> getWeatherByCity(String city) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 先获取城市ID
            String locationId = getLocationId(city);
            if (locationId == null) {
                result.put("success", false);
                result.put("error", "未找到城市: " + city);
                return result;
            }
            
            // 2. 根据城市ID获取天气
            String url = String.format("%s?key=%s&location=%s",
                    qWeatherUrl, qWeatherApiKey, locationId);
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            
            String code = rootNode.get("code").asText();
            if ("200".equals(code)) {
                JsonNode nowNode = rootNode.get("now");
                
                String temp = nowNode.get("temp").asText();           // 温度
                String text = nowNode.get("text").asText();           // 天气状况
                String windDir = nowNode.get("windDir").asText();     // 风向
                String windScale = nowNode.get("windScale").asText(); // 风力等级
                
                result.put("success", true);
                result.put("city", city);
                result.put("temperature", temp);
                result.put("weather", text);
                result.put("windDir", windDir);
                result.put("windScale", windScale);
            } else {
                result.put("success", false);
                result.put("error", "天气API返回错误码: " + code);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "解析天气数据失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据城市名称获取Location ID
     */
    private String getLocationId(String city) {
        try {
            String url = String.format("%s?key=%s&location=%s",
                    qGeoApiUrl, qWeatherApiKey, city);
            
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            
            String code = rootNode.get("code").asText();
            if ("200".equals(code)) {
                JsonNode locationNode = rootNode.get("location");
                if (locationNode != null && locationNode.isArray() && locationNode.size() > 0) {
                    return locationNode.get(0).get("id").asText();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 生成准备事项提示
     */
    private String generatePreparationTip(String tips) {
        if (tips == null || tips.isEmpty()) {
            return "无需特殊准备，轻松出发即可";
        }
        return "温馨提示：" + tips;
    }
    
    /**
     * 生成AI温馨寄语
     */
    private String generateAiMessage(PlaceEmotionLibrary place, LocalDateTime now) {
        int hour = now.getHour();
        String timeGreeting;
        
        if (hour < 6) {
            timeGreeting = "夜深了";
        } else if (hour < 9) {
            timeGreeting = "早上好";
        } else if (hour < 12) {
            timeGreeting = "上午好";
        } else if (hour < 14) {
            timeGreeting = "中午好";
        } else if (hour < 18) {
            timeGreeting = "下午好";
        } else if (hour < 22) {
            timeGreeting = "晚上好";
        } else {
            timeGreeting = "夜深了";
        }
        
        return String.format("%s！%s是一个不错的选择，希望你能在那里找到属于自己的角落 🌟",
                timeGreeting, place.getPlaceName());
    }
    
    /**
     * 计算两点间距离（Haversine公式）
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
    
    /**
     * 切换收藏状态（收藏/取消收藏）
     */
    @Override
    @Transactional
    public void toggleBookmark(Long userId, Long placeId, PlaceCard placeCard) {
        Long targetPlaceId = placeId;

        // 1. 处理外部地点入库（如果是联网搜索出来的地点，其ID为-1）
        if (placeId == -1L && placeCard != null) {
            // 先尝试根据名字和地址查找是否已存在（避免重复入库）
            Optional<PlaceEmotionLibrary> existingPlace = placeEmotionLibraryRepository
                    .findAll().stream()
                    .filter(p -> p.getPlaceName().equals(placeCard.getPlaceName()) && p.getAddress().equals(placeCard.getAddress()))
                    .findFirst();

            if (existingPlace.isPresent()) {
                targetPlaceId = existingPlace.get().getId();
            } else {
                // 入库新地点
                PlaceEmotionLibrary newPlace = new PlaceEmotionLibrary();
                newPlace.setPlaceName(placeCard.getPlaceName());
                newPlace.setAddress(placeCard.getAddress());
                newPlace.setOneSentence(placeCard.getOneSentence());
                newPlace.setImageUrl(placeCard.getImageUrl());
                newPlace.setCrowdLevel(placeCard.getCrowdLevel());
                newPlace.setLatitude(placeCard.getLatitude());
                newPlace.setLongitude(placeCard.getLongitude());
                newPlace.setTips("来自AI推荐的外部地点");
                // 保存并获取真实ID
                newPlace = placeEmotionLibraryRepository.save(newPlace);
                targetPlaceId = newPlace.getId();
            }
        }

        // 2. 正常执行收藏/取消收藏逻辑
        Optional<UserPlaceMemory> memoryOpt = userPlaceMemoryRepository
                .findByUserIdAndPlaceId(userId, targetPlaceId);
        
        if (memoryOpt.isPresent()) {
            UserPlaceMemory memory = memoryOpt.get();
            if ("BOOKMARKED".equals(memory.getInteractionType())) {
                userPlaceMemoryRepository.delete(memory);
            } else {
                memory.setInteractionType("BOOKMARKED");
                memory.setUpdatedAt(LocalDateTime.now());
                userPlaceMemoryRepository.save(memory);
            }
        } else {
            UserPlaceMemory memory = new UserPlaceMemory();
            memory.setUserId(userId);
            memory.setPlaceId(targetPlaceId);
            memory.setInteractionType("BOOKMARKED");
            memory.setVisitedAt(LocalDate.now());
            memory.setCreatedAt(LocalDateTime.now());
            memory.setUpdatedAt(LocalDateTime.now());
            userPlaceMemoryRepository.save(memory);
        }
    }
    
    /**
     * 获取用户收藏的地点列表
     */
    @Override
    public List<PlaceCard> getBookmarkedPlaces(Long userId) {
        // 查询所有收藏的地点
        List<UserPlaceMemory> bookmarkedMemories = userPlaceMemoryRepository
                .findByUserIdAndInteractionType(userId, "BOOKMARKED");
        
        // 获取地点ID列表
        List<Long> placeIds = bookmarkedMemories.stream()
                .map(UserPlaceMemory::getPlaceId)
                .collect(Collectors.toList());
        
        if (placeIds.isEmpty()) {
            return List.of();
        }
        
        // 查询地点详情
        List<PlaceEmotionLibrary> places = placeEmotionLibraryRepository.findAllById(placeIds);
        
        // 构建 PlaceCard 列表
        return places.stream().map(place -> {
            PlaceCard card = new PlaceCard();
            card.setPlaceId(place.getId());
            card.setPlaceName(place.getPlaceName());
            card.setAddress(place.getAddress());
            card.setImageUrl(place.getImageUrl());
            card.setOneSentence(place.getOneSentence());
            card.setCrowdLevel(place.getCrowdLevel());
            
            // 获取地点的情绪标签
            List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(place.getId());
            List<String> moodTags = relations.stream()
                    .map(relation -> emotionTagDictRepository.findById(relation.getTagId()))
                    .filter(Optional::isPresent)
                    .map(opt -> opt.get().getTagName())
                    .collect(Collectors.toList());
            card.setMoodTags(moodTags);
            
            return card;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取用户去过的地点及对应心情（用于地图展示）
     */
    @Override
    public List<Map<String, Object>> getVisitedPlacesWithMood(Long userId) {
        // 1. 查询所有去过的地点记录（interaction_type = 'VISITED'）
        List<UserPlaceMemory> visitedMemories = userPlaceMemoryRepository
                .findByUserIdAndInteractionType(userId, "VISITED");
        
        if (visitedMemories.isEmpty()) {
            return List.of();
        }
        
        // 2. 构建结果列表
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (UserPlaceMemory memory : visitedMemories) {
            // 获取地点信息
            Optional<PlaceEmotionLibrary> placeOpt = placeEmotionLibraryRepository.findById(memory.getPlaceId());
            if (placeOpt.isEmpty()) {
                continue;
            }
            
            PlaceEmotionLibrary place = placeOpt.get();
            
            // 获取该地点的主要情绪标签（取第一个）
            List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(place.getId());
            String moodTag = null;
            if (!relations.isEmpty()) {
                Optional<EmotionTagDict> tagOpt = emotionTagDictRepository.findById(relations.get(0).getTagId());
                moodTag = tagOpt.map(EmotionTagDict::getTagName).orElse(null);
            }
            
            // 构建地图展示数据
            Map<String, Object> placeData = new HashMap<>();
            placeData.put("placeId", place.getId());
            placeData.put("placeName", place.getPlaceName());
            placeData.put("latitude", place.getLatitude());
            placeData.put("longitude", place.getLongitude());
            placeData.put("moodTag", moodTag); // 当时的心情标签
            placeData.put("visitedAt", memory.getVisitedAt() != null ? memory.getVisitedAt().toString() : null);
            placeData.put("address", place.getAddress());
            
            result.add(placeData);
        }
        
        return result;
    }
    /**
     * 获取所有地点（包含标签信息）
     */
    @Override
    public List<PlaceEmotionLibrary> getAllPlacesWithTags() {
        return placeEmotionLibraryRepository.findAll();
    }
}
