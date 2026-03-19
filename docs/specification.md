# CreatorStimi 전체 명세서

## 1. 프로젝트 개요

### 배경
- **StepMon**: "1시간 이상 움직이지 않으면 알람해주는 앱" (OCI에서 운영 중, SpringBoot 3 + Java 21)
- **CreatorStimi**: StepMon 홍보용 YouTube Shorts 매일 2개씩 자동 생성+업로드 시스템

### 로테이션 규칙
- Day 1 (TEMPLATE): 배경이미지 슬라이드 + 자막 + TTS 음성
- Day 2 (HYBRID): AI 훅 영상(5초) + 템플릿 본문 + CTA
- Day 3 (FULL_AI): AI 클립 여러 개 이어붙이기 + 자막 오버레이
- 매일 2개 × 30일 = 월 60개 숏츠

### 영상 컨셉 풀 (8가지)
1. 공포/경고형: "하루 8시간 앉아있으면 생기는 일"
2. 공감형: "회사에서 3시간째 안 움직인 당신"
3. 팁/정보형: "1시간마다 일어나야 하는 과학적 이유"
4. 비포/애프터형: "앱 쓰기 전 vs 후 하루 걸음수"
5. 숫자형: "한국인 평균 좌식시간 OO시간, 당신은?"
6. 챌린지형: "1시간마다 일어나기 7일 챌린지"
7. 유머형: "사장님 몰래 스트레칭하는 법"
8. 리뷰형: "이 앱 깔고 달라진 점"

### AI 서비스 계정
- Claude Pro, Gemini Pro ×2, ChatGPT 유료
- Kling AI Pro 가입 예정 ($25.99/월, 3000 크레딧)

### 월 예상 비용: ~$26~31
| 항목 | 비용 |
|------|------|
| Kling Pro | $25.99 |
| Gemini API | $0 (무료 티어) |
| Edge-TTS | $0 |
| 이미지 생성 | $0~5 |
| YouTube API | $0 |

---

## 2. 인프라

### OCI 서버 (배포)
- OCPU 3, RAM 18GB (ARM Ampere A1)
- Docker, GitLab, Nginx, MySQL 8 설치됨
- StepMon 서버 운영 중, CreatorStimi는 환경만 구성된 상태

### 개발 환경
- Windows PC + Claude Code
- SpringBoot 기본 환경 구성 완료

---

## 3. 디렉토리 구조

```
CreatorStimi/
├── src/main/java/com/creatorstimi/
│   ├── CreatorStimiApplication.java
│   ├── controller/
│   │   └── GenerateController.java
│   ├── pipeline/
│   │   └── ShortsGenerationPipeline.java
│   ├── planner/
│   │   ├── ContentPlanner.java
│   │   └── ScriptGenerator.java
│   ├── asset/
│   │   ├── TtsGenerator.java
│   │   ├── ImageGenerator.java
│   │   ├── AiClipSelector.java
│   │   └── SubtitleGenerator.java
│   ├── renderer/
│   │   └── FfmpegRenderer.java
│   ├── uploader/
│   │   └── YoutubeUploader.java
│   ├── config/
│   │   ├── ApiKeyConfig.java
│   │   └── PipelineConfig.java
│   ├── domain/
│   │   ├── ShortsType.java          (enum: TEMPLATE, HYBRID, FULL_AI)
│   │   ├── ShortsProject.java       (JPA 엔티티)
│   │   ├── ConceptTemplate.java     (enum: 8가지 컨셉)
│   │   └── GenerationLog.java       (JPA 엔티티)
│   └── repository/
│       ├── ShortsProjectRepository.java
│       └── GenerationLogRepository.java
├── src/main/resources/
│   ├── application.yml
│   ├── prompts/                     (AI 프롬프트 템플릿)
│   └── assets/                      (기본 배경/소스)
├── assets/
│   └── ai-clips/                    (Kling 배치 클립)
│       ├── hook/
│       ├── scene/
│       └── cta/
├── output/                          (생성 영상 임시)
├── build.gradle
├── Dockerfile
└── docker-compose.yml
```

---

## 4. 모듈별 상세 설계

### ContentPlanner
- 입력: 현재 날짜
- 날짜 % 3 → 타입 결정 (0=TEMPLATE, 1=HYBRID, 2=FULL_AI)
- DB에서 최근 사용 컨셉 조회 → 미사용 컨셉 랜덤 선택
- ScriptGenerator 호출
- 출력: ShortsProject 객체

### ScriptGenerator (Gemini API)
- prompts/ 폴더에서 타입별 프롬프트 템플릿 로드
- Gemini 2.0 Flash API 호출 (JSON 형태 응답 요청)
- 엔드포인트: https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
- 인증: API Key
- 출력: 스크립트, 자막텍스트(타임코드), 제목, 설명, 태그, 이미지 프롬프트

### TtsGenerator
- Edge-TTS Python 패키지를 프로세스 호출
- 명령: edge-tts --voice ko-KR-SunHiNeural --text "..." --write-media output.mp3
- 한국어 음성: SunHiNeural(여), InJoonNeural(남), HyunsuNeural(남/차분)
- OCI에 Python3 + edge-tts 설치 필요

### ImageGenerator
- Gemini Imagen API로 배경 이미지 3~5장 생성
- 또는 Unsplash/Pexels API, 사전 준비 이미지 풀
- 1080×1920 리사이즈/크롭

### AiClipSelector
- assets/ai-clips/ 하위 폴더 스캔
- DB에서 사용 완료 클립 조회 → 미사용 클립 선택
- HYBRID: hook/에서 1개, cta/에서 0~1개
- FULL_AI: scene/에서 4~6개
- 클립 부족 시: 경고 로그 + TEMPLATE으로 폴백

### SubtitleGenerator
- ASS(Advanced SubStation Alpha) 형식
- 단어 단위 하이라이트 (카라오케 태그)
- 폰트: NanumSquareRound Bold, 크기 40~48
- 색상: 흰색, 하이라이트 노란색, 테두리 검정 2px

### FfmpegRenderer
- 상세: @docs/ffmpeg-rendering.md 참조

### YoutubeUploader
- google-api-services-youtube Java 라이브러리
- OAuth 2.0 리프레시 토큰 방식
- categoryId: 26(Howto) 또는 22(People&Blogs)
- #Shorts 태그 포함
- 일일 쿼터 10,000 유닛, 업로드 1회 1,600 유닛

---

## 5. application.yml 템플릿

```yaml
spring:
  datasource:
    url: jdbc:mysql://{OCI_IP}:3306/creatorstimi?useSSL=false&serverTimezone=Asia/Seoul
    username: {DB_USER}
    password: {DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
ai:
  gemini:
    api-key: {GEMINI_API_KEY}
    model: gemini-2.0-flash
youtube:
  client-id: {GOOGLE_CLIENT_ID}
  client-secret: {GOOGLE_CLIENT_SECRET}
  refresh-token: {GOOGLE_REFRESH_TOKEN}
paths:
  ai-clips: /home/creatorstimi/assets/ai-clips
  output: /home/creatorstimi/output
  fonts: /home/creatorstimi/fonts
  bgm: /home/creatorstimi/assets/bgm
ffmpeg:
  path: /usr/bin/ffmpeg
schedule:
  first-run: "0 0 9 * * *"
  second-run: "0 0 18 * * *"
tts:
  voice: ko-KR-SunHiNeural
  edge-tts-path: /usr/local/bin/edge-tts
```

## 6. build.gradle 핵심 의존성

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    runtimeOnly 'com.mysql:mysql-connector-j'
    implementation 'com.google.apis:google-api-services-youtube:v3-rev20240514-2.0.0'
    implementation 'com.google.oauth-client:google-oauth-client-jetty:1.36.0'
    implementation 'com.google.http-client:google-http-client-gson:1.44.2'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.11.0'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## 7. Kling AI 클립 운영

- Kling Pro $25.99/월 (3,000 크레딧)
- Professional 모드 5초 = ~35 크레딧 → 월 ~85클립
- 웹(app.klingai.com)에서 수동 배치 생성 → SCP로 OCI 업로드
- 네이밍: {카테고리}_{날짜}_{번호}_{설명}.mp4
- 예: hook_20260318_01_office-sitting.mp4
