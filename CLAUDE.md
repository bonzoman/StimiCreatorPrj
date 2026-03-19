# CreatorStimi

StepMon(1시간 미움직임 알람 앱) 홍보용 YouTube Shorts 자동 생성+업로드 시스템.
매일 2개씩, 3가지 스타일(TEMPLATE/HYBRID/FULL_AI) 3일 주기 로테이션.

## 기술 스택
- SpringBoot 3 + Java 21 + Gradle
- MySQL 8 (OCI 원격), JPA
- FFmpeg (영상 합성), Edge-TTS (Python, 한국어 음성)
- Gemini 2.0 Flash API (스크립트 생성)
- YouTube Data API v3 (업로드)
- Kling AI Pro (AI 영상 클립 - 웹에서 수동 배치 생성, 서버에 업로드)

## 운영 환경
- 배포: OCI ARM서버 (OCPU 3, RAM 18GB), Docker
- 기존 인프라: Docker, GitLab, Nginx, MySQL 8 설치됨
- 개발: Windows PC + Claude Code

## 코딩 컨벤션
- Java 21 최신 문법 활용 (record, sealed class, pattern matching)
- 코드 복잡도보다 품질 우선 (개발자 숙련자)
- API 키는 application.yml에서만 관리, 하드코딩 금지
- Windows 개발 → Linux 배포, 경로 구분자 주의

## 프로젝트 구조
```
src/main/java/com/creatorstimi/
├── controller/GenerateController.java     ← REST + cron 트리거
├── pipeline/ShortsGenerationPipeline.java ← 전체 오케스트레이션
├── planner/{ContentPlanner,ScriptGenerator}.java
├── asset/{TtsGenerator,ImageGenerator,AiClipSelector,SubtitleGenerator}.java
├── renderer/FfmpegRenderer.java
├── uploader/YoutubeUploader.java
├── config/{ApiKeyConfig,PipelineConfig}.java
├── domain/{ShortsType,ShortsProject,ConceptTemplate,GenerationLog}.java
└── repository/{ShortsProjectRepository,GenerationLogRepository}.java
```

## 파이프라인 흐름
1. ContentPlanner: 날짜%3으로 타입 결정 → 8가지 컨셉 중 선택 → Gemini로 스크립트 생성
2. AssetGenerator: TTS 음성 + 배경이미지 + ASS자막 + AI클립 선택(HYBRID/FULL_AI)
3. FfmpegRenderer: 타입별 FFmpeg 합성 (1080×1920, 30fps, H.264, 30초)
4. YoutubeUploader: YouTube Data API v3 업로드
5. DB 로깅: 이력, 비용, 상태 기록

## 구현 순서
- Phase 1: TEMPLATE 파이프라인 (전체 골격)
- Phase 2: HYBRID 추가 (AI 클립 선택 + 전환 효과)
- Phase 3: FULL_AI 추가 (멀티 클립 + LUT 색보정)
- Phase 4: cron 자동화 + Docker 배포 + 모니터링

## 상세 문서 위치
- 전체 명세서: @docs/specification.md
- DB 스키마: @docs/db-schema.md
- FFmpeg 렌더링 상세: @docs/ffmpeg-rendering.md
- API 연동 상세: @docs/api-integration.md
- 배포 체크리스트: @docs/deployment.md
