package org.talend.tql.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.talend.tql.excp.TqlException;
import org.talend.tql.model.Expression;

public class TqlTest {

    @Test
    public void parse() throws Exception {
        String query = "toto = 'hello world'";

        Expression parse = Tql.parse(query);

        // no exception
        assertNotNull(parse);
    }

    @Test
    public void parse_elementThrowTqlException() throws Exception {
        assertThrows(TqlException.class, () -> {
            String query = "toto";
            Tql.parse(query);
        });
    }

}