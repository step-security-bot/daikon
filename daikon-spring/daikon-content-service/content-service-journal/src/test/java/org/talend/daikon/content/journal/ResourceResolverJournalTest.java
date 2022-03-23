package org.talend.daikon.content.journal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.talend.daikon.content.DeletableResource;
import org.talend.daikon.content.ResourceResolver;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
public class ResourceResolverJournalTest {

    @InjectMocks
    private ResourceResolverJournal resourceResolverRepository;

    @Mock
    private ResourceResolver delegate;

    @Test
    public void matches() throws IOException {
        // given
        when(delegate.getResources("/**")).thenReturn(new DeletableResource[0]);

        // when
        resourceResolverRepository.matches("/**");

        // then
        verify(delegate, times(1)).getResources(eq("/**"));
    }
}