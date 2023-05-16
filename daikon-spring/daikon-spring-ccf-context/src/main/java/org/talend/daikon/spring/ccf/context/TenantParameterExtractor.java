package org.talend.daikon.spring.ccf.context;

import java.util.Arrays;
import java.util.Iterator;

public interface TenantParameterExtractor {

    default String extractAccountId(String request) {
        return extractParam(request, "tenant");
    }

    default String extractUserId(String request) {
        return extractParam(request, "user");
    }

    default String extractParam(String path, String paramName) {
        Iterator<String> uriIterator = Arrays.stream(path.split("/")).iterator();
        while (uriIterator.hasNext()) {
            String field = uriIterator.next();
            if (paramName.equals(field)) {
                return uriIterator.next();

            }
        }
        return null;
    }
}
