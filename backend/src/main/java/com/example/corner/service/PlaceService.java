package com.example.corner.service;

import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.vo.PlaceCard;
import com.example.corner.vo.PlaceDetailResponse;
import com.example.corner.vo.TravelTipCard;

import java.util.List;
import java.util.Map;

public interface PlaceService {

    /**
     * 地点详情
     */
    PlaceDetailResponse getPlaceDetail(Long userId, Long placeId);

    /**
     * 生成出行温馨提示
     */
    TravelTipCard generateTravelTips(Long userId, Long placeId);

    /**
     * 切换收藏状态（收藏/取消收藏）
     */
    Long toggleBookmark(Long userId, Long placeId, PlaceCard placeCard);

    /**
     * 获取用户收藏的地点列表
     */
    List<PlaceCard> getBookmarkedPlaces(Long userId);

    /**
     * 获取用户去过的地点及对应心情（用于地图展示）
     * 
     * @param userId
     *            用户ID
     * @return 去过的地点列表，每个地点包含：placeId, placeName, latitude, longitude, moodTag, visitedAt
     */
    List<Map<String, Object>> getVisitedPlacesWithMood(Long userId);

    List<PlaceEmotionLibrary> getAllPlacesWithTags();

    /**
     * 记录用户探索意向（点击一键出行）
     */
    void recordExploration(Long userId, Long placeId, PlaceCard placeCard);

    /**
     * 获取"发现"页面的地点列表（包含收藏和待探索的）
     */
    List<PlaceCard> getDiscoveryPlaces(Long userId);

    /**
     * 标记地点为已访问（VISITED）
     */
    void markAsVisited(Long userId, Long placeId, PlaceCard placeCard);
}
