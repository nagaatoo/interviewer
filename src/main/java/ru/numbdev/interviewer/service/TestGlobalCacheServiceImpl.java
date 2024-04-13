package ru.numbdev.interviewer.service;

import com.hazelcast.core.HazelcastInstance;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.numbdev.interviewer.component.RoomObserver;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.dto.Message;
import ru.numbdev.interviewer.enums.EventType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestGlobalCacheServiceImpl implements GlobalCacheService {

    private static final String SPLIT = "\r\n";

    // Сессии. key - id интервью. Key - id комнаты
    private static final Map<UUID, Map<UUID, VaadinSession>> sessions = new ConcurrentHashMap<>();
    // Подключенные компоненты.
    private static final Set<RoomObserver> observerRooms = new HashSet<>();

    private static final Lock offerInterviewLock = new ReentrantLock();
    private static final Lock offerDiffLock = new ReentrantLock();

    @Value("${spring.kafka.topic}")
    private String topic;

    private final KafkaTemplate<UUID, Message> kafkaTemplate;
    private final HazelcastInstance hazelcastInstance;

    @Override
    public Map<Integer, ElementValues> offerInterview(UUID interviewId, RoomObserver room) {
        try {
            offerInterviewLock.lock();
            if (!sessions.containsKey(interviewId)) {
                sessions.put(interviewId, new HashMap<>());
            }

            if (UI.getCurrent() != null) {
                var roomIdsWithSessions = sessions.get(interviewId);
                roomIdsWithSessions.put(room.getIdAsUUID(), UI.getCurrent().getSession());
            }

            observerRooms.add(room);
            return Map.copyOf(hazelcastInstance.getMap(interviewId.toString()));
        } finally {
            offerInterviewLock.unlock();
        }
    }

    @Override
    public void offerEvent(UUID interviewId, EventType type) {
        kafkaTemplate.send(topic, interviewId, new Message(
                type,
                null,
                null
        ));
//        observerRooms
//                .entrySet()
//                .stream()
//                .filter(es -> es.getValue().getInterviewId().equals(interviewId))
//                .forEach(es -> sessions.get(es.getKey()).access(() -> es.getValue().doAction(type, null)));
    }

    @Override
    public void offerComponent(UUID interviewId, ElementValues value, boolean isChange) {
        var elements = hazelcastInstance.getMap(interviewId.toString());
        elements.put(isChange ? elements.size() - 1 : elements.size(), value);

        kafkaTemplate.send(topic, interviewId, new Message(
                isChange ? EventType.CHANGE_LAST_COMPONENT : EventType.ADD_COMPONENT,
                value,
                null
        ));
    }

//    @Override
//    public void changeComponent(UUID interviewId, UUID oldElementId, ElementValues newComponentValue) {
//        kafkaTemplate.send(topic, interviewId, new Message(
//                EventType.CHANGE,
//                newComponentValue,
//                null,
//                null
//        ));
//        var existsInterview = taskElements.get(interviewId);
//        existsInterview
//                .entrySet()
//                .stream()
//                .filter(es -> UUID.fromString(es.getValue().id()).equals(oldElementId))
//                .forEach(es -> es.setValue(newComponentValue));
//    }

    @Override
    public void offerDiff(UUID interviewId, UUID elementId, Map<Integer, String> diffs) {
        try {
            offerDiffLock.lock();
            var elements = hazelcastInstance.getMap(interviewId.toString());
            var targetOptional = elements
                    .entrySet()
                    .stream()
                    .filter(es -> ((ElementValues) es.getValue()).id().equals(elementId.toString()))
                    .map(es -> (ElementValues) es.getValue())
                    .findFirst();
            targetOptional.ifPresent(elementValues -> kafkaTemplate.send(topic, interviewId, new Message(
                    EventType.DO_DIFF,
                    elementValues,
                    diffs
            )));
        } finally {
            offerDiffLock.unlock();
        }
    }

//    private ElementValues fillRows(Map<Integer, String> diffs, ElementValues values) {
//        return new ElementValues(
//                values.id(),
//                values.type(),
//                values.description(),
//                buildResult(values.value(), diffs)
//        );
//    }
//
//    private String buildResult(String old, Map<Integer, String> diffs) {
//        var seq = new AtomicInteger();
//        var result = Arrays
//                .stream(old.split(SPLIT))
//                .collect(
//                        Collectors.toConcurrentMap(
//                                e -> seq.incrementAndGet(),
//                                e -> e
//                        )
//                );
//
//        result.putAll(diffs);
//        return buildString(result);
//    }

//    @Override
//    public void addToObserve(ElementObserver element) {
//        // Добавить интерфейс
//        var component = (Component) element;
//        observerElements.put(UUID.fromString(component.getId().get()), element);
//    }

    // TODO может быть лаг между инициализации страницы, сборки кеша и получением diff
    // Возможно следует игнорировать события до окончания инициализации или что-то еще
    @KafkaListener(topics = "${spring.kafka.topic}")
    private void listen(ConsumerRecord<UUID, Message> record) {
        var interviewId = record.key();
        var message = record.value();
        var timestamp = record.timestamp();

        var sessionRooms = sessions.get(interviewId);
        if (sessionRooms != null) {
            observerRooms
                    .stream()
                    .filter(e -> sessionRooms.containsKey(e.getIdAsUUID()))
                    .forEach(e ->
                            sessionRooms
                                    .get(e.getIdAsUUID())
                                    .access(() -> e.doAction(message))
                    );
        }
    }

//    @Scheduled(cron = "")
//    public void doActionForComponents() {
//        System.out.println("run doActionForComponents");
//        // просто кидаем апдейты
//        for (var es : observerElements.entrySet()) {
//            var id = es.getKey();
//            var element = es.getValue();
//
//            element.doAction(EventType.CHANGE, taskElements.get(id));
//        }
//    }

//    @Scheduled(fixedDelay = 1000)
//    public void doActionForInterviews() {
//        System.out.println("run doAction");
//        // просто кидаем апдейты
//        for (var es : observerElements.entrySet()) {
//            var id = es.getKey();
//            var element = es.getValue();
//
//            element.doAction(EventType.CHANGE, new ElementValues(
//                    id.toString(),
//                    null,
//                    null,
//                    buildString(taskElements.get(id))
//            ));
//        }
//    }

    private String buildString(Map<Integer, String> rows) {
        return rows
                .values()
                .stream()
                .collect(Collectors.joining(SPLIT));
    }

}
