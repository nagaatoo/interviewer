package ru.numbdev.interviewer.page;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutVariant;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.context.ApplicationContext;
import ru.numbdev.interviewer.component.RoomObserver;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.dto.Message;
import ru.numbdev.interviewer.enums.EventType;
import ru.numbdev.interviewer.jpa.entity.RoomEntity;
import ru.numbdev.interviewer.page.component.InterviewComponent;
import ru.numbdev.interviewer.page.component.QuestionComponent;
import ru.numbdev.interviewer.page.component.TemplateComponent;
import ru.numbdev.interviewer.service.GlobalCacheService;
import ru.numbdev.interviewer.service.InterviewService;
import ru.numbdev.interviewer.service.crud.RoomCrudService;
import ru.numbdev.interviewer.utils.SecurityUtil;

import java.util.UUID;

@Route("/room/:identifier")
@PageTitle("Room")
@AnonymousAllowed
public class RoomPage extends VerticalLayout implements BeforeEnterObserver, RoomObserver {

    private boolean isInterviewer = false;
    private RoomEntity roomEntity;

    private final RoomCrudService roomCrudService;
    private final InterviewService interviewService;
    private final GlobalCacheService globalCacheService;
    private final ApplicationContext context;

    private TemplateComponent templateComponent;
    private QuestionComponent questionComponent;
    private HorizontalLayout title;
    private InterviewComponent main;

    private VerticalLayout startMain;

    public RoomPage(RoomCrudService roomCrudService, InterviewService interviewService,
                    GlobalCacheService globalCacheService, ApplicationContext context) {
        this.roomCrudService = roomCrudService;
        this.interviewService = interviewService;
        this.globalCacheService = globalCacheService;
        this.context = context;
        setId(UUID.randomUUID().toString());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        initRoomPage(UUID.fromString(event.getLocation().getSegments().get(1)));
    }

    private void initRoomPage(UUID roomId) {
        roomEntity = roomCrudService.getById(roomId);
        var interview = roomEntity.getInterview();
        isInterviewer = interview.getInterviewerLogin().equals(SecurityUtil.getUserName());

        if (interview.getFinishedDate() != null) {
            buildReadPage();
        } else if (!interview.getStarted()) {
            buildStartPage();
        } else if (isInterviewer){
            buildInterviewPage();
        } else {
            buildCandidatePage();
        }
    }

    private void buildReadPage() {
        buildTitle();
        buildMain(true);

        add(title);
        add(main);
    }

    private void buildInterviewPage() {
        buildTitle();
        buildMain(false);

        add(title);
        buildSplit();

        offerInterview();
    }

    private void buildCandidatePage() {
        buildTitle();
        buildMain(false);

        add(title);
        add(main);
        main.setSizeFull();
        setSizeFull();

        offerInterview();
    }

    private void buildStartPage() {
        startMain = new VerticalLayout();

        var nameField = new NativeLabel(roomEntity.getInterview().getName());
        startMain.add(nameField);
        startMain.setAlignSelf(Alignment.CENTER, nameField);

        if (!SecurityUtil.isAnonymous()) {
            var startButton = new Button("Начать интервью");
            startButton.addClickListener(e -> {
//                remove(startMain);
//                buildInterviewPage();
//                startInterview();
                globalCacheService.offerEvent(roomEntity.getInterview().getId(), getIdAsUUID(), EventType.START_INTERVIEW);

            });
            startMain.add(startButton);
            startMain.setAlignSelf(Alignment.CENTER, startButton);
            startMain.setAlignSelf(Alignment.CENTER, startButton);
        }

        add(startMain);
        setAlignSelf(Alignment.CENTER, startMain);
        setSizeFull();
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        globalCacheService.offerInterview(roomEntity.getInterview().getId(), this);
    }

    private void startInterview() {
        interviewService.startInterview(roomEntity.getInterview().getId());
    }

    private void offerInterview() {
        var elements = globalCacheService.offerInterview(roomEntity.getInterview().getId(), this);
        main.setData(elements);
        main.addCacheToAllElements();
    }

    private void buildTitle() {
        title = new HorizontalLayout();
        title.add(new Text(roomEntity.getInterview().getName()));
    }

    private void buildMain(boolean isReadOnly) {
        main = context.getBean(InterviewComponent.class);
        main.init(isInterviewer, isReadOnly);

        if (!isReadOnly) {
            main.enableCacheOperations(getInterviewId(), getIdAsUUID(), globalCacheService);
        }
    }

    private void buildSplit() {
        var questionEntity = roomEntity.getInterview().getQuestionnaire();
        var templateEntity = roomEntity.getInterview().getTemplate();

        if (questionEntity == null && templateEntity == null) {
            add(main);
        }

        var splitLayout = new SplitLayout();
        var interviewerSplit = new SplitLayout();
        interviewerSplit.setOrientation(SplitLayout.Orientation.VERTICAL);
        if (templateEntity != null) {
            var templateLayout = new VerticalLayout();

            var addButton = new Button("Новая задача");
            addButton.addClickListener(e -> {
                var element = templateComponent.getSelectedElement();
                if (element != null) {
                    main.addTaskElement(element);
                    main.addCacheToCurrentElement();
                }

                globalCacheService.offerComponent(getInterviewId(), getIdAsUUID(), element, false);
            });
            var changeButton = new Button("Заменить текущую");
            changeButton.addClickListener(e -> {
                var element = templateComponent.getSelectedElement();
                if (element != null) {
                    main.changeLastTaskElement(element);
                }

                globalCacheService.offerComponent(getInterviewId(), getIdAsUUID(), element, true);
                main.addCacheToCurrentElement();
            });
            var buttonLayout = new HorizontalLayout();
            buttonLayout.add(addButton, changeButton);

            templateComponent = context.getBean(TemplateComponent.class);
            templateComponent.init(true, templateEntity.getId(), e -> {});

            templateLayout.add(templateComponent);
            templateLayout.add(buttonLayout);
            templateLayout.setSizeFull();
            interviewerSplit.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);
            interviewerSplit.addToPrimary(templateLayout);
        }

        if (questionEntity != null) {
            questionComponent = context.getBean(QuestionComponent.class);
            questionComponent.init(false, questionEntity.getId());
            interviewerSplit.addToSecondary(questionComponent);
        }

        main.setMaxWidth("85%");
        splitLayout.addToPrimary(main);
        splitLayout.addToSecondary(interviewerSplit);
        splitLayout.setSizeFull();
        splitLayout.addThemeVariants(SplitLayoutVariant.LUMO_SMALL);
        add(splitLayout);
    }

    @Override
    public void doAction(Message message) {
        if (message.roomId().equals(getIdAsUUID())) {
            return;
        }

        switch (message.event()) {
            case START_INTERVIEW -> doStart();
            case FINISH_INTERVIEW -> doFinish();
            case ADD_COMPONENT  -> doCreate(message.value());
            case CHANGE_LAST_COMPONENT -> doChange(message.value());
            case NEXT_COMPONENT -> doNext();
            case PREVIOUS_COMPONENT -> doPreview();
            case DO_DIFF -> doDiff(message);
            case null, default -> System.out.println("Unknown type");
        }
    }

    private void doStart() {
        if (isInterviewer) {
            remove(startMain);
            buildInterviewPage();
        } else {
            remove(startMain);
            buildCandidatePage();
        }
    }

    private void doCreate(ElementValues value) {
        if (!isInterviewer) {
            main.addNewTask(value);
        }
    }

    private void doChange(ElementValues value) {
        if (!isInterviewer) {
            main.changeLastTaskElement(value);
        }
    }

    private void doNext() {
        main.doNext();
    }

    private void doPreview() {
        main.doPreview();
    }

    private void doFinish() {

    }

    private void doDiff(Message message) {
        main.offerDiff(UUID.fromString(message.value().id()), message.diffs());
    }

    @Override
    public UUID getInterviewId() {
        return roomEntity.getInterview().getId();
    }

}
