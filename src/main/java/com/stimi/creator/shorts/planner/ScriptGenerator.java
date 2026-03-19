package com.stimi.creator.shorts.planner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stimi.creator.shorts.config.ShortsConfig;
import com.stimi.creator.shorts.domain.ConceptTemplate;
import com.stimi.creator.shorts.domain.ShortsType;
import com.stimi.creator.shorts.dto.ScriptResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScriptGenerator {

    private final ShortsConfig config;
    private final Gson gson = new Gson();
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public ScriptResult generate(ShortsType type, ConceptTemplate concept) {
        String prompt = buildPrompt(type, concept);
        String responseJson = callGeminiApi(prompt);
        return parseResponse(responseJson);
    }

    private String buildPrompt(ShortsType type, ConceptTemplate concept) {
        return """
                당신은 YouTube Shorts 전문 크리에이터입니다.
                StepMon 앱(1시간 이상 안 움직이면 알람해주는 건강 앱) 홍보용 30초 숏츠 스크립트를 작성해주세요.

                ## 조건
                - 영상 타입: %s
                - 컨셉: %s (%s)
                - 길이: 정확히 30초
                - 언어: 한국어
                - 톤: 친근하고 공감 가능한 톤
                - 반드시 StepMon 앱을 자연스럽게 언급
                - 마지막 5초는 CTA (앱 다운로드 유도)

                ## 응답 형식 (반드시 아래 JSON 형식으로만 응답)
                ```json
                {
                  "title": "영상 제목 (50자 이내, 호기심 유발)",
                  "description": "영상 설명 (100자 이내)",
                  "tags": ["태그1", "태그2", "태그3", "StepMon", "건강", "Shorts"],
                  "script": "전체 내레이션 텍스트",
                  "subtitles": [
                    {"start": "00:00:00.000", "end": "00:00:03.000", "text": "자막 내용"},
                    {"start": "00:00:03.000", "end": "00:00:06.000", "text": "자막 내용"}
                  ],
                  "imagePrompts": [
                    "배경이미지1: 사무실에서 오래 앉아있는 직장인, 피로한 표정",
                    "배경이미지2: 스마트폰 알람을 확인하며 기지개 펴는 사람",
                    "배경이미지3: 밝은 분위기, 건강하게 걷는 사람"
                  ]
                }
                ```
                """.formatted(type.name(), concept.getDisplayName(), concept.getSampleTitle());
    }

    private String callGeminiApi(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s"
                .formatted(config.getGemini().getModel(), config.getGemini().getApiKey());

        JsonObject requestBody = new JsonObject();
        JsonObject contents = new JsonObject();
        JsonObject parts = new JsonObject();
        parts.addProperty("text", prompt);
        contents.add("parts", gson.toJsonTree(new Object[]{parts}));
        requestBody.add("contents", gson.toJsonTree(new Object[]{contents}));

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.addProperty("temperature", 0.8);
        generationConfig.addProperty("maxOutputTokens", 2048);
        requestBody.add("generationConfig", generationConfig);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Gemini API 호출 실패: " + response.code() + " " + response.body().string());
            }
            String body = response.body().string();
            log.info("Gemini API 응답 수신 완료");
            return extractTextFromGeminiResponse(body);
        } catch (IOException e) {
            throw new RuntimeException("Gemini API 통신 오류", e);
        }
    }

    private String extractTextFromGeminiResponse(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        return root.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
    }

    private ScriptResult parseResponse(String json) {
        try {
            return gson.fromJson(json, ScriptResult.class);
        } catch (Exception e) {
            log.error("스크립트 JSON 파싱 실패: {}", json, e);
            throw new RuntimeException("스크립트 응답 파싱 실패", e);
        }
    }
}
