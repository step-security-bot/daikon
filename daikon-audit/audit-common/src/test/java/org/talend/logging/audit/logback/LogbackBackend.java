package org.talend.logging.audit.logback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.talend.logging.audit.LogLevel;
import org.talend.logging.audit.impl.AbstractBackend;
import org.talend.logging.audit.impl.AuditConfigurationMap;

/**
 * Test backend.
 */
public class LogbackBackend extends AbstractBackend {

    private static final List<LogEntry> ENTRIES = new ArrayList<>();

    public LogbackBackend(AuditConfigurationMap config) {
        super("rootLogger");
    }

    public synchronized List<LogEntry> getEntries() {
        final List<LogEntry> answer = new ArrayList<>(ENTRIES);
        ENTRIES.clear();
        return answer;
    }

    @Override
    public synchronized void log(String category, LogLevel level, String message, Throwable throwable) {
        final LogEntry entry = new LogEntry();

        entry.category = category;
        entry.level = level;
        entry.message = message;
        entry.throwable = throwable;

        ENTRIES.add(entry);
    }

    @Override
    public synchronized Map<String, Object> getCopyOfContextMap() {
        return new HashMap<>(MDC.getCopyOfContextMap());
    }

    @Override
    public synchronized void setContextMap(Map<String, Object> newContext) {
        MDC.setContextMap(
            newContext.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())))
        );
    }

    public static class LogEntry {

        public String category;

        public LogLevel level;

        public String message;

        public Throwable throwable;
    }
}
