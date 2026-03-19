package com.stimi.creator.shorts.asset;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SubtitleGenerator {

    private static final String ASS_HEADER = """
            [Script Info]
            Title: StepMon Shorts
            ScriptType: v4.00+
            WrapStyle: 0
            PlayResX: 1080
            PlayResY: 1920
            ScaledBorderAndShadow: yes

            [V4+ Styles]
            Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding
            Style: Default,NanumSquareRound,44,&H00FFFFFF,&H000000FF,&H00000000,&H80000000,-1,0,0,0,100,100,0,0,1,2,1,2,40,40,200,1
            Style: Highlight,NanumSquareRound,48,&H0000FFFF,&H000000FF,&H00000000,&H80000000,-1,0,0,0,100,100,0,0,1,2,1,2,40,40,200,1

            [Events]
            Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
            """;

    public Path generate(String subtitleText, Path outputDir) {
        Path outputFile = outputDir.resolve("subtitle.ass");
        List<SubtitleLine> lines = parseSubtitleText(subtitleText);

        StringBuilder ass = new StringBuilder(ASS_HEADER);
        for (var line : lines) {
            String start = convertToAssTime(line.start);
            String end = convertToAssTime(line.end);
            ass.append("Dialogue: 0,").append(start).append(",").append(end)
                    .append(",Default,,0,0,0,,").append(line.text).append("\n");
        }

        try {
            Files.writeString(outputFile, ass.toString());
            log.info("ASS 자막 생성 완료: {} ({}개 라인)", outputFile, lines.size());
            return outputFile;
        } catch (IOException e) {
            throw new RuntimeException("자막 파일 생성 실패", e);
        }
    }

    private List<SubtitleLine> parseSubtitleText(String subtitleText) {
        List<SubtitleLine> lines = new ArrayList<>();
        if (subtitleText == null || subtitleText.isBlank()) return lines;

        for (String line : subtitleText.split("\n")) {
            String[] parts = line.split("\\|", 3);
            if (parts.length == 3) {
                lines.add(new SubtitleLine(parts[0].trim(), parts[1].trim(), parts[2].trim()));
            }
        }
        return lines;
    }

    private String convertToAssTime(String srtTime) {
        // 입력: "00:00:03.000" → ASS: "0:00:03.00"
        if (srtTime.length() >= 12) {
            return srtTime.substring(1, 11); // "0:00:03.00"
        }
        return srtTime;
    }

    private record SubtitleLine(String start, String end, String text) {}
}
