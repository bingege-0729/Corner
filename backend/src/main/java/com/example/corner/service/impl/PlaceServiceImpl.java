package com.example.corner.service.impl;

import com.example.corner.dto.PlaceDetailResponse;
import com.example.corner.dto.UserHistory;
import com.example.corner.entity.EmotionTagDict;
import com.example.corner.entity.PlaceEmotionLibrary;
import com.example.corner.entity.PlaceTagRelation;
import com.example.corner.entity.UserPlaceMemory;
import com.example.corner.repository.EmotionTagDictRepository;
import com.example.corner.repository.PlaceEmotionLibraryRepository;
import com.example.corner.repository.PlaceTagRelationRepository;
import com.example.corner.repository.UserPlaceMemoryRepository;
import com.example.corner.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlaceServiceImpl implements PlaceService {
    
    @Autowired
    private PlaceEmotionLibraryRepository placeEmotionLibraryRepository;
    
    @Autowired
    private PlaceTagRelationRepository placeTagRelationRepository;
    
    @Autowired
    private EmotionTagDictRepository emotionTagDictRepository;
    
    @Autowired
    private UserPlaceMemoryRepository userPlaceMemoryRepository;
    
    /**
     * 地点详情
     */
    @Override
    public PlaceDetailResponse getPlaceDetail(Long userId, Long placeId) {
        PlaceEmotionLibrary place = placeEmotionLibraryRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("地点不存在"));
        
        // 获取标签
        List<PlaceTagRelation> relations = placeTagRelationRepository.findByPlaceId(placeId);
        List<Long> tagIds = relations.stream()
                .map(PlaceTagRelation::getTagId)
                .collect(Collectors.toList());
        List<EmotionTagDict> tags = emotionTagDictRepository.findAllById(tagIds);
        List<String> moodTags = tags.stream()
                .map(EmotionTagDict::getTagName)
                .collect(Collectors.toList());
        
        // 获取用户历史
        Optional<UserPlaceMemory> memoryOpt = userPlaceMemoryRepository.findByUserIdAndPlaceId(userId, placeId);
        UserHistory userHistory = new UserHistory();
        if (memoryOpt.isPresent()) {
            UserPlaceMemory memory = memoryOpt.get();
            userHistory.setHasVisited(true);
            userHistory.setVisitCount(1); // TODO: 统计访问次数
            userHistory.setLastVisited(memory.getVisitedAt() != null ? memory.getVisitedAt().toString() : "");
            userHistory.setYourRating(memory.getRating());
            userHistory.setYourFeedback(memory.getFeedback());
        } else {
            userHistory.setHasVisited(false);
            userHistory.setVisitCount(0);
        }
        
        PlaceDetailResponse response = new PlaceDetailResponse();
        response.setPlaceId(place.getId());
        response.setPlaceName(place.getPlaceName());
        response.setAddress(place.getAddress());
        response.setLatitude(place.getLatitude());
        response.setLongitude(place.getLongitude());
        response.setMoodTags(moodTags);
        response.setCrowdLevel(place.getCrowdLevel());
        response.setBestTime(place.getBestTime());
        response.setOneSentence(place.getOneSentence());
        response.setFullDescription(place.getFullDescription());
        response.setImageUrl(place.getImageUrl());
        response.setTips(place.getTips());
        response.setYourHistory(userHistory);
        
        return response;
    }
    
    /**
     * 获取所有地点（包含标签信息）
     */
    @Override
    public List<PlaceEmotionLibrary> getAllPlacesWithTags() {
        return placeEmotionLibraryRepository.findAll();
    }
}
