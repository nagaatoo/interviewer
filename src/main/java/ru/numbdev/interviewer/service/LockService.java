package ru.numbdev.interviewer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.numbdev.interviewer.service.crud.EditLockCrudService;

@Service
@RequiredArgsConstructor
public class LockService {

    private final EditLockCrudService editLockCrudService;

}
