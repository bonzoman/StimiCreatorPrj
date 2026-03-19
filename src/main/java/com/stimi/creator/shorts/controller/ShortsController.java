package com.stimi.creator.shorts.controller;

import com.stimi.creator.shorts.domain.ShortsProject;
import com.stimi.creator.shorts.pipeline.ShortsGenerationPipeline;
import com.stimi.creator.shorts.repository.ShortsProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
public class ShortsController {

    private final ShortsGenerationPipeline pipeline;
    private final ShortsProjectRepository projectRepository;

    /**
     * 수동 트리거: 특정 날짜 기준으로 숏츠 생성
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(
            @RequestParam(required = false) String date
    ) {
        LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();

        try {
            ShortsProject project = pipeline.execute(targetDate);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "projectId", project.getId(),
                    "title", project.getTitle(),
                    "youtubeUrl", project.getYoutubeUrl() != null ? project.getYoutubeUrl() : "",
                    "shortsType", project.getShortsType().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 프로젝트 이력 조회
     */
    @GetMapping("/projects")
    public ResponseEntity<List<ShortsProject>> listProjects() {
        return ResponseEntity.ok(projectRepository.findAll());
    }

    /**
     * 개별 프로젝트 조회
     */
    @GetMapping("/projects/{id}")
    public ResponseEntity<ShortsProject> getProject(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 매일 오전 9시 자동 실행 (1번째 영상)
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void scheduledFirstRun() {
        log.info("스케줄 실행: 오전 9시 (1번째 영상)");
        try {
            pipeline.execute(LocalDate.now());
        } catch (Exception e) {
            log.error("스케줄 실행 실패 (1번째)", e);
        }
    }

    /**
     * 매일 오후 6시 자동 실행 (2번째 영상)
     */
    @Scheduled(cron = "0 0 18 * * *")
    public void scheduledSecondRun() {
        log.info("스케줄 실행: 오후 6시 (2번째 영상)");
        try {
            pipeline.execute(LocalDate.now());
        } catch (Exception e) {
            log.error("스케줄 실행 실패 (2번째)", e);
        }
    }
}
