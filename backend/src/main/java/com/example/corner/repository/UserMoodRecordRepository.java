package com.example.corner.repository;

import com.example.corner.entity.UserMoodRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserMoodRecordRepository extends JpaRepository<UserMoodRecord, Long> {
    List<UserMoodRecord> findByUserId(Long userId);
}
