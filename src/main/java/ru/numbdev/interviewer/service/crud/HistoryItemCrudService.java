package ru.numbdev.interviewer.service.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.numbdev.interviewer.jpa.entity.HistoryBuilderItemEntity;
import ru.numbdev.interviewer.jpa.repository.HistoryBuilderItemRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryItemCrudService {

    private final HistoryBuilderItemRepository repository;

    public HistoryBuilderItemEntity save(HistoryBuilderItemEntity entity) {
        return repository.save(entity);
    }

    public List<HistoryBuilderItemEntity> findByInterviewerId(UUID interviewerId) {
        return repository.findByInterviewId(interviewerId);
    }
}
