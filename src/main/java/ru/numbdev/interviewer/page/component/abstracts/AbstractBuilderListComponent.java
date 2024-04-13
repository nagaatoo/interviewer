package ru.numbdev.interviewer.page.component.abstracts;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.jpa.entity.BuilderItemEntity;
import ru.numbdev.interviewer.jpa.entity.ElementType;
import ru.numbdev.interviewer.jpa.entity.QuestionnaireEntity;
import ru.numbdev.interviewer.jpa.entity.TemplateEntity;
import ru.numbdev.interviewer.enums.BuilderType;
import ru.numbdev.interviewer.page.component.CustomRadioButtonsGroup;
import ru.numbdev.interviewer.page.component.CustomTextArea;
import ru.numbdev.interviewer.service.crud.BuilderItemCrudService;

import java.text.MessageFormat;
import java.util.*;

public abstract class AbstractBuilderListComponent extends AbstractBuilderComponent {

    @Autowired // Чтобы не тащить через конструктор
    protected BuilderItemCrudService builderItemCrudService;

    private List<BuilderItemEntity> items = new ArrayList<>();

    protected void init(UUID entityId) {
        items = switch (getType()) {
            case QUESTIONNAIRE -> this.builderItemCrudService.getItemsForQuestionnaire(entityId);
            case TEMPLATE -> this.builderItemCrudService.getItemsForTemplate(entityId);
        };
    }

    protected HorizontalLayout initEditPanel(ComponentEventListener<ClickEvent<Button>> addListener) {
        var hl = new HorizontalLayout();

        var addButton = new Button();
        addButton.setText("Добавить " + getType().getName());
        addButton.addClickListener(addListener);
        hl.add(addButton);

        return hl;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    protected abstract BuilderType getType();

    protected void saveExistsElement(ElementValues values) {
        var entity = items
                .stream()
                .filter(i -> i.getId().equals(UUID.fromString(values.id())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(MessageFormat.format("Element with id {0} not found", values.id())));

        builderItemCrudService.save(
                entity
                        .setElementType(values.type())
                        .setElementValue(values.value())
                        .setElementDescription(values.description())
        );
    }

    protected void saveExistsElement(Component component) {
        var id = String.valueOf(component.getId());
        var entity = items
                .stream()
                .filter(i -> i.getId().equals(UUID.fromString(id)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(MessageFormat.format("Element with id {0} not found", id)));

        switch (entity.getElementType()) {
            case QUESTION -> saveValueFromRadioButton(entity, component);
            case TEXT -> saveValueFromTextArea(entity, component);
        }
    }

    private void saveValueFromRadioButton(BuilderItemEntity entity, Component component) {
        var radioButtonGroup = (CustomRadioButtonsGroup) component;
        entity.setElementDescription(radioButtonGroup.getLabel());
        entity.setElementValue(parseValueFromRadioButton(entity.getElementValue(), radioButtonGroup.getValue()));
    }

    private void saveValueFromTextArea(BuilderItemEntity entity, Component component) {
        var area = (CustomTextArea) component;
        builderItemCrudService.save(entity.setElementDescription(area.getLabel()).setElementValue(area.getValue()));
    }

    protected List<Component> getElements() {
        return items
                .stream()
                .sorted(Comparator.comparing(BuilderItemEntity::getCreated))
                .map(item ->
                        buildElement(
                                item.getId().toString(),
                                item.getElementType(),
                                item.getElementDescription(),
                                item.getElementValue()
                        )
                )
                .toList();
    }

    @SuppressWarnings("all")
    protected ElementValues getElementValue(String id) {
        return items
                .stream()
                .filter(e -> e.getId().toString().equals(id))
                .map(e ->
                        new ElementValues(
                                id,
                                e.getElementType(),
                                e.getElementDescription(),
                                e.getElementValue()
                        )
                )
                .findFirst()
                .get();
    }

    protected void createElement(ElementType type, String description, String value, QuestionnaireEntity entity) {
        var element = builderItemCrudService.save(createEntity(type, description, value).setQuestionnaire(entity));
        items.add(element);
    }

    protected void createElement(ElementType type, String description, String value, TemplateEntity entity) {
        var element = builderItemCrudService.save(createEntity(type, description, value).setTemplate(entity));
        items.add(element);
    }

    protected void deleteElement(String id) {
        var entity = items
                .stream()
                .filter(i -> i.getId().equals(UUID.fromString(id)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(MessageFormat.format("Element with id {0} not found", id)));

        builderItemCrudService.delete(entity);
        items.remove(entity);
    }

    private BuilderItemEntity createEntity(ElementType type, String description, String value) {
        return new BuilderItemEntity()
                .setElementDescription(description)
                .setElementType(type)
                .setElementValue(value);
    }

    public static List<String> parseValuesQuestion(String items) {
        return List.of(items.split(","));
    }
}
