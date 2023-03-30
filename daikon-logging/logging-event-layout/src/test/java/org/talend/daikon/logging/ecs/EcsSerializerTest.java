package org.talend.daikon.logging.ecs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.talend.daikon.logging.event.field.HostData;

import co.elastic.logging.AdditionalField;

public class EcsSerializerTest {

    @Test
    public void testSerializeAdditionalFields() {
        final StringBuilder builder = new StringBuilder();
        final List<AdditionalField> additionalFields = new ArrayList<>();
        additionalFields.add(new AdditionalField("mdc_field_1", "my value 1"));
        additionalFields.add(new AdditionalField("ecs.field.second", "my value 2"));
        additionalFields.add(new AdditionalField("labels.my_awesome_label", "my value 3"));
        additionalFields.add(new AdditionalField("unknown_field", "my value 4"));

        EcsSerializer.serializeAdditionalFields(builder, additionalFields);
        final String actual = builder.toString();

        assertThat(actual, containsString("\"ecs.field.first\":\"my value 1\""));
        assertThat(actual, containsString("\"ecs.field.second\":\"my value 2\""));
        assertThat(actual, containsString("\"labels.my_awesome_label\":\"my value 3\""));
        assertThat(actual, not(containsString("unknown_field")));
        assertThat(actual, not(containsString("my value 4")));
    }

    @Test
    public void testSerializeMDC() {
        final StringBuilder builder = new StringBuilder();
        final Map<String, String> mdc = new HashMap<>();
        mdc.put("mdc_field_1", "my value 1");
        mdc.put("ecs.field.second", "my value 2");
        mdc.put("labels.my_awesome_label", "my value 3");
        mdc.put("unknown_field", "my value 4");

        EcsSerializer.serializeMDC(builder, mdc);
        final String actual = builder.toString();

        assertThat(actual, containsString("\"ecs.field.first\":\"my value 1\""));
        assertThat(actual, containsString("\"ecs.field.second\":\"my value 2\""));
        assertThat(actual, containsString("\"labels.my_awesome_label\":\"my value 3\""));
        assertThat(actual, not(containsString("unknown_field")));
        assertThat(actual, not(containsString("my value 4")));
    }

    @Test
    public void testSerializeMdcWithNumbers() {
        final StringBuilder builder = new StringBuilder();
        final Map<String, String> mdc = new HashMap<>();

        // strings
        mdc.put("mdc_field_1", "string value");
        mdc.put("labels.my_awesome_label", "label value");

        // numbers
        mdc.put("event.duration", "12345"); // long
        mdc.put("event.risk_score", "23.9"); // float
        mdc.put("log.origin.file.line", "not a number"); // unparsable

        EcsSerializer.serializeMDC(builder, mdc);
        final String actual = builder.toString();

        // strings
        assertThat(actual, containsString("\"ecs.field.first\":\"string value\","));
        assertThat(actual, containsString("\"labels.my_awesome_label\":\"label value\","));

        // numbers
        assertThat(actual, containsString("\"event.duration\":12345,"));
        assertThat(actual, containsString("\"event.risk_score\":23.9,"));
        assertThat(actual, not(containsString("\"event.duration\":\"12345\"")));
        assertThat(actual, not(containsString("\"event.risk_score\":\"23.9\"")));

        // not a number skipped
        assertThat(actual, not(containsString("log.origin.file.line")));
        assertThat(actual, not(containsString("not a number")));
    }

    @Test
    public void testSerializeHostInfo() {
        final StringBuilder builder = new StringBuilder();
        final HostData hostData = new HostData();
        hostData.setHostAddress("8.8.8.8");
        hostData.setHostName("AWESOME_HOST");
        EcsSerializer.serializeHostInfo(builder, hostData);
        assertThat(builder.toString(), is("\"host.ip\":[\"8.8.8.8\"],\"host.hostname\":\"AWESOME_HOST\","));
    }

    @Test
    public void testSerializeEventId() {
        final StringBuilder builder = new StringBuilder();
        EcsSerializer.serializeEventId(builder, UUID.fromString("b75e9427-8679-4064-8251-02ff0de61d91"));
        assertThat(builder.toString(), is("\"event.id\":\"b75e9427-8679-4064-8251-02ff0de61d91\","));
    }

    @Test
    public void testSerializeCustomMarkers() {
        final StringBuilder builder = new StringBuilder();
        EcsSerializer.serializeCustomMarker(builder, "custom_marker:my_value");
        EcsSerializer.serializeCustomMarker(builder, "custom_marker_without_value");
        EcsSerializer.serializeCustomMarker(builder, "labels.custom_marker_2:my_value_2");
        EcsSerializer.serializeCustomMarker(builder, "ecs.field.first:my_value_3");
        final String actual = builder.toString();

        assertThat(actual, containsString("\"labels.custom_marker\":\"my_value\""));
        assertThat(actual, containsString("\"labels.custom_marker_2\":\"my_value_2\""));
        assertThat(actual, containsString("\"ecs.field.first\":\"my_value_3\""));
        assertThat(actual, not(containsString("custom_marker_without_value")));
    }

    @Test
    public void testSerializeEcsVersion() {
        final StringBuilder builder = new StringBuilder();
        EcsSerializer.serializeEcsVersion(builder);
        assertThat(builder.toString(), containsString("\"ecs.version\":\"8.6.1\""));
    }

    @Test
    public void testSerializeEcsFieldItem() {
        // given a builder
        final StringBuilder builder = new StringBuilder();

        // when serializing a String
        EcsSerializer.serializeEcsFieldItem(builder, EcsFields.EVENT_ACTION, "string-example");
        // when serializing a Date
        EcsSerializer.serializeEcsFieldItem(builder, EcsFields.EVENT_CREATED,
                Date.from(Instant.parse("2016-05-23T08:05:35.101Z")));
        // when serializing a Long
        EcsSerializer.serializeEcsFieldItem(builder, EcsFields.EVENT_DURATION, 2L);
        // when serializing a Float
        EcsSerializer.serializeEcsFieldItem(builder, EcsFields.EVENT_RISK_SCORE, 3.0F);
        // when serializing a map<String, String> for labels
        final Map<String, String> mapLabels = new HashMap<>();
        mapLabels.put("labels.key-1", "value-1");
        mapLabels.put("labels.key-2", "value-2");
        EcsSerializer.serializeEcsFieldItem(builder, EcsFields.LABELS, mapLabels);

        // then all the fields should be well serialized with quotes (or not)
        final String result = builder.toString();
        assertThat(result, containsString("\"" + EcsFields.EVENT_ACTION.fieldName + "\":\"string-example\""));
        assertThat(result, containsString("\"" + EcsFields.EVENT_CREATED.fieldName + "\":\"2016-05-23T08:05:35.101Z\""));
        assertThat(result, containsString("\"" + EcsFields.EVENT_DURATION.fieldName + "\":" + 2L));
        assertThat(result, containsString("\"" + EcsFields.EVENT_RISK_SCORE.fieldName + "\":" + 3.0F));
        assertThat(result, containsString("\"labels.key-1\":\"value-1\""));
        assertThat(result, containsString("\"labels.key-2\":\"value-2\""));
    }

}
