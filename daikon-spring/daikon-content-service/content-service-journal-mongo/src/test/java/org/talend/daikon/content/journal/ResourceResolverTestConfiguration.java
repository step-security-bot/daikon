package org.talend.daikon.content.journal;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.talend.daikon.content.ResourceResolver;

@AutoConfiguration
public class ResourceResolverTestConfiguration {

    @Bean
    @Primary
    public ResourceResolver resourceResolver() {
        return Mockito.mock(ResourceResolver.class);
    }
}
