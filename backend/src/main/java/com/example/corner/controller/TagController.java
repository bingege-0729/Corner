package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.dto.TagResponse;
import com.example.corner.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 获取全部标签
     */
    @GetMapping("/tags")
    public Result<Map<String, Object>> getTags() {
        List<TagResponse> tags = tagService.getAllTags();
        Map<String, Object> data = new HashMap<>();
        data.put("tags", tags);
        return Result.success(data);
    }
}
