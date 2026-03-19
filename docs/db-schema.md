# DB 스키마

## 데이터베이스: creatorstimi (MySQL 8, OCI 서버)

### shorts_project
```sql
CREATE TABLE shorts_project (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shorts_type VARCHAR(20) NOT NULL,         -- TEMPLATE, HYBRID, FULL_AI
    concept VARCHAR(50) NOT NULL,             -- 8가지 컨셉 중 하나
    title VARCHAR(200) NOT NULL,
    description TEXT,
    tags VARCHAR(500),
    script_text TEXT,
    subtitle_text TEXT,
    video_file_path VARCHAR(500),
    youtube_url VARCHAR(200),
    youtube_video_id VARCHAR(50),
    estimated_cost DECIMAL(10,4),
    rendering_time_seconds INT,
    status VARCHAR(20) DEFAULT 'PENDING',     -- PENDING, RENDERING, UPLOADING, COMPLETED, FAILED
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### ai_clip_usage
```sql
CREATE TABLE ai_clip_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(200) NOT NULL,
    clip_category VARCHAR(20) NOT NULL,       -- hook, scene, cta
    used_in_project_id BIGINT,
    used_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (used_in_project_id) REFERENCES shorts_project(id)
);
```

### generation_schedule
```sql
CREATE TABLE generation_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheduled_date DATE NOT NULL,
    sequence_number INT NOT NULL,             -- 1 또는 2
    shorts_type VARCHAR(20) NOT NULL,
    project_id BIGINT,
    status VARCHAR(20) DEFAULT 'SCHEDULED',   -- SCHEDULED, IN_PROGRESS, COMPLETED, FAILED
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES shorts_project(id)
);
```
