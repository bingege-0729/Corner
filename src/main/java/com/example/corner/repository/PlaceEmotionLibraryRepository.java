package com.example.corner.repository;

import com.example.corner.entity.PlaceEmotionLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceEmotionLibraryRepository extends JpaRepository<PlaceEmotionLibrary, Long> {
}
