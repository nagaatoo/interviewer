package ru.numbdev.interviewer.page.component;

import com.vaadin.flow.component.textfield.TextArea;
import ru.numbdev.interviewer.component.ElementObserver;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.enums.EventType;

import java.util.Map;
import java.util.Optional;

public class CustomTextArea extends TextArea {

    public void doAction(EventType type, Map<Integer, ElementValues> value) {
        setValue(
                Optional
                        .ofNullable(value.get(0))
                        .orElseGet(() -> new ElementValues(null, null, null, null))
                        .value()
        );
    }
}
