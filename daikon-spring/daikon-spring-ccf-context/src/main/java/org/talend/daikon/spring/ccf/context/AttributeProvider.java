package org.talend.daikon.spring.ccf.context;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public class AttributeProvider {

    public static List<String> getAttributes(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        M2MFunctionalContext m2MFunctionalContextAnnotation = method.getAnnotation(M2MFunctionalContext.class);
        return Arrays.stream(m2MFunctionalContextAnnotation.userContext()).map(UserContextConstant::getValue).toList();
    }
}
