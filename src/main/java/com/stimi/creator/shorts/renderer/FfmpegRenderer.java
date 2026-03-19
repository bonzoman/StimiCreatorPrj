package com.stimi.creator.shorts.renderer;

import com.stimi.creator.shorts.config.ShortsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FfmpegRenderer {

    private final ShortsConfig config;

    /**
     * TEMPLATE 타입 렌더링: 배경이미지 슬라이드 + Ken Burns + 자막 + TTS
     */
    public Path renderTemplate(List<Path> bgImages, Path subtitleFile, Path ttsAudio, Path outputDir) {
        Path outputFile = outputDir.resolve("final_output.mp4");
        long startTime = System.currentTimeMillis();

        List<String> command = buildTemplateCommand(bgImages, subtitleFile, ttsAudio, outputFile);

        log.info("FFmpeg TEMPLATE 렌더링 시작: 이미지 {}장", bgImages.size());
        executeFFmpeg(command);

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        log.info("FFmpeg 렌더링 완료: {}초 소요, 출력: {}", elapsed, outputFile);
        return outputFile;
    }

    private List<String> buildTemplateCommand(List<Path> bgImages, Path subtitleFile, Path ttsAudio, Path outputFile) {
        List<String> cmd = new ArrayList<>();
        cmd.add(config.getFfmpeg().getPath());

        // 이미지 입력 (각각 loop)
        for (Path img : bgImages) {
            cmd.addAll(List.of("-loop", "1", "-t", "10", "-i", img.toString()));
        }
        // TTS 오디오 입력
        cmd.addAll(List.of("-i", ttsAudio.toString()));

        int imgCount = bgImages.size();
        float durationPerImage = 30.0f / imgCount;

        StringBuilder filterComplex = new StringBuilder();

        // Ken Burns 효과 (zoompan) 적용
        for (int i = 0; i < imgCount; i++) {
            filterComplex.append("[%d:v]zoompan=z='min(zoom+0.0005,1.2)':d=%d:s=1080x1920:fps=30[v%d];".formatted(
                    i, (int) (durationPerImage * 30), i));
        }

        // xfade 전환 연결
        if (imgCount == 1) {
            filterComplex.append("[v0]trim=duration=30[vid];");
        } else {
            String prev = "[v0]";
            for (int i = 1; i < imgCount; i++) {
                String outLabel = (i == imgCount - 1) ? "[merged]" : "[xf%d]".formatted(i);
                float offset = durationPerImage * i - 0.5f;
                filterComplex.append("%s[v%d]xfade=transition=fade:duration=0.5:offset=%.1f%s;".formatted(
                        prev, i, offset, outLabel));
                prev = outLabel;
            }
            filterComplex.append("[merged]trim=duration=30[vid];");
        }

        // 자막 오버레이
        String subtitlePath = subtitleFile.toString().replace("\\", "/").replace(":", "\\:");
        filterComplex.append("[vid]ass='%s'[final]".formatted(subtitlePath));

        cmd.addAll(List.of("-filter_complex", filterComplex.toString()));
        cmd.addAll(List.of("-map", "[final]", "-map", "%d:a".formatted(imgCount)));
        cmd.addAll(List.of(
                "-c:v", "libx264", "-preset", "medium",
                "-c:a", "aac", "-b:a", "128k",
                "-r", "30",
                "-t", "30",
                "-shortest",
                "-y", outputFile.toString()
        ));

        return cmd;
    }

    private void executeFFmpeg(List<String> command) {
        log.debug("FFmpeg 명령어: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("FFmpeg 실행 실패: exitCode={}\n{}", exitCode, output);
                throw new RuntimeException("FFmpeg 렌더링 실패: exitCode=" + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("FFmpeg 실행 오류", e);
        }
    }
}
