package org.talend.dsel.functions.custom;

import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.el.interpreter.api.ExprLangFunction;

public class IsValid implements ExprLangFunction {

    @Override
    public String getName() {
        return "isValid";
    }

    @Override
    public Object call(ExprLangContext exprLangContext, Object... params) {
        return new IsOfType().call(exprLangContext, params);
    }

}
