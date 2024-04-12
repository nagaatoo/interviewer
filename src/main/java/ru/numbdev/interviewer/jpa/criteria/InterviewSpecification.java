package ru.numbdev.interviewer.jpa.criteria;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import ru.numbdev.interviewer.jpa.entity.InterviewEntity;

import java.util.ArrayList;
import java.util.List;

public class InterviewSpecification {

    public static Specification<InterviewEntity> getInterview(String quick, String owner) {
        return (interview, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

//            interview.fetch("template", JoinType.LEFT);
//            interview.fetch("questionnaire", JoinType.LEFT);
//            interview.fetch("room", JoinType.LEFT);
            if (StringUtils.isNotBlank(quick)) {
                var namePredicate = cb.like(cb.lower(interview.get("name")), quick.toLowerCase());
                var solutionPredicate = cb.like(cb.lower(interview.get("solution")), quick.toLowerCase());
                predicates.add(cb.or(namePredicate, solutionPredicate));
            }

            if (StringUtils.isNotBlank(owner)) {
                predicates.add(cb.equal(interview.get("owner"), owner));
            }

            cq.orderBy(List.of(cb.desc(interview.get("createdDate"))));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
