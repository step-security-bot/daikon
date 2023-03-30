package org.talend.daikon.messages.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.talend.daikon.messages.MessageKey;

public class TestDaikonMessageSerialization {

    private DaikonMessageKeyDeserializer daikonMessageKeyDeserializer = new DaikonMessageKeyDeserializer();

    private DaikonMessageKeySerializer daikonMessageKeySerializer = new DaikonMessageKeySerializer();

    @Test
    public void testSerializationDeserialisation() {
        Map<String, String> keys = new HashMap<>();
        keys.put("key1", "value1");
        keys.put("key2", "value2");

        MessageKey data = MessageKey.newBuilder().setRandom("random1234").setTenantId("tenant1").setKeys(keys).build();
        byte[] serializeData = daikonMessageKeySerializer.serialize("topic", data);

        MessageKey deserializeData = daikonMessageKeyDeserializer.deserialize("topic", serializeData);

        assertEquals(data.getTenantId(), deserializeData.getTenantId());
        assertEquals(data.getRandom(), deserializeData.getRandom());
        assertEquals(data.getKeys(), deserializeData.getKeys());

    }

}
