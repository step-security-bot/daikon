package org.talend.daikon.logging.ecs;

import co.elastic.logging.AdditionalField;
import java.util.*;

import org.junit.Test;
import org.talend.daikon.logging.event.field.HostData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EcsSerializerTest {

    @Test
    public void testSerializeAdditionalFields() {
        StringBuilder builder = new StringBuilder();
        List<AdditionalField> additionalFields = new ArrayList<>();
        additionalFields.add(new AdditionalField("mdc_field_1", "my value 1"));
        additionalFields.add(new AdditionalField("ecs.field.second", "my value 2"));
        additionalFields.add(new AdditionalField("labels.my_awesome_label", "my value 3"));
        additionalFields.add(new AdditionalField("unknown_field", "my value 4"));

        EcsSerializer.serializeAdditionalFields(builder, additionalFields);
        String actual = builder.toString();

        assertThat(actual, containsString("\"ecs.field.first\":\"my value 1\""));
        assertThat(actual, containsString("\"ecs.field.second\":\"my value 2\""));
        assertThat(actual, containsString("\"labels.my_awesome_label\":\"my value 3\""));
        assertThat(actual, not(containsString("unknown_field")));
        assertThat(actual, not(containsString("my value 4")));
    }

    @Test
    public void testSerializeMDC() {
        StringBuilder builder = new StringBuilder();
        Map<String, String> mdc = new HashMap<>();
        mdc.put("mdc_field_1", "my value 1");
        mdc.put("ecs.field.second", "my value 2");
        mdc.put("labels.my_awesome_label", "my value 3");
        mdc.put("unknown_field", "my value 4");

        EcsSerializer.serializeMdc(builder, mdc);
        String actual = builder.toString();

        assertThat(actual, containsString("\"ecs.field.first\":\"my value 1\""));
        assertThat(actual, containsString("\"ecs.field.second\":\"my value 2\""));
        assertThat(actual, containsString("\"labels.my_awesome_label\":\"my value 3\""));
        assertThat(actual, not(containsString("unknown_field")));
        assertThat(actual, not(containsString("my value 4")));
    }

    @Test
    public void testSerializeMdcWithNumbers() {
        StringBuilder builder = new StringBuilder();
        Map<String, String> mdc = new HashMap<>();

        // strings
        mdc.put("mdc_field_1", "string value");
        mdc.put("labels.my_awesome_label", "label value");

        // numbers
        mdc.put("event.duration", "12345"); // long
        mdc.put("event.risk_score", "23.9"); // float
        mdc.put("log.origin.file.line", "not a number"); // unparsable

        EcsSerializer.serializeMdc(builder, mdc);
        String actual = builder.toString();

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
        StringBuilder builder = new StringBuilder();
        HostData hostData = new HostData();
        hostData.setHostAddress("8.8.8.8");
        hostData.setHostName("AWESOME_HOST");
        EcsSerializer.serializeHostInfo(builder, hostData);
        assertThat(builder.toString(), is("\"host.ip\":[\"8.8.8.8\"],\"host.hostname\":\"AWESOME_HOST\","));
    }

    @Test
    public void testSerializeEventId() {
        StringBuilder builder = new StringBuilder();
        EcsSerializer.serializeEventId(builder, UUID.fromString("b75e9427-8679-4064-8251-02ff0de61d91"));
        assertThat(builder.toString(), is("\"event.id\":\"b75e9427-8679-4064-8251-02ff0de61d91\","));
    }

    @Test
    public void testSerializeCustomMarkers() {
        StringBuilder builder = new StringBuilder();
        EcsSerializer.serializeCustomMarker(builder, "custom_marker:my_value");
        EcsSerializer.serializeCustomMarker(builder, "custom_marker_without_value");
        EcsSerializer.serializeCustomMarker(builder, "labels.custom_marker_2:my_value_2");
        EcsSerializer.serializeCustomMarker(builder, "ecs.field.first:my_value_3");
        String actual = builder.toString();

        assertThat(actual, containsString("\"labels.custom_marker\":\"my_value\""));
        assertThat(actual, containsString("\"labels.custom_marker_2\":\"my_value_2\""));
        assertThat(actual, containsString("\"ecs.field.first\":\"my_value_3\""));
        assertThat(actual, not(containsString("custom_marker_without_value")));
    }

    @Test
    public void testSerializeEcsVersion() {
        StringBuilder builder = new StringBuilder();
        EcsSerializer.serializeEcsVersion(builder);
        assertThat(builder.toString(), containsString("\"ecs.version\":\"4.2.0\""));
    }

}
