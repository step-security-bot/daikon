package org.talend.daikon.logging.spring;

import java.util.Optional;

public interface UserIdExtractor {

    Optional<String> extract();
}
