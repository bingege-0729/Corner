package com.example.corner.service.impl;

import com.example.corner.entity.EmotionTagDict;
import com.example.corner.repository.EmotionTagDictRepository;
import com.example.corner.service.TagService;
import com.example.corner.vo.TagResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private EmotionTagDictRepository emotionTagDictRepository;

    /**
     * 获取全部标签
     */
    @Override
    public List<TagResponse> getAllTags() {
        List<EmotionTagDict> tags = emotionTagDictRepository.findAllByOrderByCategoryAscIdAsc();
        return tags.stream().map(tag -> {
            TagResponse response = new TagResponse();
            response.setId(tag.getId());
            response.setTagName(tag.getTagName());
            response.setCategory(tag.getCategory());
            return response;
        }).collect(Collectors.toList());
    }
}
