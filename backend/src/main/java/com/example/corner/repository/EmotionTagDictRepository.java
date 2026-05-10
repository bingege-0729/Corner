package com.example.corner.repository;

import com.example.corner.entity.EmotionTagDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmotionTagDictRepository extends JpaRepository<EmotionTagDict, Long> {
    List<EmotionTagDict> findAllByOrderByCategoryAscIdAsc();
}
