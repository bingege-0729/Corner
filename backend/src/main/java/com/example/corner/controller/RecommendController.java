package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.dto.RecommendRequest;
import com.example.corner.service.ChatService;
import com.example.corner.service.RecommendService;
import com.example.corner.vo.RecommendResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
/**
 * 推荐接口
 */
@RestController
@RequestMapping("/api")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private ChatService chatService;

    /**
     * 推荐接口
     * 
     * @param request
     * @param recommendRequest
     * @return 返回推荐结果
     */
    @PostMapping("/recommend")
    public Result<RecommendResponse> recommend(HttpServletRequest request,
            @RequestBody RecommendRequest recommendRequest) {
        Long userId = (Long) request.getAttribute("userId");
        RecommendResponse response = recommendService.recommend(userId, recommendRequest);
        return Result.success(response);
    }

    /**
     * AI 对话接口（流式输出）
     * 
     * @param request
     *            HTTP请求对象
     * @param recommendRequest
     *            请求DTO（使用 userInput 字段作为对话内容）
     * @return 流式响应
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(HttpServletRequest request, @RequestBody RecommendRequest recommendRequest) {
        Long userId = (Long) request.getAttribute("userId");
        return chatService.chat(userId, recommendRequest);
    }

    /**
     * 路线规划接口（基于情绪+时空）
     * 
     * @param request
     *            HTTP请求对象
     * @param recommendRequest
     *            请求DTO（包含当前位置、情绪等信息）
     * @return 带时间轴的路线规划响应
     */
    @PostMapping("/plan-route")
    public Result<RecommendResponse> planRoute(HttpServletRequest request,
            @RequestBody RecommendRequest recommendRequest) {
        Long userId = (Long) request.getAttribute("userId");
        RecommendResponse response = recommendService.planRoute(userId, recommendRequest);
        return Result.success(response);
    }
}
