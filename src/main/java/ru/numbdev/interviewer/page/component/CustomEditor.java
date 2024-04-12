package ru.numbdev.interviewer.page.component;

import de.f0rce.ace.AceEditor;
import org.apache.commons.lang3.StringUtils;
import ru.numbdev.interviewer.component.ElementObserver;
import ru.numbdev.interviewer.dto.ElementValues;
import ru.numbdev.interviewer.enums.EventType;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CustomEditor extends AceEditor {

    private static final String SPLIT = "\r\n";
    private final Map<Integer, String> rows = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();

    public void changeState(String value) {
        try {
            lock.lock();
            saveResult(value);
            addToComponent();
        } finally {
            lock.unlock();
        }
    }

    private void saveResult(String value) {
        var seq = new AtomicInteger();
        var result = Arrays
                .stream(value.split("\r\n"))
                .collect(
                        Collectors.toConcurrentMap(
                                e -> seq.incrementAndGet(),
                                e -> e
                        )
                );

        result
                .entrySet()
                .stream()
                .filter(es -> {
                    var row = rows.get(es.getKey());
                    return StringUtils.isBlank(row) || (StringUtils.isNotBlank(row) && es.getValue().equals(row));
                })
                .forEach(es -> rows.put(es.getKey(), es.getValue()));
    }

    private void addToComponent() {
        setValue(
                rows
                        .values()
                        .stream()
                        .collect(Collectors.joining(SPLIT))
        );
    }

    public Map<Integer, String> getDiff(String value) {
        var seq = new AtomicInteger();
        var result = Arrays
                .stream(value.split("\r\n"))
                .collect(
                        Collectors.toConcurrentMap(
                                e -> seq.incrementAndGet(),
                                e -> e
                        )
                );

        return result
                .entrySet()
                .stream()
                .filter(es -> {
                    var row = rows.get(es.getKey());
                    return StringUtils.isBlank(row) || (StringUtils.isNotBlank(row) && es.getValue().equals(row));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
