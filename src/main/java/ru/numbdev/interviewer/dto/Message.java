package ru.numbdev.interviewer.dto;

import ru.numbdev.interviewer.enums.EventType;

import java.util.Map;

public record Message(
        EventType event,
        ElementValues value,
        Map<Integer, String> diffs
) {
}
