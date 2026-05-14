package com.example.corner.controller;

import com.example.corner.common.Result;
import com.example.corner.service.TagService;
import com.example.corner.vo.TagResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 标签接口
 */
@RestController
@RequestMapping("/api")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 查询全部标签
     */
    @GetMapping("/tags")
    public Result<Map<String, Object>> getTags() {
        List<TagResponse> tags = tagService.getAllTags();
        Map<String, Object> data = new HashMap<>();
        data.put("tags", tags);
        return Result.success(data);
    }
}
