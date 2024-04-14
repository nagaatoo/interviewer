package ru.numbdev.interviewer.service;

import ru.numbdev.interviewer.component.RoomObserver;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.enums.EventType;

import java.util.Map;
import java.util.UUID;

public interface GlobalCacheService {

    Map<Integer, ElementValues> offerInterview(UUID interviewId, RoomObserver room);
    void offerEvent(UUID interviewId, EventType type);
    void offerComponent(UUID interviewId, ElementValues value, boolean isChange);
    void offerDiff(UUID interviewId, UUID elementId, Map<Integer, String> diffs);

}
