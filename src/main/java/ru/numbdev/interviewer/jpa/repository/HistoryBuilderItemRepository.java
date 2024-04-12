package ru.numbdev.interviewer.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.numbdev.interviewer.jpa.entity.HistoryBuilderItemEntity;

import java.util.UUID;

public interface HistoryBuilderItemRepository extends JpaRepository<HistoryBuilderItemEntity, UUID> {
}
