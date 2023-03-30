package org.talend.logging.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

public class ContextBuilderTest {

    @Test
    public void contextBuilderCreateImutableContextPut() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ContextBuilder builder = ContextBuilder.create();

            final Context context = builder.build();

            context.put("aa", "bb");
        });
    }

    @Test
    public void contextBuilderCreateImutableContextPutAll() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ContextBuilder builder = ContextBuilder.create();

            final Context context = builder.build();

            context.putAll(Collections.<String, String> emptyMap());
        });
    }

    @Test
    public void contextBuilderCreateImutableContextRemove() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ContextBuilder builder = ContextBuilder.create();

            final Context context = builder.build();

            context.remove("aa");
        });
    }

    @Test
    public void contextBuilderCreateImutableContextClear() {
        assertThrows(UnsupportedOperationException.class, () -> {
            ContextBuilder builder = ContextBuilder.create();

            final Context context = builder.build();

            context.clear();
        });
    }

    @Test()
    public void contextBuilderCreateDifferentContext() {
        ContextBuilder builder = ContextBuilder.create();
        final Context context = builder.build();

        builder.with("test", "test");
        final Context contextWithOne = builder.build();

        assertEquals(0, context.size());
        assertEquals(1, contextWithOne.size());
    }
}