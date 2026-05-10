package com.example.corner.repository;

import com.example.corner.entity.UserPlaceMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlaceMemoryRepository extends JpaRepository<UserPlaceMemory, Long> {
    List<UserPlaceMemory> findByUserId(Long userId);
    List<UserPlaceMemory> findByUserIdAndInteractionType(Long userId, String interactionType);
    Optional<UserPlaceMemory> findByUserIdAndPlaceId(Long userId, Long placeId);
    List<UserPlaceMemory> findByUserIdAndPlaceIdIn(Long userId, List<Long> placeIds);
}
