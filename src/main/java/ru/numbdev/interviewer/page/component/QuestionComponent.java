package ru.numbdev.interviewer.page.component;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.enums.BuilderType;
import ru.numbdev.interviewer.jpa.entity.QuestionnaireEntity;
import ru.numbdev.interviewer.page.component.abstracts.AbstractBuilderListComponent;
import ru.numbdev.interviewer.service.crud.QuestionsCrudService;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class QuestionComponent extends AbstractBuilderListComponent {

    private final QuestionsCrudService questionsCrudService;

    private boolean isEditable;
    private QuestionnaireEntity questionnaire;

    private final TextField questionnaireName = new TextField();
    private final Grid<com.vaadin.flow.component.Component> tasks = new Grid<>(
            com.vaadin.flow.component.Component.class, false);

    public void init(boolean isEditable, UUID questionnaireId) {
        this.isEditable = isEditable;
        if (questionnaireId != null) {
            super.init(questionnaireId);
            questionnaire = questionsCrudService.findById(questionnaireId);
        } else {
            questionnaire = questionsCrudService.save(new QuestionnaireEntity());
        }

        questionnaireName.setPlaceholder("Название опросника");
        questionnaireName.setValue("Новый опросник");
        questionnaireName.setValue(StringUtils.isNotBlank(questionnaire.getName()) ? questionnaire.getName() : "");
        questionnaireName.addBlurListener(e -> {
            if (StringUtils.isNotBlank(questionnaireName.getValue())) {
                questionsCrudService.save(questionnaire.setName(questionnaireName.getValue()));
            }
        });

        add(questionnaireName);
        add(tasks);
        if (isEditable) {
            add(initEditPanel(e -> addAction()));
        }
        buildRows();
        setSizeFull();
    }

    private void addAction() {
        add(new CreateElementDialogComponent(getSaveAction(), getRemoveAction()));
    }

    private void buildRows() {
        buildDataProvider();
        if (isEditable) {
            tasks.addItemDoubleClickListener(chooseElement());
        }
    }

    private ComponentEventListener<ItemDoubleClickEvent<com.vaadin.flow.component.Component>> chooseElement() {
        return e -> {
            add(new CreateElementDialogComponent(
                            getSaveAction(),
                            getRemoveAction(),
                            getElementValue(e.getItem().getId().get())
                    )
            );
        };
    }

    private ElementAction<ElementValues> getSaveAction() {
        return a -> {
            if (a.id() != null) {
                saveExistsElement(a);
            } else {
                createElement(a.type(), a.description(), a.value(), questionnaire);
            }

            refresh();
        };
    }

    private ElementAction<ElementValues> getRemoveAction() {
        return a -> {
            deleteElement(a.id());
            refresh();
        };
    }

    private void buildDataProvider() {
        tasks.addComponentColumn(e -> e);
        refresh();
    }

    private void refresh() {
        tasks.setItems(
                getElements()
                        .stream()
                        .peek(this::muteElement)
                        .toList()
        );
    }

    private void muteElement(com.vaadin.flow.component.Component component) {
        if (component instanceof RadioButtonGroup<?> rb) {
            rb.setReadOnly(true);
        }

        if (component instanceof TextArea ta) {
            ta.setReadOnly(true);
        }
    }

    @Override
    protected BuilderType getType() {
        return BuilderType.QUESTIONNAIRE;
    }
}
