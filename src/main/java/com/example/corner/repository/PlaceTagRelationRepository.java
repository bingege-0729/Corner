package com.example.corner.repository;

import com.example.corner.entity.PlaceTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaceTagRelationRepository extends JpaRepository<PlaceTagRelation, Long> {
    List<PlaceTagRelation> findByTagId(Long tagId);
    List<PlaceTagRelation> findByPlaceId(Long placeId);
}
