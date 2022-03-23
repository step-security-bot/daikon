package org.talend.tqlmongo.criteria;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.talend.tqlmongo.excp.TqlMongoException;

public class TestMongoCriteria_AllFields extends TestMongoCriteria_Abstract {

    @Test
    public void testFieldEq() {
        assertThrows(TqlMongoException.class, () -> {
            /*
             * There's no way to specify a condition on all fields and visitor has no additional metadata information so
             * it can infer field names.
             */
            doTest("* = 0");
        });
    }
}
