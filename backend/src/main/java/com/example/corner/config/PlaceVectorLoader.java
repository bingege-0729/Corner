package com.example.corner.config;

import com.example.corner.service.PlaceVectorData;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PlaceVectorLoader {
    @Autowired
    private PlaceVectorData placeVectorData;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    private RedisEmbeddingStore redisEmbeddingStore;

    /**
     * 初始化向量数据
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化地点向量数据...");
        List<PlaceVectorData.PlaceEmbeddingResult> results = placeVectorData.generateAllPlaceEmbeddings();
        for (PlaceVectorData.PlaceEmbeddingResult result : results) {
            TextSegment segment = TextSegment.from(result.getPlaceText());
            segment.metadata().put("placeId", result.getPlaceId().toString());
            embeddingStore.add(result.getEmbedding(), segment);
        }
        log.info("向量数据加载完成，共加载 {} 个地点", results.size());
    }

}
