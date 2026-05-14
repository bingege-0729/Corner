package com.example.corner.service;

import com.example.corner.entity.EmotionTagDict;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.PlaceTagRelation;
import com.example.corner.repository.EmotionTagDictRepository;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.PlaceTagRelationRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaceVectorData {
    
    @Autowired
    private EmbeddingModel embeddingModel;
    
    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;
    
    @Autowired
    private PlaceTagRelationRepository placeTagRelationRepository;
    
    @Autowired
    private EmotionTagDictRepository emotionTagDictRepository;
    
    /**
     * 将所有地点信息转换为字符串并生成向量
     * @return 地点ID和对应向量的映射列表
     */
    public List<PlaceEmbeddingResult> generateAllPlaceEmbeddings() {
        // 获取所有地点
        List<PlaceEmotionLibrary> allPlaces = placeEmotionLibraryRepository.findAll();
        
        return allPlaces.stream()
                .map(place -> {
                    // 1. 将地点信息转换为字符串
                    String placeText = convertPlaceToString(place);
                    
                    // 2. 生成向量
                    Embedding embedding = embeddingModel.embed(placeText).content();
                    
                    // 3. 返回结果
                    return new PlaceEmbeddingResult(
                            place.getId(),
                            placeText,
                            embedding
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 将单个地点信息转换为字符串（包含标签）
     * @param place 地点实体
     * @return 格式化的字符串
     */
    public String convertPlaceToString(PlaceEmotionLibrary place) {
        StringBuilder sb = new StringBuilder();
        
        // 基本信息
        sb.append("地点名称：").append(place.getPlaceName()).append("。\n");
        sb.append("地址：").append(place.getAddress()).append("。\n");
        
        // 人流程度
        if (place.getCrowdLevel() != null) {
            sb.append("人流程度：").append(place.getCrowdLevel()).append("。\n");
        }
        
        // 最佳时间
        if (place.getBestTime() != null) {
            sb.append("最佳时间：").append(place.getBestTime()).append("。\n");
        }
        
        // 一句话描述
        if (place.getOneSentence() != null) {
            sb.append("简介：").append(place.getOneSentence()).append("。\n");
        }
        
        // 完整描述
        if (place.getFullDescription() != null) {
            sb.append("详细描述：").append(place.getFullDescription()).append("。\n");
        }
        
        // 小贴士
        if (place.getTips() != null) {
            sb.append("小贴士：").append(place.getTips()).append("。\n");
        }
        
        // 获取并添加情绪标签
        List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(place.getId());
        if (!relations.isEmpty()) {
            List<Long> tagIds = relations.stream()
                    .map(PlaceTagRelation::getTagId)
                    .collect(Collectors.toList());
            List<EmotionTagDict> tags = emotionTagDictRepository.findAllById(tagIds);
            
            if (!tags.isEmpty()) {
                String tagNames = tags.stream()
                        .map(EmotionTagDict::getTagName)
                        .collect(Collectors.joining("、"));
                sb.append("情绪标签：").append(tagNames).append("。\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 为指定地点生成向量
     * @param placeId 地点ID
     * @return 向量化结果
     */
    public PlaceEmbeddingResult generatePlaceEmbedding(Long placeId) {
        PlaceEmotionLibrary place = placeEmotionLibraryRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("地点不存在，ID: " + placeId));
        
        String placeText = convertPlaceToString(place);
        Embedding embedding = embeddingModel.embed(placeText).content();
        
        return new PlaceEmbeddingResult(placeId, placeText, embedding);
    }
    
    /**
     * 地点向量化结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlaceEmbeddingResult {
        private Long placeId;
        private String placeText;
        private Embedding embedding;
    }
}
