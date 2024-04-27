package ru.numbdev.interviewer.page.component;

import com.vaadin.flow.component.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.numbdev.interviewer.enums.InterviewComponentInitType;
import ru.numbdev.interviewer.page.InterviewsListPage;
import ru.numbdev.interviewer.page.component.abstracts.AbstractInterviewComponent;
import ru.numbdev.interviewer.service.InterviewService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InterviewComponent extends AbstractInterviewComponent {

    @Autowired
    private InterviewService interviewService;

    @Override
    protected void finish() {
        interviewService.finishInterview(getInterviewerId(), getInterviewResult());
        closeInterview();
//        if (type == InterviewComponentInitType.FULL) {
////            UI.getCurrent().navigate(InterviewsListPage.class);
//        } else if ()
    }
}
