package com.stimi.creator.shorts.repository;

import com.stimi.creator.shorts.domain.AiClipUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AiClipUsageRepository extends JpaRepository<AiClipUsage, Long> {

    @Query("SELECT u.fileName FROM AiClipUsage u WHERE u.clipCategory = :category")
    List<String> findUsedFileNamesByCategory(String category);
}
