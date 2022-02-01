package org.talend.daikon.logging.layout;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.talend.daikon.logging.ecs.EcsFieldsMarker;
import org.talend.daikon.logging.ecs.field.EventFieldSet;
import org.talend.daikon.logging.ecs.field.LabelsFieldSet;
import org.talend.daikon.logging.event.layout.LogbackJSONLayout;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;

public class LogBackJSONLayoutTest extends AbstractLayoutTest {

    @Test
    public void testDefaultLocationInfo() {
        final LogbackJSONLayout layout = new LogbackJSONLayout();
        assertFalse(layout.isLocationInfo());
    }

    @Test
    public void testMarkerFields() {
        final LogDetails logDetails = new LogDetails(this.getClass());
        String result = log(newEvent(logDetails), logDetails);
        // No marker results in not having any additional fields in the customInfo
        assertThat(result, hasNoJsonPath("$.['labels.accountId']"));

        final Marker parent1Marker = MarkerFactory.getDetachedMarker("custom_infos");
        logDetails.setMarker(parent1Marker);
        result = log(newEvent(logDetails), logDetails);
        // A parent marker, without any children, results in not having any additional fields in the customInfo
        assertThat(result, hasNoJsonPath("$.customInfo['accountId']"));

        final Marker child11Marker = MarkerFactory.getDetachedMarker("accountId:foo");
        final Marker child12Marker = MarkerFactory.getDetachedMarker("userId:bar");
        parent1Marker.add(child11Marker);
        parent1Marker.add(child12Marker);
        logDetails.setMarker(parent1Marker);
        result = log(newEvent(logDetails), logDetails);

        // Children of the parent marker are used to populate the customInfo node of the logging event
        assertThat(result, hasJsonPath("$.['labels.accountId']", equalTo("foo")));
        assertThat(result, hasJsonPath("$.['labels.userId']", equalTo("bar")));
    }

    @Test
    public void testMarkerEcsFields() {
        final LogDetails logDetails = new LogDetails(this.getClass());
        // given a FieldsMarker containing several fields
        final EcsFieldsMarker ecsFieldsMarker = EcsFieldsMarker.builder()
                .event(EventFieldSet.builder().action("custom-action").build())
                .labels(LabelsFieldSet.builder().addLabel("custom-key", "custom-value").build()).build();
        // when logging the FieldsMarker
        logDetails.setMarker(ecsFieldsMarker);
        final String result = log(newEvent(logDetails), logDetails);
        // then all the fields should be present into the log
        assertThat(result, hasJsonPath("$.['event.action']", equalTo("custom-action")));
        assertThat(result, hasJsonPath("$.['labels.custom-key']", equalTo("custom-value")));
    }

    @Test
    public void testNumericFields() {
        final LogDetails logDetails = new LogDetails(this.getClass());
        logDetails.getMdc().put("event.duration", "123");
        final String result = log(newEvent(logDetails), logDetails);
        assertThat(result, hasJsonPath("$.['event.duration']", equalTo(123)));
        assertThat(result, hasJsonPath("$.['event.duration']", not(equalTo("123"))));
    }

    @Test
    public void testLegacyMode() {
        final Map<String, String> meta = Collections.singletonMap("test.meta.non_ecs_meta_field", "non_ecs_meta_field");

        final LogDetails logDetails = new LogDetails(this.getClass());

        final Map<String, String> mdc = new LinkedHashMap<>();
        mdc.put("non_ecs_mdc_field", "mdc");
        mdc.put("test.meta.non_ecs_meta_field", "meta");
        logDetails.setMdc(mdc);

        final String strictResult = log(newEvent(logDetails), logDetails, meta, false);
        final String nonStrictResult = log(newEvent(logDetails), logDetails, meta, true);

        assertThat(strictResult, hasNoJsonPath("$.['non_ecs_mdc_field']"));
        assertThat(strictResult, hasNoJsonPath("$.['non_ecs_meta_field']"));

        assertThat(nonStrictResult, hasJsonPath("$.['customInfo.non_ecs_mdc_field']", is("mdc")));
        assertThat(nonStrictResult, hasJsonPath("$.['non_ecs_meta_field']", is("meta")));
    }

    @Test
    public void testStrictModeIsDefault() {
        assertFalse(new LogbackJSONLayout().isLegacyMode());
    }

    @Override
    protected Object newEvent(final LogDetails logDetails) {
        final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(logDetails.getClassName());
        final LoggingEvent event = new LoggingEvent(this.getClass().getName(), logger, Level.DEBUG, logDetails.getLogMessage(),
                logDetails.getException(), null);
        event.setThreadName(logDetails.getThreadName());
        event.setTimeStamp(logDetails.getTimeMillis());
        event.setMDCPropertyMap(logDetails.getMdc());
        event.setMarker(logDetails.getMarker());
        final StackTraceElement callerData = new StackTraceElement(logDetails.getClassName(), logDetails.getMethodName(),
                logDetails.getFileName(), logDetails.getLineNumber());
        event.setCallerData(new StackTraceElement[] { callerData });
        return event;
    }

    @Override
    protected String log(final Object event, final LogDetails logDetails) {
        return log(event, logDetails, Collections.emptyMap(), false);
    }

    protected String log(final Object event, final LogDetails logDetails, final Map<String, String> metaFields,
            final boolean legacyMode) {
        final LogbackJSONLayout layout = new LogbackJSONLayout();
        layout.setLocationInfo(logDetails.isLocationInfo());
        layout.setLegacyMode(legacyMode);
        layout.setMetaFields(metaFields);
        layout.start();
        try {
            return layout.doLayout((LoggingEvent) event);
        } finally {
            layout.stop();
        }
    }
}
