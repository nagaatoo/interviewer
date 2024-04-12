package ru.numbdev.interviewer.service.crud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.numbdev.interviewer.jpa.entity.HistoryBuilderItemEntity;
import ru.numbdev.interviewer.jpa.repository.HistoryBuilderItemRepository;

@Service
@RequiredArgsConstructor
public class HistoryItemCrudService {

    private final HistoryBuilderItemRepository repository;

    public HistoryBuilderItemEntity save(HistoryBuilderItemEntity entity) {
        return repository.save(entity);
    }

}
