package ru.numbdev.interviewer.page.component.abstracts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import org.springframework.util.CollectionUtils;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.page.component.CurrentTaskComponent;
import ru.numbdev.interviewer.page.component.CustomEditor;
import ru.numbdev.interviewer.page.component.CustomRadioButtonsGroup;
import ru.numbdev.interviewer.page.component.CustomTextArea;
import ru.numbdev.interviewer.service.GlobalCacheService;

import java.util.*;

public abstract class AbstractInterviewComponent extends AbstractBuilderComponent {

    @Getter
    private final List<Component> components = new ArrayList<>();
    private CurrentTaskComponent currentTaskComponent;

    private Button previewButton;
    private Button nextButton;

    @Getter
    private int currentIdx = 0;

    protected void initCurrentOnly(String msg) {
        currentTaskComponent = new CurrentTaskComponent(msg);
        add(currentTaskComponent);
        setAlignSelf(Alignment.CENTER, currentTaskComponent);
        currentTaskComponent.setSizeFull();
    }

    protected void initFull(String msg) {
        currentTaskComponent = new CurrentTaskComponent(msg);
        currentTaskComponent.setSizeFull();

        var endInterviewButton = new Button("Завершить интервью");
        endInterviewButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        endInterviewButton.addClickListener(e -> finish());
        initControlButtons();

        if (components.isEmpty()) {
            nextButton.setEnabled(false);
            previewButton.setEnabled(false);
        }

        var buttonLayout = new HorizontalLayout();
        buttonLayout.add(endInterviewButton, previewButton, nextButton);

        var ruleLayout = new VerticalLayout();
        ruleLayout.add(currentTaskComponent);
        ruleLayout.add(buttonLayout);
        ruleLayout.setAlignSelf(Alignment.END, buttonLayout);

        add(ruleLayout);
        setAlignSelf(Alignment.CENTER, ruleLayout);
        ruleLayout.setSizeFull();
    }

    protected void initReadOnly() {
        currentTaskComponent = new CurrentTaskComponent("");
        currentTaskComponent.setSizeFull();

        var buttonLayout = new HorizontalLayout();
        initControlButtons();
        buttonLayout.add(previewButton, nextButton);

        var ruleLayout = new VerticalLayout();
        ruleLayout.add(currentTaskComponent);
        ruleLayout.add(buttonLayout);
        ruleLayout.setAlignSelf(Alignment.END, buttonLayout);

        add(ruleLayout);
        setAlignSelf(Alignment.CENTER, ruleLayout);
        ruleLayout.setSizeFull();
    }

    private void initControlButtons() {
        previewButton = new Button(new Icon(VaadinIcon.ARROW_LEFT));
        nextButton = new Button(new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addClickListener(e -> {
            currentIdx += 1;
            currentTaskComponent.changeTask(components.get(currentIdx));

            previewButton.setEnabled(true);
            if (components.size() == currentIdx + 1) {
                nextButton.setEnabled(false);
            }
        });
        previewButton.addClickListener(e -> {
            if (components.size() == currentIdx + 1) {
                nextButton.setEnabled(true);
            }

            currentIdx -= 1;
            currentTaskComponent.changeTask(components.get(currentIdx));

            if (currentIdx == 0) {
                previewButton.setEnabled(false);
            }
        });

        if (components.isEmpty()) {
            nextButton.setEnabled(false);
            previewButton.setEnabled(false);
        }
    }

    public void offerDiff(UUID elementId, Map<Integer, String> diff) {
        components
                .stream()
                .filter(e -> UUID.fromString(e.getId().get()).equals(elementId))
                .findFirst()
                .ifPresent(e -> ((EditableComponent) e).offerDiff(diff));
    }

    protected abstract void finish();

    public abstract boolean isInterviewer();

    public void setData(Map<Integer, ElementValues> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }

        components.clear();
        data
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(es -> buildElement(es.getValue()))
                .forEach(components::add);

        currentIdx = components.size() <= 1 ? 0 : components.size() - 1;
        currentTaskComponent.changeTask(components.get(currentIdx));

        if (isInterviewer()) {
            previewButton.setEnabled(true);
        }
    }

    public void addNewTask(ElementValues value) {
        var component = buildElement(value);
        components.add(component);
        currentTaskComponent.changeTask(component);
        currentIdx += 1;
    }

    protected List<ElementValues> getInterviewResult() {
        return components
                .stream()
                .map(this::buildValueFromComponent)
                .toList();
    }

    public void addTaskElement(ElementValues value) {
        if (currentIdx > 0 || components.size() == 1) {
            previewButton.setEnabled(true);
        }

        var component = buildElement(value);
        components.add(component);
        currentTaskComponent.changeTask(component);

        if (components.size() != 1) {
            currentIdx += 1;
        }
    }

    public void changeLastTaskElement(ElementValues value) {
        if (currentIdx == 0) {
            addTaskElement(value);
            return;
        }

        var component = buildElement(value);
        components.set(currentIdx, component);
        currentTaskComponent.changeTask(component);
    }

    public Component getCurrentElement() {
        return components.get(currentIdx);
    }

    public void addCacheToCurrentElement(UUID interviewId, GlobalCacheService service) {
        addCacheToTargetComponent(interviewId, getCurrentElement(), service);
    }

    public void addCacheToAllElements(UUID interviewId, GlobalCacheService service) {
        components.forEach(e -> addCacheToTargetComponent(interviewId, e, service));
    }

    // Потому, что по другому не придумал
    private void addCacheToTargetComponent(UUID interviewId, Component component, GlobalCacheService service) {
        switch (component) {
            case CustomEditor e -> registerListenerForEditor(interviewId, e, service);
            case CustomRadioButtonsGroup rb -> registerListenerForRadioButtons(interviewId, rb, service);
            case CustomTextArea ta -> registerListenerForTextArea(interviewId, ta, service);
            case null, default -> System.out.println("Unknown element type");
        }
    }

    private void registerListenerForEditor(UUID interviewId, CustomEditor editor, GlobalCacheService service) {
        editor.addAceChangedListener(e ->
                service.offerDiff(
                        interviewId,
                        editor.getIdAsUUID(),
                        editor.getDiff(e.getValue())
                )
        );
    }

    private void registerListenerForRadioButtons(UUID interviewId,  CustomRadioButtonsGroup group, GlobalCacheService service) {
        group.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                service.offerDiff(
                        interviewId,
                        group.getIdAsUUID(),
                        group.getDiff(e.getValue())
                );
            }
        });
    }

    private void registerListenerForTextArea(UUID interviewId, CustomTextArea textArea, GlobalCacheService service) {
        textArea.addValueChangeListener(e ->
                service.offerDiff(
                        interviewId,
                        textArea.getIdAsUUID(),
                        textArea.getDiff(e.getValue())
                )
        );
    }

    private Component getComponentById(UUID componentId) {
        return components
                .stream()
                .filter(c -> c.getId().equals(componentId))
                .findFirst()
                .orElse(null);
    }
}
