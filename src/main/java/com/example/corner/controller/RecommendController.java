package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.dto.RecommendRequest;
import com.example.corner.dto.RecommendResponse;
import com.example.corner.service.RecommendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RecommendController {
    
    @Autowired
    private RecommendService recommendService;
    
    /**
     * 接口3：核心推荐
     */
    @PostMapping("/recommend")
    public Result<RecommendResponse> recommend(HttpServletRequest request, 
                                                       @RequestBody RecommendRequest recommendRequest) {
        Long userId = (Long) request.getAttribute("userId");
        RecommendResponse response = recommendService.recommend(userId, recommendRequest);
        return Result.success(response);
    }
}
