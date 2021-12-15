package org.talend.daikon.logging.spring;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserIdExtractorImpl implements UserIdExtractor {

    @Override
    public Optional<String> extract() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication authentication = context.getAuthentication();
            if (authentication != null) {
                return Optional.of(authentication.getName());
            }
        }
        return Optional.empty();
    }
}
