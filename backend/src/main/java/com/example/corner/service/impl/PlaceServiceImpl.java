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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Slf4j
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
    
    @Value("${accuweather.api.key}")
    private String accuWeatherApiKey;
    
    @Value("${accuweather.api.current-url}")
    private String accuWeatherCurrentUrl;
    
    @Value("${accuweather.api.locations-url}")
    private String accuWeatherLocationsUrl;
    
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
        
        // 5. 生成天气提示（调用AccuWeather API）
        tipCard.setWeatherTip(generateWeatherTip(user));
        
        // 6. 生成准备事项提示
        tipCard.setPreparationTip(generatePreparationTip(place.getTips()));
        
        // 7. 生成AI温馨寄语
        tipCard.setAiMessage(generateAiMessage(place, LocalDateTime.now()));
        
        return tipCard;
    }
    
    /**
     * 生成天气提示（调用AccuWeather API）
     */
    private String generateWeatherTip(UserInfo user) {
        try {
            // 检查 API Key
            if (accuWeatherApiKey == null || accuWeatherApiKey.isEmpty()) {
                log.warn("AccuWeather API Key 未配置，返回默认提示");
                return "建议出发前查看天气预报，做好相应准备 ☀️";
            }
            
            if (user.getLatitude() == null || user.getLongitude() == null) {
                log.warn("用户没有经纬度信息");
                return "建议出发前查看天气预报，做好相应准备 ☀️";
            }
            
            // 调用AccuWeather API获取实时天气
            Map<String, Object> weatherInfo = getWeatherByLocation(
                user.getLatitude().doubleValue(), 
                user.getLongitude().doubleValue()
            );
            
            if (Boolean.TRUE.equals(weatherInfo.get("success"))) {
                String city = (String) weatherInfo.get("city");
                String weather = (String) weatherInfo.get("weather");
                String temperature = (String) weatherInfo.get("temperature");
                
                // 构建天气提示
                StringBuilder tip = new StringBuilder();
                tip.append(String.format("%s今天%s，气温%s°C。", city, weather, temperature));
                
                // 根据天气给出建议
                if (weather.contains("雨") || weather.contains("Rain")) {
                    tip.append("记得带伞哦 ☔");
                } else if (weather.contains("雪") || weather.contains("Snow")) {
                    tip.append("注意保暖防滑 ❄️");
                } else if (weather.contains("晴") || weather.contains("Sun") || weather.contains("Clear")) {
                    tip.append("适合出行，注意防晒 ☀️");
                } else if (weather.contains("云") || weather.contains("Cloud")) {
                    tip.append("天气舒适，愉快出行吧 🌤️");
                } else if (weather.contains("雷") || weather.contains("Thunder")) {
                    tip.append("雷雨天气，注意安全 ⚡");
                } else if (weather.contains("雾") || weather.contains("Fog")) {
                    tip.append("能见度较低，小心慢行 🌫️");
                } else {
                    tip.append("祝您旅途愉快 🌟");
                }
                
                return tip.toString();
            } else {
                log.warn("获取天气失败: {}", weatherInfo.get("error"));
                return "建议出发前查看天气预报，做好相应准备 ☀️";
            }
        } catch (Exception e) {
            log.error("生成天气提示异常", e);
            return "建议出发前查看天气预报，做好相应准备 ☀️";
        }
    }
    
    /**
     * 根据经纬度获取天气信息（AccuWeather API）
     */
    private Map<String, Object> getWeatherByLocation(double latitude, double longitude) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 先通过经纬度获取 Location Key
            String locationUrl = String.format("%s?q=%s,%s&apikey=%s",
                    accuWeatherLocationsUrl, latitude, longitude, accuWeatherApiKey);
            
            log.debug("调用 AccuWeather Locations API: {}", locationUrl.replace(accuWeatherApiKey, "***"));
            
            String locationResponse = restTemplate.getForObject(locationUrl, String.class);
            JsonNode locationNode = objectMapper.readTree(locationResponse);
            
            if (locationNode.isArray() && locationNode.size() > 0) {
                String locationKey = locationNode.get(0).get("Key").asText();
                String cityName = locationNode.get(0).get("LocalizedName").asText();
                
                log.info("获取到 Location Key: {}, 城市: {}", locationKey, cityName);
                
                // 2. 通过 Location Key 获取当前天气
                String weatherUrl = String.format("%s%s?apikey=%s&details=true",
                        accuWeatherCurrentUrl, locationKey, accuWeatherApiKey);
                
                log.debug("调用 AccuWeather Current Conditions API");
                
                String weatherResponse = restTemplate.getForObject(weatherUrl, String.class);
                JsonNode weatherArray = objectMapper.readTree(weatherResponse);
                
                if (weatherArray.isArray() && weatherArray.size() > 0) {
                    JsonNode current = weatherArray.get(0);
                    
                    // 温度（摄氏度）
                    String temp = current.get("Temperature").get("Metric").get("Value").asText() + "°C";
                    
                    // 天气描述
                    String weatherText = current.get("WeatherText").asText();
                    
                    // 是否有降水
                    boolean hasPrecipitation = current.get("HasPrecipitation").asBoolean();
                    String precipitationType = "";
                    if (hasPrecipitation) {
                        precipitationType = current.get("PrecipitationType").asText();
                    }
                    
                    result.put("success", true);
                    result.put("city", cityName);
                    result.put("temperature", temp);
                    result.put("weather", weatherText);
                    result.put("hasPrecipitation", hasPrecipitation);
                    result.put("precipitationType", precipitationType);
                    
                    log.info("成功获取天气: {} {}, {}", cityName, weatherText, temp);
                } else {
                    result.put("success", false);
                    result.put("error", "未获取到天气数据");
                }
            } else {
                result.put("success", false);
                result.put("error", "未找到位置信息");
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("AccuWeather API 请求失败: HTTP {}", e.getStatusCode());
            result.put("success", false);
            result.put("error", "API请求失败: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("获取天气信息失败", e);
            result.put("success", false);
            result.put("error", "解析天气数据失败: " + e.getMessage());
        }
        
        return result;
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

    @Override
    public void recordExploration(Long userId, Long placeId, PlaceCard placeCard) {
        Long targetPlaceId = placeId;

        // 1. 如果是外部地点（placeId = -1），先持久化
        if (placeId == -1L && placeCard != null) {
            // 检查是否已经存在同名地点
            Optional<PlaceEmotionLibrary> existing = placeEmotionLibraryRepository
                    .findAll().stream()
                    .filter(p -> p.getPlaceName().equals(placeCard.getPlaceName()))
                    .findFirst();

            if (existing.isPresent()) {
                targetPlaceId = existing.get().getId();
            } else {
                PlaceEmotionLibrary newPlace = new PlaceEmotionLibrary();
                newPlace.setPlaceName(placeCard.getPlaceName());
                newPlace.setAddress(placeCard.getAddress());
                newPlace.setOneSentence(placeCard.getOneSentence());
                newPlace.setImageUrl(placeCard.getImageUrl());
                newPlace.setCrowdLevel(placeCard.getCrowdLevel());
                newPlace.setLatitude(placeCard.getLatitude());
                newPlace.setLongitude(placeCard.getLongitude());
                newPlace.setTips("来自AI推荐的外部地点");
                newPlace = placeEmotionLibraryRepository.save(newPlace);
                targetPlaceId = newPlace.getId();
            }
        }

        // 2. 检查记录是否存在
        Optional<UserPlaceMemory> memoryOpt = userPlaceMemoryRepository
                .findByUserIdAndPlaceId(userId, targetPlaceId);

        if (memoryOpt.isPresent()) {
            UserPlaceMemory memory = memoryOpt.get();
            // 如果已经是收藏状态，不需要改变为探索状态
            if (!"BOOKMARKED".equals(memory.getInteractionType())) {
                memory.setInteractionType("EXPLORING");
                memory.setUpdatedAt(LocalDateTime.now());
                userPlaceMemoryRepository.save(memory);
            }
        } else {
            UserPlaceMemory memory = new UserPlaceMemory();
            memory.setUserId(userId);
            memory.setPlaceId(targetPlaceId);
            memory.setInteractionType("EXPLORING");
            memory.setVisitedAt(LocalDate.now());
            memory.setCreatedAt(LocalDateTime.now());
            memory.setUpdatedAt(LocalDateTime.now());
            userPlaceMemoryRepository.save(memory);
        }
    }

    @Override
    public List<PlaceCard> getDiscoveryPlaces(Long userId) {
        // 1. 查询所有与用户有交互的地点（排除 VISITED，因为那是已经去过的）
        List<UserPlaceMemory> memories = userPlaceMemoryRepository.findByUserId(userId);
        
        return memories.stream()
                .filter(m -> "BOOKMARKED".equals(m.getInteractionType()) || "EXPLORING".equals(m.getInteractionType()))
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt())) // 按时间倒序
                .map(m -> {
                    Optional<PlaceEmotionLibrary> placeOpt = placeEmotionLibraryRepository.findById(m.getPlaceId());
                    if (placeOpt.isEmpty()) return null;
                    
                    PlaceEmotionLibrary place = placeOpt.get();
                    PlaceCard card = new PlaceCard();
                    card.setPlaceId(place.getId());
                    card.setPlaceName(place.getPlaceName());
                    card.setAddress(place.getAddress());
                    card.setImageUrl(place.getImageUrl());
                    card.setOneSentence(place.getOneSentence());
                    card.setCrowdLevel(place.getCrowdLevel());
                    card.setLatitude(place.getLatitude());
                    card.setLongitude(place.getLongitude());
                    
                    // 设置状态文字
                    if ("BOOKMARKED".equals(m.getInteractionType())) {
                        card.setStatus("记忆中");
                    } else {
                        card.setStatus("待物探");
                    }
                    
                    // 补充标签
                    List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(place.getId());
                    List<String> moodTags = relations.stream()
                            .map(r -> emotionTagDictRepository.findById(r.getTagId()))
                            .filter(Optional::isPresent)
                            .map(opt -> opt.get().getTagName())
                            .collect(Collectors.toList());
                    card.setMoodTags(moodTags);
                    
                    return card;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }
}
