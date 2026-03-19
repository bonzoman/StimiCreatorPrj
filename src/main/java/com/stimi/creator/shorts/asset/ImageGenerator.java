package com.stimi.creator.shorts.asset;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

@Slf4j
@Service
public class ImageGenerator {

    private static final int WIDTH = 1080;
    private static final int HEIGHT = 1920;
    private static final Color[] GRADIENT_COLORS = {
            new Color(26, 26, 46),    // 다크 네이비
            new Color(22, 33, 62),    // 딥 블루
            new Color(15, 52, 96),    // 미드나잇 블루
            new Color(52, 73, 94),    // 웻 애스팔트
            new Color(44, 62, 80),    // 다크 슬레이트
    };

    /**
     * 그라데이션 배경 이미지를 생성한다.
     * Phase 1에서는 Gemini Imagen 대신 단색/그라데이션 이미지를 사용한다.
     * 추후 AI 이미지 생성으로 교체 예정.
     */
    public List<Path> generate(int count, Path outputDir) {
        List<Path> images = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Path imgPath = outputDir.resolve("bg_%d.png".formatted(i));
            createGradientImage(imgPath, GRADIENT_COLORS[i % GRADIENT_COLORS.length]);
            images.add(imgPath);
        }
        log.info("배경 이미지 {}장 생성 완료", count);
        return images;
    }

    private void createGradientImage(Path path, Color baseColor) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        GradientPaint gradient = new GradientPaint(
                0, 0, baseColor,
                WIDTH, HEIGHT, baseColor.brighter().brighter()
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.dispose();

        try {
            ImageIO.write(img, "png", path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("배경 이미지 생성 실패: " + path, e);
        }
    }
}
