package org.talend.daikon.spring.ccf.context;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface M2MFunctionalContext {

    UserContextConstant[] userContext() default {UserContextConstant.NONE};
}
