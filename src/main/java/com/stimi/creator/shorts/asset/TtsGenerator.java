package com.stimi.creator.shorts.asset;

import com.stimi.creator.shorts.config.ShortsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsGenerator {

    private final ShortsConfig config;

    public Path generate(String scriptText, Path outputDir) {
        Path outputFile = outputDir.resolve("tts_voice.mp3");

        ProcessBuilder pb = new ProcessBuilder(
                config.getTts().getEdgeTtsPath(),
                "--voice", config.getTts().getVoice(),
                "--text", scriptText,
                "--write-media", outputFile.toString()
        );
        pb.redirectErrorStream(true);

        try {
            log.info("TTS 생성 시작: voice={}", config.getTts().getVoice());
            Process process = pb.start();
            String processOutput = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("TTS 생성 실패: exitCode={}, output={}", exitCode, processOutput);
                throw new RuntimeException("Edge-TTS 실행 실패: exitCode=" + exitCode);
            }

            log.info("TTS 생성 완료: {}", outputFile);
            return outputFile;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("TTS 생성 오류", e);
        }
    }
}
