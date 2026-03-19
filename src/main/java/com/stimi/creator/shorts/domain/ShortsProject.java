package com.stimi.creator.shorts.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shorts_project")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ShortsProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "shorts_type", nullable = false, length = 20)
    private ShortsType shortsType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ConceptTemplate concept;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String tags;

    @Column(name = "script_text", columnDefinition = "TEXT")
    private String scriptText;

    @Column(name = "subtitle_text", columnDefinition = "TEXT")
    private String subtitleText;

    @Column(name = "video_file_path", length = 500)
    private String videoFilePath;

    @Column(name = "youtube_url", length = 200)
    private String youtubeUrl;

    @Column(name = "youtube_video_id", length = 50)
    private String youtubeVideoId;

    @Column(name = "estimated_cost", precision = 10, scale = 4)
    private BigDecimal estimatedCost;

    @Column(name = "rendering_time_seconds")
    private Integer renderingTimeSeconds;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void markFailed(String errorMessage) {
        this.status = ProjectStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markCompleted(String youtubeVideoId, String youtubeUrl) {
        this.status = ProjectStatus.COMPLETED;
        this.youtubeVideoId = youtubeVideoId;
        this.youtubeUrl = youtubeUrl;
    }
}
