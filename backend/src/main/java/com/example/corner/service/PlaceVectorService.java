package com.example.corner.service;

import com.example.corner.dto.PlaceCard;
import com.example.corner.entity.PlaceEmotionLibrary;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PlaceVectorService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private PlaceService placeService;

    private EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 初始化地点向量数据
     */
    public void initalizePlaceVectorDatas() {
        List<PlaceCard> place = placeService.getAllPlaces();
    }
}
