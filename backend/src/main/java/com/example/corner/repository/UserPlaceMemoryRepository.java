package com.example.corner.repository;

import com.example.corner.entity.UserPlaceMemory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPlaceMemoryRepository extends JpaRepository<UserPlaceMemory, Long> {
    List<UserPlaceMemory> findByUserId(Long userId); // 根据用户ID查询所有记录
    List<UserPlaceMemory> findByUserIdAndInteractionType(Long userId, String interactionType); // 根据用户ID和互动类型查询所有记录
    Optional<UserPlaceMemory> findByUserIdAndPlaceId(Long userId, Long placeId);  // 根据用户ID和地点ID查询记录
    List<UserPlaceMemory> findByUserIdAndPlaceIdIn(Long userId, List<Long> placeIds); // 根据用户ID和地点ID列表查询记录
}
