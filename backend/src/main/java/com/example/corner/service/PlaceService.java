package com.example.corner.service;

import com.example.corner.dto.PlaceCard;
import com.example.corner.dto.PlaceDetailResponse;

import java.util.List;

public interface PlaceService {
    
    /**
     * 地点详情
     */
    PlaceDetailResponse getPlaceDetail(Long userId, Long placeId);
    /**
     * 获取所有地点的信息
     */
    List<PlaceCard> getAllPlaces();
}
