package com.example.corner.service;

import com.example.corner.dto.FeedbackRequest;
import com.example.corner.dto.MemoryListResponse;

public interface MemoryService {
    
    /**
     * 推荐反馈
     */
    void feedback(Long userId, FeedbackRequest request);
    
    /**
     * 我的记忆列表
     */
    MemoryListResponse getMemoryList(Long userId, String type);
}
