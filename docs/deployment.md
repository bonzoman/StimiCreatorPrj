# 배포 체크리스트

## OCI 서버 추가 설치

```bash
# FFmpeg
sudo apt update && sudo apt install -y ffmpeg

# Python3 + Edge-TTS
sudo apt install -y python3 python3-pip
pip3 install edge-tts

# 한국어 폰트
sudo apt install -y fonts-nanum
# NanumSquareRound은 별도 설치 필요 시 수동 다운로드

# 디렉토리 생성
mkdir -p /home/creatorstimi/assets/ai-clips/{hook,scene,cta}
mkdir -p /home/creatorstimi/output
mkdir -p /home/creatorstimi/fonts
mkdir -p /home/creatorstimi/assets/bgm
```

## Docker 구성

### Dockerfile
```dockerfile
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y ffmpeg python3 python3-pip fonts-nanum \
    && pip3 install edge-tts --break-system-packages \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY build/libs/creatorstimi-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
```yaml
version: '3.8'
services:
  creatorstimi:
    build: .
    ports:
      - "8081:8080"   # 8080은 StepMon이 쓸 수 있으므로 8081
    volumes:
      - /home/creatorstimi/assets:/app/assets
      - /home/creatorstimi/output:/app/output
      - /home/creatorstimi/fonts:/app/fonts
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - TZ=Asia/Seoul
    restart: unless-stopped
```

## 사전 준비 사항
- [ ] Google Cloud Console: YouTube Data API v3 활성화
- [ ] OAuth 동의 화면 설정 + 클라이언트 ID 생성
- [ ] 최초 OAuth 인증 → 리프레시 토큰 확보
- [ ] Gemini API Key 발급
- [ ] Kling AI Pro 가입
- [ ] MySQL에 creatorstimi DB + 테이블 생성
- [ ] BGM 파일 준비 (저작권 프리)
- [ ] LUT 파일 준비 (FULL_AI용 색보정)

## Nginx 리버스 프록시 (선택)
```nginx
server {
    listen 80;
    server_name creatorstimi.yourdomain.com;
    location / {
        proxy_pass http://localhost:8081;
    }
}
```
