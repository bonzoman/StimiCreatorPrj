package com.stimi.creator.shorts.dto;

import java.util.List;

public record ScriptResult(
        String title,
        String description,
        List<String> tags,
        String script,
        List<SubtitleEntry> subtitles,
        List<String> imagePrompts
) {
    public record SubtitleEntry(String start, String end, String text) {}

    public String tagsAsString() {
        return tags != null ? String.join(",", tags) : "";
    }
}
