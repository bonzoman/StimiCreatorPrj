# FFmpeg 렌더링 상세

## 공통 출력 스펙
- 해상도: 1080×1920 (9:16 세로)
- 프레임레이트: 30fps
- 코덱: H.264 (libx264), 프리셋 medium
- 오디오: AAC 128kbps
- 길이: 30초
- OCI ARM에서 libx264 소프트웨어 인코딩 (30초~1분 소요)

---

## TEMPLATE (순수 템플릿)

### 구조
```
[배경이미지 3~5장 슬라이드] + [ASS 자막 오버레이] + [TTS 음성] + [BGM]
```

### 자연스러움 테크닉
- **Ken Burns 효과**: zoompan 필터로 이미지 천천히 줌/패닝
- **이미지 전환**: xfade 필터로 fade/dissolve (0.5초)
- **자막**: ASS 형식, 단어 단위 하이라이트 (카라오케 스타일)
- **프로그레스 바**: drawbox 필터로 상단에 진행 표시
- **BGM**: 저작권 프리 BGM 낮은 볼륨 amix

### FFmpeg 참고 명령 (개념)
```bash
# Ken Burns + 슬라이드쇼
ffmpeg -loop 1 -i img1.jpg -loop 1 -i img2.jpg \
  -filter_complex "
    [0:v]zoompan=z='min(zoom+0.001,1.3)':d=180:s=1080x1920[v0];
    [1:v]zoompan=z='min(zoom+0.001,1.3)':d=180:s=1080x1920[v1];
    [v0][v1]xfade=transition=fade:duration=0.5:offset=5.5[vid];
    [vid]ass=subtitle.ass[final]
  " \
  -map "[final]" -map 1:a \
  -c:v libx264 -preset medium -c:a aac -shortest output.mp4
```

---

## HYBRID (하이브리드)

### 구조
```
[AI 훅 클립 0~5초] → [전환 0.3초] → [템플릿 본문 5~25초] → [CTA 25~30초]
```

### 자연스러움 테크닉
- **AI→본문 전환**: fade to black (0.3초) 또는 글리치/줌 트랜지션
- **본문 파트**: TEMPLATE과 동일한 모션 그래픽
- **CTA**: 앱 스크린샷 또는 AI 클립 + 다운로드 유도

### FFmpeg 참고
```bash
# concat + xfade 전환
ffmpeg -i hook.mp4 -i template_body.mp4 -i cta.mp4 \
  -filter_complex "
    [0:v][1:v]xfade=transition=fadeblack:duration=0.3:offset=4.7[v01];
    [v01][2:v]xfade=transition=fadeblack:duration=0.3:offset=24.7[vid];
    [vid]ass=subtitle.ass[final]
  " \
  -map "[final]" output.mp4
```

---

## FULL_AI (풀 AI)

### 구조
```
[AI클립1] + [AI클립2] + ... + [자막 오버레이] + [TTS 또는 BGM]
```

### 자연스러움 테크닉 (핵심!)
- **LUT 색보정**: 모든 클립에 동일 LUT 적용 → 색감 통일
- **크로스 디졸브**: 클립 사이 0.2~0.5초 xfade
- **자막 오버레이**: 시선 유도 → 클립 전환 덜 눈에 띄게
- **BGM**: 오디오 연속성으로 영상 끊김 보완

### FFmpeg 참고
```bash
# 멀티 클립 + LUT + xfade
ffmpeg -i clip1.mp4 -i clip2.mp4 -i clip3.mp4 ... \
  -filter_complex "
    [0:v]lut3d=cinematic.cube[c0];
    [1:v]lut3d=cinematic.cube[c1];
    [2:v]lut3d=cinematic.cube[c2];
    [c0][c1]xfade=transition=dissolve:duration=0.3:offset=4.7[v01];
    [v01][c2]xfade=transition=dissolve:duration=0.3:offset=9.4[vid];
    [vid]ass=subtitle.ass[final]
  " \
  -map "[final]" output.mp4
```

---

## 주의사항
- OCI ARM서버에서 하드웨어 가속 없음 → libx264 소프트웨어만 사용
- FFmpeg 명령어는 Java ProcessBuilder로 호출
- 임시 파일은 output/ 폴더에 생성 후 업로드 완료 시 정리
- ASS 자막 폰트는 서버에 NanumSquareRound 설치 필요
