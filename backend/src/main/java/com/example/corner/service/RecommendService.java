package com.example.corner.service;

import com.example.corner.dto.RecommendRequest;
import com.example.corner.vo.RecommendResponse;

public interface RecommendService {

    /**
     * 核心推荐
     * 
     * @param userId
     *            用户ID
     * @param request
     *            请求DTO
     * @return RecommendResponse
     */
    RecommendResponse recommend(Long userId, RecommendRequest request);

    /**
     * 路线规划（基于情绪+时空）
     * 
     * @param userId
     *            用户ID
     * @param request
     *            请求DTO（包含当前位置、情绪等信息）
     * @return 带时间轴的路线规划响应
     */
    RecommendResponse planRoute(Long userId, RecommendRequest request);
}
