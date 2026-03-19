package com.stimi.creator.shorts.domain;

public enum ShortsType {
    TEMPLATE,   // Day 1: 배경이미지 슬라이드 + 자막 + TTS
    HYBRID,     // Day 2: AI 훅 영상 + 템플릿 본문 + CTA
    FULL_AI;    // Day 3: AI 클립 여러 개 + 자막 오버레이

    public static ShortsType fromDayNumber(int dayOfYear) {
        return values()[dayOfYear % 3];
    }
}
