package ru.numbdev.interviewer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.numbdev.interviewer.service.crud.BuilderItemCrudService;

@Service
@RequiredArgsConstructor
public class BuilderService {

    private final BuilderItemCrudService builderItemCrudService;

}
