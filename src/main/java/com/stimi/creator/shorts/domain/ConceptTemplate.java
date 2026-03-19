package com.stimi.creator.shorts.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConceptTemplate {
    FEAR_WARNING("공포/경고형", "하루 8시간 앉아있으면 생기는 일"),
    EMPATHY("공감형", "회사에서 3시간째 안 움직인 당신"),
    TIP_INFO("팁/정보형", "1시간마다 일어나야 하는 과학적 이유"),
    BEFORE_AFTER("비포/애프터형", "앱 쓰기 전 vs 후 하루 걸음수"),
    NUMBER_STAT("숫자형", "한국인 평균 좌식시간 OO시간, 당신은?"),
    CHALLENGE("챌린지형", "1시간마다 일어나기 7일 챌린지"),
    HUMOR("유머형", "사장님 몰래 스트레칭하는 법"),
    REVIEW("리뷰형", "이 앱 깔고 달라진 점");

    private final String displayName;
    private final String sampleTitle;
}
