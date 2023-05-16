package org.talend.daikon.spring.ccf.context.utils;

import org.apache.commons.lang3.StringUtils;

public class UUIDValidator {

    public static final boolean isUUID(String assumedUUID) {
        if (StringUtils.isBlank(assumedUUID)) {
            return false;
        }
        return assumedUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }
}
