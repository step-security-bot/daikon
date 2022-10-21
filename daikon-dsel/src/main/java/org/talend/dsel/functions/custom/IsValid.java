package org.talend.dsel.functions.custom;

import org.apache.commons.lang3.StringUtils;
import org.talend.dsel.exception.FunctionException;
import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.el.interpreter.api.ExprLangFunction;

import java.util.Arrays;
import java.util.Objects;

public class IsValid implements ExprLangFunction {

    @Override
    public String getName() {
        return "isValid";
    }

    @Override
    public Object call(ExprLangContext exprLangContext, Object... params) {
        long numberOfNonNullParams = Arrays.stream(params).filter(Objects::nonNull).count();
        if (numberOfNonNullParams != 2) {
            throw new FunctionException(
                    String.format("Wrong number of parameters (expected: 2, found: %d)", numberOfNonNullParams));
        }
        Object value = params[0];
        if (value == null) {
            return false;
        }
        String valueAsString = params[0].toString();
        String typeName = params[1].toString();

        if (StringUtils.isEmpty(valueAsString)) {
            return false;
        } else {
            return IsOfTypeUtility.evaluate(exprLangContext, valueAsString, typeName);
        }
    }

}
