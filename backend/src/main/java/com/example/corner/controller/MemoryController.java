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
    public Result<Void> toggleBookmark(HttpServletRequest request,
                                       @PathVariable Long placeId) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("用户切换收藏状态: userId={}, placeId={}", userId, placeId);
        placeService.toggleBookmark(userId, placeId);
        return Result.success();
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
}
