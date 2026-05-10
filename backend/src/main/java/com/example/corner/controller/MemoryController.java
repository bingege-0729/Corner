package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.dto.FeedbackRequest;
import com.example.corner.dto.MemoryListResponse;
import com.example.corner.service.MemoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MemoryController {
    
    @Autowired
    private MemoryService memoryService;
    
    /**
     * 接口4：推荐反馈
     */
    @PostMapping("/place/feedback")
    public Result<Void> feedback(HttpServletRequest request, 
                                       @RequestBody FeedbackRequest feedbackRequest) {
        Long userId = (Long) request.getAttribute("userId");
        memoryService.feedback(userId, feedbackRequest);
        return Result.success();
    }
    
    /**
     * 接口5：我的记忆列表
     */
    @GetMapping("/memory/list")
    public Result<MemoryListResponse> getMemoryList(HttpServletRequest request,
                                                          @RequestParam(required = false) String type) {
        Long userId = (Long) request.getAttribute("userId");
        MemoryListResponse response = memoryService.getMemoryList(userId, type);
        return Result.success(response);
    }
}
