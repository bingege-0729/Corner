package com.example.corner.service;

import com.example.corner.dto.TagResponse;

import java.util.List;

public interface TagService {
    
    /**
     * 获取全部标签
     */
    List<TagResponse> getAllTags();
}
