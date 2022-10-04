package org.talend.dsel.functions.custom;

import org.talend.maplang.el.interpreter.api.ExprLangContext;
import org.talend.maplang.el.interpreter.api.ExprLangFunction;

public class IsInvalid implements ExprLangFunction {

    @Override
    public String getName() {
        return "isInvalid";
    }

    @Override
    public Object call(ExprLangContext exprLangContext, Object... params) {
        return !(boolean) new IsValid().call(exprLangContext, params);
    }

}
