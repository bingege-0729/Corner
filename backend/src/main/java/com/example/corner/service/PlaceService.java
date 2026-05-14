package com.example.corner.service;

import com.example.corner.dto.PlaceDetailResponse;

public interface PlaceService {
    
    /**
     * 地点详情
     */
    PlaceDetailResponse getPlaceDetail(Long userId, Long placeId);
}
