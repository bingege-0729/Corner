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
import java.util.Optional;
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
        if (placeId == null || placeId == -1L) {
            throw new RuntimeException("该地点为网络搜索结果，暂无详细信息");
        }

        PlaceEmotionLibrary place = placeEmotionLibraryRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("地点不存在，ID: " + placeId));

        List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(placeId);
        List<Long> tagIds = relations.stream().map(PlaceTagRelation::getTagId).collect(Collectors.toList());
        List<EmotionTagDict> tags = emotionTagDictRepository.findAllById(tagIds);
        List<String> moodTags = tags.stream().map(EmotionTagDict::getTagName).collect(Collectors.toList());

        Optional<UserPlaceMemory> memoryOpt = userPlaceMemoryRepository.findByUserIdAndPlaceId(userId, placeId);
        UserHistory userHistory = new UserHistory();
        if (memoryOpt.isPresent()) {
            UserPlaceMemory memory = memoryOpt.get();
            userHistory.setHasVisited(memory.getVisitedAt() != null);
            userHistory.setIsBookmarked("BOOKMARKED".equals(memory.getInteractionType()));
            userHistory.setVisitCount(1);
            userHistory.setLastVisited(memory.getVisitedAt() != null ? memory.getVisitedAt().toString() : "未知");
            userHistory.setYourRating(memory.getRating());
            userHistory.setYourFeedback(memory.getFeedback());
        } else {
            userHistory.setHasVisited(false);
            userHistory.setIsBookmarked(false);
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
        if (placeId == null || placeId == -1L) {
            throw new RuntimeException("该地点为网络搜索结果，无法生成出行提示");
        }

        PlaceEmotionLibrary place = placeEmotionLibraryRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("地点不存在，ID: " + placeId));

        UserInfo user = userInfoRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));

        String distanceText = "未知";
        if (user.getLatitude() != null && user.getLongitude() != null && place.getLatitude() != null
                && place.getLongitude() != null) {
            double distance = calculateDistance(user.getLatitude().doubleValue(), user.getLongitude().doubleValue(),
                    place.getLatitude().doubleValue(), place.getLongitude().doubleValue());
            distanceText = String.format("%.1f公里", distance);
        }

        TravelTipCard tipCard = new TravelTipCard();
        tipCard.setPlaceId(place.getId());
        tipCard.setPlaceName(place.getPlaceName());
        tipCard.setAddress(place.getAddress());
        tipCard.setDistanceText(distanceText);
        tipCard.setWeatherTip(generateWeatherTip(user));
        tipCard.setPreparationTip(generatePreparationTip(place.getTips()));
        tipCard.setAiMessage(generateAiMessage(place, LocalDateTime.now()));

        return tipCard;
    }

    private String generateWeatherTip(UserInfo user) {
        try {
            if (accuWeatherApiKey == null || accuWeatherApiKey.isEmpty()) {
                return "建议出发前查看天气预报，做好相应准备 ☀️";
            }
            if (user.getLatitude() == null || user.getLongitude() == null) {
                return "建议出发前查看天气预报，做好相应准备 ☀️";
            }

            Map<String, Object> weatherInfo = getWeatherByLocation(user.getLatitude().doubleValue(),
                    user.getLongitude().doubleValue());

            if (Boolean.TRUE.equals(weatherInfo.get("success"))) {
                String city = (String) weatherInfo.get("city");
                String weather = (String) weatherInfo.get("weather");
                String temperature = (String) weatherInfo.get("temperature");

                StringBuilder tip = new StringBuilder();
                tip.append(String.format("%s今天%s，气温%s。", city, weather, temperature));

                if (weather.contains("雨") || weather.contains("Rain")) {
                    tip.append("记得带伞哦 ☔");
                } else if (weather.contains("雪") || weather.contains("Snow")) {
                    tip.append("注意保暖防滑 ❄️");
                } else if (weather.contains("晴") || weather.contains("Sun") || weather.contains("Clear")) {
                    tip.append("适合出行，注意防晒 ☀️");
                } else if (weather.contains("云") || weather.contains("Cloud")) {
                    tip.append("天气舒适，愉快出行吧 🌤️");
                } else {
                    tip.append("祝您旅途愉快 🌟");
                }
                return tip.toString();
            }
        } catch (Exception e) {
            log.error("生成天气提示异常", e);
        }
        return "建议出发前查看天气预报，做好相应准备 ☀️";
    }

    private Map<String, Object> getWeatherByLocation(double latitude, double longitude) {
        Map<String, Object> result = new HashMap<>();
        try {
            String locationUrl = String.format("%s?q=%s,%s&apikey=%s", accuWeatherLocationsUrl, latitude, longitude,
                    accuWeatherApiKey);
            String locationResponse = restTemplate.getForObject(locationUrl, String.class);

            if (locationResponse != null && !locationResponse.trim().isEmpty()) {
                JsonNode locationNode = objectMapper.readTree(locationResponse);
                if (locationNode.isArray() && locationNode.size() > 0) {
                    String locationKey = locationNode.get(0).get("Key").asText();
                    String cityName = locationNode.get(0).get("LocalizedName").asText();

                    String weatherUrl = String.format("%s%s?apikey=%s&details=true", accuWeatherCurrentUrl, locationKey,
                            accuWeatherApiKey);
                    String weatherResponse = restTemplate.getForObject(weatherUrl, String.class);

                    if (weatherResponse != null && !weatherResponse.trim().isEmpty()) {
                        JsonNode weatherArray = objectMapper.readTree(weatherResponse);
                        if (weatherArray.isArray() && weatherArray.size() > 0) {
                            JsonNode current = weatherArray.get(0);
                            String temp = current.get("Temperature").get("Metric").get("Value").asText() + "°C";
                            String weatherText = current.get("WeatherText").asText();

                            result.put("success", true);
                            result.put("city", cityName);
                            result.put("temperature", temp);
                            result.put("weather", weatherText);
                            return result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取天气信息失败", e);
        }
        result.put("success", false);
        return result;
    }

    private String generatePreparationTip(String tips) {
        if (tips == null || tips.isEmpty()) {
            return "无需特殊准备，轻松出发即可";
        }
        return "温馨提示：" + tips;
    }

    private String generateAiMessage(PlaceEmotionLibrary place, LocalDateTime now) {
        int hour = now.getHour();
        String timeGreeting = (hour < 6 || hour >= 22) ? "夜深了" : (hour < 12) ? "早上好" : (hour < 18) ? "下午好" : "晚上好";

        return String.format("%s！%s是一个不错的选择，希望你能在那里找到属于自己的角落 🌟", timeGreeting, place.getPlaceName());
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 切换收藏状态（收藏/取消收藏）
     */
    @Override
    @Transactional
    public Long toggleBookmark(Long userId, Long placeId, PlaceCard placeCard) {
        Long targetPlaceId = placeId;

        // 1. 处理外部地点入库
        if (placeId == -1L && placeCard != null) {
            Optional<PlaceEmotionLibrary> existingPlace = placeEmotionLibraryRepository.findAll().stream()
                    .filter(p -> p.getPlaceName().equals(placeCard.getPlaceName())).findFirst();

            if (existingPlace.isPresent()) {
                targetPlaceId = existingPlace.get().getId();
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
                saveMoodTags(targetPlaceId, placeCard.getMoodTags());
            }
        }

        // 2. 核心收藏逻辑：足迹保护模式
        Optional<UserPlaceMemory> memoryOpt = userPlaceMemoryRepository.findByUserIdAndPlaceId(userId, targetPlaceId);

        if (memoryOpt.isPresent()) {
            UserPlaceMemory memory = memoryOpt.get();
            if ("BOOKMARKED".equals(memory.getInteractionType())) {
                // 如果当前是收藏状态，想取消收藏
                if (memory.getVisitedAt() != null) {
                    // 如果去过，不能删除，只能改为“已游历”状态以保留足迹
                    memory.setInteractionType("VISITED");
                    memory.setUpdatedAt(LocalDateTime.now());
                    userPlaceMemoryRepository.save(memory);
                } else {
                    // 没去过也没收藏了，可以安全删除
                    userPlaceMemoryRepository.delete(memory);
                }
            } else {
                // 如果当前是游历或其他状态，提升为“收藏”状态
                memory.setInteractionType("BOOKMARKED");
                memory.setUpdatedAt(LocalDateTime.now());
                userPlaceMemoryRepository.save(memory);
            }
        } else {
            // 新建收藏
            UserPlaceMemory memory = new UserPlaceMemory();
            memory.setUserId(userId);
            memory.setPlaceId(targetPlaceId);
            memory.setInteractionType("BOOKMARKED");
            memory.setCreatedAt(LocalDateTime.now());
            memory.setUpdatedAt(LocalDateTime.now());
            userPlaceMemoryRepository.save(memory);
        }

        return targetPlaceId;
    }

    /**
     * 获取用户收藏的地点列表 (包含已游历和待去)
     */
    @Override
    public List<PlaceCard> getBookmarkedPlaces(Long userId) {
        // 返回所有有交互的地点（收藏或游历），但不包括不感兴趣的
        List<UserPlaceMemory> memories = userPlaceMemoryRepository.findByUserId(userId);

        return memories.stream().filter(m -> !"DISLIKED".equals(m.getInteractionType())).sorted((a, b) -> {
            LocalDateTime t1 = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt();
            LocalDateTime t2 = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt();
            if (t1 == null)
                return 1;
            if (t2 == null)
                return -1;
            return t2.compareTo(t1);
        }).map(m -> {
            Optional<PlaceEmotionLibrary> placeOpt = placeEmotionLibraryRepository.findById(m.getPlaceId());
            if (placeOpt.isEmpty())
                return null;

            PlaceEmotionLibrary place = placeOpt.get();
            PlaceCard card = new PlaceCard();
            card.setPlaceId(place.getId());
            card.setPlaceName(place.getPlaceName());
            card.setAddress(place.getAddress());
            card.setImageUrl(place.getImageUrl());
            card.setOneSentence(place.getOneSentence());
            card.setCrowdLevel(place.getCrowdLevel());

            // 设置状态显示
            if (m.getVisitedAt() != null) {
                card.setStatus("已游历");
                card.setVisited(true);
            } else {
                card.setStatus("记忆中");
                card.setVisited(false);
            }

            // 补充标签
            List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(place.getId());
            List<String> moodTags = relations.stream().map(r -> emotionTagDictRepository.findById(r.getTagId()))
                    .filter(Optional::isPresent).map(opt -> opt.get().getTagName()).collect(Collectors.toList());
            card.setMoodTags(moodTags);

            return card;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 获取用户去过的地点及对应心情（用于地图展示）
     */
    @Override
    public List<Map<String, Object>> getVisitedPlacesWithMood(Long userId) {
        List<UserPlaceMemory> memories = userPlaceMemoryRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (UserPlaceMemory memory : memories) {
            // 只要有访问时间的都算去过
            if (memory.getVisitedAt() == null)
                continue;

            Optional<PlaceEmotionLibrary> placeOpt = placeEmotionLibraryRepository.findById(memory.getPlaceId());
            if (placeOpt.isEmpty())
                continue;

            PlaceEmotionLibrary place = placeOpt.get();
            List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(place.getId());
            String moodTag = null;
            if (!relations.isEmpty()) {
                Optional<EmotionTagDict> tagOpt = emotionTagDictRepository.findById(relations.get(0).getTagId());
                moodTag = tagOpt.map(EmotionTagDict::getTagName).orElse(null);
            }

            Map<String, Object> placeData = new HashMap<>();
            placeData.put("placeId", place.getId());
            placeData.put("placeName", place.getPlaceName());
            placeData.put("latitude", place.getLatitude());
            placeData.put("longitude", place.getLongitude());
            placeData.put("moodTag", moodTag);
            placeData.put("visitedAt", memory.getVisitedAt().toString());
            placeData.put("address", place.getAddress());

            result.add(placeData);
        }
        return result;
    }

    @Override
    public List<PlaceEmotionLibrary> getAllPlacesWithTags() {
        return placeEmotionLibraryRepository.findAll();
    }

    @Override
    @Transactional
    public void recordExploration(Long userId, Long placeId, PlaceCard placeCard) {
        Long targetPlaceId = placeId;

        // 1. 处理外部地点入库
        if (placeId == -1L && placeCard != null) {
            Optional<PlaceEmotionLibrary> existing = placeEmotionLibraryRepository.findAll().stream()
                    .filter(p -> p.getPlaceName().equals(placeCard.getPlaceName())).findFirst();

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
                saveMoodTags(targetPlaceId, placeCard.getMoodTags());
            }
        }

        // 2. 标记游历
        Optional<UserPlaceMemory> memoryOpt = userPlaceMemoryRepository.findByUserIdAndPlaceId(userId, targetPlaceId);

        if (memoryOpt.isPresent()) {
            UserPlaceMemory memory = memoryOpt.get();
            memory.setInteractionType("VISITED");
            if (memory.getVisitedAt() == null) {
                memory.setVisitedAt(LocalDate.now());
            }
            memory.setUpdatedAt(LocalDateTime.now());
            userPlaceMemoryRepository.save(memory);
        } else {
            UserPlaceMemory memory = new UserPlaceMemory();
            memory.setUserId(userId);
            memory.setPlaceId(targetPlaceId);
            memory.setInteractionType("VISITED");
            memory.setVisitedAt(LocalDate.now());
            memory.setCreatedAt(LocalDateTime.now());
            memory.setUpdatedAt(LocalDateTime.now());
            userPlaceMemoryRepository.save(memory);
        }
    }

    @Override
    public List<PlaceCard> getDiscoveryPlaces(Long userId) {
        List<UserPlaceMemory> memories = userPlaceMemoryRepository.findByUserId(userId);

        return memories.stream().sorted((a, b) -> {
            LocalDateTime t1 = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt();
            LocalDateTime t2 = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt();
            if (t1 == null)
                return 1;
            if (t2 == null)
                return -1;
            return t2.compareTo(t1);
        }).map(m -> {
            Optional<PlaceEmotionLibrary> placeOpt = placeEmotionLibraryRepository.findById(m.getPlaceId());
            if (placeOpt.isEmpty())
                return null;

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

            boolean isVisited = m.getVisitedAt() != null;
            card.setVisited(isVisited);
            if (isVisited) {
                card.setStatus("已游历");
            } else if ("BOOKMARKED".equals(m.getInteractionType())) {
                card.setStatus("记忆中");
            } else {
                card.setStatus("探索中");
            }

            List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(place.getId());
            List<String> moodTags = relations.stream().map(r -> emotionTagDictRepository.findById(r.getTagId()))
                    .filter(Optional::isPresent).map(opt -> opt.get().getTagName()).collect(Collectors.toList());
            card.setMoodTags(moodTags);

            return card;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsVisited(Long userId, Long placeId, PlaceCard placeCard) {
        recordExploration(userId, placeId, placeCard);
    }

    private void saveMoodTags(Long placeId, List<String> moodTags) {
        if (moodTags != null) {
            for (String tagName : moodTags) {
                final String finalTagName = tagName;
                EmotionTagDict tagDict = emotionTagDictRepository.findAll().stream()
                        .filter(t -> t.getTagName().equals(finalTagName)).findFirst().orElseGet(() -> {
                            EmotionTagDict newTag = new EmotionTagDict();
                            newTag.setTagName(finalTagName);
                            return emotionTagDictRepository.save(newTag);
                        });

                PlaceTagRelation relation = new PlaceTagRelation();
                relation.setPlaceId(placeId);
                relation.setTagId(tagDict.getId());
                placeTagRelationRepository.save(relation);
            }
        }
    }
}
