package ru.numbdev.interviewer.page.component;

import de.f0rce.ace.AceEditor;
import org.apache.commons.lang3.StringUtils;
import ru.numbdev.interviewer.page.component.abstracts.EditableComponent;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CustomEditor extends AceEditor implements EditableComponent {

    private static final String SPLIT = "\r\n";
    private final Map<Integer, String> rows = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();

    @Override
    public Map<Integer, String> getDiff(String actualState) {
        try {
            lock.lock();
            var seq = new AtomicInteger();
            var result = Arrays
                    .stream(actualState.split("\r\n"))
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
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void offerDiff(Map<Integer, String> diff) {
        try {
            lock.lock();
            saveResult(diff);
            addToComponent();
        } finally {
            lock.unlock();
        }
    }

    private void saveResult(Map<Integer, String> diff) {
        diff
                .forEach((rowIdx, value) -> {
                    // не меняем текущую строку
                    if (getCursorPosition().getRow() != rowIdx) {
                        rows.put(rowIdx, value);
                    }
                });
    }

    private void addToComponent() {
        setValue(String.join(SPLIT, rows.values()));
    }

}
