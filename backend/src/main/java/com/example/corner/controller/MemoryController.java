package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.service.PlaceService;
import com.example.corner.vo.PlaceCard;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户收藏
 */
@Slf4j
@RestController
@RequestMapping("/api/memory")
public class MemoryController {
    
    @Autowired
    private PlaceService placeService;
    
    /**
     * 切换地点收藏状态（收藏/取消收藏）
     */
    @PostMapping("/{placeId}/bookmark")
    public Result<Long> toggleBookmark(HttpServletRequest request,
                                       @PathVariable Long placeId,
                                       @RequestBody(required = false) PlaceCard placeCard) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户切换收藏状态: userId={}, placeId={}", userId, placeId);
        Long targetPlaceId = placeService.toggleBookmark(userId, placeId, placeCard);
        return Result.success(targetPlaceId);
    }

    /**
     * 获取用户收藏的地点列表
     */
    @GetMapping("/bookmarks")
    public Result<List<PlaceCard>> getBookmarks(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户收藏列表: userId={}", userId);
        List<PlaceCard> bookmarks = placeService.getBookmarkedPlaces(userId);
        return Result.success(bookmarks);
    }
    
    /**
     * 获取用户去过的地点及对应心情（用于地图展示）
     */
    @GetMapping("/visited-with-mood")
    public Result<List<Map<String, Object>>> getVisitedPlacesWithMood(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取用户去过的地点及心情: userId={}", userId);
        
        List<Map<String, Object>> visitedPlaces = placeService.getVisitedPlacesWithMood(userId);
        return Result.success(visitedPlaces);
    }

    /**
     * 记录用户探索意向（点击一键出行）
     */
    @PostMapping("/explore")
    public Result<Void> recordExploration(HttpServletRequest request,
                                          @RequestBody PlaceCard placeCard) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("记录探索意向: userId={}, placeId={}", userId, placeCard.getPlaceId());
        placeService.recordExploration(userId, placeCard.getPlaceId(), placeCard);
        return Result.success();
    }

    /**
     * 获取“发现”页面的地点列表
     */
    @GetMapping("/discovery")
    public Result<List<PlaceCard>> getDiscoveryPlaces(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("获取发现列表: userId={}", userId);
        List<PlaceCard> places = placeService.getDiscoveryPlaces(userId);
        return Result.success(places);
    }
}
