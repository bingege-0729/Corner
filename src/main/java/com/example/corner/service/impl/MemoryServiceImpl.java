package com.example.corner.service.impl;

import com.example.corner.dto.FeedbackRequest;
import com.example.corner.dto.MemoryItem;
import com.example.corner.dto.MemoryListResponse;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.service.MemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MemoryServiceImpl implements MemoryService {
    
    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;
    
    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;
    
    /**
     * 推荐反馈
     */
    @Override
    @Transactional
    public void feedback(Long userId, FeedbackRequest request) {
        UserPlaceMemory memory = userPlaceMemoryRepository
                .findByUserIdAndPlaceId(userId, request.getPlaceId())
                .orElse(new UserPlaceMemory());
        
        memory.setUserId(userId);
        memory.setPlaceId(request.getPlaceId());
        
        // 转换 action 到 interaction_type
        String interactionType;
        switch (request.getAction()) {
            case "liked":
                interactionType = "BOOKMARKED";
                break;
            case "disliked":
                interactionType = "DISLIKED";
                break;
            case "visited":
                interactionType = "VISITED";
                break;
            default:
                interactionType = "VISITED";
        }
        
        memory.setInteractionType(interactionType);
        memory.setRating(request.getRating());
        memory.setFeedback(request.getFeedback());
        memory.setVisitedAt(LocalDate.now());
        
        if (memory.getId() == null) {
            memory.setCreatedAt(LocalDateTime.now());
        }
        memory.setUpdatedAt(LocalDateTime.now());
        
        userPlaceMemoryRepository.save(memory);
    }
    
    /**
     * 我的记忆列表
     */
    @Override
    public MemoryListResponse getMemoryList(Long userId, String type) {
        List<UserPlaceMemory> memories;
        
        if (type != null && !type.isEmpty()) {
            String interactionType;
            if ("visited".equals(type)) {
                interactionType = "VISITED";
            } else if ("bookmarked".equals(type)) {
                interactionType = "BOOKMARKED";
            } else {
                interactionType = null;
            }
            
            if (interactionType != null) {
                memories = userPlaceMemoryRepository.findByUserIdAndInteractionType(userId, interactionType);
            } else {
                memories = userPlaceMemoryRepository.findByUserId(userId);
            }
        } else {
            memories = userPlaceMemoryRepository.findByUserId(userId);
        }
        
        List<Long> placeIds = memories.stream()
                .map(UserPlaceMemory::getPlaceId)
                .collect(Collectors.toList());
        
        List<PlaceEmotionLibrary> places = placeEmotionLibraryRepository.findAllById(placeIds);
        Map<Long, PlaceEmotionLibrary> placeMap = places.stream()
                .collect(Collectors.toMap(PlaceEmotionLibrary::getId, p -> p));
        
        List<MemoryItem> items = memories.stream().map(memory -> {
            PlaceEmotionLibrary place = placeMap.get(memory.getPlaceId());
            MemoryItem item = new MemoryItem();
            item.setMemoryId(memory.getId());
            item.setPlaceId(memory.getPlaceId());
            item.setPlaceName(place != null ? place.getPlaceName() : "");
            item.setImageUrl(place != null ? place.getImageUrl() : "");
            item.setInteractionType(memory.getInteractionType());
            item.setRating(memory.getRating());
            item.setFeedback(memory.getFeedback());
            item.setVisitedAt(memory.getVisitedAt() != null ? memory.getVisitedAt().toString() : "");
            return item;
        }).collect(Collectors.toList());
        
        MemoryListResponse response = new MemoryListResponse();
        response.setList(items);
        
        return response;
    }
}
