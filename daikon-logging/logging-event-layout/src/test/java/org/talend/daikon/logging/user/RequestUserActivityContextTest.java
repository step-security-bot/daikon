package org.talend.daikon.logging.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RequestUserActivityContextTest {

    @Test
    public void test() throws Exception {
        RequestUserActivityContext context = RequestUserActivityContext.getCurrent();
        assertNotNull(context);
        assertTrue(context.getCorrelationId() == null);
        assertEquals(context, RequestUserActivityContext.getCurrent());

        context.setCorrelationId("foo");
        assertTrue("foo".equals(context.getCorrelationId()));

        RequestUserActivityContext.clearCurrent();
        assertFalse(context.equals(RequestUserActivityContext.getCurrent()));
    }

}