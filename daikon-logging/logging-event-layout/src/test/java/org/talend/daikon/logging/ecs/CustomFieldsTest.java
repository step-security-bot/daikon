package org.talend.daikon.logging.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class CustomFieldsTest {

    @Test
    public void testGetByName() {
        assertEquals(CustomFields.AUTH_CLIENT_ID, CustomFields.getByName(CustomFields.AUTH_CLIENT_ID.fieldName));
        assertNull(CustomFields.getByName(UUID.randomUUID().toString()), "If you see this message - do a flip");
    }

    @Test
    public void testAllLowerCase() {
        Arrays.stream(CustomFields.values()).forEach(it -> assertEquals(it.fieldName.toLowerCase(Locale.ROOT), it.fieldName));
    }

    @Test
    public void testNoOverrideEcs() {
        Arrays.stream(CustomFields.values()).forEach(f -> ensureNoEcsField(f.fieldName));
    }

    @Test
    public void testDuplicateCheck() {
        assertThrows(AssertionError.class, () -> {
            ensureNoEcsField("event.id");
        });
    }

    private void ensureNoEcsField(String customField) {
        assertNull(RealEcsFields.getByName(customField),
                "Custom field " + customField + " duplicates a field in ECS with same name"); // in test scope, EcsFields contains
                                                                                              // test fields
    }
}
