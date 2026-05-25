package com.example.corner.service.impl;

import com.example.corner.dto.RecommendRequest;

import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.UserMoodRecord;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.UserMoodRecordRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.service.BaiduMapService;
import com.example.corner.service.RecommendService;
import com.example.corner.service.aiService.RecommendAIService;
import com.example.corner.vo.PlaceCard;
import com.example.corner.vo.RecommendResponse;
import com.example.corner.vo.RouteStepVO;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.corner.common.RedisConstant.USER_MEMORY_KEY_PREFIX;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private RecommendAIService recommendAIService;

    @Autowired
    private BaiduMapService baiduMapService;

    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;

    @Autowired
    private UserMoodRecordRepository userMoodRecordRepository;

    @Autowired
    private OpenAiChatModel chatModel;

    @Override
    public RecommendResponse recommend(Long userId, RecommendRequest request) {
        String userMessage = String.format("userId=%d, 用户输入=%s, 纬度=%s, 经度=%s, 用户心情=%s", userId,
                request.getUserInput() != null ? request.getUserInput() : "", request.getUserLat(),
                request.getUserLng(), request.getMood() != null ? request.getMood() : "");

        System.out.println("=== 开始推荐流程 ===");
        System.out.println("用户消息: " + userMessage);
        
        RecommendResponse response = recommendAIService.getRecommend(userMessage, USER_MEMORY_KEY_PREFIX + userId);

        // 【保底逻辑】如果 AI 返回的地点不足3个，记录警告
        if (response != null && response.getEmotionMatches() != null) {
            int count = response.getEmotionMatches().size();
            System.out.println("AI 返回地点数量: " + count);
            if (count < 3) {
                System.out.println("警告：AI 返回地点不足3个，可能 AI 没有正确调用搜索工具");
                System.out.println("返回的地点列表: " + response.getEmotionMatches().stream()
                        .map(com.example.corner.vo.PlaceCard::getPlaceName)
                        .collect(Collectors.toList()));
            }
        }

        // 后处理：清理 understanding 字段，确保不包含思考过程
        if (response != null && response.getUnderstanding() != null) {
            String understanding = response.getUnderstanding();

            // 移除常见的思考过程关键词
            understanding = understanding.replaceAll("(?m)^让我.*?\\n", "") // 移除"让我..."开头的行
                    .replaceAll("(?m)^首先.*?\\n", "") // 移除"首先..."开头的行
                    .replaceAll("(?m)^我需要.*?\\n", "") // 移除"我需要..."开头的行
                    .replaceAll("根据工具返回.*?[,，]", "") // 移除"根据工具返回"
                    .trim();

            response.setUnderstanding(understanding);
        }

        System.out.println("=== 推荐流程完成 ===");
        return response;
    }

    @Override
    public RecommendResponse planRoute(Long userId, RecommendRequest request) {
        return planRouteWithPlaces(userId, request, null);
    }

    /**
     * 路线规划（支持传入已有的地点列表，避免重复调用AI）
     */
    public RecommendResponse planRouteWithPlaces(Long userId, RecommendRequest request, List<PlaceCard> existingPlaces) {
        System.out.println("=== 开始路线规划 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("请求参数: userLat=" + request.getUserLat() + ", userLng=" + request.getUserLng() + ", mood=" + request.getMood());
        System.out.println("userInput: " + request.getUserInput());
        System.out.println("existingPlaces: " + (existingPlaces != null ? existingPlaces.size() : 0));
        
        // 1. 获取用户当前位置
        BigDecimal userLat = request.getUserLat();
        BigDecimal userLng = request.getUserLng();
        if (userLat == null || userLng == null) {
            // 如果没有提供位置，使用默认位置（深圳南山区）
            System.out.println("未提供位置信息，使用默认位置");
            userLat = new BigDecimal("22.5532");
            userLng = new BigDecimal("113.9456");
        }

        // 2. 获取用户当前情绪
        String mood = request.getMood();
        if (mood == null || mood.isEmpty()) {
            // 如果没有指定情绪，查询最近的情绪记录
            List<UserMoodRecord> records = userMoodRecordRepository.findByUserId(userId);
            if (!records.isEmpty()) {
                mood = records.get(records.size() - 1).getMoodTag();
                System.out.println("从记录中获取情绪: " + mood);
            } else {
                mood = "平静"; // 默认情绪
                System.out.println("使用默认情绪: " + mood);
            }
        }

        // 3. 获取候选地点
        List<PlaceCard> candidates;
        
        // 判断是否为对话模式（userInput 不是单纯的路线规划请求）
        boolean isConversationMode = request.getUserInput() != null 
                && !request.getUserInput().contains("规划一条路线")
                && !request.getUserInput().contains("为我规划");
        
        if (isConversationMode && existingPlaces != null && !existingPlaces.isEmpty()) {
            // 【对话模式】先调用 AI 获取补充地点，然后合并
            System.out.println("=== 检测到对话模式 ===");
            System.out.println("用户新需求: " + request.getUserInput());
            System.out.println("现有地点数量: " + existingPlaces.size());
            System.out.println("现有地点列表: " + existingPlaces.stream().map(PlaceCard::getPlaceName).collect(Collectors.toList()));
            
            // 调用推荐服务获取补充地点
            System.out.println("调用 AI 获取补充地点...");
            RecommendResponse supplementResponse = recommend(userId, request);
            List<PlaceCard> newPlaces = supplementResponse != null ? supplementResponse.getEmotionMatches() : new ArrayList<>();
            System.out.println("AI 返回的补充地点数量: " + (newPlaces != null ? newPlaces.size() : 0));
            if (newPlaces != null && !newPlaces.isEmpty()) {
                System.out.println("补充地点列表: " + newPlaces.stream().map(PlaceCard::getPlaceName).collect(Collectors.toList()));
            }
            
            // 合并地点列表（去重）
            candidates = new ArrayList<>(existingPlaces);
            if (newPlaces != null && !newPlaces.isEmpty()) {
                Set<String> existingPlaceNames = existingPlaces.stream()
                        .map(PlaceCard::getPlaceName)
                        .collect(Collectors.toSet());
                
                int addedCount = 0;
                for (PlaceCard newPlace : newPlaces) {
                    // 使用更宽松的去重逻辑（包含检查）
                    boolean isDuplicate = existingPlaceNames.stream()
                            .anyMatch(name -> name.contains(newPlace.getPlaceName()) 
                                    || newPlace.getPlaceName().contains(name));
                    
                    if (!isDuplicate) {
                        candidates.add(newPlace);
                        addedCount++;
                        System.out.println("✅ 添加新地点: " + newPlace.getPlaceName());
                    } else {
                        System.out.println("❌ 跳过重复地点: " + newPlace.getPlaceName());
                    }
                }
                System.out.println("成功添加 " + addedCount + " 个新地点");
            } else {
                System.out.println("⚠️ AI 没有返回补充地点，可能 AI 没有正确调用搜索工具");
            }
            System.out.println("合并后地点总数: " + candidates.size());
            System.out.println("合并后地点列表: " + candidates.stream().map(PlaceCard::getPlaceName).collect(Collectors.toList()));
            System.out.println("=== 对话模式处理完成 ===");
        } else if (existingPlaces != null && !existingPlaces.isEmpty()) {
            // 【路线规划模式】直接使用已有的地点列表
            System.out.println("使用已有的地点列表，数量: " + existingPlaces.size());
            candidates = existingPlaces;
        } else {
            // 【首次推荐模式】调用推荐服务获取候选地点
            System.out.println("调用推荐服务获取候选地点...");
            RecommendResponse baseResponse = recommend(userId, request);
            candidates = baseResponse.getEmotionMatches();
        }
        System.out.println("最终候选地点数量: " + (candidates != null ? candidates.size() : 0));

        if (candidates == null || candidates.isEmpty()) {
            // 如果没有找到地点，返回空响应而不是抛异常
            System.out.println("未找到候选地点");
            RecommendResponse emptyResponse = new RecommendResponse();
            emptyResponse.setUnderstanding("抱歉，暂时没有找到适合您的地点。");
            emptyResponse.setEmotionMatches(new ArrayList<>());
            emptyResponse.setRouteSteps(new ArrayList<>());
            return emptyResponse;
        }

        // 4. 筛选出有坐标的地点（最多5个）
        List<PlaceCard> validCandidates = candidates.stream()
                .filter(card -> card.getLatitude() != null && card.getLongitude() != null)
                .limit(5) // 最多5个地点
                .collect(Collectors.toList());
        System.out.println("有效地点数量: " + validCandidates.size());

        if (validCandidates.isEmpty()) {
            System.out.println("没有有效地点（缺少坐标信息）");
            RecommendResponse emptyResponse = new RecommendResponse();
            emptyResponse.setUnderstanding("抱歉，找到的地点缺少位置信息，无法规划路线。");
            emptyResponse.setEmotionMatches(candidates);
            emptyResponse.setRouteSteps(new ArrayList<>());
            return emptyResponse;
        }

        // 5. 使用贪心最近邻算法排序
        System.out.println("开始最近邻算法排序...");
        List<PlaceCard> sortedPlaces = nearestNeighborSort(userLat, userLng, validCandidates);
        System.out.println("排序完成，地点顺序: " + sortedPlaces.stream().map(PlaceCard::getPlaceName).collect(Collectors.toList()));

        // 6. 生成时间轴和路线步骤
        System.out.println("生成时间轴和路线步骤...");
        LocalDateTime currentTime = LocalDateTime.now();
        List<RouteStepVO> routeSteps = generateRouteSteps(userLat, userLng, sortedPlaces, currentTime, mood);
        System.out.println("生成路线步骤数量: " + routeSteps.size());

        // 7. 构建响应
        RecommendResponse response = new RecommendResponse();
        response.setUnderstanding(isConversationMode ? "已为您更新路线规划" : "已为您生成个性化探索路线");
        response.setEmotionMatches(sortedPlaces);
        response.setRouteSteps(routeSteps);

        System.out.println("=== 路线规划完成 ===");
        return response;
    }

    /**
     * 贪心最近邻算法：从当前位置开始，每次选择距离上一个地点最近的下一个地点
     */
    private List<PlaceCard> nearestNeighborSort(BigDecimal startLat, BigDecimal startLng, List<PlaceCard> candidates) {
        List<PlaceCard> sorted = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        BigDecimal currentLat = startLat;
        BigDecimal currentLng = startLng;

        while (sorted.size() < candidates.size()) {
            PlaceCard nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (PlaceCard candidate : candidates) {
                if (visited.contains(candidate.getPlaceId())) {
                    continue;
                }

                double distance = baiduMapService.calculateDistance(currentLat, currentLng,
                        candidate.getLatitude(), candidate.getLongitude());

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = candidate;
                }
            }

            if (nearest != null) {
                sorted.add(nearest);
                visited.add(nearest.getPlaceId());
                currentLat = nearest.getLatitude();
                currentLng = nearest.getLongitude();
            } else {
                break;
            }
        }

        return sorted;
    }

    /**
     * 生成路线步骤（包含时间轴、交通信息、AI点评）
     */
    private List<RouteStepVO> generateRouteSteps(BigDecimal startLat, BigDecimal startLng,
            List<PlaceCard> places, LocalDateTime startTime, String mood) {
        List<RouteStepVO> steps = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        BigDecimal currentLat = startLat;
        BigDecimal currentLng = startLng;
        LocalDateTime currentTime = startTime;

        for (int i = 0; i < places.size(); i++) {
            PlaceCard place = places.get(i);
            RouteStepVO step = new RouteStepVO();
            step.setSequence(i + 1);
            step.setPlace(place);

            // 计算交通信息
            double distance = baiduMapService.calculateDistance(currentLat, currentLng,
                    place.getLatitude(), place.getLongitude());
            int travelMinutes = estimateTravelTime(distance);
            String travelMode = travelMinutes <= 15 ? "步行" : "驾车";
            step.setTravelInfo(String.format("%s %d分钟", travelMode, travelMinutes));

            // 计算时间槽
            LocalDateTime arrivalTime = currentTime.plusMinutes(travelMinutes);
            Integer duration = place.getSuggestedDuration();
            if (duration == null || duration <= 0) {
                duration = 60; // 默认60分钟
            }
            LocalDateTime departureTime = arrivalTime.plusMinutes(duration);

            String timeSlot = String.format("%s - %s",
                    arrivalTime.format(timeFormatter),
                    departureTime.format(timeFormatter));
            step.setTimeSlot(timeSlot);

            // 生成AI点评
            String aiComment = generateAiComment(place, mood, arrivalTime, travelMinutes, duration);
            step.setAiComment(aiComment);

            steps.add(step);

            // 更新当前时间和位置
            currentTime = departureTime;
            currentLat = place.getLatitude();
            currentLng = place.getLongitude();
        }

        return steps;
    }

    /**
     * 估算交通时间（分钟）
     */
    private int estimateTravelTime(double distanceInMeters) {
        // 简单估算：步行速度 80米/分钟，驾车速度 500米/分钟
        if (distanceInMeters <= 1500) {
            // 1.5公里内步行
            return (int) Math.ceil(distanceInMeters / 80.0);
        } else {
            // 超过1.5公里驾车
            return (int) Math.ceil(distanceInMeters / 500.0);
        }
    }

    /**
     * 生成AI点评
     */
    /**
     * 生成路线点评（使用模板，避免LLM调用导致超时）
     */
    private String generateAiComment(PlaceCard place, String mood, LocalDateTime arrivalTime,
                                     int travelMinutes, int duration) {
        String timeOfDay = getTimeOfDay(arrivalTime.getHour());
        String crowdInfo = place.getCrowdLevel() != null ? "，人流" + place.getCrowdLevel() : "";
        return String.format("%s来%s正合适，建议停留%d分钟%s，享受%s的时光",
                timeOfDay,
                place.getPlaceName(),
                duration,
                crowdInfo,
                mood);
    }

//    private String generateAiComment(PlaceCard place, String mood, LocalDateTime arrivalTime,
//            int travelMinutes, int duration) {
//        try {
//            String dayOfWeek = getDayOfWeekChinese(arrivalTime.getDayOfWeek().getValue());
//            String timeOfDay = getTimeOfDay(arrivalTime.getHour());
//
//            String prompt = String.format(
//                    "你是一个温暖贴心的旅行助手。基于以下信息，生成一句简短的个性化提示（30字以内）：\n" +
//                            "- 地点：%s\n" +
//                            "- 用户心情：%s\n" +
//                            "- 时间：%s %s\n" +
//                            "- 人流：%s\n" +
//                            "- 建议停留：%d分钟\n" +
//                            "- 路上耗时：%d分钟\n\n" +
//                            "要求：简洁、温暖、实用，提到时间点或注意事项。",
//                    place.getPlaceName(),
//                    mood,
//                    dayOfWeek,
//                    timeOfDay,
//                    place.getCrowdLevel() != null ? place.getCrowdLevel() : "未知",
//                    duration,
//                    travelMinutes);
//
//            String comment = chatModel.chat(prompt);
//            return comment.trim();
//        } catch (Exception e) {
//            // AI生成失败时返回默认提示
//            return String.format("建议在这里停留%d分钟，享受%s的时光",
//                    place.getSuggestedDuration() != null ? place.getSuggestedDuration() : 60,
//                    mood);
//        }
//    }

    /**
     * 获取中文星期
     */
    private String getDayOfWeekChinese(int dayOfWeek) {
        String[] days = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return days[dayOfWeek];
    }

    /**
     * 获取时间段描述
     */
    private String getTimeOfDay(int hour) {
        if (hour >= 6 && hour < 9) {
            return "早晨";
        } else if (hour >= 9 && hour < 12) {
            return "上午";
        } else if (hour >= 12 && hour < 14) {
            return "中午";
        } else if (hour >= 14 && hour < 18) {
            return "下午";
        } else if (hour >= 18 && hour < 21) {
            return "傍晚";
        } else {
            return "晚上";
        }
    }
}
