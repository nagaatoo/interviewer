package ru.numbdev.interviewer.dto;

import ru.numbdev.interviewer.jpa.entity.ElementType;

public record ElementValues(
        String id,
        ElementType type,
        String description,
        String value
) {
}
