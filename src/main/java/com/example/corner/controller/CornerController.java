package com.example.corner.controller;

import com.example.corner.common.ApiResponse;
import com.example.corner.dto.*;
import com.example.corner.service.CornerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CornerController {
    
    @Autowired
    private CornerService cornerService;
    
    /**
     * 接口1：手机号登录
     */
    @PostMapping("/user/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = cornerService.login(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 接口2：获取全部标签
     */
    @GetMapping("/tags")
    public ApiResponse<Map<String, Object>> getTags() {
        var tags = cornerService.getAllTags();
        Map<String, Object> data = new HashMap<>();
        data.put("tags", tags);
        return ApiResponse.success(data);
    }
    
    /**
     * 接口3：核心推荐
     */
    @PostMapping("/recommend")
    public ApiResponse<RecommendResponse> recommend(HttpServletRequest request, 
                                                       @RequestBody RecommendRequest recommendRequest) {
        Long userId = (Long) request.getAttribute("userId");
        RecommendResponse response = cornerService.recommend(userId, recommendRequest);
        return ApiResponse.success(response);
    }
    
    /**
     * 接口4：推荐反馈
     */
    @PostMapping("/place/feedback")
    public ApiResponse<Void> feedback(HttpServletRequest request, 
                                       @RequestBody FeedbackRequest feedbackRequest) {
        Long userId = (Long) request.getAttribute("userId");
        cornerService.feedback(userId, feedbackRequest);
        return ApiResponse.success();
    }
    
    /**
     * 接口5：我的记忆列表
     */
    @GetMapping("/memory/list")
    public ApiResponse<MemoryListResponse> getMemoryList(HttpServletRequest request,
                                                          @RequestParam(required = false) String type) {
        Long userId = (Long) request.getAttribute("userId");
        MemoryListResponse response = cornerService.getMemoryList(userId, type);
        return ApiResponse.success(response);
    }
    
    /**
     * 接口6：地点详情
     */
    @GetMapping("/place/detail/{placeId}")
    public ApiResponse<PlaceDetailResponse> getPlaceDetail(HttpServletRequest request,
                                                            @PathVariable Long placeId) {
        Long userId = (Long) request.getAttribute("userId");
        PlaceDetailResponse response = cornerService.getPlaceDetail(userId, placeId);
        return ApiResponse.success(response);
    }
}
