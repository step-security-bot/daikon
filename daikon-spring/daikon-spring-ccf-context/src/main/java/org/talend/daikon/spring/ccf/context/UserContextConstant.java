package org.talend.daikon.spring.ccf.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Constant to use in @FunctionalContext annotation to customize the user context.
 **/
public enum UserContextConstant {

    GROUPS("groups"),
    ENTITLEMENTS("entitlements"),
    EMAIL("email"),
    GIVEN_NAME("given_name"),
    FAMILY_NAME("family_name"),
    PREFERRED_LANGUAGE("preferred_language"),
    TIMEZONE("TIMEZONE"),
    ALL("All"),
    NONE("none");

    private final String value;

    UserContextConstant(String value) {
        this.value = value;
    }

    public static List<String> allConstantsList() {
        List<String> listValue = new ArrayList<>();
        Arrays.stream(UserContextConstant.values()).forEach(userContextConstant -> listValue.add(userContextConstant.getValue()));
        return listValue;
    }

    public String getValue() {
        return value;
    }
}
