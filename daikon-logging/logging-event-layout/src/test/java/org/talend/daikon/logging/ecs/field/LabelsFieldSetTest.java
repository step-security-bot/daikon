package org.talend.daikon.logging.ecs.field;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.talend.daikon.logging.ecs.EcsFields;

public class LabelsFieldSetTest {

    @Test
    public void labelsFieldShouldMapRightEcsFields() {
        // given a labels field set with several properties
        final LabelsFieldSet labelsFieldSet = LabelsFieldSet.builder().addLabel("key-1", "value-1").addLabel("key-2", "value-2")
                .addLabel("key-3", "value-3").build();

        // when retrieving all items for serialization
        final Map<EcsFields, ?> itemsToSerialize = labelsFieldSet.getItemsToSerialize();

        // then only Map<key, value> is added to the items to serialize
        assertThat(itemsToSerialize.size(), is(1));
        assertThat(itemsToSerialize.get(EcsFields.LABELS), instanceOf(Map.class));
        // and all items should be present with the accurate value
        final HashMap labels = (HashMap) itemsToSerialize.get(EcsFields.LABELS);
        assertThat(labels.size(), is(3));
        assertThat(labels.get("labels.key-1"), is("value-1"));
        assertThat(labels.get("labels.key-2"), is("value-2"));
        assertThat(labels.get("labels.key-3"), is("value-3"));
    }
}
