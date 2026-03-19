package com.stimi.creator.shorts.uploader;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.stimi.creator.shorts.config.ShortsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeUploader {

    private final ShortsConfig config;

    public record UploadResult(String videoId, String url) {}

    public UploadResult upload(Path videoFile, String title, String description, String tags) {
        try {
            YouTube youtube = buildYoutubeService();

            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(title);
            snippet.setDescription(description + "\n\n#Shorts #StepMon #건강");
            snippet.setTags(parseTags(tags));
            snippet.setCategoryId("26"); // Howto & Style

            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public");

            Video video = new Video();
            video.setSnippet(snippet);
            video.setStatus(status);

            InputStreamContent content = new InputStreamContent(
                    "video/mp4",
                    new FileInputStream(videoFile.toFile())
            );
            content.setLength(videoFile.toFile().length());

            log.info("YouTube 업로드 시작: title={}", title);
            YouTube.Videos.Insert insert = youtube.videos()
                    .insert(List.of("snippet", "status"), video, content);
            insert.getMediaHttpUploader().setDirectUploadEnabled(false);

            Video response = insert.execute();
            String videoId = response.getId();
            String url = "https://youtube.com/shorts/" + videoId;

            log.info("YouTube 업로드 완료: videoId={}, url={}", videoId, url);
            return new UploadResult(videoId, url);
        } catch (Exception e) {
            throw new RuntimeException("YouTube 업로드 실패", e);
        }
    }

    private YouTube buildYoutubeService() throws Exception {
        var youtubeConfig = config.getYoutube();

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(youtubeConfig.getClientId())
                .setClientSecret(youtubeConfig.getClientSecret())
                .setRefreshToken(youtubeConfig.getRefreshToken())
                .build();
        credentials.refresh();

        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("CreatorStimi").build();
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of("StepMon", "건강", "Shorts");
        }
        return Arrays.asList(tags.split(","));
    }
}
