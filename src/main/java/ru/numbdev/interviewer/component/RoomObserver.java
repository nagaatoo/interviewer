package ru.numbdev.interviewer.component;

import ru.numbdev.interviewer.dto.Message;

import java.util.UUID;

public interface RoomObserver {

    UUID getInterviewId();
    void doAction(Message message);
}
