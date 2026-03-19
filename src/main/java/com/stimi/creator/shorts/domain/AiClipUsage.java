package com.stimi.creator.shorts.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_clip_usage")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiClipUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    @Column(name = "clip_category", nullable = false, length = 20)
    private String clipCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_in_project_id")
    private ShortsProject usedInProject;

    @CreationTimestamp
    @Column(name = "used_at")
    private LocalDateTime usedAt;
}
