package com.example.corner.controller;

import com.example.corner.service.aiService.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    @Autowired
    private RecommendService recommendService;


}
