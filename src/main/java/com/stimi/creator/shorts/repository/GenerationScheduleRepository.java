package com.stimi.creator.shorts.repository;

import com.stimi.creator.shorts.domain.GenerationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GenerationScheduleRepository extends JpaRepository<GenerationSchedule, Long> {

    List<GenerationSchedule> findByScheduledDateAndStatus(LocalDate date, String status);

    boolean existsByScheduledDateAndSequenceNumber(LocalDate date, int sequenceNumber);
}
