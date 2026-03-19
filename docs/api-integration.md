# API 연동 상세

## Gemini 2.0 Flash (스크립트 생성)

### 엔드포인트
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key={API_KEY}
```

### 요청 형식
```json
{
  "contents": [{
    "parts": [{"text": "프롬프트 내용"}]
  }],
  "generationConfig": {
    "responseMimeType": "application/json",
    "temperature": 0.8,
    "maxOutputTokens": 2048
  }
}
```

### 응답에서 기대하는 JSON 구조
```json
{
  "title": "영상 제목",
  "description": "영상 설명",
  "tags": ["태그1", "태그2"],
  "script": "전체 스크립트 텍스트",
  "subtitles": [
    {"start": "00:00:00.000", "end": "00:00:03.000", "text": "자막 내용"}
  ],
  "imagePrompts": ["배경이미지1 프롬프트", "배경이미지2 프롬프트"]
}
```

### 무료 티어 제한
- 15 RPM, 1,500 RPD → 하루 2회 호출 충분

---

## Edge-TTS (음성 생성)

### 설치
```bash
pip3 install edge-tts
```

### Java에서 프로세스 호출
```java
ProcessBuilder pb = new ProcessBuilder(
    "edge-tts",
    "--voice", "ko-KR-SunHiNeural",
    "--text", scriptText,
    "--write-media", outputPath
);
Process process = pb.start();
process.waitFor();
```

### 한국어 음성 옵션
- ko-KR-SunHiNeural (여성, 자연스러움) ← 기본
- ko-KR-InJoonNeural (남성)
- ko-KR-HyunsuNeural (남성, 차분)

---

## YouTube Data API v3 (업로드)

### 라이브러리
```groovy
implementation 'com.google.apis:google-api-services-youtube:v3-rev20240514-2.0.0'
implementation 'com.google.oauth-client:google-oauth-client-jetty:1.36.0'
implementation 'com.google.http-client:google-http-client-gson:1.44.2'
```

### OAuth 2.0 설정
1. Google Cloud Console → YouTube Data API v3 활성화
2. OAuth 동의 화면 설정 (외부, 테스트 모드)
3. OAuth 클라이언트 ID 생성 (데스크톱 앱)
4. 최초 1회 브라우저 인증 → 리프레시 토큰 발급
5. application.yml에 client-id, client-secret, refresh-token 저장

### 업로드 코드 핵심
```java
Video video = new Video();
VideoSnippet snippet = new VideoSnippet();
snippet.setTitle(title);
snippet.setDescription(description + "\n\n#Shorts");
snippet.setTags(tags);
snippet.setCategoryId("26"); // Howto & Style
video.setSnippet(snippet);

VideoStatus status = new VideoStatus();
status.setPrivacyStatus("public");
video.setStatus(status);

InputStreamContent content = new InputStreamContent("video/mp4", new FileInputStream(videoFile));
YouTube.Videos.Insert insert = youtube.videos().insert("snippet,status", video, content);
Video response = insert.execute();
// response.getId() → YouTube video ID
```

### 쿼터
- 일일 기본: 10,000 유닛
- 업로드 1회: 1,600 유닛
- 하루 2개: 3,200 유닛 (여유)
