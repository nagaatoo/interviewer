package ru.numbdev.interviewer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.jpa.entity.HistoryBuilderItemEntity;
import ru.numbdev.interviewer.jpa.entity.InterviewEntity;
import ru.numbdev.interviewer.service.crud.HistoryItemCrudService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryItemCrudService historyItemCrudService;

    public void saveInterviewResult(InterviewEntity interview, List<ElementValues> values) {
        int count = 0;
        for (var value : values) {
            historyItemCrudService.save(toDto(interview, value, count));
            count += 1;
        }
    }

    public Map<Integer, ElementValues> getInterviewResult(UUID interviewId) {
        return historyItemCrudService
                .findByInterviewerId(interviewId)
                .stream()
                .collect(
                        Collectors.toMap(
                                HistoryBuilderItemEntity::getElementOrder,
                                this::entityToDto
                        )
                );
    }

    private ElementValues entityToDto(HistoryBuilderItemEntity entity) {
        return new ElementValues(
                entity.getId().toString(),
                entity.getElementType(),
                entity.getElementDescription(),
                entity.getElementValue()
        );
    }

    private HistoryBuilderItemEntity toDto(InterviewEntity interview, ElementValues v, int count) {
        return new HistoryBuilderItemEntity()
                .setInterview(interview)
                .setElementOrder(count)
                .setElementType(v.type())
                .setElementValue(v.value())
                .setElementDescription(v.description());
    }
}
