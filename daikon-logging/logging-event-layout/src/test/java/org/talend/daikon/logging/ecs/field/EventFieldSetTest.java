package org.talend.daikon.logging.ecs.field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.talend.daikon.logging.ecs.EcsFields;

public class EventFieldSetTest {

    @Test
    public void eventFieldShouldMapRightEcsFields() {
        // given an event field set with all properties
        final Date date = new Date();
        final EventFieldSet eventFieldSet = EventFieldSet.builder().action("action").category(EventFieldSet.Category.PROCESS)
                .code("code").created(date).dataset("dataset").duration(1L).end(date).hash("hash").id("id").ingested(date)
                .kind(EventFieldSet.Kind.EVENT).module("module").original("original").outcome(EventFieldSet.Outcome.SUCCESS)
                .provider("provider").reason("reason").reference("reference").riskScore(1.0F).riskScoreNorm(2.0F).sequence(2L)
                .severity(3L).start(date).timeZone("timezone").type(EventFieldSet.Type.ACCESS).url("url").build();

        // when retrieving all items for serialization
        final Map<EcsFields, ?> itemsToSerialize = eventFieldSet.getItemsToSerialize();

        // then all items should be present with the accurate value
        assertThat(itemsToSerialize.get(EcsFields.EVENT_ACTION), is("action"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_CATEGORY), is(EventFieldSet.Category.PROCESS.value));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_CODE), is("code"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_CREATED), is(date));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_DATASET), is("dataset"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_DURATION), is(1L));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_END), is(date));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_HASH), is("hash"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_ID), is("id"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_INGESTED), is(date));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_KIND), is(EventFieldSet.Kind.EVENT.value));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_MODULE), is("module"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_ORIGINAL), is("original"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_OUTCOME), is(EventFieldSet.Outcome.SUCCESS.value));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_PROVIDER), is("provider"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_REASON), is("reason"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_REFERENCE), is("reference"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_RISK_SCORE), is(1.0F));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_RISK_SCORE_NORM), is(2.0F));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_SEQUENCE), is(2L));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_SEVERITY), is(3L));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_START), is(date));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_TIMEZONE), is("timezone"));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_TYPE), is(EventFieldSet.Type.ACCESS.value));
        assertThat(itemsToSerialize.get(EcsFields.EVENT_URL), is("url"));
    }
}
