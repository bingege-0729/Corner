package com.example.corner.repository;

import com.example.corner.entity.PlaceTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaceTagRelationRepository extends JpaRepository<PlaceTagRelation, Long> {
    List<PlaceTagRelation> findByTagId(Long tagId); //根据标签ID找合适的地点
    List<PlaceTagRelation> findByPlaceId(Long placeId); //根据地点ID给地点信息展示提供情绪标签
}
