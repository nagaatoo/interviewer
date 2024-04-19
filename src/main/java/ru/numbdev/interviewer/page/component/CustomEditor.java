package ru.numbdev.interviewer.page.component;

import de.f0rce.ace.AceEditor;
import org.springframework.util.CollectionUtils;
import ru.numbdev.interviewer.page.component.abstracts.EditableComponent;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class CustomEditor extends AceEditor implements EditableComponent {

    private static final String SPLIT = "\n";
    private final Map<Integer, String> rows = new ConcurrentHashMap<>();
    private final Lock lock = new ReentrantLock();

    @Override
    public Map<Integer, String> getDiff(String actualState) {
        try {
            lock.lock();
            var seq = new AtomicInteger();
            var actualRows = Arrays
                    .stream(actualState.split("\n"))
                    .collect(
                            Collectors.toConcurrentMap(
                                    e -> seq.incrementAndGet(),
                                    e -> e
                            )
                    );
            if (actualState.endsWith("\n")) {
                actualRows.put(actualRows.size() + 1, "");
            }

            if (actualRows.size() >= rows.size()) {
                var diff = actualRows
                        .entrySet()
                        .stream()
                        .filter(es -> {
                            var row = rows.get(es.getKey());
                            return row == null || !es.getValue().equals(row);
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                rows.putAll(diff);
                return diff;
            } else {
                var diff = rows
                        .entrySet()
                        .stream()
                        .filter(es -> {
                            var row = actualRows.get(es.getKey());
                            return row != null && !row.equals(es.getValue());
                        })
                        .map(es -> {
                            var row = actualRows.get(es.getKey());
                            return row == null
                                    ? new AbstractMap.SimpleEntry<Integer, String>(es.getKey(), null)
                                    : es;
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                rows.putAll(diff);
                return diff;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void offerDiff(Map<Integer, String> diff) {
        if (CollectionUtils.isEmpty(diff)) {
            return;
        }

        try {
            lock.lock();
            saveResult(diff);
            addToComponent();
        } finally {
            lock.unlock();
        }
    }

    private void saveResult(Map<Integer, String> diff) {
        diff.forEach((rowIdx, value) -> {
                    if (value == null) {
                        rows.remove(rowIdx);
                    } else {
                        rows.put(rowIdx, value);
                    }
                });
    }

    private void addToComponent() {
        var currentRow = getCursorPosition().getRow();
        var currentCol = getCursorPosition().getColumn();

        setValue(String.join(SPLIT, rows.values()));
        setCursorPosition(currentRow, currentCol, false);
    }

}
