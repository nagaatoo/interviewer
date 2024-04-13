package ru.numbdev.interviewer.page.component;

import com.vaadin.flow.component.textfield.TextArea;
import ru.numbdev.interviewer.page.component.abstracts.EditableComponent;

import java.util.Map;

public class CustomTextArea extends TextArea implements EditableComponent {

    @Override
    public Map<Integer, String> getDiff(String actualState) {
        return Map.of(1, actualState);
    }

    @Override
    public void offerDiff(Map<Integer, String> diff) {
        setValue(diff.get(1));
    }
}
