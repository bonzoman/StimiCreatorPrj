package com.stimi.creator.shorts.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "shorts")
@Getter @Setter
public class ShortsConfig {

    private Gemini gemini = new Gemini();
    private Youtube youtube = new Youtube();
    private Paths paths = new Paths();
    private Ffmpeg ffmpeg = new Ffmpeg();
    private Tts tts = new Tts();

    @Getter @Setter
    public static class Gemini {
        private String apiKey;
        private String model = "gemini-2.0-flash";
    }

    @Getter @Setter
    public static class Youtube {
        private String clientId;
        private String clientSecret;
        private String refreshToken;
    }

    @Getter @Setter
    public static class Paths {
        private String aiClips = "./assets/ai-clips";
        private String output = "./output";
        private String fonts = "./fonts";
        private String bgm = "./assets/bgm";
    }

    @Getter @Setter
    public static class Ffmpeg {
        private String path = "ffmpeg";
    }

    @Getter @Setter
    public static class Tts {
        private String voice = "ko-KR-SunHiNeural";
        private String edgeTtsPath = "edge-tts";
    }
}
