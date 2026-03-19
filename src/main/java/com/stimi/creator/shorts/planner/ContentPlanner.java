package com.stimi.creator.shorts.planner;

import com.stimi.creator.shorts.domain.ConceptTemplate;
import com.stimi.creator.shorts.domain.ShortsProject;
import com.stimi.creator.shorts.domain.ShortsType;
import com.stimi.creator.shorts.dto.ScriptResult;
import com.stimi.creator.shorts.repository.ShortsProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentPlanner {

    private final ShortsProjectRepository projectRepository;
    private final ScriptGenerator scriptGenerator;

    public ShortsProject plan(LocalDate date) {
        ShortsType type = ShortsType.fromDayNumber(date.getDayOfYear());
        ConceptTemplate concept = selectConcept();

        log.info("컨텐츠 계획: date={}, type={}, concept={}", date, type, concept.getDisplayName());

        ScriptResult script = scriptGenerator.generate(type, concept);

        return ShortsProject.builder()
                .shortsType(type)
                .concept(concept)
                .title(script.title())
                .description(script.description())
                .tags(script.tagsAsString())
                .scriptText(script.script())
                .subtitleText(serializeSubtitles(script.subtitles()))
                .build();
    }

    private ConceptTemplate selectConcept() {
        List<ConceptTemplate> recentUsed = projectRepository.findRecentUsedConcepts();
        List<ConceptTemplate> available = Arrays.stream(ConceptTemplate.values())
                .filter(c -> !recentUsed.contains(c))
                .toList();

        if (available.isEmpty()) {
            available = Arrays.asList(ConceptTemplate.values());
        }

        return available.get(ThreadLocalRandom.current().nextInt(available.size()));
    }

    private String serializeSubtitles(List<ScriptResult.SubtitleEntry> subtitles) {
        if (subtitles == null) return "";
        StringBuilder sb = new StringBuilder();
        for (var entry : subtitles) {
            sb.append(entry.start()).append("|").append(entry.end()).append("|").append(entry.text()).append("\n");
        }
        return sb.toString();
    }
}
