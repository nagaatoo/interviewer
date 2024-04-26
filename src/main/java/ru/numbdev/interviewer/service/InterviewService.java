package ru.numbdev.interviewer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.jpa.entity.InterviewEntity;
import ru.numbdev.interviewer.jpa.entity.RoomEntity;
import ru.numbdev.interviewer.service.crud.InterviewCrudService;
import ru.numbdev.interviewer.service.crud.QuestionsCrudService;
import ru.numbdev.interviewer.service.crud.RoomCrudService;
import ru.numbdev.interviewer.service.crud.TemplateCrudService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewCrudService interviewCrudService;
    private final RoomCrudService roomCrudService;
    private final QuestionsCrudService questionsCrudService;
    private final TemplateCrudService templateCrudService;

    @Transactional
    public String createInterview(
            String name,
            String interviewer,
            String hr,
            LocalDateTime date,
            UUID templateId,
            UUID questionnaireId
    ) {
        var room = roomCrudService.save(new RoomEntity());
        var interview = new InterviewEntity()
                .setName(name)
                .setStarted(false)
                .setInterviewerLogin(interviewer)
                .setHrLogin(hr)
                .setDate(date)
                .setTemplate(
                        templateId != null
                                ? templateCrudService.findById(templateId)
                                : null
                )
                .setQuestionnaire(
                        questionnaireId != null
                                ? questionsCrudService.findById(questionnaireId)
                                : null
                )
                .setRoom(room);
        interviewCrudService.save(interview);

        return room.getId().toString();
    }

    @Transactional
    public void finishInterview(List<ElementValues> values) {
        System.out.println("sdf");
    }

    @Transactional
    public void startInterview(UUID id) {
        var interview = interviewCrudService.getById(id);
        interviewCrudService.save(interview.setStarted(true));
    }
}
