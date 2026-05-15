package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.service.PlaceService;
import com.example.corner.vo.PlaceCard;
import com.example.corner.vo.PlaceDetailResponse;
import com.example.corner.vo.TravelTipCard;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 处理地点详情、出行提示等地点相关功能
 */
@RestController
@RequestMapping("/api/place")
public class PlaceController {
    
    @Autowired
    private PlaceService placeService;
    
    /**
     * 地点详情
     */
    @GetMapping("/detail/{placeId}")
    public Result<PlaceDetailResponse> getPlaceDetail(HttpServletRequest request,
                                                            @PathVariable Long placeId) {
        Long userId = (Long) request.getAttribute("userId");
        PlaceDetailResponse response = placeService.getPlaceDetail(userId, placeId);
        return Result.success(response);
    }
    
    /**
     * 生成出行温馨提示
     */
    @PostMapping("/{placeId}/travel-tips")
    public Result<TravelTipCard> generateTravelTips(HttpServletRequest request,
                                                     @PathVariable Long placeId) {
        Long userId = (Long) request.getAttribute("userId");
        TravelTipCard tips = placeService.generateTravelTips(userId, placeId);
        return Result.success(tips);
    }
    
    /**
     * 标记地点为已访问（VISITED）
     */
    @PostMapping("/{placeId}/mark-visited")
    public Result<Void> markAsVisited(HttpServletRequest request,
                                      @PathVariable Long placeId,
                                      @RequestBody(required = false) PlaceCard placeCard) {
        Long userId = (Long) request.getAttribute("userId");
        placeService.markAsVisited(userId, placeId, placeCard);
        return Result.success(null);
    }
}
