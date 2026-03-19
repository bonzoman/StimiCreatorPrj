package com.stimi.creator.shorts.pipeline;

import com.stimi.creator.shorts.asset.ImageGenerator;
import com.stimi.creator.shorts.asset.SubtitleGenerator;
import com.stimi.creator.shorts.asset.TtsGenerator;
import com.stimi.creator.shorts.config.ShortsConfig;
import com.stimi.creator.shorts.domain.ProjectStatus;
import com.stimi.creator.shorts.domain.ShortsProject;
import com.stimi.creator.shorts.domain.ShortsType;
import com.stimi.creator.shorts.planner.ContentPlanner;
import com.stimi.creator.shorts.renderer.FfmpegRenderer;
import com.stimi.creator.shorts.repository.ShortsProjectRepository;
import com.stimi.creator.shorts.uploader.YoutubeUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortsGenerationPipeline {

    private final ContentPlanner contentPlanner;
    private final TtsGenerator ttsGenerator;
    private final ImageGenerator imageGenerator;
    private final SubtitleGenerator subtitleGenerator;
    private final FfmpegRenderer ffmpegRenderer;
    private final YoutubeUploader youtubeUploader;
    private final ShortsProjectRepository projectRepository;
    private final ShortsConfig config;

    @Transactional
    public ShortsProject execute(LocalDate date) {
        ShortsProject project = null;
        Path workDir = null;

        try {
            // 1. 컨텐츠 기획 + 스크립트 생성
            log.info("=== Shorts 생성 파이프라인 시작: {} ===", date);
            project = contentPlanner.plan(date);
            project = projectRepository.save(project);
            log.info("프로젝트 생성: id={}, type={}, concept={}", project.getId(), project.getShortsType(), project.getConcept());

            // 2. 작업 디렉토리 생성
            workDir = Path.of(config.getPaths().getOutput(), "project_" + project.getId());
            Files.createDirectories(workDir);

            // 3. 에셋 생성
            project.setStatus(ProjectStatus.RENDERING);
            projectRepository.save(project);

            Path ttsFile = ttsGenerator.generate(project.getScriptText(), workDir);
            List<Path> bgImages = imageGenerator.generate(3, workDir);
            Path subtitleFile = subtitleGenerator.generate(project.getSubtitleText(), workDir);

            // 4. TEMPLATE 렌더링
            Path videoFile = ffmpegRenderer.renderTemplate(bgImages, subtitleFile, ttsFile, workDir);
            project.setVideoFilePath(videoFile.toString());

            // 5. YouTube 업로드
            project.setStatus(ProjectStatus.UPLOADING);
            projectRepository.save(project);

            YoutubeUploader.UploadResult result = youtubeUploader.upload(
                    videoFile, project.getTitle(), project.getDescription(), project.getTags()
            );

            // 6. 완료 처리
            project.markCompleted(result.videoId(), result.url());
            projectRepository.save(project);

            log.info("=== Shorts 생성 완료: id={}, url={} ===", project.getId(), result.url());
            return project;

        } catch (Exception e) {
            log.error("Shorts 생성 실패", e);
            if (project != null) {
                project.markFailed(e.getMessage());
                projectRepository.save(project);
            }
            throw new RuntimeException("파이프라인 실행 실패", e);
        } finally {
            cleanupWorkDir(workDir);
        }
    }

    private void cleanupWorkDir(Path workDir) {
        if (workDir == null) return;
        try {
            // 최종 영상 파일만 유지, 나머지 임시 파일 정리
            try (var files = Files.list(workDir)) {
                files.filter(f -> !f.getFileName().toString().equals("final_output.mp4"))
                        .forEach(f -> {
                            try { Files.deleteIfExists(f); } catch (IOException ignored) {}
                        });
            }
        } catch (IOException e) {
            log.warn("작업 디렉토리 정리 실패: {}", workDir, e);
        }
    }
}
