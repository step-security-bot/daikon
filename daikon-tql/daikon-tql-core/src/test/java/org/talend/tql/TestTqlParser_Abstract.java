package org.talend.tql;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.talend.tql.model.TqlElement;
import org.talend.tql.parser.TqlExpressionVisitor;

/**
 * Test general method that parses a string query to the target {@link TqlElement} tree,
 * according to the defined lexer and parser.
 */
public abstract class TestTqlParser_Abstract {

    protected TqlElement doTest(String query) throws Exception {
        CharStream input = CharStreams.fromString(query);
        TqlLexer lexer = new TqlLexer(input);
        TqlParser parser = new TqlParser(new CommonTokenStream(lexer));
        TqlParser.ExpressionContext expression = parser.expression();
        return expression.accept(new TqlExpressionVisitor());
    }
}
