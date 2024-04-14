package ru.numbdev.interviewer.page.component;

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import ru.numbdev.interviewer.page.component.abstracts.EditableComponent;

import java.util.Map;

public class CustomRadioButtonsGroup extends RadioButtonGroup<String> implements EditableComponent {

    @Override
    public Map<Integer, String> getDiff(String actualState) {
        return Map.of(1, actualState);
    }

    @Override
    public void offerDiff(Map<Integer, String> diff) {
        setValue(diff.get(1));
    }
}
