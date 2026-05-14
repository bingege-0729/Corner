package com.example.corner.service;

import com.example.corner.dto.PlaceDetailResponse;
import com.example.corner.entity.PlaceEmotionLibrary;

import java.util.List;

public interface PlaceService {
    
    /**
     * 地点详情
     */
    PlaceDetailResponse getPlaceDetail(Long userId, Long placeId);
    
    /**
     * 获取所有地点（包含标签信息）
     */
    List<PlaceEmotionLibrary> getAllPlacesWithTags();
}
