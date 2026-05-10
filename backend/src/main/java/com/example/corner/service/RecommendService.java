package com.example.corner.service;

import com.example.corner.dto.RecommendRequest;
import com.example.corner.dto.RecommendResponse;

public interface RecommendService {
    
    /**
     * 核心推荐
     */
    RecommendResponse recommend(Long userId, RecommendRequest request);
}
