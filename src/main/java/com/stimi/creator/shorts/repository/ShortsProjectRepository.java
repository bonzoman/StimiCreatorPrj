package com.stimi.creator.shorts.repository;

import com.stimi.creator.shorts.domain.ConceptTemplate;
import com.stimi.creator.shorts.domain.ShortsProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShortsProjectRepository extends JpaRepository<ShortsProject, Long> {

    @Query("SELECT DISTINCT p.concept FROM ShortsProject p ORDER BY p.createdAt DESC")
    List<ConceptTemplate> findRecentConcepts();

    @Query("SELECT DISTINCT p.concept FROM ShortsProject p WHERE p.status = 'COMPLETED' ORDER BY p.createdAt DESC LIMIT 5")
    List<ConceptTemplate> findRecentUsedConcepts();
}
