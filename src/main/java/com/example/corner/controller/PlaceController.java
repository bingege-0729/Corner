package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.dto.PlaceDetailResponse;
import com.example.corner.service.PlaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/place")
public class PlaceController {
    
    @Autowired
    private PlaceService placeService;
    
    /**
     * 接口6：地点详情
     */
    @GetMapping("/detail/{placeId}")
    public Result<PlaceDetailResponse> getPlaceDetail(HttpServletRequest request,
                                                            @PathVariable Long placeId) {
        Long userId = (Long) request.getAttribute("userId");
        PlaceDetailResponse response = placeService.getPlaceDetail(userId, placeId);
        return Result.success(response);
    }
}
