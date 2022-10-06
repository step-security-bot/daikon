package org.talend.dsel.functions.custom;

import org.talend.daikon.pattern.word.WordPatternToRegex;
import org.talend.dsel.exception.FunctionException;
import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.el.interpreter.api.ExprLangFunction;

public class WordComplies implements ExprLangFunction {

    @Override
    public String getName() {
        return "wordComplies";
    }

    @Override
    public Object call(ExprLangContext exprLangContext, Object... params) {
        if (params.length != 2) {
            throw new FunctionException(String.format("Wrong number of parameters (expected: 2, found: %d)", params.length));
        }

        String value = params[0].toString();
        String pattern = params[1].toString();

        return value != null && pattern != null && value.matches(WordPatternToRegex.toRegex(pattern, true));
    }
}
