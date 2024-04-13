package ru.numbdev.interviewer.page.component.abstracts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.enums.AceTheme;
import org.apache.commons.lang3.StringUtils;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.dto.KeyValueRadioButton;
import ru.numbdev.interviewer.jpa.entity.ElementType;
import ru.numbdev.interviewer.page.component.CustomEditor;
import ru.numbdev.interviewer.page.component.CustomRadioButtonsGroup;
import ru.numbdev.interviewer.page.component.CustomTextArea;

import java.util.Arrays;

public abstract class AbstractBuilderComponent extends VerticalLayout {

    protected ElementValues buildValueFromComponent(Component component) {
        return switch (component) {
            case CustomTextArea ta -> valueFromTextArea(ta);
            case CustomRadioButtonsGroup rb -> valueFromRadioButton(rb);
            case CustomEditor te -> valueFromCodeEditor(te);
            default -> throw new UnsupportedOperationException();
        };
    }

    private ElementValues valueFromTextArea(CustomTextArea ta) {
        return new ElementValues(
                ta.getId().get(),
                ElementType.TEXT,
                ta.getLabel(),
                ta.getValue()
        );
    }

    private ElementValues valueFromRadioButton(CustomRadioButtonsGroup rb) {
        return new ElementValues(
                rb.getId().get(),
                ElementType.QUESTION,
                rb.getLabel(),
                parseValueFromRadioButton(null, rb.getValue())
        );
    }

    private ElementValues valueFromCodeEditor(CustomEditor te) {
        return new ElementValues(
                te.getId().get(),
                ElementType.CODE,
                null,
                te.getValue()
        );
    }

    protected Component buildElement(ElementValues value) {
        return buildElement(value.id(), value.type(), value.description(), value.value());
    }

    protected Component buildElement(String id, ElementType type, String description, String value) {
        return switch (type) {
            case QUESTION -> buildRadioButton(id, description, value);
            case TEXT -> buildTextField(id, description, value);
            case CODE -> buildCode(id, value);
        };
    }

    protected Component buildTextField(String id, String description, String value) {
        var area = new CustomTextArea();
        area.setId(id);
        area.setLabel(description);
        area.setValue(value);

        return area;
    }

    protected Component buildRadioButton(String id, String description, String value) {
        var parsedValue = parseRadioButtonFromValue(value);
        var radioButtonGroup = new CustomRadioButtonsGroup();
        radioButtonGroup.setId(id);
        radioButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        radioButtonGroup.setLabel(description);
        radioButtonGroup.setItems(parsedValue.getValues());
        radioButtonGroup.setValue(parsedValue.getSelected());

        return radioButtonGroup;
    }

    // Формат: Foo, Boo, #Coo#, Doo
    private KeyValueRadioButton parseRadioButtonFromValue(String value) {
        var builder = KeyValueRadioButton.builder();
        if (StringUtils.isBlank(value)) {
            return builder.build();
        }

        var parts = value.split(",");
        builder.values(
                Arrays
                        .stream(parts)
                        .peek(v -> {
                            if (v.contains("#")) {
                                builder.selected(v);
                            }
                        })
                        .toList()
        );

        return builder.build();
    }

    protected String parseValueFromRadioButton(String items, String selected) {
        if (StringUtils.isBlank(selected)) {
            return items.replace("#", "");
        }

        return items.replace(selected, "#" + selected + "#");
    }

    private Component buildCode(String id, String value) {
        var ace = new CustomEditor();
        ace.setId(id);
        ace.setTheme(AceTheme.github);
        ace.setMode(AceMode.java);
        ace.setValue(value);

        return ace;
    }
}
